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

class ClassGraph {
	private ClassDoc c;
	ClassGraph(ClassDoc ic) { c = ic; }
	public void print() {
		System.out.println(c);
	}
}

public class UmlGraph {
	public static boolean start(RootDoc root) {
		prologue();
		ClassDoc[] classes = root.classes();
		for (int i = 0; i < classes.length; i++) {
			ClassGraph c = new ClassGraph(classes[i]);
			c.print();
		}
		epilogue();
		return true;
	}
	private static void prologue() {
		System.out.println(
			"#!/usr/local/bin/dot\n" +
			"#\n" +
			"# Class hirerarchy\n" +
			"#\n\n" +
			"digraph G {\n" +
			"\tnode [fontname=\"Helvetica\",fontsize=8,shape=record];"
		);
	}
	private static void epilogue() {
		System.out.println("}\n");
	}
}
