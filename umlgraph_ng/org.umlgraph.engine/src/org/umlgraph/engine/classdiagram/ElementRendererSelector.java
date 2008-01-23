/*
 * (C) Copyright 2008 Abstratt Technologies
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
package org.umlgraph.engine.classdiagram;

import java.lang.reflect.Modifier;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;
import org.umlgraph.engine.classdiagram.dot.ClassDiagramRenderer;

class ElementRendererSelector {

    private static final EClass ELEMENT_CLASS = UMLPackage.eINSTANCE
            .getElement();

    private Class<? extends ElementRenderer<?>> findRenderer(EClass elementClass) {
        if (!ELEMENT_CLASS.isSuperTypeOf(elementClass))
            return null;
        String className = elementClass.getName();
        String rendererClassName = ClassDiagramRenderer.class.getPackage().getName() + '.'
                + className + "Renderer";
        try {
            return (Class<? extends ElementRenderer<?>>) Class
                    .forName(rendererClassName);
        } catch (ClassNotFoundException e) {
            // try parent
            EList<EClass> superTypes = elementClass.getESuperTypes();
            for (EClass superType : superTypes) {
                Class<? extends ElementRenderer<?>> renderer = findRenderer(superType);
                if (renderer != null
                        && !Modifier.isAbstract(renderer.getModifiers()))
                    return renderer;
            }
            return null;
        }
    }

    public ElementRenderer<?> select(Element element) {
        Class<?> rendererClass = findRenderer(element.eClass());
        if (rendererClass == null)
            return null;
        try {
            return (ElementRenderer<?>) rendererClass.newInstance();
        } catch (InstantiationException e) {
            UMLRenderingUtils.logUnexpected(rendererClass.getName(), e);
        } catch (IllegalAccessException e) {
            UMLRenderingUtils.logUnexpected(rendererClass.getName(), e);
        }
        return null;
    }
}
