package org.umlgraph.engine.classdiagram.dot;

import org.eclipse.uml2.uml.EnumerationLiteral;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;

public class EnumerationLiteralRenderer implements ElementRenderer<EnumerationLiteral> {

	public void renderObject(EnumerationLiteral literal, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.print(literal.getName());
		w.println("\\l\\");
	}

}
