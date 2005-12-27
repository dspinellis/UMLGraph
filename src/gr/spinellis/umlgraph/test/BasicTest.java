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
 * $Id$
 *
 */

package gr.spinellis.umlgraph.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class BasicTest {

    static String testSourceFolder = "testdata/java";

    static String testDestFolder = "testdata/dot-out";

    static String testRefFolder = "testdata/dot-ref";

    static PrintWriter pw = new PrintWriter(System.out);

    public static void main(String[] args) throws IOException {
	boolean equal = true;
	
	File outFolder = new File(testDestFolder);
        if (!outFolder.exists())
            outFolder.mkdirs();
	
        equal = performBasicTests();
        equal &= performViewTests(outFolder);

        if (!equal)
            System.out.println("ERROR, some files are not structurally equal");
        else
            System.out.println("GOOD, all files are structurally equal");
    }
    
    private static boolean performViewTests(File outFolder) throws IOException {
	String[] options = new String[] { "-docletpath", "build", "-hide", "Hidden", "-private",
		"-d", outFolder.getAbsolutePath(), "-sourcepath", "testdata/java", "-subpackages", "gr.spinellis", "-views" };
	runDoclet(options);
	
	String[] javaFiles = new File(testSourceFolder, "gr/spinellis/views").list(new JavaFilter());
	boolean equal = true;
	for (int i = 0; i < javaFiles.length; i++) {
	    String viewName = javaFiles[i].substring(0, javaFiles[i].length() - 5);
	    File dotFile = new File(testDestFolder, viewName + ".dot");
	    String dotPath = dotFile.getAbsolutePath();
	    String refPath = new File(testRefFolder, viewName + ".dot").getAbsolutePath();
	    equal &= checkForDifferences(dotPath, refPath);
	}
	return equal;
    }

    private static boolean performBasicTests() throws IOException {
	String[] javaFiles = new File(testSourceFolder).list(new JavaFilter());
//	String[] javaFiles = new String[] {"MyVector.java"};
	boolean equal = true;
        for (int i = 0; i < javaFiles.length; i++) {
            String javaFileName = javaFiles[i].substring(0, javaFiles[i].length() - 5);
            File dotFile = new File(testDestFolder, javaFileName + ".dot");
	    dotFile.delete();
            String dotPath = dotFile.getAbsolutePath();
            String refPath = new File(testRefFolder, javaFileName + ".dot").getAbsolutePath();
            String javaPath = new File(testSourceFolder, javaFiles[i]).getAbsolutePath();
            String[] options = new String[] { "-docletpath", "build", "-hide", "Hidden", "-private", "-output", dotPath,
                    javaPath };

            runDoclet(options);
            equal &= checkForDifferences(dotPath, refPath);
        }
	return equal;
    }

    private static void runDoclet(String[] options) {
	com.sun.tools.javadoc.Main.execute("UMLGraph test", pw, pw, pw, "gr.spinellis.umlgraph.doclet.UmlGraph", options);
    }

    private static boolean checkForDifferences(String dotPath, String refPath) throws IOException {
	System.out.println("Performing diff:\nout:" + dotPath + "\nref:" + refPath);
	DotDiff differ = new DotDiff(new File(dotPath), new File(refPath));
	boolean equal = differ.graphEquals();
	if (equal) {
	    System.out.println("File contents are structurally equal");
	} else {
	    System.out.println("File contents are structurally not equal");
	    printList("# Lines in out but not in ref", differ.getExtraLines1());
	    printList("# Lines in ref but not in out", differ.getExtraLines2());
	    printList("# Nodes in out but not in ref", differ.getNodes1());
	    printList("# Nodes in ref but not in out", differ.getNodes2());
	    printList("# Arcs in out but not in ref", differ.getArcs1());
	    printList("# Arcs in ref but not in out", differ.getArcs2());
	}
	System.out.println("\n\n");
	return equal;
    }

    private static void printList(String message, List extraOut) {
        if (extraOut.size() > 0) {
            System.out.println(message);
            for (Object o : extraOut) {
                System.out.println(o);
            }
        }
    }
}
