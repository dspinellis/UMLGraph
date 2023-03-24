/*
 * Contibuted by Andrea Aime
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

import javax.lang.model.element.TypeElement;

import com.sun.source.util.DocTrees;

/**
 * A factory class that builds Options object for general use or for a specific class
 */
public interface OptionProvider {
    /**
     * Returns the options for the specified class.
     */
    public Options getOptionsFor(DocTrees dt, TypeElement cd);

    /**
     * Returns the options for the specified class.
     */
    public Options getOptionsFor(CharSequence name);

    /**
     * Returns the global options (the class independent definition)
     */
    public Options getGlobalOptions();

    /**
     * Gets a base Options and applies the overrides for the specified class
     */
    public void overrideForClass(Options opt, TypeElement cd);

    /**
     * Gets a base Options and applies the overrides for the specified class
     */
    public void overrideForClass(Options opt, CharSequence className);

    /**
     * Returns user displayable name for this option provider.
     * <p>
     * Will be used to provide progress feedback on the console
     */
    public String getDisplayName();
}
