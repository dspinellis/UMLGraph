/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
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
