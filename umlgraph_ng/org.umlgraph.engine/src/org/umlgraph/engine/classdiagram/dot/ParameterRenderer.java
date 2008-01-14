package org.umlgraph.engine.classdiagram.dot;

import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Type;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

public class ParameterRenderer implements ElementRenderer<Parameter> {

	public void renderObject(Parameter parameter, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.print(parameter.getName());
		w.print(" : ");
		Type paramType = parameter.getType();
		if (paramType == null)
			return;
		w.print(paramType.getName());
		w.print(UMLRenderingUtils.renderMultiplicity(parameter, true));
	}
}
