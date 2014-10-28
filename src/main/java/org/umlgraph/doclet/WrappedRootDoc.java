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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;

/**
 * RootDoc wrapper that provides WrappedClassDoc instances instead of plain ClassDoc in order
 * to optimize the overall performance of UMLDoc.
 * @author wolf
 */
public class WrappedRootDoc implements RootDoc {
    RootDoc wrapped;
    WrappedClassDoc[] wrappedClassDocs;
    
    public WrappedRootDoc(RootDoc wrapped) {
	this.wrapped = wrapped;
	ClassDoc[] classes = wrapped.classes();
	wrappedClassDocs = new WrappedClassDoc[classes.length];
	for (int i = 0; i < classes.length; i++) {
	    wrappedClassDocs[i] = new WrappedClassDoc(classes[i]);
	}
    }

    public ClassDoc[] classes() {
	return wrappedClassDocs;
    }

    public ClassDoc classNamed(String arg0) {
	return wrapped.classNamed(arg0);
    }

    public String commentText() {
	return wrapped.commentText();
    }

    public int compareTo(Object arg0) {
	return wrapped.compareTo(arg0);
    }

    public Tag[] firstSentenceTags() {
	return wrapped.firstSentenceTags();
    }

    public String getRawCommentText() {
	return wrapped.getRawCommentText();
    }

    public Tag[] inlineTags() {
	return wrapped.inlineTags();
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

    public boolean isField() {
	return wrapped.isField();
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

    public String name() {
	return wrapped.name();
    }

    public String[][] options() {
	return wrapped.options();
    }

    public PackageDoc packageNamed(String arg0) {
	return wrapped.packageNamed(arg0);
    }

    public SourcePosition position() {
	return wrapped.position();
    }

    public void printError(SourcePosition arg0, String arg1) {
	wrapped.printError(arg0, arg1);
    }

    public void printError(String arg0) {
	wrapped.printError(arg0);
    }

    public void printNotice(SourcePosition arg0, String arg1) {
	wrapped.printNotice(arg0, arg1);
    }

    public void printNotice(String arg0) {
	wrapped.printNotice(arg0);
    }

    public void printWarning(SourcePosition arg0, String arg1) {
	wrapped.printWarning(arg0, arg1);
    }

    public void printWarning(String arg0) {
	wrapped.printWarning(arg0);
    }

    public SeeTag[] seeTags() {
	return wrapped.seeTags();
    }

    public void setRawCommentText(String arg0) {
	wrapped.setRawCommentText(arg0);
    }

    public ClassDoc[] specifiedClasses() {
	return wrapped.specifiedClasses();
    }

    public PackageDoc[] specifiedPackages() {
	return wrapped.specifiedPackages();
    }

    public Tag[] tags() {
	return wrapped.tags();
    }

    public Tag[] tags(String arg0) {
	return wrapped.tags(arg0);
    }

}
