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

import org.eclipse.uml2.uml.Dependency;
import org.umlgraph.engine.classdiagram.ElementRenderer;
import org.umlgraph.engine.classdiagram.RenderingSession;

public class DependencyRenderer implements ElementRenderer<Dependency> {

    public void renderObject(Dependency element, RenderingSession context) {
        //TODO ClassInfo
        //TODO we do no support multiple clients/suppliers at this point 
        if (element.getSuppliers().get(0).getNearestPackage() != element.getClients().get(0).getNearestPackage())
            return;
        IndentedPrintWriter pw = context.getOutput();
        pw.println("//" + element.getClients().get(0).getName() + " depends upon " + element.getSuppliers().get(0).getName());
        pw.println(element.getSuppliers().get(0).getName() + " -> " + element.getClients().get(0).getName() + "[arrowhead=open, style=dashed]");
    }

}
