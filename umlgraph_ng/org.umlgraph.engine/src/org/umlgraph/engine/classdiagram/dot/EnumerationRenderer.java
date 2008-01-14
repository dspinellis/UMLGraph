package org.umlgraph.engine.classdiagram.dot;

import java.util.List;

import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Generalization;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

public class EnumerationRenderer implements ElementRenderer<Enumeration> {
	public void renderObject(Enumeration element, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.println("// enum " + element.getQualifiedName());
		w.println('"' + element.getName() + "\" [");
		w.enterLevel();
		w.print("label=\"{");
		w.print(UMLRenderingUtils.addGuillemots("enumeration"));
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
		if (!element.getOwnedLiterals().isEmpty()) {
			w.println("|\\");
			context.render(element.getOwnedLiterals());
		}
		w.exitLevel();
		w.println("}\"");
		w.exitLevel();
		w.println("]");
		List<Generalization> generalizations = element.getGeneralizations();
		context.render(generalizations);
	}

}
