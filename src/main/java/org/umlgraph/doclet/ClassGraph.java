/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2005 Diomidis Spinellis
 *
 * Permission to use, copy, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */

package org.umlgraph.doclet;

import static org.umlgraph.doclet.StringUtil.escape;
import static org.umlgraph.doclet.StringUtil.guilWrap;
import static org.umlgraph.doclet.StringUtil.guillemize;
import static org.umlgraph.doclet.StringUtil.htmlNewline;
import static org.umlgraph.doclet.StringUtil.removeTemplate;
import static org.umlgraph.doclet.StringUtil.tokenize;
import static org.umlgraph.doclet.StringUtil.buildRelativePathFromClassNames;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.javadoc.WildcardType;

/**
 * Class graph generation engine
 * @depend - - - StringUtil
 * @depend - - - Options
 * @composed - - * ClassInfo
 * @has - - - OptionProvider
 *
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class ClassGraph {
    protected static final char FILE_SEPARATOR = '/';

    enum Align {
	LEFT, CENTER, RIGHT;

	public final String lower;

	private Align() {
	    this.lower = toString().toLowerCase();
	}
    };

    protected Map<String, ClassInfo> classnames = new HashMap<String, ClassInfo>();
    protected Set<String> rootClasses;
	protected Map<String, ClassDoc> rootClassdocs = new HashMap<String, ClassDoc>();
    protected OptionProvider optionProvider;
    protected PrintWriter w;
    protected ClassDoc collectionClassDoc;
    protected ClassDoc mapClassDoc;
    protected String linePostfix;
    protected String linePrefix;
    
    // used only when generating context class diagrams in UMLDoc, to generate the proper
    // relative links to other classes in the image map
    protected final Doc contextDoc;
      
    /**
     * Create a new ClassGraph.  <p>The packages passed as an
     * argument are the ones specified on the command line.</p>
     * <p>Local URLs will be generated for these packages.</p>
     * @param root The root of docs as provided by the javadoc API
     * @param optionProvider The main option provider
     * @param contextDoc The current context for generating relative links, may be a ClassDoc 
     * 	or a PackageDoc (used by UMLDoc)
     */
    public ClassGraph(RootDoc root, OptionProvider optionProvider, Doc contextDoc) {
	this.optionProvider = optionProvider;
	this.collectionClassDoc = root.classNamed("java.util.Collection");
	this.mapClassDoc = root.classNamed("java.util.Map");
	this.contextDoc = contextDoc;
	
	// to gather the packages containing specified classes, loop thru them and gather
	// package definitions. User root.specifiedPackages is not safe, since the user
	// may specify just a list of classes (human users usually don't, but automated tools do)
	rootClasses = new HashSet<String>();
	for (ClassDoc classDoc : root.classes()) {
	    rootClasses.add(classDoc.qualifiedName());
	    rootClassdocs.put(classDoc.qualifiedName(), classDoc);
	}
	
	Options opt = optionProvider.getGlobalOptions();
	linePrefix = opt.compact ? "" : "\t";
	linePostfix = opt.compact ? "" : "\n";
    }

    

    /** Return the class's name, possibly by stripping the leading path */
    private static String qualifiedName(Options opt, String r) {
	// Nothing to do:
	if (opt.showQualified && (opt.showQualifiedGenerics || r.indexOf('<') < 0))
	    return r;
	StringBuilder buf = new StringBuilder(r.length());
	int last = 0, depth = 0;
	boolean strip = !opt.showQualified;
	for (int i = 0; i < r.length();) {
	    char c = r.charAt(i++);
	    // The last condition prevents losing the dot in A<V>.B
	    if ((c == '.' || c == '$') && strip && last + 1 < i)
		last = i; // skip
	    if (Character.isJavaIdentifierPart(c))
		continue;
	    // Handle nesting of generics
	    if (c == '<') {
		++depth;
		strip = !opt.showQualifiedGenerics;
	    } else if (c == '>' && --depth == 0)
		strip = !opt.showQualified;
	    if (last < i) {
		buf.append(r, last, i);
		last = i;
	    }
	}
	if (last < r.length())
	    buf.append(r, last, r.length());
	return buf.toString();
    }

    /**
     * Print the visibility adornment of element e prefixed by
     * any stereotypes
     */
    private String visibility(Options opt, ProgramElementDoc e) {
	return opt.showVisibility ? Visibility.get(e).symbol : " ";
    }

    /** Print the method parameter p */
    private String parameter(Options opt, Parameter p[]) {
	StringBuilder par = new StringBuilder(1000);
	for (int i = 0; i < p.length; i++) {
	    par.append(p[i].name() + typeAnnotation(opt, p[i].type()));
	    if (i + 1 < p.length)
		par.append(", ");
	}
	return par.toString();
    }

    /** Print a a basic type t */
    private String type(Options opt, Type t, boolean generics) {
	return ((generics ? opt.showQualifiedGenerics : opt.showQualified) ? //
		t.qualifiedTypeName() : t.typeName()) //
		+ typeParameters(opt, t.asParameterizedType());
    }

    /** Print the parameters of the parameterized type t */
    private String typeParameters(Options opt, ParameterizedType t) {
	if (t == null)
	    return "";
	StringBuffer tp = new StringBuffer(1000).append("&lt;");
	Type args[] = t.typeArguments();
	for (int i = 0; i < args.length; i++) {
	    tp.append(type(opt, args[i], true));
	    if (i != args.length - 1)
		tp.append(", ");
	}
	return tp.append("&gt;").toString();
    }

    /** Annotate an field/argument with its type t */
    private String typeAnnotation(Options opt, Type t) {
	if (t.typeName().equals("void"))
	    return "";
	return " : " + type(opt, t, false) + t.dimension();
    }

    /** Print the class's attributes fd */
    private void attributes(Options opt, FieldDoc fd[]) {
	for (FieldDoc f : fd) {
	    if (hidden(f))
		continue;
	    stereotype(opt, f, Align.LEFT);
	    String att = visibility(opt, f) + f.name();
	    if (opt.showType)
		att += typeAnnotation(opt, f.type());
	    tableLine(Align.LEFT, att);
	    tagvalue(opt, f);
	}
    }

    /*
     * The following two methods look similar, but can't
     * be refactored into one, because their common interface,
     * ExecutableMemberDoc, doesn't support returnType for ctors.
     */

    /** Print the class's constructors m */
    private boolean operations(Options opt, ConstructorDoc m[]) {
	boolean printed = false;
	for (ConstructorDoc cd : m) {
	    if (hidden(cd))
		continue;
	    stereotype(opt, cd, Align.LEFT);
	    String cs = visibility(opt, cd) + cd.name() //
		    + (opt.showType ? "(" + parameter(opt, cd.parameters()) + ")" : "()");
	    tableLine(Align.LEFT, cs);
	    tagvalue(opt, cd);
	    printed = true;
	}
	return printed;
    }

    /** Print the class's operations m */
    private boolean operations(Options opt, MethodDoc m[]) {
	boolean printed = false;
	for (MethodDoc md : m) {
	    if (hidden(md))
		continue;
	    // Filter-out static initializer method
	    if (md.name().equals("<clinit>") && md.isStatic() && md.isPackagePrivate())
		continue;
	    stereotype(opt, md, Align.LEFT);
	    String op = visibility(opt, md) + md.name() + //
		    (opt.showType ? "(" + parameter(opt, md.parameters()) + ")" + typeAnnotation(opt, md.returnType())
			    : "()");
	    tableLine(Align.LEFT, (md.isAbstract() ? Font.ABSTRACT : Font.NORMAL).wrap(opt, op));
	    printed = true;

	    tagvalue(opt, md);
	}
	return printed;
    }

    /** Print the common class node's properties */
    private void nodeProperties(Options opt) {
	w.print(", fontname=\"" + opt.nodeFontName + "\"");
	w.print(", fontcolor=\"" + opt.nodeFontColor + "\"");
	w.print(", fontsize=" + opt.nodeFontSize);
	w.print(opt.shape.style);
	w.println("];");
    }

    /**
     * Return as a string the tagged values associated with c
     * @param opt the Options used to guess font names
     * @param c the Doc entry to look for @tagvalue
     * @param prevterm the termination string for the previous element
     * @param term the termination character for each tagged value
     */
    private void tagvalue(Options opt, Doc c) {
	Tag tags[] = c.tags("tagvalue");
	if (tags.length == 0)
	    return;
	
	for (Tag tag : tags) {
	    String t[] = tokenize(tag.text());
	    if (t.length != 2) {
		System.err.println("@tagvalue expects two fields: " + tag.text());
		continue;
	    }
	    tableLine(Align.RIGHT, Font.TAG.wrap(opt, "{" + t[0] + " = " + t[1] + "}"));
	}
    }

    /**
     * Return as a string the stereotypes associated with c
     * terminated by the escape character term
     */
    private void stereotype(Options opt, Doc c, Align align) {
	for (Tag tag : c.tags("stereotype")) {
	    String t[] = tokenize(tag.text());
	    if (t.length != 1) {
		System.err.println("@stereotype expects one field: " + tag.text());
		continue;
	    }
	    tableLine(align, guilWrap(opt, t[0]));
	}
    }

    /** Return true if c has a @hidden tag associated with it */
    private boolean hidden(ProgramElementDoc c) {
	Tag tags[] = c.tags("hidden");
	if (tags.length > 0)
	    return true;
	tags = c.tags("view");
	if (tags.length > 0)
	    return true;
	Options opt; 
	if(c instanceof ClassDoc)
	  opt = optionProvider.getOptionsFor((ClassDoc) c);
	else 
	  opt = optionProvider.getOptionsFor(c.containingClass());
	    return opt.matchesHideExpression(c.toString());
    }
    
    protected ClassInfo getClassInfo(String className) {
	return classnames.get(removeTemplate(className));
    }
    
    private ClassInfo newClassInfo(String className, boolean printed, boolean hidden) {
	ClassInfo ci = new ClassInfo(printed, hidden);
        classnames.put(removeTemplate(className), ci);
        return ci;
    }

    /** Return true if the class name is associated to an hidden class or matches a hide expression */
    private boolean hidden(String s) {
	ClassInfo ci = getClassInfo(s);
	return (ci != null && ci.hidden) || optionProvider.getOptionsFor(s).matchesHideExpression(s);
    }

    

    /**
     * Prints the class if needed.
     * <p>
     * A class is a rootClass if it's included among the classes returned by
     * RootDoc.classes(), this information is used to properly compute
     * relative links in diagrams for UMLDoc
     */
    public String printClass(ClassDoc c, boolean rootClass) {
	ClassInfo ci;
	boolean toPrint;
	Options opt = optionProvider.getOptionsFor(c);

	String className = c.toString();
	if ((ci = getClassInfo(className)) != null)
	    toPrint = !ci.nodePrinted;
	else {
	    toPrint = true;
	    ci = newClassInfo(className, true, hidden(c));
	}
	if (toPrint && !hidden(c) && (!c.isEnum() || opt.showEnumerations)) {
	    // Associate classname's alias
	    String r = className;
	    w.println("\t// " + r);
	    // Create label
	    w.print("\t" + ci.name + " [label=");

	    boolean showMembers =
		(opt.showAttributes && c.fields().length > 0) ||
		(c.isEnum() && opt.showEnumConstants && c.enumConstants().length > 0) ||
		(opt.showOperations && c.methods().length > 0) ||
		(opt.showConstructors && c.constructors().length > 0);
	    
	    externalTableStart(opt, c.qualifiedName(), classToUrl(c, rootClass));

	    firstInnerTableStart(opt);
	    if (c.isInterface())
		tableLine(Align.CENTER, guilWrap(opt, "interface"));
	    if (c.isEnum())
		tableLine(Align.CENTER, guilWrap(opt, "enumeration"));
	    stereotype(opt, c, Align.CENTER);
	    Font font = c.isAbstract() && !c.isInterface() ? Font.CLASS_ABSTRACT : Font.CLASS;
	    String qualifiedName = qualifiedName(opt, r);
	    int startTemplate = qualifiedName.indexOf('<');
	    int idx = qualifiedName.lastIndexOf('.', startTemplate < 0 ? qualifiedName.length() - 1 : startTemplate);
	    if (opt.showComment)
		tableLine(Align.LEFT, Font.CLASS.wrap(opt, htmlNewline(escape(c.commentText()))));
	    else if (opt.postfixPackage && idx > 0 && idx < (qualifiedName.length() - 1)) {
		String packageName = qualifiedName.substring(0, idx);
		String cn = qualifiedName.substring(idx + 1);
		tableLine(Align.CENTER, font.wrap(opt, escape(cn)));
		tableLine(Align.CENTER, Font.PACKAGE.wrap(opt, packageName));
	    } else {
		tableLine(Align.CENTER, font.wrap(opt, escape(qualifiedName)));
	    }
	    tagvalue(opt, c);
	    firstInnerTableEnd(opt);
	    
	    if (showMembers) {
		if (opt.showAttributes) {
		    innerTableStart();
		    FieldDoc[] fields = c.fields();
		    // if there are no fields, print an empty line to generate proper HTML
		    if (fields.length == 0)
			tableLine(Align.LEFT, "");
		    else
			attributes(opt, c.fields());
		    innerTableEnd();
		} else if(!c.isEnum() && (opt.showConstructors || opt.showOperations)) {
		    // show an emtpy box if we don't show attributes but
		    // we show operations
		    innerTableStart();
		    tableLine(Align.LEFT, "");
		    innerTableEnd();
	    	}
		if (c.isEnum() && opt.showEnumConstants) {
		    innerTableStart();
		    FieldDoc[] ecs = c.enumConstants();
		    // if there are no constants, print an empty line to generate proper HTML		    
		    if (ecs.length == 0) {
			tableLine(Align.LEFT, "");
		    } else {
			for (FieldDoc fd : c.enumConstants()) {
			    tableLine(Align.LEFT, fd.name());
			}
		    }
		    innerTableEnd();
		}
		if (!c.isEnum() && (opt.showConstructors || opt.showOperations)) {
		    innerTableStart();
		    boolean printedLines = false;
		    if (opt.showConstructors)
			printedLines |= operations(opt, c.constructors());
		    if (opt.showOperations)
			printedLines |= operations(opt, c.methods());

		    if (!printedLines)
			// if there are no operations nor constructors,
			// print an empty line to generate proper HTML
			tableLine(Align.LEFT, "");

		    innerTableEnd();
		}
	    }
	    externalTableEnd();
	    w.print(", URL=\"" + classToUrl(c, rootClass) + "\"");
	    nodeProperties(opt);

	    // If needed, add a note for this node
	    int ni = 0;
	    for (Tag t : c.tags("note")) {
		String noteName = "n" + ni + "c" + ci.name;
		w.print("\t// Note annotation\n");
		w.print("\t" + noteName + " [label=");
		externalTableStart(UmlGraph.getCommentOptions(), c.qualifiedName(), classToUrl(c, rootClass));
		innerTableStart();
		tableLine(Align.LEFT, Font.CLASS.wrap(UmlGraph.getCommentOptions(), htmlNewline(escape(t.text()))));
		innerTableEnd();
		externalTableEnd();
		nodeProperties(UmlGraph.getCommentOptions());
		w.print("\t" + noteName + " -> " + relationNode(c) + "[arrowhead=none];\n");
		ni++;
	    }
	    ci.nodePrinted = true;
	}
	return ci.name;
    }

    private String getNodeName(ClassDoc c) {
	String className = c.toString();
	ClassInfo ci = getClassInfo(className);
	if (ci == null)
	    ci = newClassInfo(className, false, hidden(c));
	return ci.name;
    }
    
    /** Return a class's internal name */
    private String getNodeName(String c) {
	ClassInfo ci = getClassInfo(c);

	if (ci == null)
	    ci = newClassInfo(c, false, false);
	return ci.name;
    }

    /**
     * Print all relations for a given's class's tag
     * @param tagname the tag containing the given relation
     * @param from the source class
     * @param edgetype the dot edge specification
     */
    private void allRelation(Options opt, RelationType rt, ClassDoc from) {
	String tagname = rt.lower;
	for (Tag tag : from.tags(tagname)) {
	    String t[] = tokenize(tag.text());    // l-src label l-dst target
	    if (t.length != 4) {
		System.err.println("Error in " + from + "\n" + tagname + " expects four fields (l-src label l-dst target): " + tag.text());
		return;
	    }
	    ClassDoc to = from.findClass(t[3]);


	    if (to != null) {
		if(hidden(to))
		    continue;
		relation(opt, rt, from, to, t[0], t[1], t[2]);
	    } else {
		if(hidden(t[3]))
		    continue;
		relation(opt, rt, from, from.toString(), to, t[3], t[0], t[1], t[2]);
	    }
	}
    }

    /**
     * Print the specified relation
     * @param from the source class (may be null)
     * @param fromName the source class's name
     * @param to the destination class (may be null)
     * @param toName the destination class's name
     */
    private void relation(Options opt, RelationType rt, ClassDoc from, String fromName, 
	    ClassDoc to, String toName, String tailLabel, String label, String headLabel) {

	// print relation
	w.println("\t// " + fromName + " " + rt.lower + " " + toName);
	w.println("\t" + relationNode(from, fromName) + " -> " + relationNode(to, toName) + " [" +
    	"taillabel=\"" + tailLabel + "\", " +
    	((label == null || label.isEmpty()) ? "label=\"\", " : "label=\"" + guillemize(opt, label) + "\", ") +
    	"headlabel=\"" + headLabel + "\", " +
    	"fontname=\"" + opt.edgeFontName + "\", " +
    	"fontcolor=\"" + opt.edgeFontColor + "\", " +
    	"fontsize=" + opt.edgeFontSize + ", " +
    	"color=\"" + opt.edgeColor + "\", " +
    	rt.style + "];"
    	);
	
	// update relation info
	RelationDirection d = RelationDirection.BOTH;
	if(rt == RelationType.NAVASSOC || rt == RelationType.DEPEND)
	    d = RelationDirection.OUT;
	getClassInfo(fromName).addRelation(toName, rt, d);
        getClassInfo(toName).addRelation(fromName, rt, d.inverse());
    }

    /**
     * Print the specified relation
     * @param from the source class
     * @param to the destination class
     */
    private void relation(Options opt, RelationType rt, ClassDoc from,
	    ClassDoc to, String tailLabel, String label, String headLabel) {
	relation(opt, rt, from, from.toString(), to, to.toString(), tailLabel, label, headLabel);
    }


    /** Return the full name of a relation's node.
     * This may involve appending the port :p for the standard nodes
     * whose outline is rendered through an inner table.
     */
    private String relationNode(ClassDoc c) {
	return getNodeName(c) + optionProvider.getOptionsFor(c).shape.landingPort();
    }

    /** Return the full name of a relation's node c.
     * This may involve appending the port :p for the standard nodes
     * whose outline is rendered through an inner table.
     * @param c the node's class (may be null)
     * @param cName the node's class name
     */
    private String relationNode(ClassDoc c, String cName) {
	Options opt = c == null ? optionProvider.getOptionsFor(cName) : optionProvider.getOptionsFor(c);
	return getNodeName(cName) + opt.shape.landingPort();
    }

    /** Print a class's relations */
    public void printRelations(ClassDoc c) {
	Options opt = optionProvider.getOptionsFor(c);
	if (hidden(c) || c.name().equals("")) // avoid phantom classes, they may pop up when the source uses annotations
	    return;
	String className = c.toString();

	// Print generalization (through the Java superclass)
	Type s = c.superclassType();
	if (s != null &&
	    !s.toString().equals("java.lang.Object") &&
	    !c.isEnum() &&
	    !hidden(s.asClassDoc())) {
	    	ClassDoc sc = s.asClassDoc();
		w.println("\t//" + c + " extends " + s + "\n" +
		    "\t" + relationNode(sc) + " -> " + relationNode(c) +
		    " [" + RelationType.EXTENDS.style + "];");
		getClassInfo(className).addRelation(sc.toString(), RelationType.EXTENDS, RelationDirection.OUT);
		getClassInfo(sc.toString()).addRelation(className, RelationType.EXTENDS, RelationDirection.IN);
	}

	// Print generalizations (through @extends tags)
	for (Tag tag : c.tags("extends"))
	    if (!hidden(tag.text())) {
		ClassDoc from = c.findClass(tag.text());
		w.println("\t//" + c + " extends " + tag.text() + "\n" +
		    "\t" + relationNode(from, tag.text()) + " -> " + relationNode(c) + " [" + RelationType.EXTENDS.style + "];");
		getClassInfo(className).addRelation(tag.text(), RelationType.EXTENDS, RelationDirection.OUT);
		getClassInfo(tag.text()).addRelation(className, RelationType.EXTENDS, RelationDirection.IN);
	    }
	// Print realizations (Java interfaces)
	for (Type iface : c.interfaceTypes()) {
	    ClassDoc ic = iface.asClassDoc();
	    if (!hidden(ic)) {
		w.println("\t//" + c + " implements " + ic + "\n\t" + relationNode(ic) + " -> " + relationNode(c)
			+ " [" + RelationType.IMPLEMENTS.style + "];");
		getClassInfo(className).addRelation(ic.toString(), RelationType.IMPLEMENTS, RelationDirection.OUT);
		getClassInfo(ic.toString()).addRelation(className, RelationType.IMPLEMENTS, RelationDirection.IN);
	    }
	}
	// Print other associations
	allRelation(opt, RelationType.ASSOC, c);
	allRelation(opt, RelationType.NAVASSOC, c);
	allRelation(opt, RelationType.HAS, c);
	allRelation(opt, RelationType.NAVHAS, c);
	allRelation(opt, RelationType.COMPOSED, c);
	allRelation(opt, RelationType.NAVCOMPOSED, c);
	allRelation(opt, RelationType.DEPEND, c);
    }

    /** Print classes that were parts of relationships, but not parsed by javadoc */
    public void printExtraClasses(RootDoc root) {
	Set<String> names = new HashSet<String>(classnames.keySet()); 
	for(String className: names) {
	    ClassInfo info = getClassInfo(className);
	    if (!info.nodePrinted) {
		ClassDoc c = root.classNamed(className);
		if(c != null) {
		    printClass(c, false);
		} else {
		    Options opt = optionProvider.getOptionsFor(className);
		    if(opt.matchesHideExpression(className))
			continue;
		    w.println("\t// " + className);
		    w.print("\t" + info.name + "[label=");
		    externalTableStart(opt, className, classToUrl(className));
		    innerTableStart();
		    String qualifiedName = qualifiedName(opt, className);
		    int startTemplate = qualifiedName.indexOf('<');
		    int idx = qualifiedName.lastIndexOf('.', startTemplate < 0 ? qualifiedName.length() - 1 : startTemplate);
		    if(opt.postfixPackage && idx > 0 && idx < (qualifiedName.length() - 1)) {
			String packageName = qualifiedName.substring(0, idx);
			String cn = qualifiedName.substring(idx + 1);
			tableLine(Align.CENTER, Font.CLASS.wrap(opt, escape(cn)));
			tableLine(Align.CENTER, Font.PACKAGE.wrap(opt, packageName));
		    } else {
			tableLine(Align.CENTER, Font.CLASS.wrap(opt, escape(qualifiedName)));
		    }
		    innerTableEnd();
		    externalTableEnd();
		    if (className == null || className.length() == 0)
			w.print(", URL=\"" + classToUrl(className) + "\"");
		    nodeProperties(opt);
		}
	    }
	}
    }
    
    /**
     * Prints associations recovered from the fields of a class. An association is inferred only
     * if another relation between the two classes is not already in the graph.
     * @param classes
     */  
    public void printInferredRelations(ClassDoc c) {
	Options opt = optionProvider.getOptionsFor(c);

	// check if the source is excluded from inference
	if (hidden(c))
	    return;

	for (FieldDoc field : c.fields(false)) {
	    if(hidden(field))
		continue;

	    // skip statics
	    if(field.isStatic())
		continue;
	    
	    // skip primitives
	    FieldRelationInfo fri = getFieldRelationInfo(field);
	    if (fri == null)
		continue;

	    // check if the destination is excluded from inference
	    if (hidden(fri.cd))
		continue;

	    // if source and dest are not already linked, add a dependency
	    RelationPattern rp = getClassInfo(c.toString()).getRelation(fri.cd.toString());
	    if (rp == null) {
		String destAdornment = fri.multiple ? "*" : "";
		relation(opt, opt.inferRelationshipType, c, fri.cd, "", "", destAdornment);
            }
	}
    }

    /** Returns an array representing the imported classes of c.
     * Disables the deprecation warning, which is output, because the
     * imported classed are an implementation detail.
     */
    @SuppressWarnings( "deprecation" )
    ClassDoc[] importedClasses(ClassDoc c) {
        return c.importedClasses();
    }

    /**
     * Prints dependencies recovered from the methods of a class. A
     * dependency is inferred only if another relation between the two
     * classes is not already in the graph.
     * @param classes
     */  
    public void printInferredDependencies(ClassDoc c) {
	Options opt = optionProvider.getOptionsFor(c);

	String sourceName = c.toString();
	if (hidden(c))
	    return;

	Set<Type> types = new HashSet<Type>();
	// harvest method return and parameter types
	for (MethodDoc method : filterByVisibility(c.methods(false), opt.inferDependencyVisibility)) {
	    types.add(method.returnType());
	    for (Parameter parameter : method.parameters()) {
		types.add(parameter.type());
	    }
	}
	// and the field types
	if (!opt.inferRelationships) {
	    for (FieldDoc field : filterByVisibility(c.fields(false), opt.inferDependencyVisibility)) {
		types.add(field.type());
	    }
	}
	// see if there are some type parameters
	if (c.asParameterizedType() != null) {
	    ParameterizedType pt = c.asParameterizedType();
	    types.addAll(Arrays.asList(pt.typeArguments()));
	}
	// see if type parameters extend something
	for(TypeVariable tv: c.typeParameters()) {
	    if(tv.bounds().length > 0 )
		types.addAll(Arrays.asList(tv.bounds()));
	}

	// and finally check for explicitly imported classes (this
	// assumes there are no unused imports...)
	if (opt.useImports)
	    types.addAll(Arrays.asList(importedClasses(c)));

	// compute dependencies
	for (Type type : types) {
	    // skip primitives and type variables, as well as dependencies
	    // on the source class
	    if (type.isPrimitive() || type instanceof WildcardType || type instanceof TypeVariable
		    || c.toString().equals(type.asClassDoc().toString()))
		continue;

	    // check if the destination is excluded from inference
	    ClassDoc fc = type.asClassDoc();
	    if (hidden(fc))
		continue;
	    
	    // check if source and destination are in the same package and if we are allowed
	    // to infer dependencies between classes in the same package
	    if(!opt.inferDepInPackage && c.containingPackage().equals(fc.containingPackage()))
		continue;

	    // if source and dest are not already linked, add a dependency
	    RelationPattern rp = getClassInfo(sourceName).getRelation(fc.toString());
	    if (rp == null || rp.matchesOne(new RelationPattern(RelationDirection.OUT))) {
		relation(opt, RelationType.DEPEND, c, fc, "", "", "");
	    }
	    
	}
    }
    
    /**
     * Returns all program element docs that have a visibility greater or
     * equal than the specified level
     */
    private <T extends ProgramElementDoc> List<T> filterByVisibility(T[] docs, Visibility visibility) {
	if (visibility == Visibility.PRIVATE)
	    return Arrays.asList(docs);

	List<T> filtered = new ArrayList<T>();
	for (T doc : docs) {
	    if (Visibility.get(doc).compareTo(visibility) > 0)
		filtered.add(doc);
	}
	return filtered;
    }



    private FieldRelationInfo getFieldRelationInfo(FieldDoc field) {
	Type type = field.type();
	if(type.isPrimitive() || type instanceof WildcardType || type instanceof TypeVariable)
	    return null;
	
	if (type.dimension().endsWith("[]")) {
	    return new FieldRelationInfo(type.asClassDoc(), true);
	}
	
	Options opt = optionProvider.getOptionsFor(type.asClassDoc());
	if (opt.matchesCollPackageExpression(type.qualifiedTypeName())) {
	    Type[] argTypes = getInterfaceTypeArguments(collectionClassDoc, type);
	    if (argTypes != null && argTypes.length == 1 && !argTypes[0].isPrimitive())
		return new FieldRelationInfo(argTypes[0].asClassDoc(), true);

	    argTypes = getInterfaceTypeArguments(mapClassDoc, type);
	    if (argTypes != null && argTypes.length == 2 && !argTypes[1].isPrimitive())
		return new FieldRelationInfo(argTypes[1].asClassDoc(), true);
	}

	return new FieldRelationInfo(type.asClassDoc(), false);
    }
    
    private Type[] getInterfaceTypeArguments(ClassDoc iface, Type t) {
	if (t instanceof ParameterizedType) {
	    ParameterizedType pt = (ParameterizedType) t;
	    if (iface != null && iface.equals(t.asClassDoc())) {
		return pt.typeArguments();
	    } else {
		for (Type pti : pt.interfaceTypes()) {
		    Type[] result = getInterfaceTypeArguments(iface, pti);
		    if (result != null)
			return result;
		}
		if (pt.superclassType() != null)
		    return getInterfaceTypeArguments(iface, pt.superclassType());
	    }
	} else if (t instanceof ClassDoc) {
	    ClassDoc cd = (ClassDoc) t;
	    for (Type pti : cd.interfaceTypes()) {
		Type[] result = getInterfaceTypeArguments(iface, pti);
		if (result != null)
		    return result;
	    }
	    if (cd.superclassType() != null)
		return getInterfaceTypeArguments(iface, cd.superclassType());
	}
	return null;
    }

    /** Convert the class name into a corresponding URL */
    public String classToUrl(ClassDoc cd, boolean rootClass) {
	// building relative path for context and package diagrams
	if(contextDoc != null && rootClass) {
	    // determine the context path, relative to the root
	    String packageName;
	    if (contextDoc instanceof ClassDoc) {
		    packageName = ((ClassDoc) contextDoc).containingPackage().name();
		} else if (contextDoc instanceof PackageDoc) {
		    packageName = ((PackageDoc) contextDoc).name();
		} else {
		    return classToUrl(cd.qualifiedName());
		}
	    return buildRelativePathFromClassNames(packageName, cd.containingPackage().name()) + cd.name() + ".html";
	} else {
	    return classToUrl(cd.qualifiedName());
	} 
    }

	private String getPackageName(String className) {
		if (this.rootClassdocs.get(className) == null) {
			int idx = className.lastIndexOf('.');
			return idx > 0 ? className.substring(0, idx) : "";
		} else {
			return this.rootClassdocs.get(className).containingPackage().name();
		}
	}
	
	private String getUnqualifiedName(String className) {
		if (this.rootClassdocs.get(className) == null) {
			int idx = className.lastIndexOf('.');
			return idx > 0 ? className.substring(idx + 1) : className;
		} else {
			return this.rootClassdocs.get(className).name();
		}
	}
    
    /** Convert the class name into a corresponding URL */
    public String classToUrl(String className) {
	String docRoot = mapApiDocRoot(className);
	if (docRoot == null)
	    return null;
	return new StringBuilder(250).append(docRoot) //
		.append(getPackageName(className).replace('.', FILE_SEPARATOR)) //
		.append(FILE_SEPARATOR) //
		.append(getUnqualifiedName(className)) //
		.append(".html").toString();
    }

    /**
     * Returns the appropriate URL "root" for a given class name.
     * The root will be used as the prefix of the URL used to link the class in
     * the final diagram to the associated JavaDoc page.
     */
    private String mapApiDocRoot(String className) {
	/* If no packages are specified, we use apiDocRoot for all of them. */
	if (rootClasses.contains(className))
	    return optionProvider.getGlobalOptions().apiDocRoot;
	else
	    return optionProvider.getGlobalOptions().getApiDocRoot(className);
    }

    
    /** Dot prologue 
     * @throws IOException */
    public void prologue() throws IOException {
	Options opt = optionProvider.getGlobalOptions();
	OutputStream os;

	if (opt.outputFileName.equals("-"))
	    os = System.out;
	else {
	    // prepare output file. Use the output file name as a full path unless the output
	    // directory is specified
	    File file = new File(opt.outputDirectory, opt.outputFileName);
	    // make sure the output directory are there, otherwise create them
	    if (file.getParentFile() != null
		&& !file.getParentFile().exists())
		file.getParentFile().mkdirs();
	    os = new FileOutputStream(file);
	}

	// print prologue
	w = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), opt.outputEncoding));
	w.println(
	    "#!/usr/local/bin/dot\n" +
	    "#\n" +
	    "# Class diagram \n" +
	    "# Generated by UMLGraph version " +
	    Version.VERSION + " (http://www.spinellis.gr/umlgraph/)\n" +
	    "#\n\n" +
	    "digraph G {\n" +
	    "\tedge [fontname=\"" + opt.edgeFontName +
	    "\",fontsize=10,labelfontname=\"" + opt.edgeFontName +
	    "\",labelfontsize=10];\n" +
	    "\tnode [fontname=\"" + opt.nodeFontName +
	    "\",fontsize=10,shape=plaintext];"
	);

	w.println("\tnodesep=" + opt.nodeSep + ";");
	w.println("\tranksep=" + opt.rankSep + ";");
	if (opt.horizontal)
	    w.println("\trankdir=LR;");
	if (opt.bgColor != null)
	    w.println("\tbgcolor=\"" + opt.bgColor + "\";\n");
    }

    /** Dot epilogue */
    public void epilogue() {
	w.println("}\n");
	w.flush();
	w.close();
    }
    
    private void externalTableStart(Options opt, String name, String url) {
	String bgcolor = opt.nodeFillColor == null ? "" : (" bgcolor=\"" + opt.nodeFillColor + "\"");
	String href = url == null ? "" : (" href=\"" + url + "\" target=\"_parent\"");
	w.print("<<table title=\"" + name + "\" border=\"0\" cellborder=\"" + 
	    opt.shape.cellBorder() + "\" cellspacing=\"0\" " +
	    "cellpadding=\"2\" port=\"p\"" + bgcolor + href + ">" + linePostfix);
    }
    
    private void externalTableEnd() {
	w.print(linePrefix + linePrefix + "</table>>");
    }
    
    private void innerTableStart() {
	w.print(linePrefix + linePrefix + "<tr><td><table border=\"0\" cellspacing=\"0\" "
		+ "cellpadding=\"1\">" + linePostfix);
    }
    
    /**
     * Start the first inner table of a class.
     */
    private void firstInnerTableStart(Options opt) {
	w.print(linePrefix + linePrefix + "<tr>" + opt.shape.extraColumn() +
		"<td><table border=\"0\" cellspacing=\"0\" " +
		"cellpadding=\"1\">" + linePostfix);
    }
    
    private void innerTableEnd() {
	w.print(linePrefix + linePrefix + "</table></td></tr>" + linePostfix);
    }

    /**
     * End the first inner table of a class.
     */
    private void firstInnerTableEnd(Options opt) {
	w.print(linePrefix + linePrefix + "</table></td>" +
	    opt.shape.extraColumn() + "</tr>" + linePostfix);
    }

    private void tableLine(Align align, String text) {
	w.print("<tr><td align=\"" + align.lower + "\" balign=\"" + align.lower + "\">" //
		+ text // MAY contain markup!
		+ "</td></tr>" + linePostfix);
    }

    private static class FieldRelationInfo {
	ClassDoc cd;
	boolean multiple;

	public FieldRelationInfo(ClassDoc cd, boolean multiple) {
	    this.cd = cd;
	    this.multiple = multiple;
	}
    }
}
