package org.umlgraph.engine;

import org.eclipse.emf.ecore.resource.ResourceSet;

public interface DiagramRenderer<D extends Diagram, O> {
    public void render(ResourceSet resourceSet, D diagram, O output);
}
