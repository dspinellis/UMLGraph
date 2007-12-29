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
 * Constants for node options.
 */
public interface NodeOptions {
    /**
     * Specify the color to use to fill the shapes (String).
     */
    public String FILL_COLOR = "nodeFillColor";

    /**
     * Specify the font name to use inside nodes (String).
     */
    public String FONT_NAME = "nodeFontName";

    /**
     * Specify the font size to use inside nodes (int).
     */
    public String FONT_SIZE = "nodeFontSize";

    /**
     * Specify the font name to use for the tags (String).
     */
    public String FONT_NAME_TAG = "nodeTagFontName";
    /**
     * Specify the font size to use for the tags (int).
     */
    public String FONT_SIZE_TAG = "nodeTagFontSize";

    /**
     * Specify the font color to use inside nodes (String).
     */
    public String FONT_COLOR = "nodeFontColor";

}
