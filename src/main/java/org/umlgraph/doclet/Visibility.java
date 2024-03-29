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

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Enumerates the possible visibilities in a Java program. For brevity, package
 * private visibility is referred as PACKAGE.
 * 
 * @author wolf
 */
public enum Visibility {
    PRIVATE("- "), PACKAGE("~ "), PROTECTED("# "), PUBLIC("+ ");

    final public String symbol;

    private Visibility(String symbol) {
        this.symbol = symbol;
    }

    public static Visibility get(Element doc) {
        Set<Modifier> mods = doc.getModifiers();
        if (mods.contains(Modifier.PRIVATE)) {
            return PRIVATE;
        } else if (mods.contains(Modifier.PROTECTED)) {
            return PROTECTED;
        } else if (mods.contains(Modifier.PUBLIC)) {
            return PUBLIC;
        } else {
            return PACKAGE;
        }
    }
}
