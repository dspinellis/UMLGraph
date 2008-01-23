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

import java.io.OutputStream;
import java.util.Collection;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.umlgraph.engine.AbstractDiagramRenderer;
import org.umlgraph.engine.DOTRenderingUtils;
import org.umlgraph.engine.classdiagram.ClassDiagram;
import org.umlgraph.engine.classdiagram.RenderingSession;

/**
 * Knows how render a UML class diagram using Graphviz DOT language.
 */
public class ClassDiagramRenderer extends AbstractDiagramRenderer<ClassDiagram, OutputStream> {

    
    @Override
    public void render(ResourceSet resourceSet, ClassDiagram diagram,
            OutputStream diagramOutput) {
        
        Collection<NamedElement> rootNamespaces = UMLUtil.findNamedElements(
                resourceSet, diagram.getRootNamespace(), false,
                UMLPackage.Literals.PACKAGE);
        if (rootNamespaces.isEmpty())
            return;
        Package rootElement = (Package) rootNamespaces.iterator().next();
        IndentedPrintWriter out = new IndentedPrintWriter(diagramOutput);
        RenderingSession session = new RenderingSession(out);
        printPrologue(rootElement.getQualifiedName(), out);
        session.render(rootElement);
        printEpilogue(out);
        out.close();
    }
    
    private void printPrologue(String modelName, IndentedPrintWriter w) {
        w.println("digraph " + modelName + " {"); //$NON-NLS-1$ //$NON-NLS-2$
        w.enterLevel();
        DOTRenderingUtils.addAttribute(w, "ranksep", "0.5");
        DOTRenderingUtils.addAttribute(w, "nodesep", "0.85");
        DOTRenderingUtils.addAttribute(w, "nojustify", "true");
        DOTRenderingUtils.addAttribute(w, "splines", "polygonal");
        // TODO provide choice
        w.println("node [");
        w.enterLevel();
        DOTRenderingUtils.addAttribute(w, "fontsize", 12);
        DOTRenderingUtils.addAttribute(w, "shape", "record");
        w.exitLevel();
        w.println("]");
        w.println("edge [");
        w.enterLevel();
        DOTRenderingUtils.addAttribute(w, "fontsize", 10);
        // DOTRenderingUtils.addAttribute(w, "splines", "polyline");
        w.exitLevel();
        w.println("]");
    }
    
    private void printEpilogue(IndentedPrintWriter w) {
        w.exitLevel();
        w.println();
        w.println("}"); //$NON-NLS-1$
    }
}
