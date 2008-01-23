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
