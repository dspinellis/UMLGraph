package org.umlgraph.engine.classdiagram.dot;

import org.eclipse.uml2.uml.Property;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

public class PropertyRenderer implements ElementRenderer<Property> {
	public void renderObject(Property property, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.print(UMLRenderingUtils.renderVisibility(property.getVisibility()));
		w.print(property.getName());
		if (property.getType() != null) {
			w.print(" : ");
			w.print(property.getType().getName());
			w.print(UMLRenderingUtils.renderMultiplicity(property, true));
		}
		w.println("\\l\\");
	}
}
