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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class BasicTest {

    static String testSourceFolder = "./test-data/simple/java";

    static String testDestFolder = "./test-data/simple/dot-out";

    static String testRefFolder = "./test-data/simple/dot-ref";

    static PrintWriter pw = new PrintWriter(System.out);

    public static void main(String[] args) throws IOException {
        String[] javaFiles = new File(testSourceFolder).list(new JavaFilter());
//        String[] javaFiles = new String[] {"MyVector.java"};

        File outFolder = new File(testDestFolder);
        if (!outFolder.exists())
            outFolder.mkdirs();

        boolean equal = true;
        for (int i = 0; i < javaFiles.length; i++) {
            String javaFileName = javaFiles[i].substring(0, javaFiles[i].length() - 5);
            String dotPath = new File(testDestFolder, javaFileName + ".dot").getAbsolutePath();
            String refPath = new File(testRefFolder, javaFileName + ".dot").getAbsolutePath();
            String javaPath = new File(testSourceFolder, javaFiles[i]).getAbsolutePath();
            String[] options = new String[] { "-hide", "Hidden", "-private", "-output", dotPath,
                    javaPath };

            com.sun.tools.javadoc.Main.execute("UMLGraph test", pw, pw, pw, "UmlGraph", options);

            System.out.println("Performing diff:\nout:" + dotPath + "\nref:" + refPath);
            DotDiff differ = new DotDiff(new File(dotPath), new File(refPath));
            if (differ.graphEquals()) {
                System.out.println("File contents are structurally equal");
            } else {
                equal = false;
                System.out.println("File contents are structurally not equal");
                printList("# Lines in out but not in ref", differ.getExtraLines1());
                printList("# Lines in ref but not in out", differ.getExtraLines2());
                printList("# Nodes in out but not in ref", differ.getNodes1());
                printList("# Nodes in ref but not in out", differ.getNodes2());
                printList("# Arcs in out but not in ref", differ.getArcs1());
                printList("# Arcs in ref but not in out", differ.getArcs2());
            }
            System.out.println("\n\n");
        }

        if (!equal)
            System.out.println("ERROR, some file are not structurally equal");
        else
            System.out.println("GOOD, all files are structurally equal");
    }

    private static void printList(String message, List extraOut) {
        if (extraOut.size() > 0) {
            System.out.println(message);
            for (Object o : extraOut) {
                System.out.println(o);
            }
        }
    }

    private static final class JavaFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".java");
        }
    }
}
