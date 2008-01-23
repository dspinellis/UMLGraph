/*
 * (C) Copyright 2007-2008 Abstratt Technologies
 *
 * Permission to use, copy, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * $Id$
 *
 */
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
