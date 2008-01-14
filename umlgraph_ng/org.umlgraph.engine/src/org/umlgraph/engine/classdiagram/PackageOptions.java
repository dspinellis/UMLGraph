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
 * Constants for package options.
 */
public class PackageOptions implements SettingDefinitions {

    /**
     * Specify the font name to use for the package names (used only when the
     * package name is postfixed, see -postfixpackage).
     */
    public static String packageNameFontName = DiagramOptions.DEFAULT_FONT_NAME;

    /**
     * Specify the font size to use for the package names (used only when it
     * package name is postfixed, see -postfixpackage).
     */
    public static Integer packageNameFontSize = DiagramOptions.DEFAULT_FONT_SIZE;
}
