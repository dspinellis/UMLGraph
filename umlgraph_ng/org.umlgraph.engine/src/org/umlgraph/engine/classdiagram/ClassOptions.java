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

package org.umlgraph.engine.classdiagram;

import org.umlgraph.engine.DiagramOptions;
import org.umlgraph.settings.SettingDefinitions;

/**
 * Constants for class options.
 */
public class ClassOptions implements SettingDefinitions {
    /**
     * Specify the font name to use inside abstract class nodes (String).
     */
    public static String classAbstractFontName = DiagramOptions.DEFAULT_FONT_NAME;

    /**
     * Specify the font name use for the class name of abstract classes
     * (String).
     */
    public static String classAbstractNameFontName = DiagramOptions.DEFAULT_FONT_NAME;

    /**
     * Add type information to attributes and operations (boolean)
     */
    public static Boolean classFeatureTypes = false;
    /**
     * Specify the font name to use for the class names (String).
     */
    public static String classNameFontName = DiagramOptions.DEFAULT_FONT_NAME;

    /**
     * Specify the font size to use for the class names (int).
     */
    public static Integer classNameFontSize = DiagramOptions.DEFAULT_FONT_SIZE;
    /**
     * Show class attributes (boolean).
     */
    public static Boolean classShowAttributes = false;

    /**
     * For enumerations, also show the values they can take. (boolean)
     */
    public static Boolean classShowEnumValues = false;

    /**
     * Show class operations (Java methods)
     */
    public static Boolean classShowOperations = false;

    /**
     * Adorn class elements according to their visibility (private, public,
     * protected, package) (boolean).
     */
    public static Boolean classShowVisibility = false;

}
