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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represent the program options
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class Options implements Cloneable, OptionProvider {
    private Vector<Pattern> hidePatterns;
    // PrintWriter w;
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
    boolean findViews;
    String viewName;
    String outputDirectory;
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
	outputDirectory= ".";
	outputEncoding = "ISO-8859-1";
	hidePatterns = new Vector<Pattern>();
	apiDocMapFileName = null;
	apiDocRoot = null;
	useGuillemot = true;
	findViews = false;
	viewName = null;
    }

    public Object clone() {
	Options clone = null;
	try {
	     clone = (Options) super.clone();
	} catch (CloneNotSupportedException e) {
	    // Should not happen
	}
	// deep clone the hide patterns
	clone.hidePatterns = new Vector<Pattern>(hidePatterns);
	return clone;
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

    /**
     * Return the number of arguments associated with the specified option.
     * The return value includes the actual option.
     * Will return 0 if the option is not supported.
     */
    public static int optionLength(String option) {
        if(option.equals("-qualify") ||
           option.equals("-horizontal") ||
           option.equals("-attributes") ||
           option.equals("-operations") ||
           option.equals("-constructors") ||
           option.equals("-visibility") ||
           option.equals("-types") ||
           option.equals("-all") ||
           option.equals("-noguillemot") ||
           option.equals("-hideall") ||
           option.equals("-views"))

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
           option.equals("-apidocmap") ||
           option.equals("-d") ||
           option.equals("-view"))
            return 2;
        else
            return 0;
    }
    
    /** Set the options based on a lingle option and its arguments */
    void setOption(String[] opt) {
	if(opt[0].equals("-qualify")) {
	    showQualified = true;
	} else if (opt[0].equals("-!qualify")) {
	    showQualified = false;
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
	    edgeFontName = "Helvetica";
	} else if(opt[0].equals("-edgefontsize")) {
	    edgeFontSize = Integer.parseInt(opt[1]);
	} else if (opt[0].equals("-!edgefontsize")) {
	    edgeFontSize = 10;
	} else if(opt[0].equals("-nodefontcolor")) {
	    nodeFontColor = opt[1];
	} else if (opt[0].equals("-!nodefontcolor")) {
	    nodeFontColor = "black";
	} else if(opt[0].equals("-nodefontname")) {
	    nodeFontName = opt[1];
	} else if (opt[0].equals("-!nodefontname")) {
	    nodeFontName = "Helvetica";
	} else if(opt[0].equals("-nodefontabstractname")) {
	    nodeFontAbstractName = opt[1];
	} else if (opt[0].equals("-!nodefontabstractname")) {
	    nodeFontAbstractName = "Helvetica-Oblique";
	} else if(opt[0].equals("-nodefontsize")) {
	    nodeFontSize = Integer.parseInt(opt[1]);
	} else if (opt[0].equals("-!nodefontsize")) {
	    nodeFontSize = 10;
	} else if(opt[0].equals("-nodefillcolor")) {
	    nodeFillColor = opt[1];
	} else if (opt[0].equals("-!nodefillcolor")) {
	    nodeFillColor = null;
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
		hidePatterns.add(Pattern.compile(".*"));
	    } else {
		try {
		    hidePatterns.add(Pattern.compile(opt[1]));
		} catch (PatternSyntaxException e) {
		    System.err.println("Skipping invalid pattern " + opt[1]);
		}
	    }
	} else if (opt[0].equals("-!hide")) {
	    hidePatterns.clear();
	} else if(opt[0].equals("-apidocroot")) {
	    apiDocRoot = opt[1];
	} else if (opt[0].equals("-!apidocroot")) {
	    apiDocRoot = null;
	} else if(opt[0].equals("-apidocmap")) {
	    apiDocMapFileName = opt[1];
	} else if (opt[0].equals("-!apidocmap")) {
	    apiDocMapFileName = null;
	} else if(opt[0].equals("-noguillemot")) {
	    guilOpen = "\\<\\<";
	    guilClose = "\\>\\>";
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
	    outputDirectory = ".";
	} else
	    ; // Do nothing, javadoc will handle the option or complain, if needed.
    }

    /** Set the options based on the command line parameters */
    public void setOptions(String[][] options) {
	for (String s[] : options)
	    setOption(s);
    }


    /** Set the options based on the tag elements of the ClassDoc parameter */
    public void setOptions(ClassDoc p) {
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
	    Matcher m = hidePattern.matcher(s);
	    if (m.find())
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
    
}

