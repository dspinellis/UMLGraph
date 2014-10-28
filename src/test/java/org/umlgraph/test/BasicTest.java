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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UmlGraph regression tests
 * @author wolf
 * 
 */
public class BasicTest {

    static String testSourceFolder = "testdata/java";

    static String testDestFolder = "testdata/dot-out";

    static String testRefFolder = "testdata/dot-ref";

    static PrintWriter pw = new PrintWriter(System.out);

    public static void main(String[] args) throws IOException {
	List<String> differences = new ArrayList<String>();

	File outFolder = new File(testDestFolder);
	if (!outFolder.exists())
	    outFolder.mkdirs();

	TestUtils.cleanFolder(outFolder, true);

	// don't use windows specific fonts
	System.setProperty("os.name", "generic");

	// run tests
	performBasicTests(differences);
	performViewTests(differences, outFolder);
	if (differences.size() > 0) {
	    pw.println("ERROR, some files are not structurally equal or some files are missing:");
	    for (String className : differences) {
		pw.println(className);
	    }
	} else {
	    pw.println("GOOD, all files are structurally equal");
	}
	pw.println();
	pw.println();
	pw.flush();
    }

    private static void performViewTests(List<String> differences, File outFolder)
	    throws IOException {
	String[] options = new String[] { "-docletpath", "build", "-private", "-d",
		outFolder.getAbsolutePath(), "-sourcepath", "testdata/java", "-compact",
		"-subpackages", "gr.spinellis", "-views" };
	runDoclet(options);

	List<String> viewFiles = new ArrayList<String>();
	viewFiles.addAll(getViewList(new File(testSourceFolder, "gr/spinellis/basic/views")));
	viewFiles.addAll(getViewList(new File(testSourceFolder, "gr/spinellis/context/views")));
	viewFiles.addAll(getViewList(new File(testSourceFolder, "gr/spinellis/iface/views")));
	viewFiles.addAll(getViewList(new File(testSourceFolder, "gr/spinellis/subclass/views")));
	for (String fileName : viewFiles) {
	    String viewName = fileName.substring(0, fileName.length() - 5);
	    File dotFile = new File(testDestFolder, viewName + ".dot");
	    File refFile = new File(testRefFolder, viewName + ".dot");
	    if (viewName.contains("Abstract")) {
		// make sure abstract views are not generated
		if (dotFile.exists()) {
		    pw.println("Error, abstract view " + viewName + " has been generated");
		    differences.add(dotFile.getName() + " should not be there");
		}
	    } else {
		compare(differences, dotFile, refFile);
	    }
	}
    }

    private static List<String> getViewList(File viewFolder) {
	if (!viewFolder.exists())
	    throw new RuntimeException("The folder " + viewFolder.getAbsolutePath()
		    + " does not exists.");
	else if (!viewFolder.isDirectory())
	    throw new RuntimeException(viewFolder.getAbsolutePath() + " is not a folder!.");
	else if (!viewFolder.canRead())
	    throw new RuntimeException("The folder " + viewFolder.getAbsolutePath()
		    + " cannot be read.");

	return Arrays.asList(viewFolder.list(new SimpleFileFilter(".java")));
    }

    private static boolean performBasicTests(List<String> differences) throws IOException {
	String[] javaFiles = new File(testSourceFolder).list(new SimpleFileFilter(".java"));
	boolean equal = true;
	for (int i = 0; i < javaFiles.length; i++) {
	    String javaFileName = javaFiles[i].substring(0, javaFiles[i].length() - 5);
	    String outFileName = javaFileName + ".dot";
	    File dotFile = new File(testDestFolder, outFileName);
	    dotFile.delete();
	    File refFile = new File(testRefFolder, outFileName);
	    String javaPath = new File(testSourceFolder, javaFiles[i]).getAbsolutePath();
	    String[] options = new String[] { "-docletpath", "build", "-hide", "Hidden",
		    "-compact", "-private", "-d", testDestFolder, "-output", outFileName, javaPath };

	    runDoclet(options);
	    compare(differences, dotFile, refFile);
	}
	return equal;
    }

    private static void runDoclet(String[] options) {
	com.sun.tools.javadoc.Main.execute("UMLGraph test", pw, pw, pw,
		"org.umlgraph.doclet.UmlGraph", options);
    }

    private static void compare(List<String> differences, File dotFile, File refFile)
	    throws IOException {
	if (!dotFile.exists()) {
	    pw.println("Error, output file " + dotFile + " has not been generated");
	    differences.add(dotFile.getName() + " has not been generated");
	} else if (!refFile.exists()) {
	    pw.println("Error, reference file " + refFile + " is not available");
	    differences.add(refFile.getName() + " reference is not available");
	} else if (!TestUtils.dotFilesEqual(pw, dotFile.getAbsolutePath(), refFile
		.getAbsolutePath())) {
	    differences.add(dotFile.getName() + " is different from the reference");
	}
    }

}
