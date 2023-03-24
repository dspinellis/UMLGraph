package org.umlgraph.doclet.util;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import jdk.javadoc.doclet.DocletEnvironment;

public class ElementUtil {
    
    public static CharSequence getSimpleName(Types types, TypeMirror t) {
        if (t instanceof ArrayType) {
            return getSimpleName(types, ((ArrayType) t).getComponentType()) + "[]";
        }
        if (t.getKind().isPrimitive()) {
            return t.getKind().name().toLowerCase();
        }
        Element element = types.asElement(t);
        if (element != null) {
            return element.getSimpleName();
        }
        return "";
    }
    
    public static CharSequence getQualifiedName(Types types, TypeMirror t) {
        if (t instanceof ArrayType) {
            return getQualifiedName(types, ((ArrayType) t).getComponentType()) + "[]";
        }
        if (t.getKind().isPrimitive()) {
            return t.getKind().name().toLowerCase();
        }
        Element element = types.asElement(t);
        if (element instanceof TypeElement) {
            return ((TypeElement) element).getQualifiedName();
        }
        return "";
    }

    public static TypeElement containingTypeElement(Element c) {
        if (c instanceof VariableElement) {
            Element enclosing = c.getEnclosingElement();
            return enclosing instanceof TypeElement ? (TypeElement) enclosing : null;
        } else if (c instanceof ExecutableElement) {
            Element enclosing = c.getEnclosingElement();
            return enclosing instanceof TypeElement ? (TypeElement) enclosing : null;
        }
        if (c == null) {
            System.err.println("containingElement will return null for null element");
            return null;
        }
        System.err.println("containingElement will return null for kind " + c.getKind() + " class " + c.getClass() + " name " + c.getSimpleName());
        return null;
    }

    public static TypeElement getTypeElement(TypeMirror clazz) {
        if (clazz == null || clazz.getKind() == TypeKind.NONE) {
            return null;
        }
        if (clazz instanceof ArrayType) {
            return getTypeElement(((ArrayType) clazz).getComponentType());
        }
        if (!(clazz instanceof DeclaredType)) {
            return null;
        }
        Element scd = ((DeclaredType) clazz).asElement();
        return scd instanceof TypeElement ? (TypeElement) scd : null;
    }

    public static TypeMirror getSuperclassType(DeclaredType pt) {
        return pt.asElement() instanceof TypeElement ? ((TypeElement) pt.asElement()).getSuperclass() : null;
    }
    
    public static TypeElement getSuperclass(TypeElement element) {
        return getTypeElement(element.getSuperclass());
    }
    
    public static List<? extends TypeMirror> getInterfacesTypes(TypeElement element) {
        return element.getInterfaces();
    }

    public static List<TypeElement> getInterfaces(TypeElement element) {
        List<? extends TypeMirror> interfaces = getInterfacesTypes(element);
        return interfaces.stream().map(i -> getTypeElement(i)).filter(i -> i != null).collect(Collectors.toList());
    }
    
    public static ModuleElement getModuleOf(DocletEnvironment root, Element element) {
        return getModuleOf(root.getElementUtils(), element);
    }
    
    public static ModuleElement getModuleOf(Elements elements, Element element) {
        if (element == null) {
            return null;
        }
        if (element instanceof ModuleElement) {
            return (ModuleElement) element;
        }
        return elements.getModuleOf(element);
    }
    
    public static PackageElement getPackageOf(DocletEnvironment root, Element element) {
        return getPackageOf(root.getElementUtils(), element);
    }
    
    public static PackageElement getPackageOf(Elements elements, Element element) {
        if (element == null) {
            return null;
        }
        if (element instanceof PackageElement) {
            return (PackageElement) element;
        }
        return elements.getPackageOf(element);
    }
    
    public static List<VariableElement> getFields(TypeElement element) {
        List<? extends Element> enclosed = element.getEnclosedElements();
        return ElementFilter.fieldsIn(enclosed).stream().filter(v -> v.getKind() == ElementKind.FIELD).collect(Collectors.toList());
    }
    
    public static List<VariableElement> getEnumConstants(TypeElement element) {
        List<? extends Element> enclosed = element.getEnclosedElements();
        return ElementFilter.fieldsIn(enclosed).stream().filter(v -> v.getKind() == ElementKind.ENUM_CONSTANT).collect(Collectors.toList());
    }
    
    public static List<ExecutableElement> getMethods(TypeElement element) {
        List<? extends Element> enclosed = element.getEnclosedElements();
        return ElementFilter.methodsIn(enclosed);
    }
    
    public static List<ExecutableElement> getConstructors(TypeElement element) {
        List<? extends Element> enclosed = element.getEnclosedElements();
        return ElementFilter.constructorsIn(enclosed);
    }
    
    public static String dimensions(TypeMirror type) {
        StringBuilder sb = new StringBuilder();

        // First append root component type
        TypeMirror t = type;
        while (t.getKind() == TypeKind.ARRAY) {
            sb.append("[]");
            t = ((ArrayType) t).getComponentType();
        }
        return sb.toString();
    }
}
