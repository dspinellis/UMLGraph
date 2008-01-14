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
