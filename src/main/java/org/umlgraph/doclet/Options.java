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
     * Return the number of arguments associated with the specified option.
     * The return value includes the actual option.
     * Will return 0 if the option is not supported.
     */
    public static int optionLength(String option) {
        if(option.equals("-qualify") || option.equals("-!qualify") ||
           option.equals("-qualifyGenerics") || option.equals("-!qualifyGenerics") ||
           option.equals("-horizontal") || option.equals("-!horizontal") ||
           option.equals("-attributes") || option.equals("-!attributes") ||
           option.equals("-enumconstants") || option.equals("-!enumconstants") ||
           option.equals("-operations") || option.equals("-!operations") ||
           option.equals("-enumerations") || option.equals("-!enumerations") ||
           option.equals("-constructors") || option.equals("-!constructors") ||
           option.equals("-visibility") || option.equals("-!visibility") ||
           option.equals("-types") || option.equals("-!types") ||
           option.equals("-autosize") || option.equals("-!autosize") ||
           option.equals("-commentname") || option.equals("-!commentname") ||
           option.equals("-nodefontabstractitalic") || option.equals("-!nodefontabstractitalic") ||
           option.equals("-all") ||
           option.equals("-postfixpackage") ||
           option.equals("-noguillemot") ||
           option.equals("-views") ||
           option.equals("-inferrel") ||
           option.equals("-useimports") ||
           option.equals("-collapsible") ||
           option.equals("-inferdep") ||
           option.equals("-inferdepinpackage") ||
           option.equals("-compact"))

            return 1;
        else if(option.equals("-nodefillcolor") ||
           option.equals("-nodefontcolor") ||
           option.equals("-nodefontsize") ||
           option.equals("-nodefontname") ||
           option.equals("-nodefontclasssize") ||
           option.equals("-nodefontclassname") ||
   	   option.equals("-nodefonttagsize") ||
   	   option.equals("-nodefonttagname") ||
   	   option.equals("-nodefontpackagesize") ||
   	   option.equals("-nodefontpackagename") ||
           option.equals("-edgefontcolor") ||
           option.equals("-edgecolor") ||
           option.equals("-edgefontsize") ||
           option.equals("-edgefontname") ||
           option.equals("-shape") ||
           option.equals("-output") ||
           option.equals("-outputencoding") ||
           option.equals("-bgcolor") ||
           option.equals("-hide") ||
           option.equals("-include") ||
           option.equals("-apidocroot") ||
           option.equals("-apidocmap") ||
           option.equals("-d") ||
           option.equals("-view") ||
           option.equals("-inferreltype") ||
           option.equals("-inferdepvis") ||
           option.equals("-collpackages") ||
           option.equals("-nodesep") ||
           option.equals("-ranksep") ||
           option.equals("-dotexecutable") ||
           option.equals("-link"))
           return 2;
	else if(option.equals("-contextPattern") ||
		option.equals("-linkoffline"))
            return 3;
        else
            return 0;
    }
    
    /** Set the options based on a single option and its arguments */
    void setOption(String[] opt) {
	if(!opt[0].equals("-hide") && optionLength(opt[0]) > opt.length) {
	    System.err.println("Skipping option '" + opt[0] + "', missing argument");
	    return;
	}
	
	if(opt[0].equals("-qualify")) {
	    showQualified = true;
	} else if (opt[0].equals("-!qualify")) {
	    showQualified = false;
	} else if(opt[0].equals("-qualifyGenerics")) {
	    showQualifiedGenerics = true;
	} else if (opt[0].equals("-!qualifyGenerics")) {
	    showQualifiedGenerics = false;
	} else if(opt[0].equals("-horizontal")) {
	    horizontal = true;
	} else if (opt[0].equals("-!horizontal")) {
	    horizontal = false;
	} else if(opt[0].equals("-attributes")) {
	    showAttributes = true;
	} else if (opt[0].equals("-!attributes")) {
	    showAttributes = false;
	} else if(opt[0].equals("-enumconstants")) {
	    showEnumConstants = true;
	} else if (opt[0].equals("-!enumconstants")) {
	    showEnumConstants = false;
	} else if(opt[0].equals("-operations")) {
	    showOperations = true;
	} else if (opt[0].equals("-!operations")) {
	    showOperations = false;
	} else if(opt[0].equals("-enumerations")) {
	    showEnumerations = true;
	} else if (opt[0].equals("-!enumerations")) {
	    showEnumerations = false;
	} else if(opt[0].equals("-constructors")) {
	    showConstructors = true;
	} else if (opt[0].equals("-!constructors")) {
	    showConstructors = false;
	} else if(opt[0].equals("-visibility")) {
	    showVisibility = true;
	} else if (opt[0].equals("-!visibility")) {
	    showVisibility = false;
	} else if(opt[0].equals("-types")) {
	    showType = true;
	} else if (opt[0].equals("-!types")) {
	    showType = false;
	} else if(opt[0].equals("-autoSize")) {
	    autoSize = true;
	} else if (opt[0].equals("-!autoSize")) {
	    autoSize = false;
	} else if(opt[0].equals("-commentname")) {
	    showComment = true;
	} else if (opt[0].equals("-!commentname")) {
	    showComment = false;
	} else if(opt[0].equals("-all")) {
	    setAll();
	} else if(opt[0].equals("-bgcolor")) {
	    bgColor = opt[1];
	} else if (opt[0].equals("-!bgcolor")) {
	    bgColor = null;
	} else if(opt[0].equals("-edgecolor")) {
	    edgeColor = opt[1];
	} else if (opt[0].equals("-!edgecolor")) {
	    edgeColor = "black";
	} else if(opt[0].equals("-edgefontcolor")) {
	    edgeFontColor = opt[1];
	} else if (opt[0].equals("-!edgefontcolor")) {
	    edgeFontColor = "black";
	} else if(opt[0].equals("-edgefontname")) {
	    edgeFontName = opt[1];
	} else if (opt[0].equals("-!edgefontname")) {
	    edgeFontName = Font.DEFAULT_FONT;
	} else if(opt[0].equals("-edgefontsize")) {
	    edgeFontSize = Double.parseDouble(opt[1]);
	} else if (opt[0].equals("-!edgefontsize")) {
	    edgeFontSize = 10;
	} else if(opt[0].equals("-nodefontcolor")) {
	    nodeFontColor = opt[1];
	} else if (opt[0].equals("-!nodefontcolor")) {
	    nodeFontColor = "black";
	} else if(opt[0].equals("-nodefontname")) {
	    nodeFontName = opt[1];
	} else if (opt[0].equals("-!nodefontname")) {
	    nodeFontName = Font.DEFAULT_FONT;
	} else if(opt[0].equals("-nodefontabstractitalic")) {
	    nodeFontAbstractItalic = true;
	} else if (opt[0].equals("-!nodefontabstractitalic")) {
	    nodeFontAbstractItalic = false;
	} else if(opt[0].equals("-nodefontsize")) {
	    nodeFontSize = Double.parseDouble(opt[1]);
	} else if (opt[0].equals("-!nodefontsize")) {
	    nodeFontSize = 10;
	} else if(opt[0].equals("-nodefontclassname")) {
	    nodeFontClassName = opt[1];
	} else if (opt[0].equals("-!nodefontclassname")) {
	    nodeFontClassName = null;
	} else if(opt[0].equals("-nodefontclasssize")) {
	    nodeFontClassSize = Double.parseDouble(opt[1]);
	} else if (opt[0].equals("-!nodefontclasssize")) {
	    nodeFontClassSize = -1;
	} else if(opt[0].equals("-nodefonttagname")) {
	    nodeFontTagName = opt[1];
	} else if (opt[0].equals("-!nodefonttagname")) {
	    nodeFontTagName = null;
	} else if(opt[0].equals("-nodefonttagsize")) {
	    nodeFontTagSize = Double.parseDouble(opt[1]);
	} else if (opt[0].equals("-!nodefonttagsize")) {
	    nodeFontTagSize = -1;
	} else if(opt[0].equals("-nodefontpackagename")) {
	    nodeFontPackageName = opt[1];
	} else if (opt[0].equals("-!nodefontpackagename")) {
	    nodeFontPackageName = null;
	} else if(opt[0].equals("-nodefontpackagesize")) {
	    nodeFontPackageSize = Double.parseDouble(opt[1]);
	} else if (opt[0].equals("-!nodefontpackagesize")) {
	    nodeFontPackageSize = -1;
	} else if(opt[0].equals("-nodefillcolor")) {
	    nodeFillColor = opt[1];
	} else if (opt[0].equals("-!nodefillcolor")) {
	    nodeFillColor = null;
	} else if(opt[0].equals("-shape")) {
	    shape = Shape.of(opt[1]);
	} else if (opt[0].equals("-!shape")) {
	    shape = Shape.CLASS;
	} else if(opt[0].equals("-output")) {
	    outputFileName = opt[1];
	} else if (opt[0].equals("-!output")) {
	    outputFileName = "graph.dot";
	} else if(opt[0].equals("-outputencoding")) {
	    outputEncoding = opt[1];
	} else if (opt[0].equals("-!outputencoding")) {
	    outputEncoding = "ISO-8859-1";
	} else if(opt[0].equals("-hide")) {
	    if(opt.length == 1) {
		hidePatterns.clear();
		hidePatterns.add(allPattern);
	    } else {
		try {
		    hidePatterns.add(Pattern.compile(opt[1]));
		} catch (PatternSyntaxException e) {
		    System.err.println("Skipping invalid pattern " + opt[1]);
		}
	    }
	} else if (opt[0].equals("-!hide")) {
	    hidePatterns.clear();
	} else if(opt[0].equals("-include")) {
	    try {
		includePatterns.add(Pattern.compile(opt[1]));
	    } catch (PatternSyntaxException e) {
		System.err.println("Skipping invalid pattern " + opt[1]);
	    }
	} else if (opt[0].equals("-!include")) {
	    includePatterns.clear();
	} else if(opt[0].equals("-apidocroot")) {
	    apiDocRoot = fixApiDocRoot(opt[1]);
	} else if (opt[0].equals("-!apidocroot")) {
	    apiDocRoot = null;
	} else if(opt[0].equals("-apidocmap")) {
	    setApiDocMapFile(opt[1]);
	} else if (opt[0].equals("-!apidocmap")) {
	    apiDocMap.clear();
	} else if(opt[0].equals("-noguillemot")) {
	    guilOpen = "&lt;&lt;";
	    guilClose = "&gt;&gt;";
	} else if (opt[0].equals("-!noguillemot")) {
	    guilOpen = "\u00ab";
	    guilClose = "\u00bb";
	} else if (opt[0].equals("-view")) {
	    viewName = opt[1];
	} else if (opt[0].equals("-!view")) {
	    viewName = null;
	} else if (opt[0].equals("-views")) {
	    findViews = true;
	} else if (opt[0].equals("-!views")) {
	    findViews = false;
	} else if (opt[0].equals("-d")) {
	    outputDirectory = opt[1];
	} else if (opt[0].equals("-!d")) {
	    outputDirectory = null;
	} else if(opt[0].equals("-inferrel")) {
	    inferRelationships = true;
	} else if(opt[0].equals("-!inferrel")) {
	    inferRelationships = false;
	} else if(opt[0].equals("-inferreltype")) {
		try {
		    inferRelationshipType = RelationType.valueOf(opt[1].toUpperCase());
		} catch(IllegalArgumentException e) {
		    System.err.println("Unknown association type " + opt[1]);
		}
	} else if(opt[0].equals("-!inferreltype")) {
	    inferRelationshipType = RelationType.NAVASSOC;
	} else if(opt[0].equals("-inferdepvis")) {
	    try {
		Visibility vis = Visibility.valueOf(opt[1].toUpperCase());
		inferDependencyVisibility = vis;
	    } catch(IllegalArgumentException e) {
		System.err.println("Ignoring invalid visibility specification for " +
				"dependency inference: " + opt[1]);
	    }
	} else if(opt[0].equals("-!inferdepvis")) {
	    inferDependencyVisibility = Visibility.PRIVATE;
	} else if(opt[0].equals("-collapsible")) {
	    collapsibleDiagrams = true;
	} else if(opt[0].equals("-!collapsible")) {
	    collapsibleDiagrams = false;
	} else if(opt[0].equals("-inferdep")) {
	    inferDependencies = true;
	} else if(opt[0].equals("-!inferdep")) {
	    inferDependencies = false;
	} else if(opt[0].equals("-inferdepinpackage")) {
	    inferDepInPackage = true;
	} else if(opt[0].equals("-!inferdepinpackage")) {
	    inferDepInPackage = false;
	} else if(opt[0].equals("-useimports")) {
	    useImports = true;
	} else if(opt[0].equals("-!useimports")) {
	    useImports = false;
	} else if (opt[0].equals("-collpackages")) {
	    try {
		collPackages.add(Pattern.compile(opt[1]));
	    } catch (PatternSyntaxException e) {
		System.err.println("Skipping invalid pattern " + opt[1]);
	    }
	} else if (opt[0].equals("-!collpackages")) {
	    collPackages.clear();
	} else if (opt[0].equals("-compact")) {
	    compact = true;
	} else if (opt[0].equals("-!compact")) {
	    compact = false;
	} else if (opt[0].equals("-postfixpackage")) {
	    postfixPackage = true;
	} else if (opt[0].equals("-!postfixpackage")) {
	    postfixPackage = false;
	} else if (opt[0].equals("-link")) {
	    addApiDocRoots(opt[1]);
	} else if (opt[0].equals("-linkoffline")) {
	    addApiDocRootsOffline(opt[1], opt[2]);
	} else if(opt[0].equals("-contextPattern")) {
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
		
	} else if (opt[0].equals("-nodesep")) {
	    try {
		nodeSep = Double.parseDouble(opt[1]);
	    } catch (NumberFormatException e) {
		System.err.println("Skipping invalid nodesep " + opt[1]);
	    }
	} else if (opt[0].equals("-!nodesep")) {
	    nodeSep = 0.25;
	} else if (opt[0].equals("-ranksep")) {
	    try {
		rankSep = Double.parseDouble(opt[1]);
	    } catch (NumberFormatException e) {
		System.err.println("Skipping invalid ranksep " + opt[1]);
	    }
	} else if (opt[0].equals("-!ranksep")) {
	    rankSep = 0.5;
    } else if (opt[0].equals("-dotexecutable")) {
        dotExecutable = opt[1];
	} else
	    ; // Do nothing, javadoc will handle the option or complain, if
                // needed.
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

	for (Tag tag : p.tags("opt")) {
	    String[] opt = StringUtil.tokenize(tag.text());
	    opt[0] = "-" + opt[0];
	    setOption(opt);
	}
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
