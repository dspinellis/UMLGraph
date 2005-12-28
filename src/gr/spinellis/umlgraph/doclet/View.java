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
 * Contains the definition of a View. A View is a set of option overrides 
 * that will lead to the creation of a UML class diagram. Multiple views can
 * be defined on the same source tree, effectively allowing to create multiple
 * class diagram out of it.
 * @author wolf
 *
 */
class View {
    Map<Pattern, String[][]> optionOverrides = new LinkedHashMap<Pattern, String[][]>();
    ArrayList<String[]> globalOptions = new ArrayList<String[]>();
    ClassDoc viewDoc;
    
    /**
     * Builds a view given the class that contains its definition
     * @param c
     * @throws PatternSyntaxException
     */
    public View(ClassDoc c) throws PatternSyntaxException {
	this.viewDoc = c;
	Tag[] tags = c.tags();
	String currPattern = null;
	List<String[]> patternOptions = new ArrayList<String[]>();
	for (int i = 0; i < tags.length; i++) {
	    if(tags[i].name().equals("@match")) {
		if(currPattern != null) {
		    String[][] options = patternOptions.toArray(new String[patternOptions.size()][]);
		    optionOverrides.put(Pattern.compile(currPattern), options);
		}
		currPattern = tags[i].text();
	    } else if(tags[i].name().equals("@opt")) {
		String[] opts = StringUtil.tokenize(tags[i].text());
		opts[0] = "-" + opts[0];
		if(currPattern == null) {
		    globalOptions.add(opts);
		} else {
		    patternOptions.add(opts);
		}
	    } 
	}
	if(currPattern != null) {
	    String[][] options = patternOptions.toArray(new String[patternOptions.size()][]);
	    optionOverrides.put(Pattern.compile(currPattern), options);
	}
    }

    /**
     * Applies global view overrides. <br>
     * If not output tag has been specified, the view name will be used as the output file name.
     */
    public void applyOverrides(Options o) {
	boolean outputSet = false;
	for (String[] opts : globalOptions) {
	    if(opts[0].equals("-output")) 
		outputSet = true;
	    o.setOption(opts);
	}
	if(!outputSet)
	    o.setOption(new String[] {"-output", viewDoc.name() + ".dot"});
    }

    /**
     * Applies local view overrides
     */
    public void applyOverrides(Options o, ClassDoc c) {
	String className = c.toString();
	for (Iterator<Map.Entry<Pattern, String[][]>> iter = optionOverrides.entrySet().iterator(); iter
		.hasNext();) {
	    Map.Entry<Pattern, String[][]> mapEntry = iter.next();
	    Pattern regex = mapEntry.getKey();
	    Matcher matcher = regex.matcher(className);
	    if (matcher.matches()) {
		String[][] overrides = mapEntry.getValue(); // the first element is in fact the pattern
		for (int i = 0; i < overrides.length; i++) {
		    o.setOption(overrides[i]);
		}
	    }
	}
    }
}
