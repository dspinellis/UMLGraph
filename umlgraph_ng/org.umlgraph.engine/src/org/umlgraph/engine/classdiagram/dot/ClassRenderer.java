package org.umlgraph.engine.classdiagram.dot;

import java.util.List;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Stereotype;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

public class ClassRenderer implements ElementRenderer<Class> {

	public void renderObject(Class element, RenderingSession context) {
	    IndentedPrintWriter w = context.getOutput(); 
		w.println("// class " + element.getQualifiedName());
		w.println('"' + element.getName() + "\" [");
		w.enterLevel();
		DOTRenderingUtils.addAttribute(w, "nojustify", "true");
		w.print("label=\"{");
		if (!element.getAppliedStereotypes().isEmpty())
			for (Stereotype current : element.getAppliedStereotypes()) {
				w.print(UMLRenderingUtils.addGuillemots(current.getName()));
				DOTRenderingUtils.newLine(w);
			}
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
		List<InterfaceRealization> realizations = element.getInterfaceRealizations();
		context.render(realizations);

	}
}
