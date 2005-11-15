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
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class ClassGraph {
    private Map<String, ClassInfo> classnames = new HashMap<String, ClassInfo>();
    private String apiDocRoot;
    private Map<Pattern, String> apiDocMap = new HashMap<Pattern, String>();

    private static final String DEFAULT_EXTERNAL_APIDOC = "http://java.sun.com/j2se/1.4.2/docs/api/";

    private static final char FILE_SEPARATOR = '/';

    private Options opt;
    private Set<String> specifiedPackages;

    /**
     * Create a new ClassGraph.  The packages passed as an
     * argument are the ones specified on the command line.
     * Local URLs will be generated for these packages.
     */
    public ClassGraph(PackageDoc[] packages, String root, String mapFileName) throws IOException {
	specifiedPackages = new HashSet<String>();
	for (PackageDoc p : packages)
	    specifiedPackages.add(p.name());
	apiDocRoot = fixApiDocRoot(root);
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
    private String qualifiedName(String r) {
	if (!opt.showQualified) {
	    // Create readable string by stripping leading path
	    int dotpos = r.lastIndexOf('.');
	    if (dotpos != -1)
		return r.substring(dotpos + 1, r.length());
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
    private String guillemize(String s) {
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
    private String guilWrap(String str) {
	return opt.guilOpen + str + opt.guilClose;
    }

    /**
     * Print the visibility adornment of element e prefixed by
     * any stereotypes
     */
    private void visibility(ProgramElementDoc e) {
	opt.w.print(stereotype(e, 'l'));
	if (!opt.showVisibility)
	    return;
	if (e.isPrivate())
	    opt.w.print('-');
	else if (e.isPublic())
	    opt.w.print('+');
	else if (e.isProtected())
	    opt.w.print('#');
	else if (e.isPackagePrivate())
	    opt.w.print('~');
	opt.w.print(' ');
    }

    /** Print the method parameter p */
    private void parameter(Parameter p[]) {
	for (int i = 0; i < p.length; i++) {
	    opt.w.print(p[i].name());
	    typeAnnotation(p[i].type());
	    if (i + 1 < p.length)
		opt.w.print(", ");
	}
    }

    /** Print a a basic type t */
    private void type(Type t) {
	if (opt.showQualified)
	    opt.w.print(t.qualifiedTypeName());
	else
	    opt.w.print(t.typeName());
	typeParameters(t.asParameterizedType());
    }

    /** Print the parameters of the parameterized type t */
    private void typeParameters(ParameterizedType t) {
	if (t == null)
	    return;
	Type args[] = t.typeArguments();
	opt.w.print("\\<");
	for (int i = 0; i < args.length; i++) {
	    type(args[i]);
	    if (i != args.length - 1)
		opt.w.print(", ");
	}
	opt.w.print("\\>");
    }

    /** Annotate an field/argument with its type t */
    private void typeAnnotation(Type t) {
	if (t.typeName().equals("void"))
	    return;
	opt.w.print(" : ");
	type(t);
	opt.w.print(t.dimension());
    }

    /** Print the class's attributes fd */
    private void attributes(FieldDoc fd[]) {
	for (FieldDoc f : fd) {
	    if (hidden(f))
		continue;
	    visibility(f);
	    opt.w.print(f.name());
	    if (opt.showType)
		typeAnnotation(f.type());
	    opt.w.print("\\l");
	    opt.w.print(tagvalue(f, "", 'r'));
	}
    }

    /*
     * The following two methods look similar, but can't
     * be refactored into one, because their common interface,
     * ExecutableMemberDoc, doesn't support returnType for ctors.
     */

    /** Print the class's constructors m */
    private void operations(ConstructorDoc m[]) {
	for (ConstructorDoc cd : m) {
	    if (hidden(cd))
		continue;
	    visibility(cd);
	    opt.w.print(cd.name());
	    if (opt.showType) {
		opt.w.print("(");
		parameter(cd.parameters());
		opt.w.print(")");
	    } else
		opt.w.print("()");
	    opt.w.print("\\l");
	    opt.w.print(tagvalue(cd, "", 'r'));
	}
    }

    /** Print the class's operations m */
    private void operations(MethodDoc m[]) {
	for (MethodDoc md : m) {
	    if (hidden(md))
		continue;
	    visibility(md);
	    opt.w.print(md.name());
	    if (opt.showType) {
		opt.w.print("(");
		parameter(md.parameters());
		opt.w.print(")");
		typeAnnotation(md.returnType());
	    } else
		opt.w.print("()");
	    opt.w.print("\\l");
	    opt.w.print(tagvalue(md, "", 'r'));
	}
    }

    /** Print the common class node's properties */
    private void nodeProperties(String s) {
	if (opt.nodeFillColor != null)
	    opt.w.print(", style=filled, fillcolor=\"" + opt.nodeFillColor + "\"");
	opt.w.print(", fontcolor=\"" + opt.nodeFontColor + "\"");
	opt.w.print(", fontsize=" + opt.nodeFontSize);
	String url = classToUrl(s);
	if (url != null)
	    opt.w.print(", URL=\"" + url + "\"");
	opt.w.println("];");
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
    private String stereotype(Doc c, char term) {
	String r = "";
	for (Tag tag : c.tags("stereotype")) {
	    String t[] = StringUtil.tokenize(tag.text());
	    if (t.length != 1) {
		System.err.println("@stereotype expects one field: " + tag.text());
		return ("");
	    }
	    r += guilWrap(t[0]) + " \\" + term;
	}
	return (r);
    }

    /** Return true if c has a @hidden tag associated with it */
    private boolean hidden(Doc c) {
	Tag tags[] = c.tags("hidden");
	if (tags.length > 0)
	    return true;
	return opt.matchesHideExpression(c.toString());
    }

    /** Return true if the class name is associated to an hidden class or matches a hide expression */
    private boolean hidden(String s) {
	ClassInfo ci = classnames.get(s);
	return (ci != null ? ci.hidden : false) || opt.matchesHideExpression(s);
    }

    /** Return a class's internal name */
    private String name(String c) {
	ClassInfo ci;

	if ((ci = classnames.get(c)) == null)
	    classnames.put(c, ci = new ClassInfo(false, false));
	return ci.name;
    }

    /** Prints the class if needed */
    public String printClass(Options opt, ClassDoc c) {
	ClassInfo ci;
	boolean toPrint;
	this.opt = opt;

	if ((ci = classnames.get(c.toString())) != null)
	    toPrint = !ci.nodePrinted;
	else {
	    toPrint = true;
	    classnames.put(c.toString(), ci = new ClassInfo(true, hidden(c)));
	}
	if (toPrint && !hidden(c) && (!c.isEnum() || opt.showEnumerations)) {
	    // Associate classname's alias
	    String r = c.toString();
	    opt.w.println("\t// " + r);
	    // Create label
	    opt.w.print("\t" + ci.name + " [");
	    r = stereotype(c, 'n') + escapeLG(qualifiedName(r));
	    if (c.isInterface())
		r = guilWrap("interface") + " \\n" + r;
	    if (c.isEnum())
		r = guilWrap("enumeration") + " \\n" + r;
	    boolean showMembers =
		(opt.showAttributes && c.fields().length > 0) ||
		(c.isEnum() && opt.showEnumConstants && c.enumConstants().length > 0) ||
		(opt.showOperations && c.methods().length > 0) ||
		(opt.showConstructors && c.constructors().length > 0);
	    r += tagvalue(c, "\\n", 'r');
	    if (showMembers)
		opt.w.print("label=\"{" + r + "\\n|");
	    else
		opt.w.print("label=\"" + r + "\"");
	    if (opt.showAttributes)
		attributes(c.fields());
	    if (c.isEnum() && opt.showEnumConstants) {
		for (FieldDoc fd : c.enumConstants()) {
		    opt.w.print(fd.name());
		    opt.w.print("\\l");
		}
	    }
	    if (showMembers)
		opt.w.print("|");
	    if (opt.showConstructors && !c.isEnum())
		operations(c.constructors());
	    if (opt.showOperations && !c.isEnum())
		operations(c.methods());
	    if (showMembers)
		opt.w.print("}\"");
	    // Use ariali [sic] for gif output of abstract classes
	    opt.w.print(", fontname=\"" +
		(c.isAbstract() ?
		 opt.nodeFontAbstractName :
		 opt.nodeFontName) + "\"");
	    nodeProperties(c.qualifiedName());
	    ci.nodePrinted = true;
	}
	return ci.name;
    }

    private String getNodeName(ClassDoc c) {
	ClassInfo ci;

	if ((ci = classnames.get(c.toString())) == null)
	    classnames.put(c.toString(), ci = new ClassInfo(false, hidden(c)));
	return ci.name;
    }

    /**
     * Print all relations for a given's class's tag
     * @param tagname the tag containing the given relation
     * @param from the source class
     * @param name the source class internal name
     * @param edgetype the dot edge specification
     */
    private void relation(String tagname, Doc from, String name, String edgetype) {
	for (Tag tag : from.tags(tagname)) {
	    String t[] = StringUtil.tokenize(tag.text());    // l-src label l-dst target
	    if (t.length != 4) {
		System.err.println("Error in " + from + "\n" + tagname + " expects four fields (l-src label l-dst target): " + tag.text());
		return;
	    }
	    if(hidden(t[3]))
		return;
	    opt.w.println("\t// " + from + " " + tagname + " " + t[3]);
	    opt.w.println("\t" + name + " -> " + name(t[3]) + " [" +
		"taillabel=\"" + t[0] + "\", " +
		"label=\"" + guillemize(t[1]) + "\", " +
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
    public void printRelations(Options iopt, ClassDoc c) {
	opt = iopt;
	if (opt.matchesHideExpression(c.toString()) || hidden(c))
	    return;
	String cs = getNodeName(c);

	// Print generalization (through the Java superclass)
	Type s = c.superclassType();
	if (s != null &&
	    !s.toString().equals("java.lang.Object") &&
	    !c.isEnum() &&
	    !hidden(s.asClassDoc()))
		opt.w.println("\t//" + c + " extends " + s + "\n" +
		    "\t" + getNodeName(s.asClassDoc()) + " -> " + cs + " [dir=back,arrowtail=empty];");

	// Print generalizations (through @extends tags)
	for (Tag tag : c.tags("extends"))
	    if (!hidden(tag.text()))
		opt.w.println("\t//" + c + " extends " + tag.text() + "\n" +
		    "\t" + name(tag.text()) + " -> " + cs + " [dir=back,arrowtail=empty];");
	// Print realizations (Java interfaces)
	for (Type iface : c.interfaceTypes())
	    if (!hidden(iface.asClassDoc()))
		opt.w.println("\t" + getNodeName(iface.asClassDoc()) + " -> " + cs + " [dir=back,arrowtail=empty,style=dashed];" +
		    "\t//" + c + " implements " + iface.asClassDoc());
	// Print other associations
	relation("assoc", c, cs, "arrowhead=none");
	relation("navassoc", c, cs, "arrowhead=open");
	relation("has", c, cs, "arrowhead=none, arrowtail=ediamond");
	relation("composed", c, cs, "arrowhead=none, arrowtail=diamond");
	relation("depend", c, cs, "arrowhead=open, style=dashed");
    }

    /** Print classes that were parts of relationships, but not parsed by javadoc */
    public void printExtraClasses(Options opt, RootDoc root) {
	Collection<Map.Entry<String, ClassInfo>> myClassInfos = classnames.entrySet();
	Iterator<Map.Entry<String, ClassInfo>> iter = myClassInfos.iterator();
	while (iter.hasNext()) {
	    Map.Entry<String, ClassInfo> entry = iter.next();
	    ClassInfo info = entry.getValue();
	    if (!info.nodePrinted) {
		String r = entry.getKey();
		ClassDoc c = root.classNamed(removeTemplate(r));
		if(c != null) {
		    Options localOpt = (Options) opt.clone();
		    localOpt.setOptions(c);
		    printClass(localOpt, c);
		} else {
		    opt.w.println("\t// " + r);
		    opt.w.print("\t" + info.name + "[label=\"" + escapeLG(qualifiedName(r)) + "\"");
		    opt.w.print(", fontname=\"" + opt.nodeFontName + "\"");
		    nodeProperties(entry.getKey().toString());
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
}
