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
 *
 */

package org.umlgraph.doclet;

import java.util.*;
import java.util.regex.Pattern;

/**
 * String utility functions
 * 
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
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

    private final static Pattern ESCAPE_BASIC_XML = Pattern.compile("[&<>]");

    /**
     * Escape &lt;, &gt;, and &amp; characters in the string with the corresponding
     * HTML entity code.
     */
    public static String escape(String s) {
	if (ESCAPE_BASIC_XML.matcher(s).find()) {
	    StringBuilder sb = new StringBuilder(s);
	    for (int i = 0; i < sb.length();) {
		switch (sb.charAt(i)) {
		case '&':
		    sb.replace(i, i + 1, "&amp;");
		    i += "&amp;".length();
		    break;
		case '<':
		    sb.replace(i, i + 1, "&lt;");
		    i += "&lt;".length();
		    break;
		case '>':
		    sb.replace(i, i + 1, "&gt;");
		    i += "&gt;".length();
		    break;
		default:
		    i++;
		}
	    }
	    return sb.toString();
	} else
	    return s;
    }

    /**
     * Convert embedded newlines into HTML line breaks
     */
    public static String htmlNewline(String s) {
	if (s.indexOf('\n') == -1)
	    return s;

	StringBuilder sb = new StringBuilder(s);
	for (int i = 0; i < sb.length();) {
	    if (sb.charAt(i) == '\n') {
		sb.replace(i, i + 1, "<br/>");
		i += "<br/>".length();
	    } else
		i++;
	}
	return sb.toString();
    }

    /**
     * Convert &lt; and &gt; characters in the string to the respective guillemot
     * characters.
     */
    public static String guillemize(Options opt, String s) {
	StringBuilder r = new StringBuilder(s);
	for (int i = 0; i < r.length();)
	    switch (r.charAt(i)) {
	    case '<':
		r.replace(i, i + 1, opt.guilOpen);
		i += opt.guilOpen.length();
		break;
	    case '>':
		r.replace(i, i + 1, opt.guilClose);
		i += opt.guilClose.length();
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
    public static String guilWrap(Options opt, String str) {
	return opt.guilOpen + str + opt.guilClose;
    }

    /** Removes the template specs from a class name. */
    public static String removeTemplate(String name) {
	int openIdx = name.indexOf('<');
	if (openIdx == -1)
	    return name;
	StringBuilder buf = new StringBuilder(name.length());
	for (int i = 0, depth = 0; i < name.length(); i++) {
	    char c = name.charAt(i);
	    if (c == '<')
		depth++;
	    else if (c == '>')
		depth--;
	    else if (depth == 0)
		buf.append(c);
	}
	return buf.toString();
    }
}
