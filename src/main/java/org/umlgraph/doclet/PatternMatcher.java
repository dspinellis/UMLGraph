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
 *
 */
package org.umlgraph.doclet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.ClassDoc;

/**
 * Matches classes performing a regular expression match on the qualified class
 * name
 * @author wolf
 */
public class PatternMatcher implements ClassMatcher {
    Pattern pattern;

    public PatternMatcher(Pattern pattern) {
	this.pattern = pattern;
    }

    public boolean matches(ClassDoc cd) {
	return matches(cd.toString());
    }

    public boolean matches(String name) {
	Matcher matcher = pattern.matcher(name);
	return matcher.matches();
    }

}
