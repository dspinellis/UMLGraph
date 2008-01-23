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
package org.umlgraph.engine.classdiagram;

import java.util.Collection;

import org.eclipse.uml2.uml.Element;
import org.umlgraph.engine.classdiagram.dot.IndentedPrintWriter;

/**
 * The representation for rendering sessions.
 * 
 *  A rendering session encapsulates  
 */
public class RenderingSession {
	private ElementRendererSelector selector = new ElementRendererSelector();
	private IndentedPrintWriter writer;

	public RenderingSession(IndentedPrintWriter writer) {
		this.writer = writer;
	}
	
	public IndentedPrintWriter getOutput() {
	    return writer;
	}

	/**
	 * Convenience method that renders a collection of elements.
	 */
	public void render(Collection<? extends Element> toRender) {
		for (Element element : toRender)
			render(element);
	}

	/**
	 * Renders an element (and possibly its children).
	 * 
	 * @param toRender element to render
	 */
	@SuppressWarnings("unchecked")
    public <C extends Element> void render(C toRender) {
		ElementRenderer<C> renderer = (ElementRenderer<C>) selector.select(toRender);
		if (renderer != null)
			renderer.renderObject(toRender, this);
	}
}
