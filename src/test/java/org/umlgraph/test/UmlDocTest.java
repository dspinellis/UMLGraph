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

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

import org.junit.Ignore;
import org.junit.Test;
import org.umlgraph.doclet.UmlGraphDoc;

/**
 * UmlGraphDoc doclet regression tests
 * @author wolf
 * 
 */
public class UmlDocTest {

    static final String testSourceFolder = "testdata/umldoc-src";

    static final String testDestFolder = "testdata/umldoc-out";

    static final String testRefFolder = "testdata/umldoc-ref";
    static final String doclet = "org.umlgraph.doclet.UmlGraphDoc";

    static PrintWriter pw = new PrintWriter(System.out);

    @Test
    @Ignore
    public void test() throws IOException {
	List<String> differences = new ArrayList<String>();

	File outFolder = new File(testDestFolder);
	if (!outFolder.exists())
	    outFolder.mkdirs();

	TestUtils.cleanFolder(outFolder, true);

	// run tests
	runTest(differences);
	if (differences.size() > 0) {
	    pw.println("ERROR, some files are not structurally equal or some files are missing:");
	    for (String className : differences) {
		pw.println(className);
	    }
            System.exit(1);
	} else {
	    pw.println("GOOD, all files are structurally equal");
	}
	pw.println();
	pw.println();
	pw.flush();
    }

    private static void runTest(List<String> differences) throws IOException {
	File outFolder = new File(testDestFolder);
	File srcFolder = new File(testSourceFolder);
	String[] options = new String[] { "-private", "--d=\"" + outFolder.getAbsolutePath() + "\"",
	        "--source-path=\"" + srcFolder.getAbsolutePath() + "\"", "-compact",
		"-subpackages", "gr.spinellis", "-inferrel", "-inferdep", "-qualify",
		"-postfixpackage", "--collpackages=\"java.util.*\"" };
	runDoclet(options);

	compareDocletOutputs(differences, new File(testRefFolder), new File(testDestFolder));
    }

    /**
         * Ensures that reference and output have the same contents in terms of:
         * <ul>
         * <li> html files </li>
         * <li> dot files </li>
         * <li> folders </li>
         * </ul>
         * @throws IOException
         */
    private static void compareDocletOutputs(List<String> differences, File refFolder,
	    File outFolder) throws IOException {
	if(refFolder.getName().equals("CVS"))
	    return;
	
	if (!refFolder.exists() || !refFolder.isDirectory())
	    throw new IllegalArgumentException("Reference does not exists or is not a folder: "
		    + refFolder.getAbsolutePath());
	if (!outFolder.exists() || !outFolder.isDirectory())
	    throw new IllegalArgumentException("Output does not exists or is not a folder: "
		    + outFolder.getAbsolutePath());

	// get elements and sort
	String[] refFiles = refFolder.list();
	String[] outFiles = refFolder.list();
	Arrays.sort(refFiles);
	Arrays.sort(outFiles);

	// parallel scan (mergesort inspired)
	int i = 0, j = 0;
	while (i < refFiles.length && j < outFiles.length) {
	    File ref = new File(refFolder, refFiles[i]);
	    File out = new File(outFolder, outFiles[j]);
	    int compare = refFiles[i].compareTo(outFiles[i]);
	    if (compare == 0) {
		String refName = ref.getName().toLowerCase();
		if (ref.isDirectory()) {
		    compareDocletOutputs(differences, ref, out);
		} else if (refName.endsWith(".dot")) {
		    if (!TestUtils.dotFilesEqual(pw, ref.getAbsolutePath(), out.getAbsolutePath()))
			differences.add(out.getName() + " is different from the reference");

		} else {
		    if (!TestUtils.textFilesEquals(pw, ref, out)) {
			differences.add(out.getName() + " is different from the reference");
		    }
		}
		i++;
		j++;
	    } else if (compare < 0) {
		differences.add("Reference file/folder not found in output: "
			+ ref.getAbsolutePath());
		i++;
	    } else {
		j++;
	    }
	}

	// all ref files remaining are missing ones
	while (i < refFiles.length) {
	    File ref = new File(refFolder, refFiles[i]);
	    differences.add("Reference file/folder not found in output: " + ref.getAbsolutePath());
	    i++;
	}
    }

    /**
         * Runs the UmlGraphDoc doclet
         * @param options
         */
    private static void runDoclet(String[] options) {
        pw.print("Run javadoc -doclet " + doclet);
        for (String o : options) {
            pw.print(" " + o);
        }
        pw.println();
        DocumentationTool systemDocumentationTool = ToolProvider.getSystemDocumentationTool();
        DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(pw, null, null, UmlGraphDoc.class, Arrays.asList(options), null);
        task.call();
    }

}
