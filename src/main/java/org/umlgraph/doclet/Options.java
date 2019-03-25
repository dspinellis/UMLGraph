/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2010 Diomidis Spinellis
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;

/**
 * Represent the program options
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
public class Options implements Cloneable, OptionProvider {
    // reused often, especially in UmlGraphDoc, worth creating just once and reusing
    private static final Pattern allPattern = Pattern.compile(".*");
    protected static final String DEFAULT_EXTERNAL_APIDOC = "http://docs.oracle.com/javase/7/docs/api/";
    
    // instance fields
    List<Pattern> hidePatterns = new ArrayList<Pattern>();
    List<Pattern> includePatterns = new ArrayList<Pattern>();
    boolean showQualified = false;
    boolean showQualifiedGenerics = false;
    boolean hideGenerics = false;
    boolean showAttributes = false;
    boolean showEnumerations = false;
    boolean showEnumConstants = false;
    boolean showOperations = false;
    boolean showConstructors = false;
    boolean showVisibility = false;
    boolean horizontal;
    boolean showType = false;
    boolean showComment = false;
    boolean autoSize = true;
    String edgeFontName = Font.DEFAULT_FONT;
    String edgeFontColor = "black";
    String edgeColor = "black";
    double edgeFontSize = 10;
    String nodeFontName = Font.DEFAULT_FONT;
    boolean nodeFontAbstractItalic = true;
    String nodeFontColor = "black";
    double nodeFontSize = 10;
    String nodeFillColor = null;
    double nodeFontClassSize = -1;
    String nodeFontClassName = null;
    double nodeFontTagSize = -1;
    String nodeFontTagName = null;
    double nodeFontPackageSize = -1;
    String nodeFontPackageName = null;
    Shape shape = Shape.CLASS;
    String bgColor = null;
    public String outputFileName = "graph.dot";
    String outputEncoding = "ISO-8859-1"; // TODO: default to UTF-8 now?
    Map<Pattern, String> apiDocMap = new HashMap<Pattern, String>();
    String apiDocRoot = null;
    boolean postfixPackage = false;
    boolean useGuillemot = true;
    boolean findViews = false;
    String viewName = null;
    double nodeSep = 0.25;
    double rankSep = 0.5;
    public String outputDirectory = null;
    /*
     * Numeric values are preferable to symbolic here.
     * Symbolic reportedly fail on MacOSX, and also are
     * more difficult to verify with XML tools.
     */
    /** Guillemot left (open) */
    String guilOpen = "&#171;";		// &laquo; \u00ab
    /** Guillemot right (close) */
    String guilClose = "&#187;";	// &raquo; \u00bb
    boolean inferRelationships = false;
    boolean inferDependencies = false;
    boolean collapsibleDiagrams = false;
    RelationPattern contextRelationPattern = new RelationPattern(RelationDirection.BOTH);
    boolean useImports = false;
    Visibility inferDependencyVisibility = Visibility.PRIVATE;
    boolean inferDepInPackage = false;
    RelationType inferRelationshipType = RelationType.NAVASSOC;
    private List<Pattern> collPackages = new ArrayList<Pattern>();
    boolean compact = false;
    boolean hidePrivateInner = false;
    // internal option, used by UMLDoc to generate relative links between classes
    boolean relativeLinksForSourcePackages = false;
    // internal option, used by UMLDoc to force strict matching on the class names
    // and avoid problems with packages in the template declaration making UmlGraph hide 
    // classes outside of them (for example, class gr.spinellis.Foo<T extends java.io.Serializable>
    // would have been hidden by the hide pattern "java.*"
    // TODO: consider making this standard behaviour
    boolean strictMatching = false;
    String dotExecutable = "dot";

    Options() {
    }

    @Override
	public Object clone() {
	Options clone = null;
	try {
	     clone = (Options) super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new RuntimeException("Cannot clone?!?", e); // Should not happen
	}
	// deep clone the hide and collection patterns
	clone.hidePatterns = new ArrayList<Pattern>(hidePatterns);
	clone.includePatterns = new ArrayList<Pattern>(includePatterns);
	clone.collPackages= new ArrayList<Pattern>(collPackages);
	clone.apiDocMap = new HashMap<Pattern, String>(apiDocMap);
	return clone;
    }

    /** Most complete output */
    public void setAll() {
	showAttributes = true;
	showEnumerations = true;
	showEnumConstants = true;
	showOperations = true;
	showConstructors = true;
	showVisibility = true;
	showType = true;
    }
    
    /**
     * Match strings, ignoring leading <tt>-</tt>, <tt>-!</tt>, and <tt>!</tt>.
     *
     * @param given Given string
     * @param expect Expected string
     * @return {@code true} on success
     */
    protected static boolean matchOption(String given, String expect) {
	return matchOption(given, expect, false);
    }

    /**
     * Match strings, ignoring leading <tt>-</tt>, <tt>-!</tt>, and <tt>!</tt>.
     *
     * @param given Given string
     * @param expect Expected string
     * @param negative May be negative
     * @return {@code true} on success
     */
    protected static boolean matchOption(String given, String expect, boolean negative) {
	int begin = 0, end = given.length();
	if (begin < end && given.charAt(begin) == '-')
	    ++begin;
	if (negative && begin < end && given.charAt(begin) == '!')
	    ++begin;
	return expect.length() == end - begin && expect.regionMatches(0, given, begin, end - begin);
    }

    /**
     * Return the number of arguments associated with the specified option.
     * The return value includes the actual option.
     * Will return 0 if the option is not supported.
     */
    public static int optionLength(String option) {
        if(matchOption(option, "qualify", true) ||
           matchOption(option, "qualifyGenerics", true) ||
           matchOption(option, "hideGenerics", true) ||
           matchOption(option, "horizontal", true) ||
           matchOption(option, "all") ||
           matchOption(option, "attributes", true) ||
           matchOption(option, "enumconstants", true) ||
           matchOption(option, "operations", true) ||
           matchOption(option, "enumerations", true) ||
           matchOption(option, "constructors", true) ||
           matchOption(option, "visibility", true) ||
           matchOption(option, "types", true) ||
           matchOption(option, "autosize", true) ||
           matchOption(option, "commentname", true) ||
           matchOption(option, "nodefontabstractitalic", true) ||
           matchOption(option, "postfixpackage", true) ||
           matchOption(option, "noguillemot", true) ||
           matchOption(option, "views", true) ||
           matchOption(option, "inferrel", true) ||
           matchOption(option, "useimports", true) ||
           matchOption(option, "collapsible", true) ||
           matchOption(option, "inferdep", true) ||
           matchOption(option, "inferdepinpackage", true) ||
           matchOption(option, "hideprivateinner", true) ||
           matchOption(option, "compact", true))

            return 1;
        else if(matchOption(option, "nodefillcolor") ||
           matchOption(option, "nodefontcolor") ||
           matchOption(option, "nodefontsize") ||
           matchOption(option, "nodefontname") ||
           matchOption(option, "nodefontclasssize") ||
           matchOption(option, "nodefontclassname") ||
           matchOption(option, "nodefonttagsize") ||
           matchOption(option, "nodefonttagname") ||
           matchOption(option, "nodefontpackagesize") ||
           matchOption(option, "nodefontpackagename") ||
           matchOption(option, "edgefontcolor") ||
           matchOption(option, "edgecolor") ||
           matchOption(option, "edgefontsize") ||
           matchOption(option, "edgefontname") ||
           matchOption(option, "shape") ||
           matchOption(option, "output") ||
           matchOption(option, "outputencoding") ||
           matchOption(option, "bgcolor") ||
           matchOption(option, "hide") ||
           matchOption(option, "include") ||
           matchOption(option, "apidocroot") ||
           matchOption(option, "apidocmap") ||
           matchOption(option, "d") ||
           matchOption(option, "view") ||
           matchOption(option, "inferreltype") ||
           matchOption(option, "inferdepvis") ||
           matchOption(option, "collpackages") ||
           matchOption(option, "nodesep") ||
           matchOption(option, "ranksep") ||
           matchOption(option, "dotexecutable") ||
           matchOption(option, "link"))
           return 2;
        else if(matchOption(option, "contextPattern") ||
           matchOption(option, "linkoffline"))
            return 3;
        else
            return 0;
    }
    
    /** Set the options based on a single option and its arguments */
    void setOption(String[] opt) {
	if(!matchOption(opt[0], "hide") && optionLength(opt[0]) > opt.length) {
	    System.err.println("Skipping option '" + opt[0] + "', missing argument");
	    return;
	}
	boolean dash = opt[0].length() > 1 && opt[0].charAt(0) == '-';
	boolean positive = !(opt[0].length() > 1 && opt[0].charAt(dash ? 1 : 0) == '!');
	
	if(matchOption(opt[0], "qualify", true)) {
	    showQualified = positive;
	} else if(matchOption(opt[0], "qualifyGenerics", true)) {
	    showQualifiedGenerics = positive;
	} else if(matchOption(opt[0], "hideGenerics", true)) {
	    hideGenerics = positive;
	} else if(matchOption(opt[0], "horizontal", true)) {
	    horizontal = positive;
	} else if(matchOption(opt[0], "attributes", true)) {
	    showAttributes = positive;
	} else if(matchOption(opt[0], "enumconstants", true)) {
	    showEnumConstants = positive;
	} else if(matchOption(opt[0], "operations", true)) {
	    showOperations = positive;
	} else if(matchOption(opt[0], "enumerations", true)) {
	    showEnumerations = positive;
	} else if(matchOption(opt[0], "constructors", true)) {
	    showConstructors = positive;
	} else if(matchOption(opt[0], "visibility", true)) {
	    showVisibility = positive;
	} else if(matchOption(opt[0], "types", true)) {
	    showType = positive;
	} else if(matchOption(opt[0], "autoSize", true)) {
	    autoSize = positive;
	} else if(matchOption(opt[0], "commentname", true)) {
	    showComment = positive;
	} else if(matchOption(opt[0], "all")) {
	    setAll();
	} else if(matchOption(opt[0], "bgcolor", true)) {
	    bgColor = positive ? opt[1] : null;
	} else if(matchOption(opt[0], "edgecolor", true)) {
	    edgeColor = positive ? opt[1] : "black";
	} else if(matchOption(opt[0], "edgefontcolor", true)) {
	    edgeFontColor = positive ? opt[1] : "black";
	} else if(matchOption(opt[0], "edgefontname", true)) {
	    edgeFontName = positive ? opt[1] : Font.DEFAULT_FONT;
	} else if(matchOption(opt[0], "edgefontsize", true)) {
	    edgeFontSize = positive ? Double.parseDouble(opt[1]) : 10;
	} else if(matchOption(opt[0], "nodefontcolor", true)) {
	    nodeFontColor = positive ? opt[1] : "black";
	} else if(matchOption(opt[0], "nodefontname", true)) {
	    nodeFontName = positive ? opt[1] : Font.DEFAULT_FONT;
	} else if(matchOption(opt[0], "nodefontabstractitalic", true)) {
	    nodeFontAbstractItalic = positive;
	} else if(matchOption(opt[0], "nodefontsize", true)) {
	    nodeFontSize = positive ? Double.parseDouble(opt[1]) : 10;
	} else if(matchOption(opt[0], "nodefontclassname", true)) {
	    nodeFontClassName = positive ? opt[1] : null;
	} else if(matchOption(opt[0], "nodefontclasssize", true)) {
	    nodeFontClassSize = positive ? Double.parseDouble(opt[1]) : -1;
	} else if(matchOption(opt[0], "nodefonttagname", true)) {
	    nodeFontTagName = positive ? opt[1] : null;
	} else if(matchOption(opt[0], "nodefonttagsize", true)) {
	    nodeFontTagSize = positive ? Double.parseDouble(opt[1]) : -1;
	} else if(matchOption(opt[0], "nodefontpackagename", true)) {
	    nodeFontPackageName = positive ? opt[1] : null;
	} else if(matchOption(opt[0], "nodefontpackagesize", true)) {
	    nodeFontPackageSize = positive ? Double.parseDouble(opt[1]) : -1;
	} else if(matchOption(opt[0], "nodefillcolor", true)) {
	    nodeFillColor = positive ? opt[1] : null;
	} else if(matchOption(opt[0], "shape", true)) {
	    shape = positive ? Shape.of(opt[1]) : Shape.CLASS;
	} else if(matchOption(opt[0], "output", true)) {
	    outputFileName = positive ? opt[1] : "graph.dot";
	} else if(matchOption(opt[0], "outputencoding", true)) {
	    outputEncoding = positive ? opt[1] : "ISO-8859-1";
	} else if(matchOption(opt[0], "hide", true)) {
	    if (positive) {
		if (opt.length == 1) {
		    hidePatterns.clear();
		    hidePatterns.add(allPattern);
		} else {
		    try {
			hidePatterns.add(Pattern.compile(opt[1]));
		    } catch (PatternSyntaxException e) {
			System.err.println("Skipping invalid pattern " + opt[1]);
		    }
		}
	    } else
		hidePatterns.clear();
	} else if(matchOption(opt[0], "include", true)) {
	    if (positive) {
		try {
		    includePatterns.add(Pattern.compile(opt[1]));
		} catch (PatternSyntaxException e) {
		    System.err.println("Skipping invalid pattern " + opt[1]);
		}
	    } else
		includePatterns.clear();
	} else if(matchOption(opt[0], "apidocroot", true)) {
	    apiDocRoot = positive ? fixApiDocRoot(opt[1]) : null;
	} else if(matchOption(opt[0], "apidocmap", true)) {
	    if (positive)
		setApiDocMapFile(opt[1]);
	    else
		apiDocMap.clear();
	} else if(matchOption(opt[0], "noguillemot", true)) {
	    guilOpen = positive ? "&lt;&lt;" : "\u00ab";
	    guilClose = positive ? "&gt;&gt;" : "\u00bb";
	} else if (matchOption(opt[0], "view", true)) {
	    viewName = positive ? opt[1] : null;
	} else if (matchOption(opt[0], "views", true)) {
	    findViews = positive;
	} else if (matchOption(opt[0], "d", true)) {
	    outputDirectory = positive ? opt[1] : null;
	} else if(matchOption(opt[0], "inferrel", true)) {
	    inferRelationships = positive;
	} else if(matchOption(opt[0], "inferreltype", true)) {
	    if (positive) {
		try {
		    inferRelationshipType = RelationType.valueOf(opt[1].toUpperCase());
		} catch(IllegalArgumentException e) {
		    System.err.println("Unknown association type " + opt[1]);
		}
	    } else 
		inferRelationshipType = RelationType.NAVASSOC;
	} else if(matchOption(opt[0], "inferdepvis", true)) {
	    if (positive) {
		try {
		    Visibility vis = Visibility.valueOf(opt[1].toUpperCase());
		    inferDependencyVisibility = vis;
		} catch(IllegalArgumentException e) {
		    System.err.println("Ignoring invalid visibility specification for " +
			    "dependency inference: " + opt[1]);
		}
	    } else
		inferDependencyVisibility = Visibility.PRIVATE;
	} else if(matchOption(opt[0], "collapsible", true)) {
	    collapsibleDiagrams = positive;
	} else if(matchOption(opt[0], "inferdep", true)) {
	    inferDependencies = positive;
	} else if(matchOption(opt[0], "inferdepinpackage", true)) {
	    inferDepInPackage = positive;
	} else if (matchOption(opt[0], "hideprivateinner", true)) {
	    hidePrivateInner = positive;
	} else if(matchOption(opt[0], "useimports", true)) {
	    useImports = positive;
	} else if (matchOption(opt[0], "collpackages", true)) {
	    if (positive) {
		try {
		    collPackages.add(Pattern.compile(opt[1]));
		} catch (PatternSyntaxException e) {
		    System.err.println("Skipping invalid pattern " + opt[1]);
		}
	    } else
		collPackages.clear();
	} else if (matchOption(opt[0], "compact", true)) {
	    compact = positive;
	} else if (matchOption(opt[0], "postfixpackage", true)) {
	    postfixPackage = positive;
	} else if (matchOption(opt[0], "link")) {
	    addApiDocRoots(opt[1]);
	} else if (matchOption(opt[0], "linkoffline")) {
	    addApiDocRootsOffline(opt[1], opt[2]);
	} else if(matchOption(opt[0], "contextPattern")) {
	    RelationDirection d; RelationType rt;
	    try {
		d = RelationDirection.valueOf(opt[2].toUpperCase());
		if(opt[1].equalsIgnoreCase("all")) {
		    contextRelationPattern = new RelationPattern(d);
		} else {
		    rt = RelationType.valueOf(opt[1].toUpperCase());
		    contextRelationPattern.addRelation(rt, d);
		}
	    } catch(IllegalArgumentException e) {
		
	    }
		
	} else if (matchOption(opt[0], "nodesep", true)) {
	    try {
		nodeSep = positive ? Double.parseDouble(opt[1]) : 0.25;
	    } catch (NumberFormatException e) {
		System.err.println("Skipping invalid nodesep " + opt[1]);
	    }
	} else if (matchOption(opt[0], "ranksep", true)) {
	    try {
		rankSep = positive ? Double.parseDouble(opt[1]) : 0.5;
	    } catch (NumberFormatException e) {
		System.err.println("Skipping invalid ranksep " + opt[1]);
	    }
	} else if (matchOption(opt[0], "dotexecutable")) {
	    dotExecutable = opt[1];
	} else
	    ; // Do nothing, javadoc will handle the option or complain, if needed.
    }

    /**
     * Adds api doc roots from a link. The folder reffered by the link should contain a package-list
     * file that will be parsed in order to add api doc roots to this configuration 
     * @param packageListUrl
     */
    private void addApiDocRoots(String packageListUrl) {
	BufferedReader br = null;
	packageListUrl = fixApiDocRoot(packageListUrl);
	try {
	    URL url = new URL(packageListUrl + "/package-list");
	    br = new BufferedReader(new InputStreamReader(url.openStream()));
	    String line;
	    while((line = br.readLine()) != null) {
		line = line + ".";
		Pattern pattern = Pattern.compile(line.replace(".", "\\.") + "[^\\.]*");
		apiDocMap.put(pattern, packageListUrl);
	    }
	} catch(IOException e) {
	    System.err.println("Errors happened while accessing the package-list file at "
		    + packageListUrl);
	} finally {
	    if(br != null)
		try {
		    br.close();
		} catch (IOException e) {}
	}
	
    }

    /**
     * Adds api doc roots from an offline link.
     * The folder specified by packageListUrl should contain the package-list associed with the docUrl folder.
     * @param docUrl folder containing the javadoc
     * @param packageListUrl folder containing the package-list
     */
    private void addApiDocRootsOffline(String docUrl, String packageListUrl) {
	BufferedReader br = null;
	packageListUrl = fixApiDocRoot(packageListUrl);
	try {
	    URL url = new URL(packageListUrl + "/package-list");
	    br = new BufferedReader(new InputStreamReader(url.openStream()));
	    String line;
	    while((line = br.readLine()) != null) {
		line = line + ".";
		Pattern pattern = Pattern.compile(line.replace(".", "\\.") + "[^\\.]*");
		apiDocMap.put(pattern, fixApiDocRoot(docUrl));
	    }
	} catch(IOException e) {
	    System.err.println("Unable to access the package-list file at " + packageListUrl);
	} finally {
	    if (br != null)
	    try {
		    br.close();
	    } catch (IOException e) {}
	}
    }

    /**
     * Loads the property file referred by <code>apiDocMapFileName</code> and fills the apiDocMap
     * accordingly
     * @param apiDocMapFileName
     */
    void setApiDocMapFile(String apiDocMapFileName) {
	try {
	    InputStream is = new FileInputStream(apiDocMapFileName);
	    Properties userMap = new Properties();
	    userMap.load(is);
	    for (Map.Entry<?, ?> mapEntry : userMap.entrySet()) {
		try {
		    String thisRoot = (String) mapEntry.getValue();
		    if (thisRoot != null) {
			thisRoot = fixApiDocRoot(thisRoot);
			apiDocMap.put(Pattern.compile((String) mapEntry.getKey()), thisRoot);
		    } else {
			System.err.println("No URL for pattern " + mapEntry.getKey());
		    }
		} catch (PatternSyntaxException e) {
		    System.err.println("Skipping bad pattern " + mapEntry.getKey());
		}
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("File " + apiDocMapFileName + " was not found: " + e);
	} catch (IOException e) {
	    System.err.println("Error reading the property api map file " + apiDocMapFileName
		    + ": " + e);
	}
    }
    
    /**
     * Returns the appropriate URL "root" for an external class name.  It will
     * match the class name against the regular expressions specified in the
     * <code>apiDocMap</code>; if a match is found, the associated URL
     * will be returned.
     * <p>
     * <b>NOTE:</b> The match order of the match attempts is the one specified by the
     * constructor of the api doc root, so it depends on the order of "-link" and "-apiDocMap"
     * parameters.
     */
    public String getApiDocRoot(String className) {
	if(apiDocMap.isEmpty())
	    apiDocMap.put(Pattern.compile(".*"), DEFAULT_EXTERNAL_APIDOC);
	
	for (Map.Entry<Pattern, String> mapEntry : apiDocMap.entrySet()) {
	    if (mapEntry.getKey().matcher(className).matches())
		return mapEntry.getValue();
	}
	return null;
    }
    
    /** Trim and append a file separator to the string */
    private String fixApiDocRoot(String str) {
	if (str == null)
	    return null;
	String fixed = str.trim();
	if (fixed.isEmpty())
	    return "";
	if (File.separatorChar != '/')
	    fixed = fixed.replace(File.separatorChar, '/');
	if (!fixed.endsWith("/"))
	    fixed = fixed + "/";
	return fixed;
    }

    /** Set the options based on the command line parameters */
    public void setOptions(String[][] options) {
	for (String s[] : options)
	    setOption(s);
    }


    /** Set the options based on the tag elements of the ClassDoc parameter */
    public void setOptions(Doc p) {
	if (p == null)
	    return;

	for (Tag tag : p.tags("opt"))
	    setOption(StringUtil.tokenize(tag.text()));
    }

    /**
     * Check if the supplied string matches an entity specified
     * with the -hide parameter.
     * @return true if the string matches.
     */
    public boolean matchesHideExpression(String s) {
	for (Pattern hidePattern : hidePatterns) {
	    // micro-optimization because the "all pattern" is heavily used in UmlGraphDoc
	    if(hidePattern == allPattern)
		return true;
	    
	    Matcher m = hidePattern.matcher(s);
	    if (strictMatching ? m.matches() : m.find())
		return true;
	}
	return false;
    }
    
    /**
     * Check if the supplied string matches an entity specified
     * with the -include parameter.
     * @return true if the string matches.
     */
    public boolean matchesIncludeExpression(String s) {
	for (Pattern includePattern : includePatterns) {
	    Matcher m = includePattern.matcher(s);
	    if (strictMatching ? m.matches() : m.find())
		return true;
	}
	return false;
    }

    /**
     * Check if the supplied string matches an entity specified
     * with the -collpackages parameter.
     * @return true if the string matches.
     */
    public boolean matchesCollPackageExpression(String s) {
	for (Pattern collPattern : collPackages) {
	    Matcher m = collPattern.matcher(s);
	    if (strictMatching ? m.matches() : m.find())
		return true;
	}
	return false;
    }
    
    // ---------------------------------------------------------------- 
    // OptionProvider methods
    // ---------------------------------------------------------------- 
    
    public Options getOptionsFor(ClassDoc cd) {
	Options localOpt = getGlobalOptions();
	localOpt.setOptions(cd);
	return localOpt;
    }

    public Options getOptionsFor(String name) {
	return getGlobalOptions();
    }

    public Options getGlobalOptions() {
	return (Options) clone();
    }

    public void overrideForClass(Options opt, ClassDoc cd) {
	// nothing to do
    }

    public void overrideForClass(Options opt, String className) {
	// nothing to do
    }

    public String getDisplayName() {
	return "general class diagram";
    }
    
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("UMLGRAPH OPTIONS\n");
	for(Field f : this.getClass().getDeclaredFields()) {
	    if(!Modifier.isStatic(f.getModifiers())) {
		f.setAccessible(true);
		try {
		    sb.append(f.getName() + ":" + f.get(this) + "\n");
		} catch (Exception e) {
		}
	    }
	}
	return sb.toString();
    }

}
