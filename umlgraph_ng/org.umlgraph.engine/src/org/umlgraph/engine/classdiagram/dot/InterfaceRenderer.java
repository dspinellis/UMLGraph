package org.umlgraph.engine.classdiagram.dot;

import java.util.List;

import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

public class InterfaceRenderer implements ElementRenderer<Interface> {

	public void renderObject(Interface element, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.println("// interface " + element.getQualifiedName());
		w.println('"' + element.getName() + "\" [");
		w.enterLevel();
		w.print("label=\"{");
		w.print(UMLRenderingUtils.addGuillemots("interface"));
		DOTRenderingUtils.newLine(w);
		w.print(element.getName());
		w.enterLevel();
		if (!element.getAttributes().isEmpty()) {
			w.println("|\\");
			context.render(element.getAttributes());
		}
		if (!element.getOperations().isEmpty()) {
			w.println("|\\");
			context.render(element.getOperations());
		}
		w.exitLevel();
		w.println("}\"");
		w.exitLevel();
		w.println("]");
		List<Generalization> generalizations = element.getGeneralizations();
		context.render(generalizations);
	}

}
