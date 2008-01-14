/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package org.umlgraph.engine.classdiagram.dot;

import org.eclipse.uml2.uml.Generalization;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;

/**
 * 
 */
public class GeneralizationRenderer implements ElementRenderer<Generalization> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.modelviewer.render.IRenderer#renderObject(java.lang.Object,
	 *      com.abstratt.modelviewer.IndentedPrintWriter,
	 *      com.abstratt.modelviewer.render.IRenderingSession)
	 */
	public void renderObject(Generalization element, RenderingSession context) {
		if (element.getGeneral().getNearestPackage() != element.getSpecific().getNearestPackage())
			return;
		IndentedPrintWriter pw = context.getOutput(); 
		pw.print("edge ");
		// if (element.getName() != null)
		// pw.print("\"" + element.getName() + "\" ");
		pw.println("[");
		pw.enterLevel();
		pw.println("arrowtail = \"empty\"");
		pw.println("arrowhead = \"none\"");
		pw.println("taillabel = \"\"");
		pw.println("headlabel = \"\"");
		DOTRenderingUtils.addAttribute(pw, "constraint", "true");
		pw.println("style = \"none\"");
		pw.exitLevel();
		pw.println("]");
		pw.println(element.getGeneral().getName() + " -- " + element.getSpecific().getName());
	}

}
