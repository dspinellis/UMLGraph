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

class ClassInfo {
	private static int classnum;
	/** Alias name for the class */
	String name;
	/** True if the class class node has been printed */
	boolean nodePrinted;

	ClassInfo(boolean p) {
		nodePrinted = p;
		name = "c" + (new Integer(classnum)).toString();
		classnum++;
	}
}

class ClassGraph {
	private ClassDoc c;
	private static HashMap classnames = new HashMap();
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
			type(p[i].type());
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

	private String name(String c) {
		ClassInfo ci;

		if ((ci = (ClassInfo)classnames.get(c)) == null)
			classnames.put(c, ci = new ClassInfo(false));
		return ci.name;
	}

	/** Return true if c has a @hidden tag associated with it */
	private boolean hidden(ClassDoc c) {
		Tag tags[] = c.tags("hidden");
		return (tags.length > 0);
	}

	private String name(ClassDoc c) {
		ClassInfo ci;
		boolean toPrint;

		if ((ci = (ClassInfo)classnames.get(c.toString())) != null)
			toPrint = !ci.nodePrinted;
		else {
			toPrint = true;
			classnames.put(c.toString(), ci = new ClassInfo(true));
		}
		if (toPrint && !hidden(c)) {
			// Associate classnames alias
			String r = c.toString();
			if (!opt.showQualified) {
				// Create readable string by stripping leading path
				int dotpos = r.lastIndexOf('.');
				if (dotpos != -1)
					r = r.substring(dotpos + 1, r.length());
			}
			// Create label
			opt.w.print("\t" + ci.name + " [");
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
			ci.nodePrinted = true;
		}
		return ci.name;
	}

	private String[] tokenize(String s) {
		String r[] = new String[4];
		String remain = s, tok;
		int n = 0, pos;

		remain.trim();
		while (remain.length() > 0 && n < 4) {
			if (remain.startsWith("\"")) {
				// Field in quotes
				pos = remain.indexOf('"', 1);
				if (pos == -1)
					break;
				r[n] = remain.substring(1, pos);
				if (pos + 1 < remain.length())
					pos++;
			} else {
				// Space-separated field
				pos = remain.indexOf(' ', 0);
				if (pos == -1)
					r[n] = remain;
				else
					r[n] = remain.substring(0, pos);
			}
			remain = remain.substring(pos + 1);
			remain.trim();
			// - is used as a placeholder for empy fields
			if (r[n].equals("-"))
				r[n] = "";
			n++;
		}
		if (n != 4)
			System.err.println("Expected four fields: " + s);
		return (r);
	}

	private void relation(String tagname, Doc from, String name, String edgetype) {
		Tag tags[] = from.tags(tagname);
		for (int i = 0; i < tags.length; i++) {
			String t[] = tokenize(tags[i].text());	// l-src label l-dst target
			opt.w.println("\t" + name + " -> " + name(t[3]) + "[" +
				"taillabel=\"" + t[0] + "\", " + 
				"label=\"" + t[1] + "\", " + 
				"headlabel=\"" + t[2] + "\", " + 
				edgetype + "]"
			);
		}
	}

	public void print() {
		String cs = name(c);
		// Print generalization (through the Java superclass)
		ClassDoc s = c.superclass();
		if (s != null && !s.toString().equals("java.lang.Object")) {
			opt.w.print("\t" + name(s) + " -> " + cs + " [dir=back,arrowtail=empty];");
			opt.w.println("\t//" + c + " extends " + s);
		}
		// Print realizations (Java interfaces)
		ClassDoc ifs[] = c.interfaces();
		for (int i = 0; i < ifs.length; i++) {
			opt.w.print("\t" + name(ifs[i]) + " -> " + cs + " [dir=back,arrowtail=empty,style=dashed];");
			opt.w.println("\t//" + c + " implements " + s);
		}
		// Print other associations
		relation("assoc", c, cs, "arrowhead=none");
		relation("has", c, cs, "arrowhead=none, arrowtail=ediamond");
		relation("composed", c, cs, "arrowhead=none, arrowtail=diamond");
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
			"# Class diagram \n" +
			"# Generated by $Id$\n" +
			"#\n\n" +
			"digraph G {\n" +
			"edge [fontname=\"Helvetica\",fontsize=8,labelfontname=\"Helvetica\",labelfontsize=8];\n" +

			"\tnode [fontname=\"Helvetica\",fontsize=8,shape=record];"
		);
	}

	private static void epilogue() {
		opt.w.println("}\n");
		opt.w.flush();
	}
}
