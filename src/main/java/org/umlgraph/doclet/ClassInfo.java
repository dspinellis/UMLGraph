/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class's dot-compatible alias name (for fully qualified class names)
 * and printed information
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class ClassInfo {
    private static int classNumber;
    /** Alias name for the class */
    String name;
    /** True if the class class node has been printed */
    boolean nodePrinted;
    /** True if the class class node is hidden */
    boolean hidden;
    /** 
     * The list of classes that share a relation with this one. Contains
     * all the classes linked with a bi-directional relation , and the ones 
     * referred by a directed relation 
     */
    Map<String, RelationPattern> relatedClasses = new HashMap<String, RelationPattern>();

    ClassInfo(boolean p, boolean h) {
	nodePrinted = p;
	hidden = h;
	name = "c" + (new Integer(classNumber)).toString();
	classNumber++;
    }
    
    public void addRelation(String dest, RelationType rt, RelationDirection d) {
	RelationPattern ri = relatedClasses.get(dest);
	if(ri == null) {
	    ri = new RelationPattern(RelationDirection.NONE);
	    relatedClasses.put(dest, ri);
	}
	ri.addRelation(rt, d);
    }
    
    public RelationPattern getRelation(String dest) {
	return relatedClasses.get(dest);
    }

    /** Start numbering from zero. */
    public static void reset() {
	classNumber = 0;
    }

    
}

