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
	    //TODO ClassInfo
		if (element.getGeneral().getNearestPackage() != element.getSpecific().getNearestPackage())
			return;
		IndentedPrintWriter pw = context.getOutput();
		pw.println("//" + element.getSpecific().getName() + " extends " + element.getGeneral().getName());
		pw.println(element.getGeneral().getName() + " -> " + element.getSpecific().getName() + "[dir=back,arrowtail=empty]");
	}

}
