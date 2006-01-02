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
 * @depend - - - Options
 * 
 */
class View implements OptionProvider {
    Map<Pattern, String[][]> optionOverrides = new LinkedHashMap<Pattern, String[][]>();
    ClassDoc viewDoc;
    OptionProvider provider;
    List<String[]> globalOptions;

    /**
     * Builds a view given the class that contains its definition
     * @param c
     * @throws PatternSyntaxException
     */
    public View(ClassDoc c, OptionProvider provider) throws PatternSyntaxException {
	this.viewDoc = c;
	this.provider = provider;
	Tag[] tags = c.tags();
	String currPattern = null;
	// parse options, get the global ones, and build a map of the
	// pattern matched overrides
	List<String[]> patternOptions = new ArrayList<String[]>();
	globalOptions = new ArrayList<String[]>();
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
    }

    // ---------------------------------------------------------------- 
    // OptionProvider methods
    // ---------------------------------------------------------------- 

    public Options getOptionsFor(ClassDoc cd) {
	Options localOpt = getGlobalOptions();
	overrideForClass(localOpt, cd);
	localOpt.setOptions(cd);
	return localOpt;
    }

    public Options getOptionsFor(String name) {
	Options localOpt = getGlobalOptions();
	overrideForClass(localOpt, name);
	return localOpt;
    }

    public Options getGlobalOptions() {
	Options go = provider.getGlobalOptions();
	
	boolean outputSet = false;
	for (String[] opts : globalOptions) {
	    if (opts[0].equals("-output"))
		outputSet = true;
	    go.setOption(opts);
	}
	if (!outputSet)
	    go.setOption(new String[] { "-output", viewDoc.name() + ".dot" });
	
	return go;
    }

    public void overrideForClass(Options opt, ClassDoc cd) {
	provider.overrideForClass(opt, cd);
	overrideForClass_(opt, cd.toString());
    }

    public void overrideForClass(Options opt, String className) {
	provider.overrideForClass(opt, className);
	overrideForClass_(opt, className);
    }

    private void overrideForClass_(Options opt, String className) {
	for (Iterator<Map.Entry<Pattern, String[][]>> iter = optionOverrides.entrySet().iterator(); iter
		.hasNext();) {
	    Map.Entry<Pattern, String[][]> mapEntry = iter.next();
	    Pattern regex = mapEntry.getKey();
	    Matcher matcher = regex.matcher(className);
	    if (matcher.matches()) {
		String[][] overrides = mapEntry.getValue();
		for (int i = 0; i < overrides.length; i++) {
		    opt.setOption(overrides[i]);
		}
	    }
	}
    }

}
