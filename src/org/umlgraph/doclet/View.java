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

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    Map<Pattern, String[]> optionOverrides = new LinkedHashMap<Pattern, String[]>();
    ClassDoc viewDoc;

    public View(ClassDoc c) {
	this.viewDoc = c;
	Tag[] tags = c.tags("opt_override");
	for (int i = 0; i < tags.length; i++) {
	    String[] opts = StringUtil.tokenize(tags[i].text());
	    try {
		optionOverrides.put(Pattern.compile(opts[0]), opts);
	    } catch (PatternSyntaxException e) {
		System.err.println("Skipping invalid pattern " + opts[0] + " in view "
			+ c.toString());
	    }

	}
    }

    /**
     * Applies global view overrides
     */
    public void applyOverrides(Options o) {
	o.setOptions(viewDoc);
	File dotFile = new File(o.outputDirectory, viewDoc.name() + ".dot");
	o.setOption(new String[] {"-output", dotFile.getPath()});
    }

    /**
     * Applies local view overrides
     */
    public void applyOverrides(Options o, ClassDoc c) {
	String className = c.toString();
	for (Iterator<Map.Entry<Pattern, String[]>> iter = optionOverrides.entrySet().iterator(); iter
		.hasNext();) {
	    Map.Entry<Pattern, String[]> mapEntry = iter.next();
	    Pattern regex = mapEntry.getKey();
	    Matcher matcher = regex.matcher(className);
	    if (matcher.matches()) {
		String[] overrides = mapEntry.getValue(); // the first element is in fact the pattern
		for (int i = 1; i < overrides.length; i++) {
		    String[] option = null;
		    boolean reset = false;
		    if (overrides[i].contains("=")) {
			option = overrides[i].split("=");
		    } else {
			option = new String[] { overrides[i] };
		    }
		    if (option[0].charAt(0) == '!') {
			reset = true;
			option[0] = option[0].substring(1);
		    }
		    option[0] = "-" + option[0];
		    if (reset) {
			o.resetOption(option);
		    } else {
			o.setOption(option);
		    }
		}
	    }
	}
    }
}
