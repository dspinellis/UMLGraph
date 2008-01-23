/*
 * (C) Copyright 2007-2008 Abstratt Technologies
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

import org.umlgraph.engine.Diagram;
import org.umlgraph.engine.DiagramOptions;
import org.umlgraph.engine.EdgeOptions;
import org.umlgraph.engine.NodeOptions;
import org.umlgraph.engine.matching.ElementMatcher;
import org.umlgraph.settings.Settings;

/**
 * Any features specific to class diagrams are defined here.
 */
public class ClassDiagram extends Diagram {

    public ClassDiagram(String rootNamespace,
            ElementMatcher matcher) {
        super(rootNamespace, matcher);
    }

    @Override
    protected Settings createSettings() {
        return new Settings(ClassDiagramOptions.class, ClassOptions.class, PackageOptions.class, NodeOptions.class, EdgeOptions.class, DiagramOptions.class);
    }

}
