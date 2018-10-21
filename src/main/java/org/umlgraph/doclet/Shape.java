/*
 * Manage the GraphViz interface for node shapes
 *
 * (C) Copyright 2007 Diomidis Spinellis
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

import java.util.HashMap;
import java.util.Locale;

/**
 * Properties of node shapes
 *
 * @version $Revision$
 * @author Erich Schubert
 */
public enum Shape {
    CLASS(""), //
    NOTE(", shape=note"), //
    NODE(", shape=box3d"), //
    COMPONENT(", shape=component"), //
    PACKAGE(", shape=tab"), //
    COLLABORATION(", shape=ellipse, style=dashed"), //
    USECASE(", shape=ellipse"), //
    ACTIVECLASS("");

    /** Graphviz style */
    public final String style;

    /** Map for valid shape names */
    private static final HashMap<String, Shape> index = new HashMap<String, Shape>(16);

    /** Initialize the lookup index */
    static {
	for (Shape s : Shape.values()) {
	    index.put(s.name(), s);
	    index.put(s.name().toLowerCase(Locale.ROOT), s);
	}
    }

    /**
     * Get the shape from a string. This allows both the uppercase and the lowercase
     * name. Prefer this to {{@link #valueOf(String)} which only accepts uppercase.
     *
     * @param s String
     * @return Shape
     */
    public static Shape of(String s) {
	Shape shp = index.get(s);
	if (shp != null)
	    return shp;
	System.err.println("Ignoring invalid shape: " + s);
	return CLASS;
    }

    /** Enum constructor, must be private! */
    private Shape(String style) {
	this.style = style;
    }

    /** Return the shape's GraphViz landing port */
    public String landingPort() {
	return this == CLASS || this == ACTIVECLASS ? ":p" : "";
    }

    /** Return the table border required for the shape */
    public String extraColumn() {
	return this == Shape.ACTIVECLASS ? ("<td rowspan=\"10\"></td>") : "";
    }

    /** Return the cell border required for the shape */
    public String cellBorder() {
	return this == CLASS || this == ACTIVECLASS ? "1" : "0";
    }
}
