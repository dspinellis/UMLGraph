/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2010 Diomidis Spinellis
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

package org.umlgraph.doclet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import org.umlgraph.doclet.util.TagUtil;

import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.Doclet;

/**
 * Represent the program options
 * 
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
public class Options implements Cloneable, OptionProvider {
    
    public final Set<? extends Doclet.Option> OPTIONS = Set.of(
        new Option("--d", true, "Specify the output directory (defaults to the current directory).", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                outputDirectory = arguments.get(0);
                return true;
            }
        },
        new Option("-qualify", false, "Produce fully-qualified class names.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showQualified = true;
                return true;
            }
        },
        new Option("-qualifyGenerics", false, "Use fully-qualified class names in Java generics.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showQualifiedGenerics = true;
                return true;
            }
        },
        new Option("-hideGenerics", false, "FIXME Missing doc", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                hideGenerics = true;
                return true;
            }
        },
        new Option("-horizontal", false, "Layout the graph in the horizontal direction.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                horizontal = true;
                return true;
            }
        },
        new Option("-attributes", false, "Show class attributes (Java fields)", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showAttributes = true;
                return true;
            }
        },
        new Option("-enumconstants", false, "When showing enumerations, also show the values they can take", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showEnumConstants = true;
                return true;
            }
        },
        new Option("-operations", false, "Show class operations (Java methods)", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showOperations = true;
                return true;
            }
        },
        new Option("-enumerations", false, "Show enumarations as separate stereotyped primitive types", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showEnumerations = true;
                return true;
            }
        },
        new Option("-constructors", false, "Show a class's constructors", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showConstructors = true;
                return true;
            }
        },
        new Option("-visibility", false, "Adorn class elements according to their visibility (private, public, protected, package)", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showVisibility = true;
                return true;
            }
        },
        new Option("-types", false, "Add type information to attributes and operations", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showType = true;
                return true;
            }
        },
        new Option("-autosize", false, "Fits generated graph to the width of the page/window. Defaults to true.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                autoSize = true;
                return true;
            }
        },
        new Option("-commentname", false, "Name the element using the text in the javadoc comment, instead of the name of its class.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                showComment = true;
                return true;
            }
        },
        new Option("-all", false, "Same as -attributes -operations -visibility -types -enumerations -enumconstants", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                setAll();
                return true;
            }
        },
        new Option("--bgcolor", true, "Specify the graph's background color.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                bgColor = arguments.get(0);
                return true;
            }
        },
        new Option("--edgecolor", true, "Specify the color for drawing edges.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                edgeColor = arguments.get(0);
                return true;
            }
        },
        new Option("--edgefontcolor", true, "Specify the font color to use for edge labels.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                edgeFontColor = arguments.get(0);
                return true;
            }
        },
        new Option("--edgefontname", true, "Specify the font name to use for edge labels.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                edgeFontName = arguments.get(0);
                return true;
            }
        },
        new Option("-edgefontsize", true, "Specify the font size to use for edge labels.", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                edgeFontSize = Double.parseDouble(arguments.get(0));
                return true;
            }
        },
        new Option("-nodefontcolor", true, "Specify the font color to use inside nodes", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontColor = arguments.get(0);
                return true;
            }
        },
        new Option("-nodefontname", true, "Specify the font name to use inside nodes", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontName = arguments.get(0);
                return true;
            }
        },
        new Option("-nodefontabstractitalic", false, "FIXME no documentation", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontAbstractItalic = true;
                return true;
            }
        },
        new Option("-nodefontsize", true, "Specify the font size to use inside nodes", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontSize = Double.parseDouble(arguments.get(0));
                return true;
            }
        },
        new Option("-nodefontclassname", true, "Specify the font name to use for the class names", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontClassName = arguments.get(0);
                return true;
            }
        },
        new Option("-nodefontclasssize", true, "Specify the font size to use for the class names.", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontClassSize = Double.parseDouble(arguments.get(0));
                return true;
            }
        },
        new Option("-nodefonttagname", true, "Specify the font name to use for the tag names.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontTagName = arguments.get(0);
                return true;
            }
        },
        new Option("-nodefonttagsize", true, "Specify the font size to use for the tag names", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontTagSize = Double.parseDouble(arguments.get(0));
                return true;
            }
        },
        new Option("-nodefontpackagename", true, "Specify the font name to use for the package names (used only when the package name is postfixed, see -postfixpackage).", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontPackageName = arguments.get(0);
                return true;
            }
        },
        new Option("-nodefontpackagesize", true, "Specify the font size to use for the package names (used only when it package name is postfixed, see -postfixpackage).", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFontPackageSize = Double.parseDouble(arguments.get(0));
                return true;
            }
        },
        new Option("-nodefillcolor", true, "Specify the color to use to fill the shapes", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                nodeFillColor = arguments.get(0);
                return true;
            }
        },
        new Option("-shape", true, "Specify the shape to use for the rendered element(s).\n"
                + " The following UML shapes are available: class (default), node,"
                + " component, package, collaboration, usecase, activeclass", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                shape = Shape.of(arguments.get(0));
                return true;
            }
        },
        new Option("--output", true, "Specify the output file (default <code>graph.dot</code>).\n"
                + "If the output directory is provided, -output can only specify a file name,\n"
                + "otherwise a full path is accepted as well.\n"
                + "If the filename specified is a dash, then the results are printed on the\n"
                + "standard output, and can be directly piped into <em>dot</em>.\n"
                + "Note that, in order to avoid <em>javadoc</em> messages to contaminate\n"
                + "UMLGraph's output, you must execute UMLGraph directly as a jar,\n"
                + "not through <em>javadoc</em>.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                outputFileName = arguments.get(0);
                return true;
            }
        },
        new Option("-outputencoding", true, "Specify the output encoding character set (default <code>UTF-8</code>).\n"
                + "When using <em>dot</em> to generate SVG diagrams you should specify\n"
                + "<code>UTF-8</code> as the output encoding, to have guillemots correctly\n"
                + "appearing in the resulting SVG.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                outputEncoding = arguments.get(0);
                return true;
            }
        },
        new Option("--hide", true, "Specify entities to hide from the graph."
                + " Matching is done using a non-anchored regular match."
                + " For instance, \"<code>-hide (Big|\\.)Widget</code>\" would hide \"<code>com.foo.widgets.Widget</code>\" and "
                + " \"<code>com.foo.widgets.BigWidget</code>\". Can also be used without arguments, "
                + " in this case it will hide everything (useful in the context of views "
                + " to selectively unhide some portions of the graph, see the view chapter for "
                + " further details).", "<pattern>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                if (arguments == null || arguments.isEmpty()) {
                    hidePatterns.clear();
                    hidePatterns.add(allPattern);
                } else {
                    try {
                        hidePatterns.add(Pattern.compile(arguments.get(0)));
                    } catch (PatternSyntaxException e) {
                        System.err.println("Skipping invalid pattern " + arguments.get(0));
                    }
                }
                return true;
            }
        },
        new Option("--include", true, "Match classes to include with a non-anchored match. This is weaker than\n"
                + " the <code>-hide</code> option, but can be used to include classes from foreign packages\n"
                + " in the package view (which would by default filter to only include package members).", "<pattern>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                try {
                    includePatterns.add(Pattern.compile(arguments.get(0)));
                } catch (PatternSyntaxException e) {
                    System.err.println("Skipping invalid pattern " + arguments.get(0));
                }
                return true;
            }
        },
        new Option("-apidocroot", true, "Specify the URL that should be used as the \"root\" for local classes.\n"
                + "This URL will be used as a prefix, to which the page name for the local class or\n"
                + "package will be appended (following the JavaDoc convention).\n"
                + "For example, if the value <code>http://www.acme.org/apidocs</code> is\n"
                + "provided, the class <code>org.acme.util.MyClass</code> will be mapped to the URL\n"
                + "<code>http://www.acme.org/apidocs/org/acme/util/MyClass.html</code>.\n"
                + "This URL will then be added to .dot diagram and can be surfaced in the\n"
                + "final class diagram by setting the output to SVG, or by creating an HTML page\n"
                + "that associates the diagram static image (a .gif or .png) with a client-side\n"
                + "image map.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                apiDocRoot = fixApiDocRoot(arguments.get(0));
                return true;
            }
        },
        new Option("-apidocmap", true, "Specify the file name of the URL mapping table. \n"
                + "The is a standard Java property file, where the property name is a regular\n"
                + "expression (as defined in the java.util.regex package) and the property value is\n"
                + "an URL \"root\" as described above.\n"
                + "This table is used to resolved external class names (class names that do not\n"
                + "belong to the current package being processed by UMLGraph). If no file is provided,\n"
                + "external classes will just be mapped to the on-line Java API documentation.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                setApiDocMapFile(arguments.get(0));
                return true;
            }
        },
        new Option("-noguillemot", false, "Specify that guillemot characters should not be used to denote "
                + "special terms like \"interface\" and stereotype names."
                + "This is used on some platforms to circumvent problems associated with displaying non-ASCII characters.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                guilOpen = "&lt;&lt;";
                guilClose = "&gt;&gt;";
                return true;
            }
        },
        new Option("-view", true, "Specify the fully qualified name of a class that contains\n"
                + " a view definition. Only the class diagram specified by this view will be generated. \n"
                + " <br/>See the views chapter for more details.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                viewName = arguments.get(0);
                return true;
            }
        },
        new Option("-views", false, "Generate a class diagram for every view found in the source path..", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                findViews = true;
                return true;
            }
        },
        new Option("-inferrel", false, "Try to automatically infer relationships between classes by inspecting "
                + "field values. See the class diagram inference chapter for further details. Disabled by default.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                inferRelationships = true;
                return true;
            }
        },
        new Option("-inferreltype", true, "The type of relationship inferred when -inferrel is activated. \n"
                + "Defaults to \"navassoc\" (see the class modelling chapter for a list of relationship types).", "<RelationType>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                try {
                    inferRelationshipType = RelationType.valueOf(arguments.get(0).toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown association type " + arguments.get(0));
                }
                return true;
            }
        },
        new Option("-inferdepvis", true, "Specifies the lowest visibility level of elements used to infer\n"
                + "dependencies among classes. Possible values are private, package, protected, public, in this\n"
                + "order. The default value is private. Use higher levels to limit the number of inferred dependencies", "<Visibility>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                try {
                    inferDependencyVisibility = Visibility.valueOf(arguments.get(0).toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Ignoring invalid visibility specification for " + "dependency inference: " + arguments.get(0));
                }
                return true;
            }
        },
        new Option("-collapsible", false, "Enhance the javadoc HTML files containing UML diagrams with Javascript "
                + "that provides a link for showing the (initially collapsed) diagrams.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                collapsibleDiagrams = true;
                return true;
            }
        },
        new Option("-inferdep", false, "Try to automatically infer dependencies between classes by inspecting "
                + "methods and fields. See the class diagram inference chapter for more details. Disabled by default.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                inferDependencies = true;
                return true;
            }
        },
        new Option("-inferdepinpackage", false, "Enable or disable dependency inference among classes in the\n"
                + "same package. This option is disabled by default, because classes in the same package are supposed\n"
                + "to be related anyway, and also because there's no working mechanism to actually detect all\n"
                + "of these dependencies since imports are not required to use classes in the same package.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                inferDepInPackage = true;
                return true;
            }
        },
        new Option("-hideprivateinner", false, "FIXME NO DOC", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                hidePrivateInner = true;
                return true;
            }
        },
        new Option("-useimports", false, "Will also use imports to infer dependencies. \n"
                + "Disabled by default, since it does not work properly if there are multiple\n"
                + "classes in the same source file (will add dependencies to every class in\n"
                + "the source file).", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                System.err.println("useimports has been set but will have no effect since not (yet) available");
                useImports = true;
                return true;
            }
        },
        new Option("--collpackages", true, "Specify the classes that will be treated as "
                + "containers for one to many relationships when inference is enabled. "
                + "Matching is done using a non-anchored regular match. Empty by default.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                try {
                    collPackages.add(Pattern.compile(arguments.get(0)));
                } catch (PatternSyntaxException e) {
                    System.err.println("Skipping invalid pattern " + arguments.get(0));
                }
                return true;
            }
        },
        new Option("-compact", false, "Generate compact dot files, that is, print HTML labels\n"
                + "in a single line instead of \"pretty printing\" them. Useful if the dot file\n"
                + "has to be manipulated by an automated tool\n"
                + "(e.g., the UMLGraph regression test suite).", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                compact = true;
                return true;
            }
        },
        new Option("-postfixpackage", false, "When using qualified class names, put the package name in the line after the class name, in order to reduce the width of class nodes.", null) {
            @Override
            public boolean process(String option, List<String> arguments) {
                postfixPackage = true;
                return true;
            }
        },
        
        new Option("--link", true, "A clone of the standard doclet\n"
                + "<a href=\"http://java.sun.com/j2se/1.5.0/docs/tooldocs/windows/javadoc.html#link\">-link</a>\n"
                + "option, allows UMLGraph to generate links from class symbols to their external javadoc\n"
                + "documentation (image maps are automatically generated in UMLGraphDoc, you'll have to generate them\n"
                + "manually with graphviz if using UMLGraph).", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                System.err.println("add -link option with args : " + arguments.get(0));
                addApiDocRoots(arguments.get(0));
                return true;
            }
        },
        new Option("-linkoffline", 2, "Specify\n"
                + "links to javadoc-generated documentation for external referenced classes.\n"
                + "The <code>-linkoffline</code> option takes two arguments:\n"
                + "the first for the string to be embedded in the <code>href</code>\n"
                + "links, the second telling it where to find the <code>package-list</code>.\n"
                + "Example:\n"
                + "<pre>\n"
                + "-linkoffline http://developer.android.com/reference file:/home/doc/android/\n"
                + "</pre>\n"
                + "See the <em>javadoc</em> documentation for more details.", "<string> <string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                addApiDocRootsOffline(arguments.get(0), arguments.get(1));
                return true;
            }
        },
        new Option("-contextPattern", 2, "FIXME MISSING DOC.", "<string> <string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                RelationDirection d;
                RelationType rt;
                try {
                    d = RelationDirection.valueOf(arguments.get(1).toUpperCase());
                    if ("all".equalsIgnoreCase(arguments.get(0))) {
                        contextRelationPattern = new RelationPattern(d);
                    } else {
                        rt = RelationType.valueOf(arguments.get(0).toUpperCase());
                        contextRelationPattern.addRelation(rt, d);
                    }
                } catch (IllegalArgumentException e) {

                }
                return true;
            }
        },
        new Option("-nodesep", true, "Specify the horizontal separation between the class nodes (0.25 by default).  Decreasing this can make a diagram more compact.", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                try {
                    nodeSep = Double.parseDouble(arguments.get(0));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid nodesep " + arguments.get(0));
                }
                return true;
            }
        },
        new Option("-ranksep", true, "Specify the vertical separation between the class nodes (0.5 by default).  Decreasing this can make a diagram more compact.", "<double>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                try {
                    rankSep = Double.parseDouble(arguments.get(0));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid ranksep " + arguments.get(0));
                }
                return true;
            }
        },
        new Option("-dotexecutable", true, "Specify the path of the <em>dot</em> executable.", "<string>") {
            @Override
            public boolean process(String option, List<String> arguments) {
                dotExecutable = arguments.get(0);
                return true;
            }
        }

        
    );
    
    // reused often, especially in UmlGraphDoc, worth creating just once and reusing
    private static final Pattern allPattern = Pattern.compile(".*");
    protected static final String DEFAULT_EXTERNAL_APIDOC = "https://docs.oracle.com/javase/9/docs/api/";

    // instance fields
    List<Pattern> hidePatterns = new ArrayList<>();
    List<Pattern> includePatterns = new ArrayList<>();
    boolean showQualified = false;
    boolean showQualifiedGenerics = false;
    boolean hideGenerics = false;
    boolean showAttributes = false;
    boolean showEnumerations = false;
    boolean showEnumConstants = false;
    boolean showOperations = false;
    boolean showConstructors = false;
    boolean showVisibility = false;
    boolean horizontal;
    boolean showType = false;
    boolean showComment = false;
    boolean autoSize = true;
    String edgeFontName = Font.DEFAULT_FONT;
    String edgeFontColor = "black";
    String edgeColor = "black";
    double edgeFontSize = 10;
    String nodeFontName = Font.DEFAULT_FONT;
    boolean nodeFontAbstractItalic = true;
    String nodeFontColor = "black";
    double nodeFontSize = 10;
    String nodeFillColor = null;
    double nodeFontClassSize = -1;
    String nodeFontClassName = null;
    double nodeFontTagSize = -1;
    String nodeFontTagName = null;
    double nodeFontPackageSize = -1;
    String nodeFontPackageName = null;
    Shape shape = Shape.CLASS;
    String bgColor = null;
    public String outputFileName = "graph.dot";
    String outputEncoding = StandardCharsets.UTF_8.name();
    Map<Pattern, String> apiDocMap = new HashMap<>();
    String apiDocRoot = null;
    boolean postfixPackage = false;
    boolean useGuillemot = true;
    boolean findViews = false;
    String viewName = null;
    double nodeSep = 0.25;
    double rankSep = 0.5;
    public String outputDirectory = ".";
    /*
     * Numeric values are preferable to symbolic here. Symbolic reportedly fail on
     * MacOSX, and also are more difficult to verify with XML tools.
     */
    /** Guillemot left (open) */
    String guilOpen = "&#171;"; // &laquo; \u00ab
    /** Guillemot right (close) */
    String guilClose = "&#187;"; // &raquo; \u00bb
    boolean inferRelationships = false;
    boolean inferDependencies = false;
    boolean collapsibleDiagrams = false;
    RelationPattern contextRelationPattern = new RelationPattern(RelationDirection.BOTH);
    boolean useImports = false;
    Visibility inferDependencyVisibility = Visibility.PRIVATE;
    boolean inferDepInPackage = false;
    RelationType inferRelationshipType = RelationType.NAVASSOC;
    private List<Pattern> collPackages = new ArrayList<>();
    boolean compact = false;
    boolean hidePrivateInner = false;
    // internal option, used by UMLDoc to generate relative links between classes
    boolean relativeLinksForSourcePackages = false;
    // internal option, used by UMLDoc to force strict matching on the class names
    // and avoid problems with packages in the template declaration making UmlGraph hide
    // classes outside of them (for example, class gr.spinellis.Foo<T extends java.io.Serializable>
    // would have been hidden by the hide pattern "java.*"
    boolean strictMatching = true;
    String dotExecutable = "dot";

    Options() {
    }

    @Override
    public Options clone() {
        Options clone = null;
        try {
            clone = (Options) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cannot clone?!?", e); // Should not happen
        }
        // deep clone the hide and collection patterns
        clone.hidePatterns = new ArrayList<>(hidePatterns);
        clone.includePatterns = new ArrayList<>(includePatterns);
        clone.collPackages = new ArrayList<>(collPackages);
        clone.apiDocMap = new HashMap<>(apiDocMap);
        return clone;
    }

    /** Most complete output */
    public void setAll() {
        showAttributes = true;
        showEnumerations = true;
        showEnumConstants = true;
        showOperations = true;
        showConstructors = true;
        showVisibility = true;
        showType = true;
    }

    /**
     * Match strings, ignoring leading <code>-</code>, <code>--</code>, <code>-!</code>, and <code>!</code>.
     *
     * @param given  Given string
     * @param expect Expected string
     * @return {@code true} on success
     */
    protected static boolean matchOption(String given, String expect) {
        return matchOption(given, expect, false);
    }

    /**
     * Match strings, ignoring leading <code>-</code>, <code>--</code>, <code>-!</code>, and <code>!</code>.
     *
     * @param given    Given string
     * @param expect   Expected string
     * @param negative May be negative
     * @return {@code true} on success
     */
    protected static boolean matchOption(String given, String expect, boolean negative) {
        int begin = 0, end = given.length();
        if (begin < end && given.charAt(begin) == '-') {
            ++begin;
        }
        // double dashed option
        if (begin < end && given.charAt(begin) == '-') {
            ++begin;
        }
        if (negative && begin < end && given.charAt(begin) == '!') {
            ++begin;
        }
        return expect.length() == end - begin && expect.regionMatches(0, given, begin, end - begin);
    }

    /**
     * Return the number of arguments associated with the specified option. The
     * return value includes the actual option. Will return 0 if the option is not
     * supported.
     */
    public static int optionLength(String option) {
        if (matchOption(option, "qualify", true) || matchOption(option, "qualifyGenerics", true)
                || matchOption(option, "hideGenerics", true) || matchOption(option, "horizontal", true)
                || matchOption(option, "all") || matchOption(option, "attributes", true)
                || matchOption(option, "enumconstants", true) || matchOption(option, "operations", true)
                || matchOption(option, "enumerations", true) || matchOption(option, "constructors", true)
                || matchOption(option, "visibility", true) || matchOption(option, "types", true)
                || matchOption(option, "autosize", true) || matchOption(option, "commentname", true)
                || matchOption(option, "nodefontabstractitalic", true) || matchOption(option, "postfixpackage", true)
                || matchOption(option, "noguillemot", true) || matchOption(option, "views", true)
                || matchOption(option, "inferrel", true) || matchOption(option, "useimports", true)
                || matchOption(option, "collapsible", true) || matchOption(option, "inferdep", true)
                || matchOption(option, "inferdepinpackage", true) || matchOption(option, "hideprivateinner", true)
                || matchOption(option, "compact", true)) {

            return 1;
        } else if (matchOption(option, "nodefillcolor") || matchOption(option, "nodefontcolor")
                || matchOption(option, "nodefontsize") || matchOption(option, "nodefontname")
                || matchOption(option, "nodefontclasssize") || matchOption(option, "nodefontclassname")
                || matchOption(option, "nodefonttagsize") || matchOption(option, "nodefonttagname")
                || matchOption(option, "nodefontpackagesize") || matchOption(option, "nodefontpackagename")
                || matchOption(option, "edgefontcolor") || matchOption(option, "edgecolor")
                || matchOption(option, "edgefontsize") || matchOption(option, "edgefontname")
                || matchOption(option, "shape") || matchOption(option, "output")
                || matchOption(option, "outputencoding") || matchOption(option, "bgcolor")
                || matchOption(option, "hide") || matchOption(option, "include") || matchOption(option, "apidocroot")
                || matchOption(option, "apidocmap") || matchOption(option, "d") || matchOption(option, "view")
                || matchOption(option, "inferreltype") || matchOption(option, "inferdepvis")
                || matchOption(option, "collpackages") || matchOption(option, "nodesep")
                || matchOption(option, "ranksep") || matchOption(option, "dotexecutable")
                || matchOption(option, "link")) {
            return 2;
        } else if (matchOption(option, "contextPattern") || matchOption(option, "linkoffline")) {
            return 3;
        } else {
            return 0;
        }
    }

    /** Set the options based on a single option and its arguments */
    void setOption(String[] opt) {
        if (!matchOption(opt[0], "hide") && optionLength(opt[0]) > opt.length) {
            System.err.println("Skipping option '" + opt[0] + "', missing argument");
            return;
        }
        boolean dash = opt[0].length() > 1 && opt[0].charAt(0) == '-';
        boolean positive = !(opt[0].length() > 1 && opt[0].charAt(dash ? 1 : 0) == '!');

        if (matchOption(opt[0], "qualify", true)) {
            showQualified = positive;
        } else if (matchOption(opt[0], "qualifyGenerics", true)) {
            showQualifiedGenerics = positive;
        } else if (matchOption(opt[0], "hideGenerics", true)) {
            hideGenerics = positive;
        } else if (matchOption(opt[0], "horizontal", true)) {
            horizontal = positive;
        } else if (matchOption(opt[0], "attributes", true)) {
            showAttributes = positive;
        } else if (matchOption(opt[0], "enumconstants", true)) {
            showEnumConstants = positive;
        } else if (matchOption(opt[0], "operations", true)) {
            showOperations = positive;
        } else if (matchOption(opt[0], "enumerations", true)) {
            showEnumerations = positive;
        } else if (matchOption(opt[0], "constructors", true)) {
            showConstructors = positive;
        } else if (matchOption(opt[0], "visibility", true)) {
            showVisibility = positive;
        } else if (matchOption(opt[0], "types", true)) {
            showType = positive;
        } else if (matchOption(opt[0], "autoSize", true)) {
            autoSize = positive;
        } else if (matchOption(opt[0], "commentname", true)) {
            showComment = positive;
        } else if (matchOption(opt[0], "all")) {
            setAll();
        } else if (matchOption(opt[0], "bgcolor", true)) {
            bgColor = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "edgecolor", true)) {
            edgeColor = positive ? opt[1] : "black";
        } else if (matchOption(opt[0], "edgefontcolor", true)) {
            edgeFontColor = positive ? opt[1] : "black";
        } else if (matchOption(opt[0], "edgefontname", true)) {
            edgeFontName = positive ? opt[1] : Font.DEFAULT_FONT;
        } else if (matchOption(opt[0], "edgefontsize", true)) {
            edgeFontSize = positive ? Double.parseDouble(opt[1]) : 10;
        } else if (matchOption(opt[0], "nodefontcolor", true)) {
            nodeFontColor = positive ? opt[1] : "black";
        } else if (matchOption(opt[0], "nodefontname", true)) {
            nodeFontName = positive ? opt[1] : Font.DEFAULT_FONT;
        } else if (matchOption(opt[0], "nodefontabstractitalic", true)) {
            nodeFontAbstractItalic = positive;
        } else if (matchOption(opt[0], "nodefontsize", true)) {
            nodeFontSize = positive ? Double.parseDouble(opt[1]) : 10;
        } else if (matchOption(opt[0], "nodefontclassname", true)) {
            nodeFontClassName = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "nodefontclasssize", true)) {
            nodeFontClassSize = positive ? Double.parseDouble(opt[1]) : -1;
        } else if (matchOption(opt[0], "nodefonttagname", true)) {
            nodeFontTagName = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "nodefonttagsize", true)) {
            nodeFontTagSize = positive ? Double.parseDouble(opt[1]) : -1;
        } else if (matchOption(opt[0], "nodefontpackagename", true)) {
            nodeFontPackageName = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "nodefontpackagesize", true)) {
            nodeFontPackageSize = positive ? Double.parseDouble(opt[1]) : -1;
        } else if (matchOption(opt[0], "nodefillcolor", true)) {
            nodeFillColor = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "shape", true)) {
            shape = positive ? Shape.of(opt[1]) : Shape.CLASS;
        } else if (matchOption(opt[0], "output", true)) {
            outputFileName = positive ? opt[1] : "graph.dot";
        } else if (matchOption(opt[0], "outputencoding", true)) {
            outputEncoding = positive ? opt[1] : "ISO-8859-1";
        } else if (matchOption(opt[0], "hide", true)) {
            if (positive) {
                if (opt.length == 1) {
                    hidePatterns.clear();
                    hidePatterns.add(allPattern);
                } else {
                    try {
                        hidePatterns.add(Pattern.compile(opt[1]));
                    } catch (PatternSyntaxException e) {
                        System.err.println("Skipping invalid pattern " + opt[1]);
                    }
                }
            } else
                hidePatterns.clear();
        } else if (matchOption(opt[0], "include", true)) {
            if (positive) {
                try {
                    includePatterns.add(Pattern.compile(opt[1]));
                } catch (PatternSyntaxException e) {
                    System.err.println("Skipping invalid pattern " + opt[1]);
                }
            } else
                includePatterns.clear();
        } else if (matchOption(opt[0], "apidocroot", true)) {
            apiDocRoot = positive ? fixApiDocRoot(opt[1]) : null;
        } else if (matchOption(opt[0], "apidocmap", true)) {
            if (positive)
                setApiDocMapFile(opt[1]);
            else
                apiDocMap.clear();
        } else if (matchOption(opt[0], "noguillemot", true)) {
            guilOpen = positive ? "&lt;&lt;" : "\u00ab";
            guilClose = positive ? "&gt;&gt;" : "\u00bb";
        } else if (matchOption(opt[0], "view", true)) {
            viewName = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "views", true)) {
            findViews = positive;
        } else if (matchOption(opt[0], "d", true)) {
            outputDirectory = positive ? opt[1] : null;
        } else if (matchOption(opt[0], "inferrel", true)) {
            inferRelationships = positive;
        } else if (matchOption(opt[0], "inferreltype", true)) {
            if (positive) {
                try {
                    inferRelationshipType = RelationType.valueOf(opt[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown association type " + opt[1]);
                }
            } else
                inferRelationshipType = RelationType.NAVASSOC;
        } else if (matchOption(opt[0], "inferdepvis", true)) {
            if (positive) {
                try {
                    Visibility vis = Visibility.valueOf(opt[1].toUpperCase());
                    inferDependencyVisibility = vis;
                } catch (IllegalArgumentException e) {
                    System.err.println(
                            "Ignoring invalid visibility specification for " + "dependency inference: " + opt[1]);
                }
            } else
                inferDependencyVisibility = Visibility.PRIVATE;
        } else if (matchOption(opt[0], "collapsible", true)) {
            collapsibleDiagrams = positive;
        } else if (matchOption(opt[0], "inferdep", true)) {
            inferDependencies = positive;
        } else if (matchOption(opt[0], "inferdepinpackage", true)) {
            inferDepInPackage = positive;
        } else if (matchOption(opt[0], "hideprivateinner", true)) {
            hidePrivateInner = positive;
        } else if (matchOption(opt[0], "useimports", true)) {
            useImports = positive;
        } else if (matchOption(opt[0], "collpackages", true)) {
            if (positive) {
                try {
                    collPackages.add(Pattern.compile(opt[1]));
                } catch (PatternSyntaxException e) {
                    System.err.println("Skipping invalid pattern " + opt[1]);
                }
            } else
                collPackages.clear();
        } else if (matchOption(opt[0], "compact", true)) {
            compact = positive;
        } else if (matchOption(opt[0], "postfixpackage", true)) {
            postfixPackage = positive;
        } else if (matchOption(opt[0], "link")) {
            addApiDocRoots(opt[1]);
        } else if (matchOption(opt[0], "linkoffline")) {
            addApiDocRootsOffline(opt[1], opt[2]);
        } else if (matchOption(opt[0], "contextPattern")) {
            RelationDirection d;
            RelationType rt;
            try {
                d = RelationDirection.valueOf(opt[2].toUpperCase());
                if (opt[1].equalsIgnoreCase("all")) {
                    contextRelationPattern = new RelationPattern(d);
                } else {
                    rt = RelationType.valueOf(opt[1].toUpperCase());
                    contextRelationPattern.addRelation(rt, d);
                }
            } catch (IllegalArgumentException e) {

            }

        } else if (matchOption(opt[0], "nodesep", true)) {
            try {
                nodeSep = positive ? Double.parseDouble(opt[1]) : 0.25;
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid nodesep " + opt[1]);
            }
        } else if (matchOption(opt[0], "ranksep", true)) {
            try {
                rankSep = positive ? Double.parseDouble(opt[1]) : 0.5;
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid ranksep " + opt[1]);
            }
        } else if (matchOption(opt[0], "dotexecutable")) {
            dotExecutable = opt[1];
        } else
            ; // Do nothing, javadoc will handle the option or complain, if needed.
    }

    /**
     * Adds api doc roots from a link. The folder reffered by the link should
     * contain a package-list file that will be parsed in order to add api doc roots
     * to this configuration
     * 
     * @param packageListUrl
     */
    private void addApiDocRoots(String packageListUrl) {
        tryFetch(null, packageListUrl);
    }

    private void tryFetch(String docUrl, String packageListUrl) {
        BufferedReader br = null;
        packageListUrl = fixApiDocRoot(packageListUrl);
        String finalDocUrl = docUrl == null ? null : fixApiDocRoot(docUrl);
        for (String suffix : List.of("package-list", "element-list")) {
            try {
                URL url = new URL(packageListUrl + suffix);
                br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith("module:")) {
                        line = line + ".";
                        Pattern pattern = Pattern.compile(line.replace(".", "\\.") + "[^\\.]*");
                        apiDocMap.put(pattern, finalDocUrl == null ? packageListUrl : finalDocUrl);
                    }
                }
                break;
            } catch (IOException e) {
                System.err.println("Errors happened while accessing the " + suffix + " file at " + packageListUrl);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * Adds api doc roots from an offline link. The folder specified by
     * packageListUrl should contain the package-list associed with the docUrl
     * folder.
     * 
     * @param docUrl         folder containing the javadoc
     * @param packageListUrl folder containing the package-list
     */
    private void addApiDocRootsOffline(String docUrl, String packageListUrl) {
        tryFetch(docUrl, packageListUrl);
    }

    /**
     * Loads the property file referred by <code>apiDocMapFileName</code> and fills
     * the apiDocMap accordingly
     * 
     * @param apiDocMapFileName
     */
    void setApiDocMapFile(String apiDocMapFileName) {
        try {
            InputStream is = new FileInputStream(apiDocMapFileName);
            Properties userMap = new Properties();
            userMap.load(is);
            for (Map.Entry<?, ?> mapEntry : userMap.entrySet()) {
                try {
                    String thisRoot = (String) mapEntry.getValue();
                    if (thisRoot != null) {
                        thisRoot = fixApiDocRoot(thisRoot);
                        apiDocMap.put(Pattern.compile((String) mapEntry.getKey()), thisRoot);
                    } else {
                        System.err.println("No URL for pattern " + mapEntry.getKey());
                    }
                } catch (PatternSyntaxException e) {
                    System.err.println("Skipping bad pattern " + mapEntry.getKey());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + apiDocMapFileName + " was not found: " + e);
        } catch (IOException e) {
            System.err.println("Error reading the property api map file " + apiDocMapFileName + ": " + e);
        }
    }

    /**
     * Returns the appropriate URL "root" for an external class name. It will match
     * the class name against the regular expressions specified in the
     * <code>apiDocMap</code>; if a match is found, the associated URL will be
     * returned.
     * <p>
     * <b>NOTE:</b> The match order of the match attempts is the one specified by
     * the constructor of the api doc root, so it depends on the order of "-link"
     * and "-apiDocMap" parameters.
     */
    public String getApiDocRoot(Name className) {
        if (apiDocMap.isEmpty()) {
            apiDocMap.put(Pattern.compile(".*"), DEFAULT_EXTERNAL_APIDOC);
        }

        for (Map.Entry<Pattern, String> mapEntry : apiDocMap.entrySet()) {
            if (mapEntry.getKey().matcher(className).matches()) {
                return mapEntry.getValue();
            }
        }
        return null;
    }

    /** Trim and append a file separator to the string */
    private String fixApiDocRoot(String str) {
        if (str == null) {
            return null;
        }
        String fixed = str.trim();
        if (fixed.isEmpty()) {
            return "";
        }
        if (File.separatorChar != '/') {
            fixed = fixed.replace(File.separatorChar, '/');
        }
        if (!fixed.endsWith("/")) {
            fixed = fixed + "/";
        }
        return fixed;
    }

    /** Set the options based on the tag elements of the ClassDoc parameter */
    public void setOptions(DocTrees docTrees, Element p) {
        if (p == null) {
            return;
        }

        List<String> tags = TagUtil.getTag(docTrees, p, "opt");
        for (String tag : tags) {
            setOption(StringUtil.tokenize(tag));
        }
    }

    /**
     * Check if the supplied string matches an entity specified with the -hide
     * parameter.
     * 
     * @return true if the string matches.
     */
    public boolean matchesHideExpression(CharSequence s) {
        for (Pattern hidePattern : hidePatterns) {
            // micro-optimization because the "all pattern" is heavily used in UmlGraphDoc
            if (hidePattern == allPattern) {
                return true;
            }

            Matcher m = hidePattern.matcher(s);
            if (strictMatching ? m.matches() : m.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the supplied string matches an entity specified with the -include
     * parameter.
     * 
     * @return true if the string matches.
     */
    public boolean matchesIncludeExpression(CharSequence s) {
        for (Pattern includePattern : includePatterns) {
            Matcher m = includePattern.matcher(s);
            if (strictMatching ? m.matches() : m.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the supplied string matches an entity specified with the
     * -collpackages parameter.
     * 
     * @return true if the string matches.
     */
    public boolean matchesCollPackageExpression(CharSequence s) {
        for (Pattern collPattern : collPackages) {
            Matcher m = collPattern.matcher(s);
            if (strictMatching ? m.matches() : m.find()) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------
    // OptionProvider methods
    // ----------------------------------------------------------------

    public Options getOptionsFor(DocTrees docTrees, TypeElement cd) {
        Options localOpt = getGlobalOptions();
        localOpt.setOptions(docTrees, cd);
        return localOpt;
    }

    public Options getOptionsFor(CharSequence name) {
        return getGlobalOptions();
    }

    public Options getGlobalOptions() {
        return (Options) clone();
    }

    public void overrideForClass(Options opt, TypeElement cd) {
        // nothing to do
    }

    public void overrideForClass(Options opt, CharSequence className) {
        // nothing to do
    }

    public String getDisplayName() {
        return "general class diagram";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UMLGRAPH OPTIONS\n");
        for (Field f : this.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                try {
                    sb.append(f.getName() + ":" + f.get(this) + "\n");
                } catch (Exception e) {
                }
            }
        }
        return sb.toString();
    }

}
