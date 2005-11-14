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

package gr.spinellis.umlgraph;

import com.sun.javadoc.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represent the program options
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
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

