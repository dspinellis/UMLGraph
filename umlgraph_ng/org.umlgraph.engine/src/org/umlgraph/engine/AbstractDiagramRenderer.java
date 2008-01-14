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
