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

import org.eclipse.uml2.uml.Package;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;

/**
 * 
 */
public class PackageRenderer implements ElementRenderer<Package> {
	public void renderObject(Package allPackage, RenderingSession context) {
		context.render(allPackage.getOwnedElements());
	}
}
