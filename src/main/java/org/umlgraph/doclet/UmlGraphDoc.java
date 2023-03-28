package org.umlgraph.doclet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.umlgraph.doclet.util.ElementUtil;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;

/**
 * Chaining doclet that runs the standart Javadoc doclet first, and on success,
 * runs the generation of dot files by UMLGraph
 * 
 * @author wolf
 * 
 * @depend - - - WrappedClassDoc
 * @depend - - - WrappedRootDoc
 */
public class UmlGraphDoc implements Doclet {

    private Locale locale;
    private Reporter reporter;
    private Options options;
    private StandardDoclet standard;
    
    public UmlGraphDoc() {
        this.options = new Options();
        this.standard = new StandardDoclet();
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
        this.standard.init(locale, reporter);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<? extends Doclet.Option> getSupportedOptions() {
        Set<Doclet.Option> options = new HashSet<>(standard.getSupportedOptions());
        for (Doclet.Option opt : this.options.OPTIONS) {
            options.add(opt);
        }
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return standard.getSupportedSourceVersion();
    }

    /**
     * Standard doclet entry point
     * 
     * @param root
     * @return
     */
    @Override
    public boolean run(DocletEnvironment root) {
        reporter.print(Diagnostic.Kind.NOTE, "UmlGraphDoc version " + Version.VERSION + ", running the standard doclet");
        standard.run(root);
        reporter.print(Diagnostic.Kind.NOTE, "UmlGraphDoc version " + Version.VERSION + ", altering javadocs");
        try {
            Options opt = UmlGraph.buildOptions(root, this.options);
            // in javadoc enumerations are always printed
            opt.showEnumerations = true;
            opt.relativeLinksForSourcePackages = true;
            // enable strict matching for hide expressions
            opt.strictMatching = true;
            reporter.print(Diagnostic.Kind.NOTE, opt.toString());

            generatePackageDiagrams(root, reporter, opt, this.options.outputDirectory);
            generateContextDiagrams(root, reporter, opt, this.options.outputDirectory);
        } catch (Throwable t) {
            reporter.print(Diagnostic.Kind.WARNING, "Error: " + t.toString());
            t.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Generates the package diagrams for all of the packages that contain classes
     * among those returned by RootDoc.class()
     */
    private static void generatePackageDiagrams(DocletEnvironment root, Reporter reporter, Options opt, String outputFolder) throws IOException {
        Set<Name> packages = new HashSet<>();
        for (Element classDoc : root.getIncludedElements()) {
            PackageElement packageDoc = null;
            if (classDoc instanceof PackageElement) {
                packageDoc = (PackageElement) classDoc;
            } else if (classDoc instanceof TypeElement) {
                packageDoc = ElementUtil.getPackageOf(root, classDoc);
            } else {
                continue;
            }
            if (!packages.contains(packageDoc.getQualifiedName())) {
                packages.add(packageDoc.getQualifiedName());
                OptionProvider view = new PackageView(outputFolder, packageDoc, root, opt);
                UmlGraph.buildGraph(reporter, root, opt, view, packageDoc);
                runGraphviz(opt.dotExecutable, outputFolder, ElementUtil.getModuleOf(root, packageDoc), packageDoc.getQualifiedName(), packageDoc.getSimpleName(), reporter);
                alterHtmlDocs(opt, outputFolder, ElementUtil.getModuleOf(root, packageDoc), packageDoc.getQualifiedName(), packageDoc.getSimpleName(), "package-summary.html",
                        Pattern.compile("(</[Hh]2>)|(<h1 title=\"Package( |\")).*"), reporter);
            }
        }
    }

    /**
     * Generates the context diagram for a single class
     */
    private static void generateContextDiagrams(DocletEnvironment root, Reporter reporter, Options opt, String outputFolder) throws IOException {
        Set<TypeElement> classDocs = new TreeSet<>(new Comparator<>() {
            public int compare(TypeElement cd1, TypeElement cd2) {
                return cd1.getSimpleName().toString().compareTo(cd2.getSimpleName().toString());
            }
        });
        for (Element classDoc : root.getIncludedElements()) {
            if (classDoc instanceof TypeElement) {
                classDocs.add((TypeElement) classDoc);
            }
        }

        ContextView view = null;
        for (TypeElement classDoc : classDocs) {
            try {
                if (view == null) {
                    view = new ContextView(outputFolder, classDoc, root, opt);
                } else {
                    view.setContextCenter(classDoc);
                }
                UmlGraph.buildGraph(reporter, root, opt, view, classDoc);
                runGraphviz(opt.dotExecutable, outputFolder, ElementUtil.getModuleOf(root, classDoc), ElementUtil.getPackageOf(root, classDoc).getQualifiedName(), classDoc.getSimpleName(), reporter);
                alterHtmlDocs(opt, outputFolder, ElementUtil.getModuleOf(root, classDoc), ElementUtil.getPackageOf(root, classDoc).getQualifiedName(), classDoc.getSimpleName(),
                        classDoc.getSimpleName() + ".html",
                        Pattern.compile(".*(Class|Interface|Enum) " + classDoc.getSimpleName() + ".*"), reporter);
            } catch (Exception e) {
                throw new RuntimeException("Error generating " + classDoc.getSimpleName(), e);
            }
        }
    }

    /**
     * Runs Graphviz dot building both a diagram (in png format) and a client side
     * map for it.
     */
    private static void runGraphviz(String dotExecutable, String outputFolder, ModuleElement module, Name packageName, Name name, Reporter reporter) {
        if (dotExecutable == null) {
            dotExecutable = "dot";
        }
        String fileName = packageName.toString().replace(".", "/") + "/" + name;
        if (Runtime.version().major() > 10 && module != null) {
            fileName = module.getQualifiedName().toString() + '/' + fileName;
        }
        File dotFile = new File(outputFolder, fileName + ".dot");
        File svgFile = new File(outputFolder, fileName + ".svg");

        try {
            Process p = Runtime.getRuntime().exec(new String[] { dotExecutable, "-Tsvg", "-o",
                    svgFile.getAbsolutePath(), dotFile.getAbsolutePath() });
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                reporter.print(Diagnostic.Kind.WARNING, line);
            }
            int result = p.waitFor();
            if (result != 0) {
                reporter.print(Diagnostic.Kind.WARNING, "Errors running Graphviz on " + dotFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ensure that dot is in your path and that its path does not contain spaces");
        }
    }

    // Format string for the uml image div tag.
    private static final String UML_DIV_TAG = "<div align=\"center\">"
            + "<object width=\"100%%\" height=\"100%%\" type=\"image/svg+xml\" data=\"%1$s.svg\" alt=\"Package class diagram package %1$s\" border=0></object>"
            + "</div>";

    private static final String UML_AUTO_SIZED_DIV_TAG = "<div align=\"center\">"
            + "<object type=\"image/svg+xml\" data=\"%1$s.svg\" alt=\"Package class diagram package %1$s\" border=0></object>"
            + "</div>";

    private static final String EXPANDABLE_UML_STYLE = "font-family: Arial,Helvetica,sans-serif;font-size: 1.5em; display: block; width: 250px; height: 20px; background: #009933; padding: 5px; text-align: center; border-radius: 8px; color: white; font-weight: bold;";

    // Format string for the java script tag.
    private static final String EXPANDABLE_UML = "<script type=\"text/javascript\">\n" + "function show() {\n"
            + "    document.getElementById(\"uml\").innerHTML = \n" + "        \'<a style=\"" + EXPANDABLE_UML_STYLE
            + "\" href=\"javascript:hide()\">%3$s</a>\' +\n" + "        \'%1$s\';\n" + "}\n" + "function hide() {\n"
            + "	document.getElementById(\"uml\").innerHTML = \n" + "	\'<a style=\"" + EXPANDABLE_UML_STYLE
            + "\" href=\"javascript:show()\">%2$s</a>\' ;\n" + "}\n" + "</script>\n" + "<div id=\"uml\" >\n"
            + "	<a href=\"javascript:show()\">\n" + "	<a style=\"" + EXPANDABLE_UML_STYLE
            + "\" href=\"javascript:show()\">%2$s</a> \n" + "</div>";

    /**
     * Takes an HTML file, looks for the first instance of the specified insertion
     * point, and inserts the diagram image reference and a client side map in that
     * point.
     */
    private static void alterHtmlDocs(Options opt, String outputFolder, ModuleElement module, Name packageName, Name className,
            String htmlFileName, Pattern insertPointPattern, Reporter reporter) throws IOException {
        // setup files
        String prefix = Runtime.version().major() > 10 && module != null ? module.getQualifiedName().toString() + '/' : "";
        File output = new File(outputFolder, prefix + packageName.toString().replace(".", "/"));
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
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(alteredFile), opt.outputEncoding));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile), opt.outputEncoding));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                if (!matched && insertPointPattern.matcher(line).matches()) {
                    matched = true;

                    String tag;
                    if (opt.autoSize) {
                        tag = String.format(UML_AUTO_SIZED_DIV_TAG, className);
                    } else {
                        tag = String.format(UML_DIV_TAG, className);
                    }
                    if (opt.collapsibleDiagrams) {
                        tag = String.format(EXPANDABLE_UML, tag, "Show UML class diagram", "Hide UML class diagram");
                    }
                    writer.write("<!-- UML diagram added by UMLGraph version " + Version.VERSION + " (http://www.spinellis.gr/umlgraph/) -->");
                    writer.newLine();
                    writer.write(tag);
                    writer.newLine();
                }
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
        }

        // if altered, delete old file and rename new one to the old file name
        if (matched) {
            htmlFile.delete();
            alteredFile.renameTo(htmlFile);
        } else {
            reporter.print(Diagnostic.Kind.NOTE, "Warning, could not find a line that matches the pattern '"
                    + insertPointPattern.pattern() + "'.\n Class diagram reference not inserted");
            alteredFile.delete();
        }
    }
}
