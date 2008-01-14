package org.umlgraph.engine.classdiagram.dot;

import org.eclipse.uml2.uml.InterfaceRealization;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;

public class InterfaceRealizationRenderer implements ElementRenderer<InterfaceRealization> {

	public void renderObject(InterfaceRealization element, RenderingSession context) {
		if (element.getImplementingClassifier().getNearestPackage() != element.getContract().getNearestPackage())
			return;
		IndentedPrintWriter pw = context.getOutput(); 
		pw.print("edge ");
		pw.println("[");
		pw.enterLevel();
		DOTRenderingUtils.addAttribute(pw, "arrowtail", "empty");
		DOTRenderingUtils.addAttribute(pw, "arrowhead", "none");
		DOTRenderingUtils.addAttribute(pw, "taillabel", "");
		DOTRenderingUtils.addAttribute(pw, "headlabel", "");
		DOTRenderingUtils.addAttribute(pw, "syle", "dashed");
		pw.exitLevel();
		pw.println("]");
		pw.println(element.getContract().getName() + " -> " + element.getImplementingClassifier().getName());
	}
}
