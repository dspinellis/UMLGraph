package org.umlgraph.engine.classdiagram.dot;

import org.eclipse.uml2.uml.Extension;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;

public class ExtensionRenderer implements ElementRenderer<Extension> {

	public void renderObject(Extension element, RenderingSession context) {
	    IndentedPrintWriter pw = context.getOutput(); 
		pw.print("edge ");
		// if (element.getName() != null)
		// pw.print("\"" + element.getName() + "\" ");
		pw.println("[");
		pw.enterLevel();
		pw.println("arrowtail = \"none\"");
		pw.println("arrowhead = \"normal\"");
		pw.println("taillabel = \"\"");
		pw.println("headlabel = \"\"");
		DOTRenderingUtils.addAttribute(pw, "constraint", "true");
		pw.println("style = \"none\"");
		pw.exitLevel();
		pw.println("]");
		pw.println(element.getStereotype().getName() + " -- " + element.getMetaclass().getName());

	}

}
