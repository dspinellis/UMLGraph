/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002 Diomidis Spinellis
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

import com.sun.javadoc.*;
import java.io.*;
import java.lang.*;
import java.util.*;

class Options {
	PrintWriter w;
	boolean showQualified;
	boolean showAttributes;
	boolean showOperations;
	boolean showVisibility;
	boolean showType;

	Options() {
		showQualified = false;
		showAttributes = false;
		showOperations = false;
		showVisibility = false;
		showType = false;
	}

	public void setAll() {
		showAttributes = true;
		showOperations = true;
		showVisibility = true;
		showType = true;
	}

	public void openFile() throws IOException, UnsupportedEncodingException {
		FileOutputStream fos = new FileOutputStream("graph.dot");
		w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
	}
}

class ClassGraph {
	private ClassDoc c;
	private static HashMap classnames = new HashMap();
	private static int classnum;
	private Options opt;

	ClassGraph(Options iopt, ClassDoc ic) { 
		c = ic;
		opt = iopt;
	}

	private void visibility(ProgramElementDoc e) {
		if (!opt.showVisibility)
			return;
		if (e.isPrivate())
			opt.w.print('-');
		if (e.isPublic())
			opt.w.print('+');
		if (e.isProtected())
			opt.w.print('#');
		opt.w.print(' ');
	}

	private void parameter(Parameter p[]) {
		for (int i = 0; i < p.length; i++) {
			opt.w.print(p[i].name());
			opt.w.print(" : ");
			opt.w.print(p[i].type());
			if (i + 1 < p.length)
				opt.w.print(", ");
		}
	}

	private void type(Type t) {
		opt.w.print(" : ");
		if (opt.showQualified)
			opt.w.print(t.qualifiedTypeName());
		else
			opt.w.print(t.typeName());
		opt.w.print(t.dimension());
	}

	private void attributes(FieldDoc f[]) {
		for (int i = 0; i < f.length; i++) {
			visibility(f[i]);
			opt.w.print(f[i].name());
			if (opt.showType)
				type(f[i].type());
			opt.w.print("\\l");
		}
	}

	private void operations(MethodDoc m[]) {
		for (int i = 0; i < m.length; i++) {
			visibility(m[i]);
			opt.w.print(m[i].name());
			if (opt.showType) {
				opt.w.print("(");
				parameter(m[i].parameters());
				opt.w.print(")");
				type(m[i].returnType());
			} else
				opt.w.print("()");
			opt.w.print("\\l");
		}
	}

	private String name(ClassDoc c) {
		String name;

		if ((name = (String)classnames.get(c)) == null) {
			// Associate classnames alias
			name = "c" + (new Integer(classnum)).toString();
			classnames.put(c, name);
			classnum++;
			String r = c.toString();
			if (!opt.showQualified) {
				// Create readable string by stripping leading path
				int dotpos = r.lastIndexOf('.');
				if (dotpos != -1)
					r = r.substring(dotpos + 1, r.length());
			}
			// Create label
			opt.w.print("\t" + name + " [");
			if (opt.showAttributes || opt.showOperations)
				opt.w.print("label=\"{" + r + "\\n|");
			else
				opt.w.print("label=\"" + r + "\"");
			if (opt.showAttributes)
				attributes(c.fields());
			if (opt.showAttributes || opt.showOperations)
				opt.w.print("|");
			if (opt.showAttributes)
				operations(c.methods());
			if (opt.showAttributes || opt.showOperations)
				opt.w.print("}\"");
			if (c.isAbstract())
				opt.w.print(", fontname=\"Helvetica-Oblique\"");
			opt.w.println("];");
		}
		return name;
	}

	public void print() {
		String cs = name(c);
		// Print the derivation path
		ClassDoc s = c.superclass();
		if (s != null && !s.toString().equals("java.lang.Object")) {
			opt.w.print("\t" + name(s) + " -> " + cs + " [dir=back,arrowtail=empty];");
			opt.w.println("\t//" + c + " extends " + s);
		}
	}
}

public class UmlGraph {
	private static Options opt = new Options();

	public static boolean start(RootDoc root)
                            throws IOException, UnsupportedEncodingException {
		opt.openFile();
		prologue();
		ClassDoc[] classes = root.classes();
		for (int i = 0; i < classes.length; i++) {
			ClassGraph c = new ClassGraph(opt, classes[i]);
			c.print();
		}
		epilogue();
		return true;
	}

	public static int optionLength(String option) {
		if(option.equals("-qualify")) {
			opt.showQualified = true;
			return 1;
		}
		if(option.equals("-attributes")) {
			opt.showAttributes = true;
			return 1;
		}
		if(option.equals("-operations")) {
			opt.showOperations = true;
			return 1;
		}
		if(option.equals("-visibility")) {
			opt.showVisibility = true;
			return 1;
		}
		if(option.equals("-types")) {
			opt.showType = true;
			return 1;
		}
		if(option.equals("-all")) {
			opt.setAll();
			return 1;
		}
		return 0;
	}

	private static void prologue() {
		opt.w.println(
			"#!/usr/local/bin/dot\n" +
			"#\n" +
			"# Class hirerarchy\n" +
			"#\n\n" +
			"digraph G {\n" +
			"\tnode [fontname=\"Helvetica\",fontsize=8,shape=record];"
		);
	}

	private static void epilogue() {
		opt.w.println("}\n");
		opt.w.flush();
	}
}
