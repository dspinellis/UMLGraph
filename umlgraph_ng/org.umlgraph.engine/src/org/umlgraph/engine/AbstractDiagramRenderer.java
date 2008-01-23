/*
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

import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * This abstract class establishes the protocol for any diagram renderer.
 * <p>
 * A diagram renderer knows how to render a specific type 
 * of diagram.
 * </p>
 */
public abstract class AbstractDiagramRenderer<D extends Diagram,O> {
    /**
     * Renders a diagram.
     *  
     * @param resourceSet the input models
     * @param diagram the diagram definition
     * @param diagramOutput where to render the diagram to
     */
    public abstract void render(ResourceSet resourceSet, D diagram,
            O diagramOutput);
}
