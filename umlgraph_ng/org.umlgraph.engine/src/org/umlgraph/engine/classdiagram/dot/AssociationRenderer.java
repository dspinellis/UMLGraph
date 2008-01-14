/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package org.umlgraph.engine.classdiagram.dot;

import java.util.List;

import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;
import org.umlgraph.engine.classdiagram.UMLRenderingUtils;

public class AssociationRenderer implements ElementRenderer<Association> {

	public void renderObject(Association element, RenderingSession context) {
		if (!element.isBinary())
			// we humbly admit we can't handle n-ary associations
			return;
		IndentedPrintWriter pw = context.getOutput(); 
		List<Property> ends = element.getMemberEnds();
		Property source = ends.get(0);
		Property target = ends.get(1);
		if (!ends.get(0).isNavigable() || ends.get(1).getAggregation() != AggregationKind.NONE_LITERAL) {
			source = ends.get(1);
			target = ends.get(0);
		}
		Type targetType = target.getType();
		Type sourceType = source.getType();
		if (targetType == null || sourceType == null)
			return;
		pw.print("edge ");
		pw.println("[");
		pw.enterLevel();
		pw.println("style = \"none\"");
		String style;
		boolean constraint = true;
		switch (source.getAggregation()) {
		case COMPOSITE_LITERAL:
			style = "diamond";
			break;
		case SHARED_LITERAL:
			style = "ediamond";
			break;
		default:
			constraint = false;
			style = (target.isNavigable() || !source.isNavigable()) ? "none" : "open";
		}
		DOTRenderingUtils.addAttribute(pw, "labelangle", "-12.5");
		DOTRenderingUtils.addAttribute(pw, "arrowtail", style);
		DOTRenderingUtils.addAttribute(pw, "arrowhead", "none");
		DOTRenderingUtils.addAttribute(pw, "taillabel", source.getName() == null ? "" : source.getName()
						+ UMLRenderingUtils.renderMultiplicity(source, false));
		DOTRenderingUtils.addAttribute(pw, "headlabel", target.getName() == null ? "" : target.getName()
						+ UMLRenderingUtils.renderMultiplicity(target, false));
		// DOTRenderingUtils.addAttribute(pw, "labeldistance", "1.5");
		DOTRenderingUtils.addAttribute(pw, "labelangle", -30);
		DOTRenderingUtils.addAttribute(pw, "len", "1.5");
		DOTRenderingUtils.addAttribute(pw, "constraint", Boolean.toString(constraint));
		pw.exitLevel();
		pw.println("]");
		pw.print(targetType.getName() + " -- " + sourceType.getName() + " ");

	}
}
