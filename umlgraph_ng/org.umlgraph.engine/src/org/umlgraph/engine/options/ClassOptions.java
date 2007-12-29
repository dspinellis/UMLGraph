/*
 * (C) Copyright 2002-2008 Diomidis Spinellis
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

package org.umlgraph.engine.options;

/**
 * Constants for class options.
 */
public interface ClassOptions {
    /**
     * Specify the font name to use inside abstract class nodes (String).
     */
    String FONT_NAME_ABSTRACT_CLASS = "classAbstractFontName";

    /**
     * Specify the font name to use for the class names (String).
     */
    String FONT_NAME_CLASS_NAME = "classNameFontName";

    /**
     * Specify the font name use for the class name of abstract classes
     * (String).
     */
    String FONT_NAME_ABSTRACT_CLASS_NAME = "classAbstractNameFontName";
    /**
     * Specify the font size to use for the class names (int).
     */
    String FONT_SIZE_CLASS_NAME = "classNameFontSize";
    
    /**
     * Show class attributes (boolean).
     */
    String SHOW_ATTRIBUTES = "classShowAttributes";
    /**
     * Show class operations (Java methods)
     */
    String SHOW_OPERATIONS = "classShowOperations";
    
    /**
     * Adorn class elements according to their visibility (private, public, protected, package) (boolean).
     */
    String SHOW_VISIBILITY = "classShowVisibility";
    
    /**
     * Add type information to attributes and operations (boolean)
     */
    String SHOW_TYPES = "classFeatureTypes";
    
    /**
     * For enumerations, also show the values they can take. (boolean) 
     */
    String SHOW_ENUM_VALUES = "classShowEnumValues";
    
    
}
