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
 * $Id$
 *
 */

package gr.spinellis.umlgraph.doclet;

import com.sun.javadoc.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class graph generation engine
 * @depend - - - StringUtil
 * @composed - - * ClassInfo
 * @has - - - OptionProvider
 *
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class ClassGraph {
    private Map<String, ClassInfo> classnames = new HashMap<String, ClassInfo>();
    private String apiDocRoot;
    private Map<Pattern, String> apiDocMap = new HashMap<Pattern, String>();

    private static final String DEFAULT_EXTERNAL_APIDOC = "http://java.sun.com/j2se/1.4.2/docs/api/";

    private static final char FILE_SEPARATOR = '/';

    private Set<String> specifiedPackages;
    private OptionProvider optionProvider;
    private PrintWriter w;

    /**
     * Create a new ClassGraph.  The packages passed as an
     * argument are the ones specified on the command line.
     * Local URLs will be generated for these packages.
     */
    public ClassGraph(PackageDoc[] packages, OptionProvider optionProvider) throws IOException {
	this.optionProvider = optionProvider;
	specifiedPackages = new HashSet<String>();
	for (PackageDoc p : packages)
	    specifiedPackages.add(p.name());
	Options opt = optionProvider.getGlobalOptions();
	apiDocRoot = fixApiDocRoot(opt.apiDocRoot);
	String mapFileName = opt.apiDocMapFileName;
 	if (mapFileName != null) {
	    InputStream is = new FileInputStream(mapFileName);
	    Properties userMap = new Properties();
	    userMap.load(is);
	    for (Map.Entry mapEntry : userMap.entrySet()) {
		try {
		    Pattern regex = Pattern.compile((String)mapEntry.getKey());
		    String thisRoot = (String)mapEntry.getValue();
		    if (thisRoot != null) {
			thisRoot = fixApiDocRoot(thisRoot);
			apiDocMap.put(regex, thisRoot);
		    } else {
			System.err.println("No URL for pattern " + mapEntry.getKey());
		    }
		} catch (PatternSyntaxException e) {
		    System.err.println("Skipping bad pattern " + mapEntry.getKey());
		}
	    }
	} else
	    apiDocMap.put(Pattern.compile(".*"), DEFAULT_EXTERNAL_APIDOC);
    }

    /** Trim and append a file separator to the string */
    private String fixApiDocRoot(String str) {
	String fixed = null;
	if (str != null) {
	    fixed = str.trim();
	    if (fixed.length() > 0) {
		if (!File.separator.equals("/"))
		    fixed = fixed.replace(File.separator.charAt(0), '/');
		if (!fixed.endsWith("/"))
		    fixed = fixed + "/";
	    }
	}
	return fixed;
    }

    /** Return the class's name, possibly by stripping the leading path */
    private String qualifiedName(Options opt, String r) {
	if (!opt.showQualified) {
	    // Create readable string by stripping leading path
	    for (;;)  {
		int dotpos = r.lastIndexOf('.');
		if (dotpos == -1) break; // Work done!
		/*
		 * Change all occurences of
		 * "p1.p2.myClass<S extends dummy.Otherclass>" into
		 * "myClass<S extends Otherclass>"
		 */
		int start = dotpos;
		while (start > 0 && Character.isJavaIdentifierPart(r.charAt(start - 1)))
		    start--;
		r = r.substring(0, start) + r.substring(dotpos + 1);
	    }
	}
	return r;
    }

    /** Escape &lt; and &gt; characters in the string with a backslash. */
    private String escapeLG(String s) {
	StringBuffer r = new StringBuffer(s);
	for (int i = 0; i < r.length(); i++)
	    switch (r.charAt(i)) {
	    case '<':
	    case '>':
		r.insert(i++, '\\');
	    }
	return r.toString();
    }

    /**
     * Convert &lt; and &gt; characters in the string to the respective guillemot characters.
     */
    private String guillemize(Options opt, String s) {
	StringBuffer r = new StringBuffer(s);
	for (int i = 0; i < r.length();)
	    switch (r.charAt(i)) {
	    case '<':
		r.replace(i, i + 1, opt.guilOpen);
		i += opt.guilOpen.length();
		break;
	    case '>':
		r.replace(i, i + 1, opt.guilClose);
		i += opt.guilOpen.length();
		break;
	    default:
		i++;
		break;
	    }
	return r.toString();
    }

    /**
     * Wraps a string in Guillemot (or an ASCII substitute) characters.
     *
     * @param str the <code>String</code> to be wrapped.
     * @return the wrapped <code>String</code>.
     */
    private String guilWrap(Options opt, String str) {
	return opt.guilOpen + str + opt.guilClose;
    }

    /**
     * Print the visibility adornment of element e prefixed by
     * any stereotypes
     */
    private void visibility(Options opt, ProgramElementDoc e) {
	w.print(stereotype(opt, e, 'l'));
	if (!opt.showVisibility)
	    return;
	if (e.isPrivate())
	    w.print('-');
	else if (e.isPublic())
	    w.print('+');
	else if (e.isProtected())
	    w.print('#');
	else if (e.isPackagePrivate())
	    w.print('~');
	w.print(' ');
    }

    /** Print the method parameter p */
    private void parameter(Options opt, Parameter p[]) {
	for (int i = 0; i < p.length; i++) {
	    w.print(p[i].name());
	    typeAnnotation(opt, p[i].type());
	    if (i + 1 < p.length)
		w.print(", ");
	}
    }

    /** Print a a basic type t */
    private void type(Options opt, Type t) {
	if (opt.showQualified)
	    w.print(t.qualifiedTypeName());
	else
	    w.print(t.typeName());
	typeParameters(opt, t.asParameterizedType());
    }

    /** Print the parameters of the parameterized type t */
    private void typeParameters(Options opt, ParameterizedType t) {
	if (t == null)
	    return;
	Type args[] = t.typeArguments();
	w.print("\\<");
	for (int i = 0; i < args.length; i++) {
	    type(opt, args[i]);
	    if (i != args.length - 1)
		w.print(", ");
	}
	w.print("\\>");
    }

    /** Annotate an field/argument with its type t */
    private void typeAnnotation(Options opt, Type t) {
	if (t.typeName().equals("void"))
	    return;
	w.print(" : ");
	type(opt, t);
	w.print(t.dimension());
    }

    /** Print the class's attributes fd */
    private void attributes(Options opt, FieldDoc fd[]) {
	for (FieldDoc f : fd) {
	    if (hidden(f))
		continue;
	    visibility(opt, f);
	    w.print(f.name());
	    if (opt.showType)
		typeAnnotation(opt, f.type());
	    w.print("\\l");
	    w.print(tagvalue(f, "", 'r'));
	}
    }

    /*
     * The following two methods look similar, but can't
     * be refactored into one, because their common interface,
     * ExecutableMemberDoc, doesn't support returnType for ctors.
     */

    /** Print the class's constructors m */
    private void operations(Options opt, ConstructorDoc m[]) {
	for (ConstructorDoc cd : m) {
	    if (hidden(cd))
		continue;
	    visibility(opt, cd);
	    w.print(cd.name());
	    if (opt.showType) {
		w.print("(");
		parameter(opt, cd.parameters());
		w.print(")");
	    } else
		w.print("()");
	    w.print("\\l");
	    w.print(tagvalue(cd, "", 'r'));
	}
    }

    /** Print the class's operations m */
    private void operations(Options opt, MethodDoc m[]) {
	for (MethodDoc md : m) {
	    if (hidden(md))
		continue;
	    visibility(opt, md);
	    w.print(md.name());
	    if (opt.showType) {
		w.print("(");
		parameter(opt, md.parameters());
		w.print(")");
		typeAnnotation(opt, md.returnType());
	    } else
		w.print("()");
	    w.print("\\l");
	    w.print(tagvalue(md, "", 'r'));
	}
    }

    /** Print the common class node's properties */
    private void nodeProperties(Options opt, String s) {
	if (opt.nodeFillColor != null)
	    w.print(", style=filled, fillcolor=\"" + opt.nodeFillColor + "\"");
	w.print(", fontcolor=\"" + opt.nodeFontColor + "\"");
	w.print(", fontsize=" + opt.nodeFontSize);
	String url = classToUrl(s);
	if (url != null)
	    w.print(", URL=\"" + url + "\"");
	w.println("];");
    }

    /**
     * Return as a string the tagged values associated with c
     * @param c the Doc entry to look for @tagvalue
     * @param prevterm the termination string for the previous element
     * @param term the termination character for each tagged value
     */
    private static String tagvalue(Doc c, String prevterm, char term) {
	String r;
	Tag tags[] = c.tags("tagvalue");
	if (tags.length > 0)
	    r = prevterm;
	else
	    r = "";
	for (Tag tag : tags) {
	    String t[] = StringUtil.tokenize(tag.text());
	    if (t.length != 2) {
		System.err.println("@tagvalue expects two fields: " + tag.text());
		return ("");
	    }
	    r += "\\{" + t[0] + " = " + t[1] + "\\}\\" + term;
	}
	return (r);
    }

    /**
     * Return as a string the stereotypes associated with c
     * terminated by the escape character term
     */
    private String stereotype(Options opt, Doc c, char term) {
	String r = "";
	for (Tag tag : c.tags("stereotype")) {
	    String t[] = StringUtil.tokenize(tag.text());
	    if (t.length != 1) {
		System.err.println("@stereotype expects one field: " + tag.text());
		return ("");
	    }
	    r += guilWrap(opt, t[0]) + " \\" + term;
	}
	return (r);
    }

    /** Return true if c has a @hidden tag associated with it */
    private boolean hidden(Doc c) {
	Tag tags[] = c.tags("hidden");
	if (tags.length > 0)
	    return true;
	tags = c.tags("view");
	if (tags.length > 0)
	    return true;
	Options opt = optionProvider.getOptionsFor(c.toString());
	return opt.matchesHideExpression(c.toString());
    }
    
    private ClassInfo getClassInfo(String className) {
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
	Options opt = optionProvider.getOptionsFor(s);
	if(ci != null)
	    return ci.hidden || opt.matchesHideExpression(s);
	else
	    return opt.matchesHideExpression(s);
    }

    

    /** Prints the class if needed */
    public String printClass(ClassDoc c) {
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
	    w.print("\t" + ci.name + " [");
	    r = stereotype(opt, c, 'n') + escapeLG(qualifiedName(opt, r));
	    if (c.isInterface())
		r = guilWrap(opt, "interface") + " \\n" + r;
	    if (c.isEnum())
		r = guilWrap(opt, "enumeration") + " \\n" + r;
	    boolean showMembers =
		(opt.showAttributes && c.fields().length > 0) ||
		(c.isEnum() && opt.showEnumConstants && c.enumConstants().length > 0) ||
		(opt.showOperations && c.methods().length > 0) ||
		(opt.showConstructors && c.constructors().length > 0);
	    r += tagvalue(c, "\\n", 'r');
	    if (showMembers)
		w.print("label=\"{" + r + "\\n|");
	    else
		w.print("label=\"" + r + "\"");
	    if (opt.showAttributes)
		attributes(opt, c.fields());
	    if (c.isEnum() && opt.showEnumConstants) {
		for (FieldDoc fd : c.enumConstants()) {
		    w.print(fd.name());
		    w.print("\\l");
		}
	    }
	    if (showMembers)
		w.print("|");
	    if (opt.showConstructors && !c.isEnum())
		operations(opt, c.constructors());
	    if (opt.showOperations && !c.isEnum())
		operations(opt, c.methods());
	    if (showMembers)
		w.print("}\"");
	    // Use ariali [sic] for gif output of abstract classes
	    w.print(", fontname=\"" +
		(c.isAbstract() ?
		 opt.nodeFontAbstractName :
		 opt.nodeFontName) + "\"");
	    nodeProperties(opt, c.qualifiedName());
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
     * @param name the source class internal name
     * @param edgetype the dot edge specification
     */
    private void relation(Options opt, String tagname, ClassDoc from, String name, String edgetype) {
	for (Tag tag : from.tags(tagname)) {
	    String t[] = StringUtil.tokenize(tag.text());    // l-src label l-dst target
	    if (t.length != 4) {
		System.err.println("Error in " + from + "\n" + tagname + " expects four fields (l-src label l-dst target): " + tag.text());
		return;
	    }
	    String destName = t[3];
	    String nodeName = null;
	    ClassDoc to = from.findClass(t[3]);
	    if(to != null) {
		destName = to.toString();
		nodeName = getNodeName(destName);
	    } else {
		nodeName = getNodeName(t[3]);
	    }
	    
	    if(hidden(destName))
		continue;
	    w.println("\t// " + from + " " + tagname + " " +destName);
	    w.println("\t" + name + " -> " + nodeName + " [" +
		"taillabel=\"" + t[0] + "\", " +
		"label=\"" + guillemize(opt, t[1]) + "\", " +
		"headlabel=\"" + t[2] + "\", " +
		"fontname=\"" + opt.edgeFontName + "\", " +
		"fontcolor=\"" + opt.edgeFontColor + "\", " +
		"fontsize=" + opt.edgeFontSize + ", " +
		"color=\"" + opt.edgeColor + "\", " +
		edgetype + "];"
	    );
	}
    }

    /** Print a class's relations */
    public void printRelations(ClassDoc c) {
	Options opt = optionProvider.getOptionsFor(c);
	if (hidden(c))
	    return;
	String cs = getNodeName(c);

	// Print generalization (through the Java superclass)
	Type s = c.superclassType();
	if (s != null &&
	    !s.toString().equals("java.lang.Object") &&
	    !c.isEnum() &&
	    !hidden(s.asClassDoc()))
		w.println("\t//" + c + " extends " + s + "\n" +
		    "\t" + getNodeName(s.asClassDoc()) + " -> " + cs + " [dir=back,arrowtail=empty];");

	// Print generalizations (through @extends tags)
	for (Tag tag : c.tags("extends"))
	    if (!hidden(tag.text()))
		w.println("\t//" + c + " extends " + tag.text() + "\n" +
		    "\t" + getNodeName(tag.text()) + " -> " + cs + " [dir=back,arrowtail=empty];");
	// Print realizations (Java interfaces)
	for (Type iface : c.interfaceTypes())
	    if (!hidden(iface.asClassDoc()))
		w.println("\t" + getNodeName(iface.asClassDoc()) + " -> " + cs + " [dir=back,arrowtail=empty,style=dashed];" +
		    "\t//" + c + " implements " + iface.asClassDoc());
	// Print other associations
	relation(opt, "assoc", c, cs, "arrowhead=none");
	relation(opt, "navassoc", c, cs, "arrowhead=open");
	relation(opt, "has", c, cs, "arrowhead=none, arrowtail=ediamond");
	relation(opt, "composed", c, cs, "arrowhead=none, arrowtail=diamond");
	relation(opt, "depend", c, cs, "arrowhead=open, style=dashed");
    }

    /** Print classes that were parts of relationships, but not parsed by javadoc */
    public void printExtraClasses(RootDoc root) {
	Set<String> names = new HashSet<String>(classnames.keySet()); 
	for(String className: names) {
	    ClassInfo info = getClassInfo(className);
	    if (!info.nodePrinted) {
		ClassDoc c = root.classNamed(className);
		if(c != null) {
		    printClass(c);
		} else {
		    Options opt = optionProvider.getOptionsFor(className);
		    if(opt.matchesHideExpression(className))
			continue;
		    w.println("\t// " + className);
		    w.print("\t" + info.name + "[label=\"" + escapeLG(qualifiedName(opt, className)) + "\"");
		    w.print(", fontname=\"" + opt.nodeFontName + "\"");
		    nodeProperties(opt, className);
		}
	    }
	}
    }

    /** Removes the template specs from a class name. */
    private String removeTemplate(String name) {
	int openIdx = name.indexOf("<");
	if(openIdx == -1)
	    return name;
	else
	    return name.substring(0, openIdx);
    }

    /** Return true if the class name has been specified in the command line */
    public boolean isSpecifiedPackage(String className) {
	int idx = className.lastIndexOf(".");
	String packageName = idx > 0 ? className.substring(0, idx) : className;
	return specifiedPackages.contains(packageName);
    }

    /** Convert the class name into a corresponding URL */
    public String classToUrl(String className) {
	String result = null;
	String docRoot = mapApiDocRoot(className);
	if (docRoot != null) {
	    StringBuffer buf = new StringBuffer(docRoot);
	    buf.append(className.replace('.', FILE_SEPARATOR));
	    buf.append(".html");
	    result = buf.toString();
	}
	return result;
    }

    /**
     * Returns the appropriate URL "root" for a given class name.
     * The root will be used as the prefix of the URL used to link the class in
     * the final diagram to the associated JavaDoc page.
     */
    private String mapApiDocRoot(String className) {

	String root = null;
	/* If no packages are specified, we use apiDocRoot for all of them. */
	if (specifiedPackages.isEmpty() || isSpecifiedPackage(className))
	    root = apiDocRoot;
	else
	    root = getExternalApiDocRoot(className);
	return root;
    }

    /**
     * Returns the appropriate URL "root" for an external class name.  It will
     * match the class name against the regular expressions specified in the
     * <code>apiDocMapFileName</doc> file; if a match is found, the associated URL
     * will be returned.
     *
     * <b>NOTE:</b> Currently the order of the match attempts is not specified,
     * so if more then one regular expression matches the result is undetermined.
     */
    public String getExternalApiDocRoot(String className) {
	for (Map.Entry<Pattern, String> mapEntry : apiDocMap.entrySet()) {
	    Pattern regex = mapEntry.getKey();
	    Matcher matcher = regex.matcher(className);
	    if (matcher.matches())
		return mapEntry.getValue();
	}
	return null;
    }
    
    /** Dot prologue 
     * @throws IOException */
    public void prologue() throws IOException {
	Options opt = optionProvider.getGlobalOptions();
	FileOutputStream fos = new FileOutputStream(new File(opt.outputDirectory, opt.outputFileName));
	w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos, opt.outputEncoding)));
	w.println(
	    "#!/usr/local/bin/dot\n" +
	    "#\n" +
	    "# Class diagram \n" +
	    "# Generated by UmlGraph version " +
	    Version.VERSION + " (http://www.spinellis.gr/sw/umlgraph)\n" +
	    "#\n\n" +
	    "digraph G {\n" +
	    "\tedge [fontname=\"" + opt.edgeFontName +
	    "\",fontsize=10,labelfontname=\"" + opt.edgeFontName +
	    "\",labelfontsize=10];\n" +
	    "\tnode [fontname=\"" + opt.nodeFontName +
	    "\",fontsize=10,shape=record];"
	);
	if (opt.horizontal)
	    w.println("\trankdir=LR;\n\tranksep=1;");
	if (opt.bgColor != null)
	    w.println("\tbgcolor=\"" + opt.bgColor + "\";\n");
    }

    /** Dot epilogue */
    public void epilogue() {
	w.println("}\n");
	w.flush();
	w.close();
    }
}
