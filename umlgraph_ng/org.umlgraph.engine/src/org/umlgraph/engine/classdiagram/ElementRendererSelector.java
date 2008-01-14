package org.umlgraph.engine.classdiagram;

import java.lang.reflect.Modifier;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;

class ElementRendererSelector {

	private static final EClass ELEMENT_CLASS = UMLPackage.eINSTANCE.getElement();

	private Class<? extends ElementRenderer<?>> findRenderer(EClass elementClass) {
		if (!ELEMENT_CLASS.isSuperTypeOf(elementClass))
			return null;
		String className = elementClass.getName();
		String rendererClassName = getClass().getPackage().getName() + '.' + className + "Renderer";
		try {
			return (Class<? extends ElementRenderer<?>>) Class.forName(rendererClassName);
		} catch (ClassNotFoundException e) {
			// try parent
			EList<EClass> superTypes = elementClass.getESuperTypes();
			for (EClass superType : superTypes) {
				Class<? extends ElementRenderer<?>> renderer = findRenderer(superType);
				if (renderer != null && !Modifier.isAbstract(renderer.getModifiers()))
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
