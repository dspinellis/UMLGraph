/*
 * Contributed by Andrea Aime
 * (C) Copyright 2005-2008 Diomidis Spinellis
 * (C) Copyright 2008 Abstratt Technologies
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
package org.umlgraph.engine.matching;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

/**
 * Matches named elements performing a regular expression match on the element's
 * qualified name.
 * 
 * @author wolf
 * @author rchaves
 */
public class PatternMatcher implements ElementMatcher {
    private Pattern pattern;

    public PatternMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean matches(Element element) {
        if (!(element instanceof NamedElement))
            return false;
        NamedElement named = (NamedElement) element;
        String elementName = named.getQualifiedName();
        Matcher matcher = pattern.matcher(elementName);
        return matcher.matches();
    }
}
