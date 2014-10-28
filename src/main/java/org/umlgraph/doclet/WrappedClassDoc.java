/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2005 Diomidis Spinellis
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
 *
 */

package org.umlgraph.doclet;

import com.sun.javadoc.AnnotatedType;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.javadoc.WildcardType;

/**
 * A ClassDoc wrapper that caches answer to the most common requests performed
 * by UMLGraph, considerably improving the overall UMLDoc performance (ClassDoc
 * computes most of the results for more fine grained information at each call).
 * <p>
 * Unfortunately this has a side effect, since it breaks the equals() call between
 * plain ClassDoc instances and WrappedClassDoc ones, so use it with due care.
 * <p>
 * In particular, don't provide WrappedClassDoc instances to the standard doclet.
 * @author wolf
 * 
 */
public class WrappedClassDoc implements ClassDoc {
    ClassDoc wrapped;
    String toString;
    String name;
    Tag[] tags;

    public WrappedClassDoc(ClassDoc wrapped) {
	this.wrapped = wrapped;
    }

    public AnnotationDesc[] annotations() {
	return wrapped.annotations();
    }

    public AnnotationTypeDoc asAnnotationTypeDoc() {
	return wrapped.asAnnotationTypeDoc();
    }

    public ClassDoc asClassDoc() {
	return wrapped.asClassDoc();
    }

    public ParameterizedType asParameterizedType() {
	return wrapped.asParameterizedType();
    }

    public TypeVariable asTypeVariable() {
	return wrapped.asTypeVariable();
    }

    public WildcardType asWildcardType() {
	return wrapped.asWildcardType();
    }

    public String commentText() {
	return wrapped.commentText();
    }

    public int compareTo(Object arg0) {
	if (arg0 instanceof WrappedClassDoc) {
	    WrappedClassDoc other = (WrappedClassDoc) arg0;
	    return wrapped.compareTo(other.wrapped);
	}
	return wrapped.compareTo(arg0);
    }

    public ConstructorDoc[] constructors() {
	return wrapped.constructors();
    }

    public ConstructorDoc[] constructors(boolean arg0) {
	return wrapped.constructors(arg0);
    }

    public ClassDoc containingClass() {
	return wrapped.containingClass();
    }

    public PackageDoc containingPackage() {
	return wrapped.containingPackage();
    }

    public boolean definesSerializableFields() {
	return wrapped.definesSerializableFields();
    }

    public String dimension() {
	return wrapped.dimension();
    }

    public FieldDoc[] enumConstants() {
	return wrapped.enumConstants();
    }

    public FieldDoc[] fields() {
	return wrapped.fields();
    }

    public FieldDoc[] fields(boolean arg0) {
	return wrapped.fields(arg0);
    }

    public ClassDoc findClass(String arg0) {
	return wrapped.findClass(arg0);
    }

    public Tag[] firstSentenceTags() {
	return wrapped.firstSentenceTags();
    }

    public String getRawCommentText() {
	return wrapped.getRawCommentText();
    }

    /** @deprecated */
    public @Deprecated ClassDoc[] importedClasses() {
	return wrapped.importedClasses();
    }

    /** @deprecated */
    public @Deprecated PackageDoc[] importedPackages() {
	return wrapped.importedPackages();
    }

    public Tag[] inlineTags() {
	return wrapped.inlineTags();
    }

    public ClassDoc[] innerClasses() {
	return wrapped.innerClasses();
    }

    public ClassDoc[] innerClasses(boolean arg0) {
	return wrapped.innerClasses(arg0);
    }

    public ClassDoc[] interfaces() {
	return wrapped.interfaces();
    }

    public Type[] interfaceTypes() {
	return wrapped.interfaceTypes();
    }

    public boolean isAbstract() {
	return wrapped.isAbstract();
    }

    public boolean isAnnotationType() {
	return wrapped.isAnnotationType();
    }

    public boolean isAnnotationTypeElement() {
	return wrapped.isAnnotationTypeElement();
    }

    public boolean isClass() {
	return wrapped.isClass();
    }

    public boolean isConstructor() {
	return wrapped.isConstructor();
    }

    public boolean isEnum() {
	return wrapped.isEnum();
    }

    public boolean isEnumConstant() {
	return wrapped.isEnumConstant();
    }

    public boolean isError() {
	return wrapped.isError();
    }

    public boolean isException() {
	return wrapped.isException();
    }

    public boolean isExternalizable() {
	return wrapped.isExternalizable();
    }

    public boolean isField() {
	return wrapped.isField();
    }

    public boolean isFinal() {
	return wrapped.isFinal();
    }

    public boolean isIncluded() {
	return wrapped.isIncluded();
    }

    public boolean isInterface() {
	return wrapped.isInterface();
    }

    public boolean isMethod() {
	return wrapped.isMethod();
    }

    public boolean isOrdinaryClass() {
	return wrapped.isOrdinaryClass();
    }

    public boolean isPackagePrivate() {
	return wrapped.isPackagePrivate();
    }

    public boolean isPrimitive() {
	return wrapped.isPrimitive();
    }

    public boolean isPrivate() {
	return wrapped.isPrivate();
    }

    public boolean isProtected() {
	return wrapped.isProtected();
    }

    public boolean isPublic() {
	return wrapped.isPublic();
    }

    public boolean isSerializable() {
	return wrapped.isSerializable();
    }

    public boolean isStatic() {
	return wrapped.isStatic();
    }

    public MethodDoc[] methods() {
	return wrapped.methods();
    }

    public MethodDoc[] methods(boolean arg0) {
	return wrapped.methods(arg0);
    }

    public String modifiers() {
	return wrapped.modifiers();
    }

    public int modifierSpecifier() {
	return wrapped.modifierSpecifier();
    }

    public String name() {
	if (name == null)
	    name = wrapped.name();
	return name;
    }

    public SourcePosition position() {
	return wrapped.position();
    }

    public String qualifiedName() {
	return wrapped.qualifiedName();
    }

    public String qualifiedTypeName() {
	return wrapped.qualifiedTypeName();
    }

    public SeeTag[] seeTags() {
	return wrapped.seeTags();
    }

    public FieldDoc[] serializableFields() {
	return wrapped.serializableFields();
    }

    public MethodDoc[] serializationMethods() {
	return wrapped.serializationMethods();
    }

    public void setRawCommentText(String arg0) {
	wrapped.setRawCommentText(arg0);
    }

    public String simpleTypeName() {
	return wrapped.simpleTypeName();
    }

    public boolean subclassOf(ClassDoc arg0) {
	return wrapped.subclassOf(arg0);
    }

    public ClassDoc superclass() {
	return wrapped.superclass();
    }

    public Type superclassType() {
	return wrapped.superclassType();
    }

    public Tag[] tags() {
	if (tags == null)
	    tags = wrapped.tags();
	return tags;
    }

    public Tag[] tags(String arg0) {
	return wrapped.tags(arg0);
    }

    public String toString() {
	if (toString == null) {
	    toString = wrapped.toString();
	}
	return toString;
    }

    public String typeName() {
	return wrapped.typeName();
    }

    public TypeVariable[] typeParameters() {
	return wrapped.typeParameters();
    }

    public ParamTag[] typeParamTags() {
	return wrapped.typeParamTags();
    }

    public AnnotatedType asAnnotatedType() {
	return wrapped.asAnnotatedType();
    }

    public Type getElementType() {
	return wrapped.getElementType();
    }
}
