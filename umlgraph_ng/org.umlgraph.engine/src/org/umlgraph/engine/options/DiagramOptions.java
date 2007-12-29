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
 * Generic constants for options applicable to all types of UML diagrams.
 */
public interface DiagramOptions {

    /**
     * Layout the graph in the horizontal direction (boolean).
     */
    String HORIZONTAL = "diagramHorizontal";
    
    /**
     * Specify the graph's background color.
     */
    String BACKGROUND_COLOR = "diagramBackgroundColor";

    /**
     * Specify entities to hide from the graph. Matching is done using a non-anchored regular match. For instance, "-hide (Big|\.)Widget" would hide "com.foo.widgets.Widget" and "com.foo.widgets.BigWidget". Can also be used without arguments, in this case it will hide everything (useful in the context of views to selectively unhide some portions of the graph, see the view chapter for further details).     */
    String hide();
}
