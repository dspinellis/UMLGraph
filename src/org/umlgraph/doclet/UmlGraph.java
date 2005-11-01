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

import com.sun.javadoc.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represent the program options
 */
class Options implements Cloneable {
	private Vector<Pattern> hidePatterns;
	PrintWriter w;
	boolean showQualified;
	boolean showAttributes;
	boolean showEnumerations;
	boolean showEnumConstants;
	boolean showOperations;
	boolean showConstructors;
	boolean showVisibility;
	boolean horizontal;
	boolean showType;
	String edgeFontName;
	String edgeFontColor;
	String edgeColor;
	double edgeFontSize;
	String nodeFontName;
	String nodeFontAbstractName;
	String nodeFontColor;
	double nodeFontSize;
	String nodeFillColor;
	String bgColor;
	String outputFileName;
	String outputEncoding;
	String apiDocMapFileName;
	String apiDocRoot;
	boolean useGuillemot;
	/** Guillemot left (open) */
	String guilOpen = "\u00ab";
	/** Guillemot right (close) */
	String guilClose = "\u00bb";

	Options() {
		showQualified = false;
		showAttributes = false;
		showEnumConstants = false;
		showOperations = false;
		showVisibility = false;
		showEnumerations = false;
		showConstructors = false;
		showType = false;
		edgeFontName = "Helvetica";
		edgeFontColor = "black";
		edgeColor = "black";
		edgeFontSize = 10;
		nodeFontName = "Helvetica";
		nodeFontAbstractName = "Helvetica-Oblique";
		nodeFontColor = "black";
		nodeFontSize = 10;
		nodeFillColor = null;
		bgColor = null;
		outputFileName = "graph.dot";
		outputEncoding = "ISO-8859-1";
		hidePatterns = new Vector<Pattern>();
		apiDocMapFileName = null;
		apiDocRoot = null;
		useGuillemot = true;
	}

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			// Should not happen
		}
		return o;
	}

	/** Most verbose output */
	public void setAll() {
		showAttributes = true;
		showEnumerations = true;
		showEnumConstants = true;
		showOperations = true;
		showConstructors = true;
		showVisibility = true;
		showType = true;
	}

	/** Set the options based on a lingle option and its arguments */
	private void setOption(String[] opt) {
		if(opt[0].equals("-qualify")) {
			showQualified = true;
		} else if(opt[0].equals("-horizontal")) {
			horizontal = true;
		} else if(opt[0].equals("-attributes")) {
			showAttributes = true;
		} else if(opt[0].equals("-enumconstants")) {
			showEnumConstants = true;
		} else if(opt[0].equals("-operations")) {
			showOperations = true;
		} else if(opt[0].equals("-enumerations")) {
			showEnumerations = true;
		} else if(opt[0].equals("-constructors")) {
			showConstructors = true;
		} else if(opt[0].equals("-visibility")) {
			showVisibility = true;
		} else if(opt[0].equals("-types")) {
			showType = true;
		} else if(opt[0].equals("-all")) {
			setAll();
		} else if(opt[0].equals("-bgcolor")) {
			bgColor = opt[1];
		} else if(opt[0].equals("-edgecolor")) {
			edgeColor = opt[1];
		} else if(opt[0].equals("-edgefontcolor")) {
			edgeFontColor = opt[1];
		} else if(opt[0].equals("-edgefontname")) {
			edgeFontName = opt[1];
		} else if(opt[0].equals("-edgefontsize")) {
			edgeFontSize = Integer.parseInt(opt[1]);
		} else if(opt[0].equals("-nodefontcolor")) {
			nodeFontColor = opt[1];
		} else if(opt[0].equals("-nodefontname")) {
			nodeFontName = opt[1];
		} else if(opt[0].equals("-nodefontabstractname")) {
			nodeFontAbstractName = opt[1];
		} else if(opt[0].equals("-nodefontsize")) {
			nodeFontSize = Integer.parseInt(opt[1]);
		} else if(opt[0].equals("-nodefillcolor")) {
			nodeFillColor = opt[1];
		} else if(opt[0].equals("-output")) {
			outputFileName = opt[1];
		} else if(opt[0].equals("-outputencoding")) {
			outputEncoding = opt[1];
		} else if(opt[0].equals("-hide")) {
			try {
				hidePatterns.add(Pattern.compile(opt[1]));
			} catch (PatternSyntaxException e) {
				System.err.println("Skipping invalid pattern " + opt[1]);
			}
		} else if(opt[0].equals("-apidocroot")) {
			apiDocRoot = opt[1];
		} else if(opt[0].equals("-apidocmap")) {
			apiDocMapFileName = opt[1];
		} else if(opt[0].equals("-noguillemot")) {
			guilOpen = "\\<\\<";
			guilClose = "\\>\\>";
		} else
			; // Do nothing, javadoc will handle the option or complain, if needed.
	}

	/** Set the options based on the command line parameters */
	public void setOptions(String[][] options) {
		for (int i = 0; i < options.length; i++)
			setOption(options[i]);
	}


	/** Set the options based on the tag elements of the ClassDoc parameter */
	public void setOptions(ClassDoc p) {
		if (p == null)
			return;

		Tag tags[] = p.tags("opt");
		for (int i = 0; i < tags.length; i++) {
			String[] opt = StringUtil.tokenize(tags[i].text());
			opt[0] = "-" + opt[0];
			setOption(opt);
		}
	}

	public void openFile() throws IOException {
		FileOutputStream fos = new FileOutputStream(outputFileName);
		w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos, outputEncoding)));
	}

	/**
	 * Check if the supplied string matches an entity specified
	 * with the -hide parameter.
	 * @return true if the string matches.
	 */
	public boolean matchesHideExpression(String s) {
		for (int i = 0; i < hidePatterns.size(); i++) {
			Pattern hidePattern = hidePatterns.get(i);
			Matcher m = hidePattern.matcher(s);
			if (m.find())
				return true;
		}
		return false;
	}
}

/**
 * Class's dot-comaptible alias name (for fully qualified class names)
 * and printed information
 */
class ClassInfo {
	private static int classnum;
	/** Alias name for the class */
	String name;
	/** True if the class class node has been printed */
	boolean nodePrinted;

	ClassInfo(boolean p) {
		nodePrinted = p;
		name = "c" + (new Integer(classnum)).toString();
		classnum++;
	}
}

/** String utility functions */
class StringUtil {
	/** Tokenize string s into an array */
	public static String[] tokenize(String s) {
		ArrayList<String> r = new ArrayList<String>();
		String remain = s;
		int n = 0, pos;

		remain = remain.trim();
		while (remain.length() > 0) {
			if (remain.startsWith("\"")) {
				// Field in quotes
				pos = remain.indexOf('"', 1);
				if (pos == -1)
					break;
				r.add(remain.substring(1, pos));
				if (pos + 1 < remain.length())
					pos++;
			} else {
				// Space-separated field
				pos = remain.indexOf(' ', 0);
				if (pos == -1) {
					r.add(remain);
					remain = "";
				} else
					r.add(remain.substring(0, pos));
			}
			remain = remain.substring(pos + 1);
			remain = remain.trim();
			// - is used as a placeholder for empy fields
			if (r.get(n).equals("-"))
				r.set(n, "");
			n++;
		}
		return r.toArray(new String[0]);
	}

}

/**
 * Class graph generation engine
 */
class ClassGraph {
	private static Map<String, ClassInfo> classnames = new HashMap<String, ClassInfo>();
	private static String apiDocRoot;
	private static Map<Pattern, String> apiDocMap = new HashMap<Pattern, String>();

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
		for (int i = 0; i < packages.length; i++)
			specifiedPackages.add(packages[i].name());
		apiDocRoot = fixApiDocRoot(root);
		if (mapFileName != null) {
			InputStream is = new FileInputStream(mapFileName);
			Properties userMap = new Properties();
			userMap.load(is);
			for (Iterator iter = userMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry mapEntry = (Map.Entry)iter.next();

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

	/** Print the class's attributes f */
	private void attributes(FieldDoc f[]) {
		for (int i = 0; i < f.length; i++) {
			if (hidden(f[i]))
				continue;
			visibility(f[i]);
			opt.w.print(f[i].name());
			if (opt.showType)
				typeAnnotation(f[i].type());
			opt.w.print("\\l");
			opt.w.print(tagvalue(f[i], "", 'r'));
		}
	}

	/** Print the class's constructors m */
	private void operations(ConstructorDoc m[]) {
		for (int i = 0; i < m.length; i++) {
			if (hidden(m[i]))
				continue;
			visibility(m[i]);
			opt.w.print(m[i].name());
			if (opt.showType) {
				opt.w.print("(");
				parameter(m[i].parameters());
				opt.w.print(")");
			} else
				opt.w.print("()");
			opt.w.print("\\l");
			opt.w.print(tagvalue(m[i], "", 'r'));
		}
	}

	/** Print the class's operations m */
	private void operations(MethodDoc m[]) {
		for (int i = 0; i < m.length; i++) {
			if (hidden(m[i]))
				continue;
			visibility(m[i]);
			opt.w.print(m[i].name());
			if (opt.showType) {
				opt.w.print("(");
				parameter(m[i].parameters());
				opt.w.print(")");
				typeAnnotation(m[i].returnType());
			} else
				opt.w.print("()");
			opt.w.print("\\l");
			opt.w.print(tagvalue(m[i], "", 'r'));
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
		for (int i = 0; i < tags.length; i++) {
			String t[] = StringUtil.tokenize(tags[i].text());
			if (t.length != 2) {
				System.err.println("@tagvalue expects two fields: " + tags[i].text());
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
		Tag tags[] = c.tags("stereotype");
		for (int i = 0; i < tags.length; i++) {
			String t[] = StringUtil.tokenize(tags[i].text());
			if (t.length != 1) {
				System.err.println("@stereotype expects one field: " + tags[i].text());
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

	/** Return a class's internal name */
	private static String name(String c) {
		ClassInfo ci;

		if ((ci = classnames.get(c)) == null)
			classnames.put(c, ci = new ClassInfo(false));
		return ci.name;
	}

	/** Return a class's internal name, printing the class if needed */
	private String name(ClassDoc c) {
		ClassInfo ci;
		boolean toPrint;

		if ((ci = classnames.get(c.toString())) != null)
			toPrint = !ci.nodePrinted;
		else {
			toPrint = true;
			classnames.put(c.toString(), ci = new ClassInfo(true));
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
				FieldDoc ec[] = c.enumConstants();
				for (int i = 0; i < ec.length; i++) {
					opt.w.print(ec[i].name());
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


	/**
	 * Print all relations for a given's class's tag
	 * @param tagname the tag containing the given relation
	 * @param from the source class
	 * @param name the source class internal name
	 * @param edgetype the dot edge specification
	 */
	private void relation(String tagname, Doc from, String name, String edgetype) {
		Tag tags[] = from.tags(tagname);
		for (int i = 0; i < tags.length; i++) {
			String t[] = StringUtil.tokenize(tags[i].text());	// l-src label l-dst target
			if (t.length != 4) {
				System.err.println("Error in " + from + "\n" + tagname + " expectes four fields (l-src label l-dst target): " + tags[i].text());
				return;
			}
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

	/** Print a class */
	public void print(Options iopt, ClassDoc c) {
		opt = (Options)iopt.clone();
		// Process class-local options (through @opt tags)
		opt.setOptions(c);
		String cs = name(c);
		// Print generalization (through the Java superclass)
		Type s = c.superclassType();
		if (s != null && !s.toString().equals("java.lang.Object") && !c.isEnum()) {
			if (!opt.matchesHideExpression(c.toString())
				&& !opt.matchesHideExpression(s.toString())
				&& !hidden(c) && !hidden(s.asClassDoc())) {
				opt.w.println("\t//" + c + " extends " + s);
				opt.w.println("\t" + name(s.asClassDoc()) + " -> " + cs + " [dir=back,arrowtail=empty];");
			}
		}
		// Print generalizations (through @extends tags)
		Tag tags[] = c.tags("extends");
		for (int i = 0; i < tags.length; i++) {
			if (!opt.matchesHideExpression(c.toString())
				&& !opt.matchesHideExpression(tags[i].text())) {
				opt.w.println("\t//" + c + " extends " + tags[i].text());
				opt.w.println("\t" + name(tags[i].text()) + " -> " + cs + " [dir=back,arrowtail=empty];");
			}
		}
		// Print realizations (Java interfaces)
		Type ifs[] = c.interfaceTypes();
		for (int i = 0; i < ifs.length; i++) {
			if (!opt.matchesHideExpression(c.toString())
				&& !opt.matchesHideExpression(ifs[i].toString())
				&& !hidden(c) && !hidden(ifs[i].asClassDoc())) {
				opt.w.print("\t" + name(ifs[i].asClassDoc()) + " -> " + cs + " [dir=back,arrowtail=empty,style=dashed];");
				opt.w.println("\t//" + c + " implements " + ifs[i].asClassDoc());
			}
		}
		// Print other associations
		relation("assoc", c, cs, "arrowhead=none");
		relation("navassoc", c, cs, "arrowhead=open");
		relation("has", c, cs, "arrowhead=none, arrowtail=ediamond");
		relation("composed", c, cs, "arrowhead=none, arrowtail=diamond");
		relation("depend", c, cs, "arrowhead=open, style=dashed");
	}

	/** Print classes that were parts of relationships, but not parsed by javadoc */
	public void printExtraClasses() {
		Collection<Map.Entry<String, ClassInfo>> myClassInfos = classnames.entrySet();
		Iterator<Map.Entry<String, ClassInfo>> iter = myClassInfos.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, ClassInfo> entry = iter.next();
			ClassInfo info = entry.getValue();
			if (!info.nodePrinted) {
				String r = entry.getKey().toString();
				opt.w.println("\t// " + r);

				opt.w.print("\t" + info.name + "[label=\"" + escapeLG(qualifiedName(r)) + "\"");
				opt.w.print(", fontname=\"" + opt.nodeFontName + "\"");
				nodeProperties(entry.getKey().toString());
			}
		}
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
		for (Iterator<Map.Entry<Pattern, String>> iter = apiDocMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<Pattern, String> mapEntry = iter.next();
			Pattern regex = mapEntry.getKey();
			Matcher matcher = regex.matcher(className);
			if (matcher.matches())
				return mapEntry.getValue();
		}
		return null;
	}
}

/** Doclet API implementation */
public class UmlGraph {
	private static Options opt = new Options();

	/** Entry point */
	public static boolean start(RootDoc root) throws IOException {
		opt.setOptions(root.options());
		opt.openFile();
		opt.setOptions(root.classNamed("UMLOptions"));
		prologue();
		ClassDoc[] classes = root.classes();

		ClassGraph c = new ClassGraph(root.specifiedPackages(),
					opt.apiDocRoot, opt.apiDocMapFileName);
		for (int i = 0; i < classes.length; i++)
			c.print(opt, classes[i]);
		c.printExtraClasses();
		epilogue();
		return true;
	}

	/** Option checking */
	public static int optionLength(String option) {
		if(option.equals("-qualify") ||
		   option.equals("-horizontal") ||
		   option.equals("-attributes") ||
		   option.equals("-operations") ||
		   option.equals("-constructors") ||
		   option.equals("-visibility") ||
		   option.equals("-types") ||
		   option.equals("-all") ||
		   option.equals("-noguillemot"))
			return 1;
		else if(option.equals("-nodefillcolor") ||
		   option.equals("-nodefontcolor") ||
		   option.equals("-nodefontsize") ||
		   option.equals("-nodefontname") ||
		   option.equals("-nodefontabstractname") ||
		   option.equals("-edgefontcolor") ||
		   option.equals("-edgecolor") ||
		   option.equals("-edgefontsize") ||
		   option.equals("-edgefontname") ||
		   option.equals("-output") ||
		   option.equals("-outputencoding") ||
		   option.equals("-bgcolor") ||
		   option.equals("-hide") ||
		   option.equals("-apidocroot") ||
		   option.equals("-apidocmap"))
			return 2;
		else
			return 0;
	}

	/** Indicate the language version we support */
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	/** Dot prologue */
	private static void prologue() {
		opt.w.println(
			"#!/usr/local/bin/dot\n" +
			"#\n" +
			"# Class diagram \n" +
			"# Generated by $Id$\n" +
			"#\n\n" +
			"digraph G {\n" +
			"\tedge [fontname=\"" + opt.edgeFontName +
			"\",fontsize=10,labelfontname=\"" + opt.edgeFontName +
			"\",labelfontsize=10];\n" +
			"\tnode [fontname=\"" + opt.nodeFontName +
			"\",fontsize=10,shape=record];"
		);
		if (opt.horizontal)
			opt.w.println("\trankdir=LR;\n\tranksep=1;");
		if (opt.bgColor != null)
			opt.w.println("\tbgcolor=\"" + opt.bgColor + "\";\n");
	}

	/** Dot epilogue */
	private static void epilogue() {
		opt.w.println("}\n");
		opt.w.flush();
	}
}
