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
 * Generic constants for options applicable to all types of UML diagrams.
 */
public class DiagramOptions implements SettingDefinitions {

    /** The default font name. */
    public static final String DEFAULT_FONT_NAME = "Times";

    /** The default font size. */
    public static final int DEFAULT_FONT_SIZE = 10;

    /**
     * Specify the graph's background color.
     */
    public static String diagramBackgroundColor;

    /**
     * Layout the graph in the horizontal direction (boolean).
     */
    public static boolean diagramHorizontal;

    /**
     * Specify entities to hide from the graph. Matching is done using a
     * non-anchored regular match. For instance, "-hide (Big|\.)Widget" would
     * hide "com.foo.widgets.Widget" and "com.foo.widgets.BigWidget". Can also
     * be used without arguments, in this case it will hide everything (useful
     * in the context of views to selectively unhide some portions of the graph,
     * see the view chapter for further details).
     */
    // TODO String hide();
}
