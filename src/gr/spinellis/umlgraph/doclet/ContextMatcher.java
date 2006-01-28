/*
 * Contibuted by Andrea Aime
 * (C) Copyright 2005 Diomidis Spinellis
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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Matches classes that are directly connected to one of the classes matched by
 * the regual expression specified. The context center is computed by regex lookup.
 * Depending on the specified Options, inferred relations and dependencies will
 * be used as well.
 * @author wolf
 */
public class ContextMatcher implements ClassMatcher {
    ClassGraphHack cg;
    Pattern pattern;
    ArrayList<ClassDoc> matched;
    Options opt;

    /**
     * Builds the context matcher
     * @param root The root doc returned by JavaDoc
     * @param pattern The pattern that will match the "center" of this
     *                context
     * @param opt The options will be used to decide on inference
     * @throws IOException
     */
    public ContextMatcher(RootDoc root, Pattern pattern, Options options) throws IOException {
	this.pattern = pattern;
	opt = (Options) options.clone();
	opt.setOption(new String[] {"-!hide"});
	opt.setOption(new String[] {"-!attributes"});
	opt.setOption(new String[] {"-!operations"});
	this.cg = new ClassGraphHack(root, opt);

	// build up the classgraph printing the relations for all of the
	// classes that make up the "center" of this context
	matched = new ArrayList<ClassDoc>();
	for (ClassDoc cd : root.classes()) {
	    if (pattern.matcher(cd.toString()).matches()) {
		matched.add(cd);
		addToGraph(cd);
	    }
	}
    }

    /**
     * Adds the specified class to the internal class graph along with its
     * relations and depencies, eventually inferring them, according to the
     * Options specified for this matcher
     * @param cd
     */
    private void addToGraph(ClassDoc cd) {
	cg.printClass(cd);
	cg.printRelations(cd);
	if (opt.inferRelationships) {
	    cg.printInferredRelations(cd);
	}
	if (opt.inferDependencies) {
	    cg.printInferredDependencies(cd);
	}
    }

    /**
     * @see gr.spinellis.umlgraph.doclet.ClassMatcher#matches(com.sun.javadoc.ClassDoc)
     */
    public boolean matches(ClassDoc cd) {
	// if the class is matched, it's in by default.
	if (matched.contains(cd))
	    return true;

	// otherwise, add the class to the graph and see if it's associated
	// with any of the matched classes using the classgraph hack
	addToGraph(cd);
	return matches(cd.toString());
    }

    /**
     * @see gr.spinellis.umlgraph.doclet.ClassMatcher#matches(java.lang.String)
     */
    public boolean matches(String name) {
	if (pattern.matcher(name).matches())
	    return true;

	for (ClassDoc mcd : matched) {
	    String mcName = mcd.toString();
	    ClassInfo ciMatched = cg.getClassInfo(mcName);
	    if (ciMatched != null && ciMatched.isRelated(name))
		return true;
	    ClassInfo ci = cg.getClassInfo(name);
	    if (ci != null && ci.isRelated(mcName))
		return true;
	}
	return false;
    }

    /**
     * A quick hack to compute class dependencies reusing ClassGraph but
     * without generating output. Will be removed once the ClassGraph class
     * will be split into two classes for graph computation and output
     * generation.
     * @author wolf
     * 
     */
    private static class ClassGraphHack extends ClassGraph {

	public ClassGraphHack(RootDoc root, OptionProvider optionProvider) throws IOException {
	    super(root, optionProvider);
	    prologue();
	}

	public void prologue() throws IOException {
	    w = new PrintWriter(new DevNullWriter());
	}

    }

    /**
     * Simple dev/null imitation
     * @author wolf
     */
    private static class DevNullWriter extends Writer {

	public void write(char[] cbuf, int off, int len) throws IOException {
	    // nothing to do
	}

	public void flush() throws IOException {
	    // nothing to do
	}

	public void close() throws IOException {
	    // nothing to do
	}

    }

}
