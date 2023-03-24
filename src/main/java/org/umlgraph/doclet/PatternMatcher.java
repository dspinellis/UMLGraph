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

import java.util.regex.Pattern;

import javax.lang.model.element.TypeElement;

/**
 * Matches classes performing a regular expression match on the qualified class
 * name
 * 
 * @author wolf
 */
public class PatternMatcher implements ClassMatcher {
    Pattern pattern;

    public PatternMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean matches(TypeElement cd) {
        return matches(cd.getQualifiedName());
    }

    public boolean matches(CharSequence name) {
        return pattern.matcher(name).matches();
    }

}
