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

class ClassGraph {
	private ClassDoc c;
	private PrintWriter w;

	ClassGraph(PrintWriter iw, ClassDoc ic) { 
		c = ic;
		w = iw;
	}
	public void print() {
		w.print(c + "[");
		if (c.isAbstract())
			w.print("fontname=\"Helvetica-Oblique\"");
		w.println("];");
		ClassDoc s = c.superclass();
		if (s != null && !s.toString().equals("java.lang.Object"))
			w.println(s + " -> " + c + " [dir=back,arrowtail=empty];\n");
	}
}

public class UmlGraph {
	private static PrintWriter w;

	public static boolean start(RootDoc root)
                            throws IOException, UnsupportedEncodingException {
		FileOutputStream fos = new FileOutputStream("graph.dot");
		w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
		prologue();
		ClassDoc[] classes = root.classes();
		for (int i = 0; i < classes.length; i++) {
			ClassGraph c = new ClassGraph(w, classes[i]);
			c.print();
		}
		epilogue();
		return true;
	}
	private static void prologue() {
		w.println(
			"#!/usr/local/bin/dot\n" +
			"#\n" +
			"# Class hirerarchy\n" +
			"#\n\n" +
			"digraph G {\n" +
			"\tnode [fontname=\"Helvetica\",fontsize=8,shape=record];"
		);
	}
	private static void epilogue() {
		w.println("}\n");
		w.flush();
	}
}
