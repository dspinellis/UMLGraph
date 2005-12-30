/*
 * Contibuted by Andrea Aime
 * (C) Copyright 2005 Diomidis Spinellis
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Tag;

/**
 * Contains the definition of a View. A View is a set of option overrides that
 * will lead to the creation of a UML class diagram. Multiple views can be
 * defined on the same source tree, effectively allowing to create multiple
 * class diagram out of it.
 * @author wolf
 * 
 */
class View implements OptionProvider {
    Map<Pattern, String[][]> optionOverrides = new LinkedHashMap<Pattern, String[][]>();
    ClassDoc viewDoc;
    Options baseOptions;

    /**
     * Builds a view given the class that contains its definition
     * @param c
     * @throws PatternSyntaxException
     */
    public View(ClassDoc c, Options generalOptions) throws PatternSyntaxException {
	this.viewDoc = c;
	this.baseOptions = (Options) generalOptions.clone();
	Tag[] tags = c.tags();
	String currPattern = null;
	// parse options, get the global ones, and build a map of the
	// pattern matched overrides
	List<String[]> patternOptions = new ArrayList<String[]>();
	List<String[]> globalOptions = new ArrayList<String[]>();
	for (int i = 0; i < tags.length; i++) {
	    if (tags[i].name().equals("@match")) {
		if (currPattern != null) {
		    String[][] options = patternOptions
			    .toArray(new String[patternOptions.size()][]);
		    optionOverrides.put(Pattern.compile(currPattern), options);
		}
		currPattern = tags[i].text();
		patternOptions.clear();
	    } else if (tags[i].name().equals("@opt")) {
		String[] opts = StringUtil.tokenize(tags[i].text());
		opts[0] = "-" + opts[0];
		if (currPattern == null) {
		    globalOptions.add(opts);
		} else {
		    patternOptions.add(opts);
		}
	    }
	}
	if (currPattern != null) {
	    String[][] options = patternOptions.toArray(new String[patternOptions.size()][]);
	    optionOverrides.put(Pattern.compile(currPattern), options);
	}
	
	// apply the view global options
	boolean outputSet = false;
	for (String[] opts : globalOptions) {
	    if (opts[0].equals("-output"))
		outputSet = true;
	    baseOptions.setOption(opts);
	}
	if (!outputSet)
	    baseOptions.setOption(new String[] { "-output", viewDoc.name() + ".dot" });
    }

    /**
     * Applies local view overrides
     */
    private void applyOverrides(Options o, String className) {
	for (Iterator<Map.Entry<Pattern, String[][]>> iter = optionOverrides.entrySet().iterator(); iter
		.hasNext();) {
	    Map.Entry<Pattern, String[][]> mapEntry = iter.next();
	    Pattern regex = mapEntry.getKey();
	    Matcher matcher = regex.matcher(className);
	    if (matcher.matches()) {
		String[][] overrides = mapEntry.getValue(); 
		for (int i = 0; i < overrides.length; i++) {
		    o.setOption(overrides[i]);
		}
	    }
	}
    }

    // ---------------------------------------------------------------- 
    // OptionProvider methods
    // ---------------------------------------------------------------- 

    public Options getOptionsFor(ClassDoc cd) {
	Options localOpt = (Options) baseOptions.clone();
	localOpt.setOptions(cd);
	applyOverrides(localOpt, cd.toString());
	return localOpt;
    }

    public Options getOptionsFor(String name) {
	Options localOpt = (Options) baseOptions.clone();
	applyOverrides(localOpt, name);
	return localOpt;
    }

    public Options getGlobalOptions() {
	return baseOptions;
    }
}
