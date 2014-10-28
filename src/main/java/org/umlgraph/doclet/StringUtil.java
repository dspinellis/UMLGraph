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

/**
 * String utility functions
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

}
