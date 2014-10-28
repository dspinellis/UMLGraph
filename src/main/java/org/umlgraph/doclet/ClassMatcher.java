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

import com.sun.javadoc.ClassDoc;

/**
 * A ClassMatcher is used to check if a class definition matches a
 * specific condition. The nature of the condition is dependent on
 * the kind of matcher 
 * @author wolf
 */
public interface ClassMatcher {
    /**
     * Returns the options for the specified class. 
     */
    public boolean matches(ClassDoc cd);
    
    /**
     * Returns the options for the specified class. 
     */
    public boolean matches(String name);
}
