/*
 * UmlGraph class diagram testing framework
 *
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
 *
 */

package org.umlgraph.test;

import java.io.File;
import java.io.PrintWriter;

public class RunDoc {

    static String sourcesFolder = "src";

    static String docFolder = "javadoc";

    static PrintWriter pw = new PrintWriter(System.out);

    public static void main(String[] args) {
	File outFolder = new File(docFolder);
	if (!outFolder.exists())
	    outFolder.mkdirs();
	String[] options = new String[] { "-docletpath", "build", "-private", "-d", docFolder,
		"-sourcepath", sourcesFolder, "-subpackages", "gr.spinellis" };
	runDoclet(options);
    }

    private static void runDoclet(String[] options) {
	com.sun.tools.javadoc.Main.execute("UMLGraph test", pw, pw, pw,
		"org.umlgraph.doclet.UmlGraphDoc", options);
    }

}
