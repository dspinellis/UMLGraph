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

package org.umlgraph.engine;

import org.umlgraph.settings.SettingDefinitions;

/**
 * Constants for edge options.
 */
public class EdgeOptions implements SettingDefinitions {
    /**
     * Specify the color for drawing edges.
     */
    public static String edgeColor;
    /**
     * Specify the font color to use for edge labels (String).
     */
    public static String edgeFontColor;
    /**
     * Specify the font name to use for edge labels (String).
     */
    public static String edgeFontName = DiagramOptions.DEFAULT_FONT_NAME;
    /**
     * Specify the font size to use for edge labels (int).
     */
    public static Integer edgeFontSize = DiagramOptions.DEFAULT_FONT_SIZE;
}
