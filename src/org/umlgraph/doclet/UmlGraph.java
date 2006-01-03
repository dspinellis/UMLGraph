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
 * $Id$
 *
 */

package gr.spinellis.umlgraph.doclet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

/**
 * Doclet API implementation
 * @depend - - * OptionProvider
 * @depend - - * Options
 * @depend - - * View
 * @depend - - - ClassGraph
 * @depend - - - Version
 *
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
public class UmlGraph {
    /** Entry point */
    public static boolean start(RootDoc root) throws IOException {
	Options opt = new Options();
	opt.setOptions(root.options());
	opt.setOptions(findUMLOptions(root));

	View[] views = buildViews(opt, root);
	if(views == null)
	    return false;
	if (views.length == 0) {
	    buildGraph(root, opt);
	} else {
	    for (int i = 0; i < views.length; i++) {
		buildGraph(root, views[i]);
	    }
	}

	return true;

    }
    
    private static ClassDoc findUMLOptions(RootDoc root) {
	ClassDoc[] classes = root.classes();
	for (ClassDoc cd : classes) {
	    if(cd.name().equals("UMLOptions"))
		    return cd;
	}
	return null;
    }

    /**
     * Builds and outputs a single graph according to the view overrides
     */
    private static void buildGraph(RootDoc root, OptionProvider op) throws IOException {
	Options opt = op.getGlobalOptions();
	ClassDoc[] classes = root.classes();

	ClassGraph c = new ClassGraph(root, op);
	c.prologue();
	for (int i = 0; i < classes.length; i++) {
	    c.printClass(classes[i]);
	}
	for (int i = 0; i < classes.length; i++) {
	    c.printRelations(classes[i]);
	}
	if(opt.inferAssociations)
            c.printInferredRelations(classes);
        if(opt.inferDependencies)
            c.printInferredDependencies(classes);

	c.printExtraClasses(root);
	c.epilogue();
    }

    
    
    /**
     * Builds the views according to the parameters on the command line
     */
    private static View[] buildViews(Options opt, RootDoc root) {
	if (opt.viewName != null) {
	    ClassDoc viewClass = root.classNamed(opt.viewName);
	    if(viewClass == null) {
		System.out.println("View " + opt.viewName + " not found! Exiting without generating any output.");
		return null;
	    }
	    if(viewClass.tags("view").length == 0) {
		System.out.println(viewClass + " is not a view!");
		return null;
	    }
	    if(viewClass.isAbstract()) {
		System.out.println(viewClass + " is an abstract view, no output will be generated!");
		return null;
	    }
	    return new View[] { buildView(viewClass, opt) };
	} else if (opt.findViews) {
	    List<View> views = new ArrayList<View>();
	    ClassDoc[] classes = root.classes();
	    
	    // find view classes
	    for (int i = 0; i < classes.length; i++) {
		if (classes[i].tags("view").length > 0 && !classes[i].isAbstract()) {
		    views.add(buildView(classes[i], opt));
		}
	    }
	    
	    return views.toArray(new View[views.size()]);
	} else {
	    return new View[0];
	}
    }

    /**
     * Builds a view along with its parent views, recursively 
     */
    private static View buildView(ClassDoc viewClass, OptionProvider provider) {
	ClassDoc superClass = viewClass.superclass();
	if(superClass == null || superClass.tags("view").length == 0)
	    return new View(viewClass, provider);
	
	return new View(viewClass, buildView(superClass, provider));
    }

    /** Option checking */
    public static int optionLength(String option) {
	return Options.optionLength(option);
    }

    /** Indicate the language version we support */
    public static LanguageVersion languageVersion() {
	return LanguageVersion.JAVA_1_5;
    }

    
}
