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

/**
 * Represent the program options
 */
class Options {
	PrintWriter w;
	boolean showQualified;
	boolean showAttributes;
	boolean showOperations;
	boolean showVisibility;
	boolean horizontal;
	boolean showType;

	Options() {
		showQualified = false;
		showAttributes = false;
		showOperations = false;
		showVisibility = false;
		showType = false;
	}

	/** Most verbose output */
	public void setAll() {
		showAttributes = true;
		showOperations = true;
		showVisibility = true;
		showType = true;
	}

	/** Set the options based on the tag elements of the ClassDoc parameter */
	public void setOptions(ClassDoc p) {
		if (p == null)
			return;

		Tag tags[] = p.tags("opt");
		for (int i = 0; i < tags.length; i++)
			if (UmlGraph.optionLength("-" + tags[i].text()) == 0)
				System.err.println("Unknown option " + tags[i].text());
	}

	public void openFile() throws IOException, UnsupportedEncodingException {
		FileOutputStream fos = new FileOutputStream("graph.dot");
		w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
	}
}

/**
 * Class's dot-comaptible alias name (for fully qualified class names)
 * and printed information
 */
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

/**
 * Class graph generation engine
 */
class ClassGraph {
	private static HashMap classnames = new HashMap();
	private Options opt;
	/** Guillemot left (open) */
	private static char guilopen = (char)0xab;
	/** Guillemot right (close) */
	private static char guilclose = (char)0xbb;

	ClassGraph(Options iopt) { 
		opt = iopt;
	}

	/** Print the visibility adornment of element e */
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

	/** Print the method parameter p */
	private void parameter(Parameter p[]) {
		for (int i = 0; i < p.length; i++) {
			opt.w.print(p[i].name());
			type(p[i].type());
			if (i + 1 < p.length)
				opt.w.print(", ");
		}
	}

	/** Print the type t */
	private void type(Type t) {
		if (t.typeName().equals("void"))
			return;
		opt.w.print(" : ");
		if (opt.showQualified)
			opt.w.print(t.qualifiedTypeName());
		else
			opt.w.print(t.typeName());
		opt.w.print(t.dimension());
	}

	/** Print the class's attributes f */
	private void attributes(FieldDoc f[]) {
		for (int i = 0; i < f.length; i++) {
			if (hidden(f[i]))
				continue;
			visibility(f[i]);
			opt.w.print(f[i].name());
			if (opt.showType)
				type(f[i].type());
			opt.w.print("\\l");
		}
	}

	/** Print the class's operations m */
	private void operations(MethodDoc m[]) {
		for (int i = 0; i < m.length; i++) {
			if (hidden(m[i]))
				continue;
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

	/** Return true if c has a @hidden tag associated with it */
	private static boolean hidden(Doc c) {
		Tag tags[] = c.tags("hidden");
		return (tags.length > 0);
	}

	/** Return a class's internal name */
	private static String name(String c) {
		ClassInfo ci;

		if ((ci = (ClassInfo)classnames.get(c)) == null)
			classnames.put(c, ci = new ClassInfo(false));
		return ci.name;
	}

	/** Return a class's internal name, printing the class if needed */
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
			opt.w.println("\t// " + r);
			if (!opt.showQualified) {
				// Create readable string by stripping leading path
				int dotpos = r.lastIndexOf('.');
				if (dotpos != -1)
					r = r.substring(dotpos + 1, r.length());
			}
			// Create label
			opt.w.print("\t" + ci.name + " [");
			if (c.isInterface())
				r = guilopen + "interface" + guilclose + "\\n" + r;
			boolean showMembers = 
				(opt.showAttributes || opt.showOperations) &&
				(c.methods().length > 0 || c.fields().length > 0);
			if (showMembers)
				opt.w.print("label=\"{" + r + "\\n|");
			else
				opt.w.print("label=\"" + r + "\"");
			if (opt.showAttributes)
				attributes(c.fields());
			if (showMembers)
				opt.w.print("|");
			if (opt.showAttributes)
				operations(c.methods());
			if (showMembers)
				opt.w.print("}\"");
			// Use ariali for gif output
			if (c.isAbstract())
				opt.w.print(", fontname=\"Helvetica-Oblique\"");
			opt.w.println("];");
			ci.nodePrinted = true;
		}
		return ci.name;
	}

	/**
	 * Tokenize s into four elements and return them
	 */
	private static String[] tokenize(String s) {
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

	/** Convert < and > characters in the string to the respective guillemot characters */
	private static String guillemize(String s) {
		StringBuffer r = new StringBuffer(s);

		for (int i = 0; i < r.length(); i++)
			switch (r.charAt(i)) {
			case '<':
				r.setCharAt(i, guilopen);
				break;
			case '>':
				r.setCharAt(i, guilclose);
				break;
			}
		return r.toString();
	}

	/** 
	 * Print all relations for a given's class's tag
	 * @param tagname the tag containing the given relation
	 * @param from the source class
	 * @param name the source class internal name
	 * @param edgetype the dot edge specification
	 */
	private void relation(String tagname, Doc from, String name, String edgetype) {
		Tag tags[] = from.tags(tagname);
		for (int i = 0; i < tags.length; i++) {
			String t[] = tokenize(tags[i].text());	// l-src label l-dst target
			opt.w.println("\t// " + from + " " + tagname + " " + t[3]);
			opt.w.println("\t" + name + " -> " + name(t[3]) + " [" +
				"taillabel=\"" + t[0] + "\", " + 
				"label=\"" + guillemize(t[1]) + "\", " + 
				"headlabel=\"" + t[2] + "\", " + 
				edgetype + "]"
			);
		}
	}

	/** Print a class */
	public void print(ClassDoc c) {
		String cs = name(c);
		// Print generalization (through the Java superclass)
		ClassDoc s = c.superclass();
		if (s != null && !s.toString().equals("java.lang.Object")) {
			opt.w.println("\t//" + c + " extends " + s);
			opt.w.println("\t" + name(s) + " -> " + cs + " [dir=back,arrowtail=empty];");
		}
		// Print generalization (through @extends tags)
		Tag tags[] = c.tags("extends");
		for (int i = 0; i < tags.length; i++) {
			opt.w.println("\t//" + c + " extends " + tags[i].text());
			opt.w.println("\t" + name(tags[i].text()) + " -> " + cs + " [dir=back,arrowtail=empty];");
		}
		// Print realizations (Java interfaces)
		ClassDoc ifs[] = c.interfaces();
		for (int i = 0; i < ifs.length; i++) {
			opt.w.print("\t" + name(ifs[i]) + " -> " + cs + " [dir=back,arrowtail=empty,style=dashed];");
			opt.w.println("\t//" + c + " implements " + s);
		}
		// Print other associations
		relation("assoc", c, cs, "arrowhead=none");
		relation("navassoc", c, cs, "arrowhead=open");
		relation("has", c, cs, "arrowhead=none, arrowtail=ediamond");
		relation("composed", c, cs, "arrowhead=none, arrowtail=diamond");
		relation("depend", c, cs, "arrowhead=open, style=dashed");
	}
}

/** Doclet API implementation */
public class UmlGraph {
	private static Options opt = new Options();

	/** Entry point */
	public static boolean start(RootDoc root)
                            throws IOException, UnsupportedEncodingException {
		opt.openFile();
		opt.setOptions(root.classNamed("UMLOptions"));
		prologue();
		ClassDoc[] classes = root.classes();
		ClassGraph c = new ClassGraph(opt);
		for (int i = 0; i < classes.length; i++) {
			c.print(classes[i]);
		}
		epilogue();
		return true;
	}

	/** Option checking */
	public static int optionLength(String option) {
		if(option.equals("-qualify")) {
			opt.showQualified = true;
			return 1;
		}
		if(option.equals("-horizontal")) {
			opt.horizontal = true;
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

	/** Dot prologue */
	private static void prologue() {
		opt.w.println(
			"#!/usr/local/bin/dot\n" +
			"#\n" +
			"# Class diagram \n" +
			"# Generated by $Id$\n" +
			"#\n\n" +
			"digraph G {\n" +
			"\tedge [fontname=\"Helvetica\",fontsize=10,labelfontname=\"Helvetica\",labelfontsize=10];\n" +

			"\tnode [fontname=\"Helvetica\",fontsize=10,shape=record];"
		);
		if (opt.horizontal)
			opt.w.println("\trankdir=LR;\n\tranksep=1;");
	}

	/** Dot epilogue */
	private static void epilogue() {
		opt.w.println("}\n");
		opt.w.flush();
	}
}
