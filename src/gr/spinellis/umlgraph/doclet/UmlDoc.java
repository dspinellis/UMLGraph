package gr.spinellis.umlgraph.doclet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;

/**
 * Chaining doclet that runs the standart Javadoc doclet first, and on success,
 * runs the generation of dot files by UMLGraph
 * @author wolf
 * 
 */
public class UmlDoc {
    public static int optionLength(String option) {
	int result = Standard.optionLength(option);
	if (result != 0)
	    return result;
	else
	    return UmlGraph.optionLength(option);
    }

    public static boolean start(RootDoc root) throws IOException {
	if (Standard.start(root)) {
	    String outputFolder = findOutputPath(root.options());

	    Options opt = new Options();
	    opt.setOptions(root.options());
	    opt.relativeLinksForSourcePackages = true;

	    generatePackageDiagrams(root, opt, outputFolder);
	    generateContextDiagrams(root, opt, outputFolder);
	    return true;
	} else {
	    return false;
	}
    }

    public static LanguageVersion languageVersion() {
	return Standard.languageVersion();
    }

    private static void generatePackageDiagrams(RootDoc root, Options opt, String outputFolder)
	    throws IOException {
	if (opt.verbose2)
	    System.out.println("Examining packages");
	for (PackageDoc packageDoc : root.specifiedPackages()) {
	    if (opt.verbose2)
		System.out.println("Altering javadocs for pacakge " + packageDoc);
	    OptionProvider view = new PackageView(outputFolder, packageDoc, root, opt);
	    UmlGraph.buildGraph(root, view, packageDoc);
	    runGraphviz(outputFolder, packageDoc.name(), packageDoc.name());
	    alterHtmlDocs(outputFolder, packageDoc.name(), packageDoc.name(),
		    "package-summary.html", "</H2>");
	}
    }

    private static void generateContextDiagrams(RootDoc root, Options opt, String outputFolder)
	    throws IOException {
	if (opt.verbose2)
	    System.out.println("Examining classes");
	for (ClassDoc classDoc : root.classes()) {
	    if (!isIncluded(classDoc, root.specifiedPackages()))
		continue;
	    if (opt.verbose2)
		System.out.println("Altering class doc for " + classDoc);
	    ContextView view = new ContextView(outputFolder, classDoc, root, opt);
	    UmlGraph.buildGraph(root, view, classDoc);
	    runGraphviz(outputFolder, classDoc.containingPackage().name(), classDoc.name());
	    alterHtmlDocs(outputFolder, classDoc.containingPackage().name(), classDoc.name(),
		    classDoc.name() + ".html", classDoc.name() + "</H2>");
	}
    }

    private static void runGraphviz(String outputFolder, String packageName, String name) {
	File dotFile = new File(outputFolder, packageName.replace(".", "/") + "/" + name + ".dot");
	File pngFile = new File(outputFolder, packageName.replace(".", "/") + "/" + name + ".png");
	File mapFile = new File(outputFolder, packageName.replace(".", "/") + "/" + name + ".map");

	try {
	    String command = "dot -Tcmapx -o" + mapFile.getAbsolutePath() + " -Tpng -o"
		    + pngFile.getAbsolutePath() + " " + dotFile.getAbsolutePath();
	    Process p = Runtime.getRuntime().exec(command);
	    int result = p.waitFor();
	    if (result != 0)
		System.out.println("Errors running Graphviz on " + dotFile);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void alterHtmlDocs(String outputFolder, String packageName, String name,
	    String htmlFileName, String matchLineEnd) throws IOException {
	// setup files
	File output = new File(outputFolder, packageName.replace(".", "/"));
	File htmlFile = new File(output, htmlFileName);
	File alteredFile = new File(htmlFile.getAbsolutePath() + ".uml");
	File mapFile = new File(output, name + ".map");
	if (!htmlFile.exists()) {
	    System.err.println("Expected file not found: " + htmlFile.getAbsolutePath());
	    return;
	}

	// parse & rewrite
	BufferedWriter writer = null;
	BufferedReader reader = null;
	boolean matched = false;
	try {
	    writer = new BufferedWriter(new FileWriter(alteredFile));
	    reader = new BufferedReader(new FileReader(htmlFile));

	    String line;
	    while ((line = reader.readLine()) != null) {
		writer.write(line);
		writer.newLine();
		if (!matched && line.endsWith(matchLineEnd)) {
		    matched = true;
		    if (mapFile.exists())
			insertClientSideMap(mapFile, writer);
		    else
			System.out.println("Could not find map file " + mapFile);
		    writer.write("<div align=\"center\"><img src=\"" + name
			    + ".png\" alt=\"Package class diagram package " + name
			    + "\" usemap=\"#G\" border=0/></a></div>");
		    writer.newLine();
		}
	    }
	} finally {
	    if (writer != null)
		writer.close();
	    if (reader != null)
		reader.close();
	}

	// if altered, delete old file and rename new one to the old file name
	if (matched) {
	    htmlFile.delete();
	    alteredFile.renameTo(htmlFile);
	} else {
	    System.out.println("Error, could not find a line that ends with '" + matchLineEnd
		    + "'.\n Class diagram reference not inserted");
	    alteredFile.delete();
	}
    }

    /**
     * Reads the map file and outputs in to the specified writer
     * @throws IOException
     */
    private static void insertClientSideMap(File mapFile, BufferedWriter writer) throws IOException {
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(mapFile));
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		writer.write(line);
		writer.newLine();
	    }
	} finally {
	    if (reader != null)
		reader.close();
	}
    }

    /**
     * Return true if the class name has been specified in the command line
     */
    private static boolean isIncluded(ClassDoc cd, PackageDoc[] docs) {
	for (PackageDoc pd : docs) {
	    for (ClassDoc pcd : pd.allClasses())
		if (pcd.equals(cd))
		    return true;
	}
	return false;
    }

    /**
     * Returns the output path specified on the javadoc options
     */
    private static String findOutputPath(String[][] options) {
	for (int i = 0; i < options.length; i++) {
	    if (options[i][0].equals("-d"))
		return options[i][1];
	}
	return ".";
    }

}
