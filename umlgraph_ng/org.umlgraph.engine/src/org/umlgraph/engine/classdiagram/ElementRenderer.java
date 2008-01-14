package org.umlgraph.engine.classdiagram;

import org.eclipse.uml2.uml.Element;


/**
 * A renderer knows how to render a specific type of object.
 * 
 * Clients to implement.
 */
public interface ElementRenderer<C extends Element> {
	public void renderObject(C element, RenderingSession context);
}
