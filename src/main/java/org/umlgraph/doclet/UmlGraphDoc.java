package org.umlgraph.doclet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;

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
 * @depend - - - WrappedClassDoc
 * @depend - - - WrappedRootDoc
 */
public class UmlGraphDoc {
    /**
     * Option check, forwards options to the standard doclet, if that one refuses them,
     * they are sent to UmlGraph
     */
    public static int optionLength(String option) {
	int result = Standard.optionLength(option);
	if (result != 0)
	    return result;
	else
	    return UmlGraph.optionLength(option);
    }

    /**
     * Standard doclet entry point
     * @param root
     * @return
     */
    public static boolean start(RootDoc root) {
	root.printNotice("UmlGraphDoc version " + Version.VERSION +  ", running the standard doclet");
	Standard.start(root);
	root.printNotice("UmlGraphDoc version " + Version.VERSION + ", altering javadocs");
	try {
	    String outputFolder = findOutputPath(root.options());

        Options opt = UmlGraph.buildOptions(root);
	    opt.setOptions(root.options());
	    // in javadoc enumerations are always printed
	    opt.showEnumerations = true;
	    opt.relativeLinksForSourcePackages = true;
	    // enable strict matching for hide expressions
	    opt.strictMatching = true;
//	    root.printNotice(opt.toString());

	    root = new WrappedRootDoc(root);
	    generatePackageDiagrams(root, opt, outputFolder);
	    generateContextDiagrams(root, opt, outputFolder);
	} catch(Throwable t) {
	    root.printWarning("Error!");
	    root.printWarning(t.toString());
	    t.printStackTrace();
	    return false;
	}
	return true;
    }

    /**
     * Standand doclet entry
     * @return
     */
    public static LanguageVersion languageVersion() {
	return Standard.languageVersion();
    }

    /**
     * Generates the package diagrams for all of the packages that contain classes among those 
     * returned by RootDoc.class() 
     */
    private static void generatePackageDiagrams(RootDoc root, Options opt, String outputFolder)
	    throws IOException {
	Set<String> packages = new HashSet<String>();
	for (ClassDoc classDoc : root.classes()) {
	    PackageDoc packageDoc = classDoc.containingPackage();
	    if(!packages.contains(packageDoc.name())) {
		packages.add(packageDoc.name());
    	    OptionProvider view = new PackageView(outputFolder, packageDoc, root, opt);
    	    UmlGraph.buildGraph(root, view, packageDoc);
    	    runGraphviz(opt.dotExecutable, outputFolder, packageDoc.name(), packageDoc.name(), root);
    	    alterHtmlDocs(opt, outputFolder, packageDoc.name(), packageDoc.name(),
    		    "package-summary.html", Pattern.compile("(</[Hh]2>)|(<h1 title=\"Package\").*"), root);
	    }
	}
    }

    /**
     * Generates the context diagram for a single class
     */
    private static void generateContextDiagrams(RootDoc root, Options opt, String outputFolder)
	    throws IOException {
        Set<ClassDoc> classDocs = new TreeSet<ClassDoc>(new Comparator<ClassDoc>() {
            public int compare(ClassDoc cd1, ClassDoc cd2) {
                return cd1.name().compareTo(cd2.name());
            }
        });
        for (ClassDoc classDoc : root.classes())
            classDocs.add(classDoc);

	ContextView view = null;
	for (ClassDoc classDoc : classDocs) {
	    if(view == null)
		view = new ContextView(outputFolder, classDoc, root, opt);
	    else
		view.setContextCenter(classDoc);
	    UmlGraph.buildGraph(root, view, classDoc);
	    runGraphviz(opt.dotExecutable, outputFolder, classDoc.containingPackage().name(), classDoc.name(), root);
	    alterHtmlDocs(opt, outputFolder, classDoc.containingPackage().name(), classDoc.name(),
		    classDoc.name() + ".html", Pattern.compile(".*(Class|Interface|Enum) " + classDoc.name() + ".*") , root);
	}
    }

    /**
     * Runs Graphviz dot building both a diagram (in png format) and a client side map for it.
     */
    private static void runGraphviz(String dotExecutable, String outputFolder, String packageName, String name, RootDoc root) {
    if (dotExecutable == null) {
      dotExecutable = "dot";
    }
	File dotFile = new File(outputFolder, packageName.replace(".", "/") + "/" + name + ".dot");
  File svgFile = new File(outputFolder, packageName.replace(".", "/") + "/" + name + ".svg");

	try {
	    Process p = Runtime.getRuntime().exec(new String [] {
		dotExecutable,
    "-Tsvg",
		"-o",
		svgFile.getAbsolutePath(),
		dotFile.getAbsolutePath()
	    });
	    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    String line;
	    while((line = reader.readLine()) != null)
		root.printWarning(line);
	    int result = p.waitFor();
	    if (result != 0)
		root.printWarning("Errors running Graphviz on " + dotFile);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("Ensure that dot is in your path and that its path does not contain spaces");
	}
    }

    //Format string for the uml image div tag.
    private static final String UML_DIV_TAG = 
	"<div align=\"center\">" +
	    "<object  width=\"100%%\" height=\"100%%\" type=\"image/svg+xml\" data=\"%1$s.svg\" alt=\"Package class diagram package %1$s\" border=0></object>" +
	"</div>";
    
    private static final String EXPANDABLE_UML_STYLE = "font-family: Arial,Helvetica,sans-serif;font-size: 1.5em; display: block; width: 250px; height: 20px; background: #009933; padding: 5px; text-align: center; border-radius: 8px; color: white; font-weight: bold;";

    //Format string for the java script tag.
    private static final String EXPANDABLE_UML = 
	"<script type=\"text/javascript\">\n" + 
	"function show() {\n" + 
	"    document.getElementById(\"uml\").innerHTML = \n" + 
	"        \'<a style=\"" + EXPANDABLE_UML_STYLE + "\" href=\"javascript:hide()\">%3$s</a>\' +\n" +
	"        \'%1$s\';\n" + 
	"}\n" + 
	"function hide() {\n" + 
	"	document.getElementById(\"uml\").innerHTML = \n" + 
	"	\'<a style=\"" + EXPANDABLE_UML_STYLE + "\" href=\"javascript:show()\">%2$s</a>\' ;\n" +
	"}\n" + 
	"</script>\n" + 
	"<div id=\"uml\" >\n" + 
	"	<a href=\"javascript:show()\">\n" + 
	"	<a style=\"" + EXPANDABLE_UML_STYLE + "\" href=\"javascript:show()\">%2$s</a> \n" +
	"</div>";
    
    /**
     * Takes an HTML file, looks for the first instance of the specified insertion point, and
     * inserts the diagram image reference and a client side map in that point.
     */
    private static void alterHtmlDocs(Options opt, String outputFolder, String packageName, String className,
	    String htmlFileName, Pattern insertPointPattern, RootDoc root) throws IOException {
	// setup files
	File output = new File(outputFolder, packageName.replace(".", "/"));
	File htmlFile = new File(output, htmlFileName);
	File alteredFile = new File(htmlFile.getAbsolutePath() + ".uml");
	if (!htmlFile.exists()) {
	    System.err.println("Expected file not found: " + htmlFile.getAbsolutePath());
	    return;
	}

	// parse & rewrite
	BufferedWriter writer = null;
	BufferedReader reader = null;
	boolean matched = false;
	try {
	    writer = new BufferedWriter(new OutputStreamWriter(new
		    FileOutputStream(alteredFile), opt.outputEncoding));
	    reader = new BufferedReader(new InputStreamReader(new
		    FileInputStream(htmlFile), opt.outputEncoding));

	    String line;
	    while ((line = reader.readLine()) != null) {
		writer.write(line);
		writer.newLine();
		if (!matched && insertPointPattern.matcher(line).matches()) {
		    matched = true;
			
		    String tag = String.format(UML_DIV_TAG, className);
		    if (opt.collapsibleDiagrams)
		    	tag = String.format(EXPANDABLE_UML, tag, "Show UML class diagram", "Hide UML class diagram");
		    writer.write("<!-- UML diagram added by UMLGraph version " +
		    		Version.VERSION + 
				" (http://www.spinellis.gr/umlgraph/) -->");
		    writer.newLine();
		    writer.write(tag);
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
	    root.printNotice("Warning, could not find a line that matches the pattern '" + insertPointPattern.pattern() 
		    + "'.\n Class diagram reference not inserted");
	    alteredFile.delete();
	}
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
