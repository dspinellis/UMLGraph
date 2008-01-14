/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package org.umlgraph.engine.classdiagram.dot;

import java.util.List;

import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

/**
 * 
 */
public class OperationRenderer implements ElementRenderer<Operation> {
	public void renderObject(Operation operation, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.print(UMLRenderingUtils.renderVisibility(operation.getVisibility()));
		w.print(operation.getName());
		w.print("(");
		List<Parameter> parameters = operation.getOwnedParameters();
		Parameter returnParameter = null;
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			if (parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL)
				returnParameter = parameter;
			else {
				if (i > 0)
					w.print(", ");
				context.render(parameter);
			}
		}
		w.print(")");
		if (returnParameter != null) {
			w.print(" : ");
			Type returnType = returnParameter.getType();
			w.print(returnType != null ? returnType.getName() : "null");
		}
		w.println("\\l\\");
	}
}
