/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2010 Diomidis Spinellis
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.umlgraph.doclet.util.ElementUtil;
import org.umlgraph.doclet.util.TagUtil;

/**
 * Doclet API implementation
 * 
 * @depend - - - OptionProvider
 * @depend - - - Options
 * @depend - - - View
 * @depend - - - ClassGraph
 * @depend - - - Version
 *
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
public class UmlGraph implements Doclet {

    private static final String programName = "UmlGraph";
    private static final String docletName = "org.umlgraph.doclet.UmlGraph";

    /** Options used for commenting nodes */
    private static Options commentOptions;


    private Locale locale;
    private Reporter reporter;
    private Options options;
    private StandardDoclet standard;
    
    public UmlGraph() {
        this.options = new Options();
        this.standard = new StandardDoclet();
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return docletName;
    }

    @Override
    public Set<? extends Doclet.Option> getSupportedOptions() {
        Set<Doclet.Option> options = new HashSet<>(standard.getSupportedOptions());
        for (Doclet.Option opt : this.options.OPTIONS) {
            options.add(opt);
        }
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return standard.getSupportedSourceVersion();
    }

    /**
     * Standard doclet entry point
     * 
     * @param root
     * @return
     */
    @Override
    /** Entry point through javadoc */
    public boolean run(DocletEnvironment root) {
        reporter.print(Diagnostic.Kind.NOTE, "UMLGraph doclet version " + Version.VERSION + " started");

        List<View> views = buildViews(options, root, root);
        if (views == null) {
            return false;
        }
        try {
            if (views.isEmpty()) {
                buildGraph(reporter, root, options, null, null);
            } else {
                for (View view : views) {
                    buildGraph(reporter, root, options, view, null);
                }
            }
        } catch (IOException ioe) {
            reporter.print(Diagnostic.Kind.ERROR, "UMLGraph doclet error : " + ioe);
        }
        return true;
    }

    public static Options getCommentOptions() {
        return commentOptions;
    }

    /**
     * Creates the base Options object. This contains both the options specified on
     * the command line and the ones specified in the UMLOptions class, if
     * available. Also create the globally accessible commentOptions object.
     */
    public static Options buildOptions(DocletEnvironment root, Options o) {
        commentOptions = o.clone();
        commentOptions.setOptions(root.getDocTrees(), findClass(root, "UMLNoteOptions", false));
        commentOptions.shape = Shape.NOTE;

        Options opt = o.clone();
        opt.setOptions(root.getDocTrees(), findClass(root, "UMLOptions", false));
        return opt;
    }

    /** Return the ClassDoc for the specified class; null if not found. */
    private static TypeElement findClass(DocletEnvironment root, String name, boolean qualified) {
        Set<? extends Element> classes = root.getIncludedElements();
        for (Element element : classes) {
            if (qualified && element instanceof TypeElement && ((TypeElement) element).getQualifiedName().toString().equals(name)) {
                return (TypeElement) element;
            } else if (!qualified && element instanceof TypeElement && element.getSimpleName().toString().equals(name)) {
                return (TypeElement) element;
            }
        }
        return null;
    }

    /**
     * Builds and outputs a single graph according to the view overrides
     */
    public static void buildGraph(Reporter reporter, DocletEnvironment root, Options options, OptionProvider op, Element contextDoc) throws IOException {
        if (getCommentOptions() == null) {
            buildOptions(root, options);
        }
        Options opt = op.getGlobalOptions();
        reporter.print(Diagnostic.Kind.NOTE, "Building " + op.getDisplayName());
        Set<? extends Element> elements = root.getIncludedElements();
        Set<TypeElement> classes = new HashSet<>();
        for (Element element : elements) {
            if (element instanceof TypeElement) {
                classes.add((TypeElement) element);
            }
        }

        ClassGraph c = new ClassGraph(root, op, contextDoc);
        c.prologue();
        for (TypeElement cd : classes) {
            c.printClass(cd, true);
        }
        for (TypeElement cd : classes) {
            c.printRelations(cd);
        }
        if (opt.inferRelationships) {
            for (TypeElement cd : classes) {
                c.printInferredRelations(cd);
            }
        }
        if (opt.inferDependencies) {
            for (TypeElement cd : classes) {
                c.printInferredDependencies(cd);
            }
        }

        c.printExtraClasses(root);
        c.epilogue();
    }

    /**
     * Builds the views according to the parameters on the command line
     * 
     * @param opt         The options
     * @param srcRootDoc  The RootDoc for the source classes
     * @param viewRootDoc The RootDoc for the view classes (may be different, or may
     *                    be the same as the srcRootDoc)
     */
    public static List<View> buildViews(Options opt, DocletEnvironment srcRootDoc, DocletEnvironment viewRootDoc) {
        if (opt.viewName != null) {
            TypeElement viewClass = findClass(viewRootDoc, opt.viewName, true);
            if (viewClass == null) {
                System.out.println("View " + opt.viewName + " not found! Exiting without generating any output.");
                return null;
            }
            if (TagUtil.getTag(viewRootDoc, viewClass, "view").isEmpty()) {
                System.out.println(viewClass + " is not a view!");
                return null;
            }
            if (viewClass.getModifiers().contains(Modifier.ABSTRACT)) {
                System.out.println(viewClass + " is an abstract view, no output will be generated!");
                return null;
            }
            return List.of(buildView(srcRootDoc, viewClass, opt));
        } else if (opt.findViews) {
            List<View> views = new ArrayList<>();
            Set<? extends Element> classes = viewRootDoc.getIncludedElements();

            // find view classes
            for (Element elmt : classes) {
                if (!(elmt instanceof TypeElement)) {
                    continue;
                }
                TypeElement element = (TypeElement) elmt;
                if (TagUtil.getTag(viewRootDoc, element, "view").size() > 0 && !element.getModifiers().contains(Modifier.ABSTRACT)) {
                    views.add(buildView(srcRootDoc, element, opt));
                }
            }

            return views;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Builds a view along with its parent views, recursively
     */
    private static View buildView(DocletEnvironment root, TypeElement viewClass, OptionProvider provider) {
        TypeElement superClass = ElementUtil.getSuperclass(viewClass);
        if (superClass == null || TagUtil.getTag(root, superClass, "view").isEmpty()) {
            return new View(root, viewClass, provider);
        }

        return new View(root, viewClass, buildView(root, superClass, provider));
    }
}
