/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2005 Diomidis Spinellis
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

import static org.umlgraph.doclet.StringUtil.buildRelativePathFromClassNames;
import static org.umlgraph.doclet.StringUtil.escape;
import static org.umlgraph.doclet.StringUtil.fmt;
import static org.umlgraph.doclet.StringUtil.guilWrap;
import static org.umlgraph.doclet.StringUtil.guillemize;
import static org.umlgraph.doclet.StringUtil.htmlNewline;
import static org.umlgraph.doclet.StringUtil.removeTemplate;
import static org.umlgraph.doclet.StringUtil.splitPackageClass;
import static org.umlgraph.doclet.StringUtil.tokenize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.umlgraph.doclet.util.ElementUtil;
import org.umlgraph.doclet.util.TagUtil;

import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * Class graph generation engine
 * 
 * @depend - - - StringUtil
 * @depend - - - Options
 * @composed - - * ClassInfo
 * @has - - - OptionProvider
 *
 * @version $Revision$
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class ClassGraph {

    enum Align {
        LEFT, CENTER, RIGHT;

        public final String lower;

        private Align() {
            this.lower = toString().toLowerCase();
        }
    };

    private final Elements elementUtils;
    private final DocTrees docTrees;
    private final Types types;
    private final JavaFileManager fileManager;
    protected Map<Name, ClassInfo> classnames = new HashMap<>();
    protected Set<Name> rootClasses;
    protected Map<Name, TypeElement> rootClassdocs = new HashMap<>();
    protected OptionProvider optionProvider;
    protected PrintWriter w;
    protected TypeElement collectionClassDoc;
    protected TypeElement mapClassDoc;
    protected String linePostfix;
    protected String linePrefix;

    // used only when generating context class diagrams in UMLDoc, to generate the
    // proper relative links to other classes in the image map
    protected final Name contextPackageName;

    /**
     * Create a new ClassGraph.
     * <p>
     * The packages passed as an argument are the ones specified on the command
     * line.
     * </p>
     * <p>
     * Local URLs will be generated for these packages.
     * </p>
     * 
     * @param root           The root of docs as provided by the javadoc API
     * @param optionProvider The main option provider
     * @param contextDoc     The current context for generating relative links, may
     *                       be a ClassDoc or a PackageDoc (used by UMLDoc)
     */
    public ClassGraph(DocletEnvironment root, OptionProvider optionProvider, Element contextDoc) {
        this.optionProvider = optionProvider;
        this.elementUtils = root.getElementUtils();
        this.docTrees = root.getDocTrees();
        this.types = root.getTypeUtils();
        this.fileManager = root.getJavaFileManager();
        this.collectionClassDoc = elementUtils.getTypeElement("java.util.Collection");
        this.mapClassDoc = elementUtils.getTypeElement("java.util.Map");

        // to gather the packages containing specified classes, loop thru them and
        // gather
        // package definitions. User root.specifiedPackages is not safe, since the user
        // may specify just a list of classes (human users usually don't, but automated
        // tools do)
        rootClasses = new HashSet<>();
        for (Element classDoc : root.getIncludedElements()) {
            if (classDoc instanceof TypeElement) {
                rootClasses.add(((TypeElement) classDoc).getQualifiedName());
                rootClassdocs.put(((TypeElement) classDoc).getQualifiedName(), (TypeElement) classDoc);
            }
        }

        // determine the context path, relative to the root
        if (contextDoc instanceof TypeElement) {
            contextPackageName = ElementUtil.getPackageOf(elementUtils, contextDoc).getQualifiedName();
        } else if (contextDoc instanceof PackageElement) {
            contextPackageName = ((PackageElement) contextDoc).getQualifiedName();
        } else {
            contextPackageName = null; // Not available
        }

        Options opt = optionProvider.getGlobalOptions();
        linePrefix = opt.compact ? "" : "\t";
        linePostfix = opt.compact ? "" : "\n";
    }

    /** Return the class's name, possibly by stripping the leading path */
    private String qualifiedName(Options opt, Name className) {
        if (opt.hideGenerics) {
            className = removeTemplate(elementUtils, className);
        }
        // Fast path - nothing to do:
        if (opt.showQualified && (opt.showQualifiedGenerics || className.toString().indexOf('<') < 0)) {
            return className.toString();
        }
        StringBuilder buf = new StringBuilder(className.length());
        qualifiedNameInner(opt, className, buf, 0, !opt.showQualified);
        return buf.toString();
    }

    private static int qualifiedNameInner(Options opt, Name r, StringBuilder buf, int last, boolean strip) {
        strip = strip && last < r.length() && Character.isLowerCase(r.charAt(last));
        for (int i = last; i < r.length(); i++) {
            char c = r.charAt(i);
            if (c == '.' || c == '$') {
                if (strip)
                    last = i + 1; // skip dot
                strip = strip && last < r.length() && Character.isLowerCase(r.charAt(last));
                continue;
            }
            if (Character.isJavaIdentifierPart(c))
                continue;
            buf.append(r, last, i);
            last = i;
            // Handle nesting of generics
            if (c == '<') {
                buf.append('<');
                i = last = qualifiedNameInner(opt, r, buf, ++last, !opt.showQualifiedGenerics);
                buf.append('>');
            } else if (c == '>')
                return i + 1;
        }
        buf.append(r, last, r.length());
        return r.length();
    }

    /**
     * Print the visibility adornment of element e prefixed by any stereotypes
     */
    private String visibility(Options opt, Element e) {
        return opt.showVisibility ? Visibility.get(e).symbol : " ";
    }

    /** Print the method parameter p */
    private String parameter(Options opt, List<? extends VariableElement> params) {
        StringBuilder par = new StringBuilder(1000);
        for (int i = 0; i < params.size(); i++) {
            par.append(params.get(i).getSimpleName() + typeAnnotation(opt, params.get(i).asType()));
            if (i + 1 < params.size()) {
                par.append(", ");
            }
        }
        return par.toString();
    }

    /** Print a a basic type t */
    private String type(Options opt, TypeMirror t, boolean generics) {
        return ((generics ? opt.showQualifiedGenerics : opt.showQualified) ? //
                ElementUtil.getQualifiedName(types, t) : ElementUtil.getSimpleName(types, t)) //
                + (opt.hideGenerics ? "" : typeParameters(opt, t instanceof DeclaredType ? (DeclaredType) t : null));
    }

    /** Print the parameters of the parameterized type t */
    private String typeParameters(Options opt, DeclaredType t) {
        if (t == null || t.getTypeArguments() == null || t.getTypeArguments().isEmpty()) {
            return "";
        }
        StringBuffer tp = new StringBuffer(1000).append("&lt;");
        List<? extends TypeMirror> args = t.getTypeArguments();
        for (int i = 0; i < args.size(); i++) {
            tp.append(type(opt, args.get(i), true));
            if (i != args.size() - 1) {
                tp.append(", ");
            }
        }
        return tp.append("&gt;").toString();
    }

    /** Annotate an field/argument with its type t */
    private String typeAnnotation(Options opt, TypeMirror t) {
        if (t.getKind() == TypeKind.VOID) {
            return "";
        }
        return " : " + type(opt, t, false) + ElementUtil.dimensions(t);
    }

    /** Print the class's attributes fd */
    private void attributes(Options opt, List<VariableElement> fd) {
        for (VariableElement f : fd) {
            if (hidden(f)) {
                continue;
            }
            stereotype(opt, f, Align.LEFT);
            String att = visibility(opt, f) + f.getSimpleName();
            if (opt.showType) {
                att += typeAnnotation(opt, f.asType());
            }
            tableLine(Align.LEFT, att);
            tagvalue(opt, f);
        }
    }

    /** Print the class's operations m */
    private boolean operations(Options opt, List<ExecutableElement> m) {
        boolean printed = false;
        for (ExecutableElement md : m) {
            if (hidden(md)) {
                continue;
            }
            // Filter-out static initializer method
            if (md.getSimpleName().toString().equals("<clinit>") && md.getModifiers().contains(Modifier.STATIC) && Visibility.get(md) == Visibility.PACKAGE) {
                continue;
            }
            Name name = "<init>".equals(md.getSimpleName().toString()) ? ElementUtil.containingTypeElement(md).getSimpleName() : md.getSimpleName();
            stereotype(opt, md, Align.LEFT);
            String op = visibility(opt, md) + name +
                    (opt.showType ? "(" + parameter(opt, md.getParameters()) + ")" + typeAnnotation(opt, md.getReturnType()) : "()");
            tableLine(Align.LEFT, (md.getModifiers().contains(Modifier.ABSTRACT) ? Font.ABSTRACT : Font.NORMAL).wrap(opt, op));
            printed = true;

            tagvalue(opt, md);
        }
        return printed;
    }

    /** Print the common class node's properties */
    private void nodeProperties(Options opt) {
        Options def = opt.getGlobalOptions();
        if (opt.nodeFontName != def.nodeFontName) {
            w.print(",fontname=\"" + opt.nodeFontName + "\"");
        }
        if (opt.nodeFontColor != def.nodeFontColor) {
            w.print(",fontcolor=\"" + opt.nodeFontColor + "\"");
        }
        if (opt.nodeFontSize != def.nodeFontSize) {
            w.print(",fontsize=" + fmt(opt.nodeFontSize));
        }
        w.print(opt.shape.style);
        w.println("];");
    }

    /**
     * Return as a string the tagged values associated with c
     * 
     * @param opt      the Options used to guess font names
     * @param c        the Doc entry to look for @tagvalue
     * @param prevterm the termination string for the previous element
     * @param term     the termination character for each tagged value
     */
    private void tagvalue(Options opt, Element c) {
        List<String> tags = TagUtil.getTag(docTrees, c, "tagvalue");
        if (tags.isEmpty()) {
            return;
        }

        for (String tag : tags) {
            String t[] = tokenize(tag);
            if (t.length != 2) {
                System.err.println("@tagvalue expects two fields: " + tag);
                continue;
            }
            tableLine(Align.RIGHT, Font.TAG.wrap(opt, "{" + t[0] + " = " + t[1] + "}"));
        }
    }

    /**
     * Return as a string the stereotypes associated with c terminated by the escape character term
     */
    private void stereotype(Options opt, Element c, Align align) {
        List<String> tags = TagUtil.getTag(docTrees, c, "stereotype");
        if (tags.isEmpty()) {
            return;
        }
        for (String tag : tags) {
            String t[] = tokenize(tag);
            if (t.length != 1) {
                System.err.println("@stereotype expects one field: " + tag);
                continue;
            }
            tableLine(align, guilWrap(opt, t[0]));
        }
    }

    /** Return true if c has a @hidden tag associated with it */
    private boolean hidden(Element c) {
        Map<String, List<String>> tags = TagUtil.getTags(docTrees, c);
        if (tags.get("hidden") != null || tags.get("view") != null) {
            return true;
        }
        Options opt = optionProvider.getOptionsFor(docTrees, c instanceof TypeElement ? (TypeElement) c : ElementUtil.containingTypeElement(c));
        return opt.matchesHideExpression(c.getSimpleName()) //
                || (opt.hidePrivateInner && c instanceof TypeElement && c.getModifiers().contains(Modifier.PRIVATE)
                        && ((TypeElement) c).getEnclosingElement() != null);
    }

    protected ClassInfo getClassInfo(TypeElement cd, boolean create) {
        return getClassInfo(cd, cd.getQualifiedName(), create);
    }

    protected ClassInfo getClassInfo(Name className, boolean create) {
        return getClassInfo(null, className, create);
    }

    protected ClassInfo getClassInfo(TypeElement cd, Name className, boolean create) {
        className = removeTemplate(elementUtils, className);
        ClassInfo ci = classnames.get(className);
        if (ci == null && create) {
            boolean hidden = cd != null ? hidden(cd)
                    : optionProvider.getOptionsFor(className).matchesHideExpression(className);
            ci = new ClassInfo(hidden);
            classnames.put(className, ci);
        }
        return ci;
    }

    /**
     * Return true if the class name is associated to an hidden class or matches a
     * hide expression
     */
    private boolean hidden(CharSequence className) {
        ClassInfo ci = classnames.get(className);
        return ci != null ? ci.hidden : optionProvider.getOptionsFor(className).matchesHideExpression(className);
    }

    /**
     * Prints the class if needed.
     * <p>
     * A class is a rootClass if it's included among the classes returned by
     * RootDoc.classes(), this information is used to properly compute relative
     * links in diagrams for UMLDoc
     */
    public String printClass(TypeElement c, boolean rootClass) {
        ClassInfo ci = getClassInfo(c, true);
        if (ci.nodePrinted || ci.hidden)
            return ci.name;
        Options opt = optionProvider.getOptionsFor(docTrees, c);
        if (c.getKind() == ElementKind.ENUM && !opt.showEnumerations) {
            return ci.name;
        }
        String className = c.toString();
        // Associate classname's alias
        w.println(linePrefix + "// " + className);
        // Create label
        w.print(linePrefix + ci.name + " [label=");

        boolean showMembers = (opt.showAttributes && !ElementUtil.getFields(c).isEmpty())
                || (c.getKind() == ElementKind.ENUM && opt.showEnumConstants && !ElementUtil.getEnumConstants(c).isEmpty())
                || (opt.showOperations && !ElementUtil.getMethods(c).isEmpty())
                || (opt.showConstructors && !ElementUtil.getConstructors(c).isEmpty());

        final String url = classToUrl(c, rootClass);
        externalTableStart(opt, c.getQualifiedName(), url);

        firstInnerTableStart(opt);
        if (c.getKind() == ElementKind.INTERFACE) {
            tableLine(Align.CENTER, guilWrap(opt, "interface"));
        }
        if (c.getKind() == ElementKind.ENUM) {
            tableLine(Align.CENTER, guilWrap(opt, "enumeration"));
        }
        stereotype(opt, c, Align.CENTER);
        Font font = c.getModifiers().contains(Modifier.ABSTRACT) && c.getKind() != ElementKind.INTERFACE ? Font.CLASS_ABSTRACT : Font.CLASS;
        String qualifiedName = qualifiedName(opt, c.getQualifiedName());
        int idx = splitPackageClass(qualifiedName);
        if (opt.showComment) {
            tableLine(Align.LEFT, Font.CLASS.wrap(opt, htmlNewline(escape(TagUtil.getComment(docTrees, c)))));
        } else if (opt.postfixPackage && idx > 0 && idx < (qualifiedName.length() - 1)) {
            String packageName = qualifiedName.substring(0, idx);
            String cn = qualifiedName.substring(idx + 1);
            tableLine(Align.CENTER, font.wrap(opt, escape(cn)));
            tableLine(Align.CENTER, Font.PACKAGE.wrap(opt, packageName));
        } else {
            tableLine(Align.CENTER, font.wrap(opt, escape(qualifiedName)));
        }
        tagvalue(opt, c);
        firstInnerTableEnd(opt);

        /*
         * Warning: The boolean expressions guarding innerTableStart() in this block,
         * should match those in the code block above marked:
         * "Calculate the number of innerTable rows we will emmit"
         */
        if (showMembers) {
            if (opt.showAttributes) {
                innerTableStart();
                List<VariableElement> fields = ElementUtil.getFields(c);
                // if there are no fields, print an empty line to generate proper HTML
                if (fields.size() == 0) {
                    tableLine(Align.LEFT, "");
                } else {
                    attributes(opt, fields);
                }
                innerTableEnd();
            } else if (c.getKind() != ElementKind.ENUM && (opt.showConstructors || opt.showOperations)) {
                // show an emtpy box if we don't show attributes but
                // we show operations
                innerTableStart();
                tableLine(Align.LEFT, "");
                innerTableEnd();
            }
            if (c.getKind() == ElementKind.ENUM && opt.showEnumConstants) {
                innerTableStart();
                List<VariableElement> ecs = ElementUtil.getEnumConstants(c);
                // if there are no constants, print an empty line to generate proper HTML
                if (ecs.size() == 0) {
                    tableLine(Align.LEFT, "");
                } else {
                    for (VariableElement fd : ecs) {
                        tableLine(Align.LEFT, fd.getSimpleName());
                    }
                }
                innerTableEnd();
            }
            if (c.getKind() != ElementKind.ENUM && (opt.showConstructors || opt.showOperations)) {
                innerTableStart();
                boolean printedLines = false;
                if (opt.showConstructors) {
                    printedLines |= operations(opt, ElementUtil.getConstructors(c));
                }
                if (opt.showOperations) {
                    printedLines |= operations(opt, ElementUtil.getMethods(c));
                }

                if (!printedLines) {
                    // if there are no operations nor constructors,
                    // print an empty line to generate proper HTML
                    tableLine(Align.LEFT, "");
                }

                innerTableEnd();
            }
        }
        externalTableEnd();
        if (url != null)
            w.print(", URL=\"" + url + "\"");
        nodeProperties(opt);

        // If needed, add a note for this node
        int ni = 0;
        List<String> tags = TagUtil.getTag(docTrees, c, "note");
        for (String t : tags) {
            String noteName = "n" + ni + "c" + ci.name;
            w.print(linePrefix + "// Note annotation\n");
            w.print(linePrefix + noteName + " [label=");
            externalTableStart(UmlGraph.getCommentOptions(), c.getQualifiedName(), url);
            innerTableStart();
            tableLine(Align.LEFT, Font.CLASS.wrap(UmlGraph.getCommentOptions(), htmlNewline(escape(t))));
            innerTableEnd();
            externalTableEnd();
            nodeProperties(UmlGraph.getCommentOptions());
            ClassInfo ci1 = getClassInfo(c, true);
            w.print(linePrefix + noteName + " -> " + ci1.name + "[arrowhead=none];\n");
            ni++;
        }
        ci.nodePrinted = true;
        return ci.name;
    }

    /**
     * Print all relations for a given's class's tag
     * 
     * @param tagname  the tag containing the given relation
     * @param from     the source class
     * @param edgetype the dot edge specification
     */
    private void allRelation(Options opt, RelationType rt, TypeElement from) {
        String tagname = rt.lower;
        List<String> tags = TagUtil.getTag(docTrees, from, tagname);
        for (String tag : tags) {
            String t[] = tokenize(tag); // l-src label l-dst target
            t = t.length == 1 ? new String[] { "-", "-", "-", t[0] } : t; // Shorthand
            if (t.length != 4) {
                System.err.println("Error in " + from + "\n" + tagname
                        + " expects four fields (l-src label l-dst target): " + tag);
                return;
            }
            TypeElement to = elementUtils.getTypeElement(t[3]);
            if (to != null) {
                if (hidden(to)) {
                    continue;
                }
                relation(opt, rt, from, to, t[0], t[1], t[2]);
            } else {
                Name t3 = elementUtils.getName(t[3]);
                if (hidden(t3)) {
                    continue;
                }
                relation(opt, rt, from, from.getQualifiedName(), to, t3, t[0], t[1], t[2]);
            }
        }
    }

    /**
     * Print the specified relation
     * 
     * @param from     the source class (may be null)
     * @param fromName the source class's name
     * @param to       the destination class (may be null)
     * @param toName   the destination class's name
     */
    private void relation(Options opt, RelationType rt, TypeElement from, Name fromName, TypeElement to, Name toName,
            String tailLabel, String label, String headLabel) {
        tailLabel = (tailLabel != null && !tailLabel.isEmpty()) ? ",taillabel=\"" + tailLabel + "\"" : "";
        label = (label != null && !label.isEmpty()) ? ",label=\"" + guillemize(opt, label) + "\"" : "";
        headLabel = (headLabel != null && !headLabel.isEmpty()) ? ",headlabel=\"" + headLabel + "\"" : "";
        boolean unLabeled = tailLabel.isEmpty() && label.isEmpty() && headLabel.isEmpty();

        ClassInfo ci1 = getClassInfo(from, fromName, true), ci2 = getClassInfo(to, toName, true);
        String n1 = ci1.name, n2 = ci2.name;
        // For ranking we need to output extends/implements backwards.
        if (rt.backorder) { // Swap:
            n1 = ci2.name;
            n2 = ci1.name;
            String tmp = tailLabel;
            tailLabel = headLabel;
            headLabel = tmp;
        }
        Options def = opt.getGlobalOptions();
        // print relation
        w.println(linePrefix + "// " + fromName + " " + rt.lower + " " + toName);
        w.println(linePrefix + n1 + " -> " + n2 + " [" + rt.style
                + (opt.edgeColor != def.edgeColor ? ",color=\"" + opt.edgeColor + "\"" : "")
                + (unLabeled ? ""
                        : (opt.edgeFontName != def.edgeFontName ? ",fontname=\"" + opt.edgeFontName + "\"" : "")
                                + (opt.edgeFontColor != def.edgeFontColor ? ",fontcolor=\"" + opt.edgeFontColor + "\""
                                        : "")
                                + (opt.edgeFontSize != def.edgeFontSize ? ",fontsize=" + fmt(opt.edgeFontSize) : ""))
                + tailLabel + label + headLabel + "];");

        // update relation info
        RelationDirection d = RelationDirection.BOTH;
        if (rt == RelationType.NAVASSOC || rt == RelationType.DEPEND)
            d = RelationDirection.OUT;
        ci1.addRelation(toName, rt, d);
        ci2.addRelation(fromName, rt, d.inverse());
    }

    /**
     * Print the specified relation
     * 
     * @param from the source class
     * @param to   the destination class
     */
    private void relation(Options opt, RelationType rt, TypeElement from, TypeElement to, String tailLabel, String label,
            String headLabel) {
        relation(opt, rt, from, from.getQualifiedName(), to, to.getQualifiedName(), tailLabel, label, headLabel);
    }

    /** Print a class's relations */
    public void printRelations(TypeElement c) {
        Options opt = optionProvider.getOptionsFor(docTrees, c);
        if (hidden(c) || "".equals(c.getSimpleName().toString())) { // avoid phantom classes, they may pop up when the source uses annotations
            return;
        }
        // Print generalization (through the Java superclass)
        TypeMirror clazz = c.getSuperclass();
        Element scd;
        if (clazz == null || clazz.getKind() == TypeKind.NONE || !(clazz instanceof DeclaredType)) {
            scd = null;
        } else {
            scd = ((DeclaredType) clazz).asElement();
        }
        TypeElement s = scd instanceof TypeElement ? (TypeElement) scd : null;
        TypeElement sc = s != null && !s.getQualifiedName().toString().equals(Object.class.getName()) ? s : null;
        if (sc != null  && c.getKind() != ElementKind.ENUM && !hidden(sc)) {
            relation(opt, RelationType.EXTENDS, c, sc, null, null, null);
        }
        // Print generalizations (through @extends tags)
        List<String> tags = TagUtil.getTag(docTrees, c, "extends");
        for (String tag : tags) {
            if (!hidden(tag)) {
                relation(opt, RelationType.EXTENDS, c, elementUtils.getTypeElement(tag), null, null, null);
            }
        }
        // Print realizations (Java interfaces)
        for (TypeMirror iface : c.getInterfaces()) {
            TypeElement ic = ElementUtil.getTypeElement(iface);
            if (!hidden(ic)) {
                relation(opt, RelationType.IMPLEMENTS, c, ic, null, null, null);
            }
        }
        // Print other associations
        allRelation(opt, RelationType.COMPOSED, c);
        allRelation(opt, RelationType.NAVCOMPOSED, c);
        allRelation(opt, RelationType.HAS, c);
        allRelation(opt, RelationType.NAVHAS, c);
        allRelation(opt, RelationType.ASSOC, c);
        allRelation(opt, RelationType.NAVASSOC, c);
        allRelation(opt, RelationType.DEPEND, c);
    }

    /** Print classes that were parts of relationships, but not parsed by javadoc */
    public void printExtraClasses(DocletEnvironment root) {
        Set<Name> names = new HashSet<>(classnames.keySet());
        for (Name className : names) {
            ClassInfo info = getClassInfo(className, true);
            if (info.nodePrinted) {
                continue;
            }
            TypeElement c = elementUtils.getTypeElement(className);
            if (c != null) {
                printClass(c, false);
                continue;
            }
            // Handle missing classes:
            Options opt = optionProvider.getOptionsFor(className);
            if (opt.matchesHideExpression(className))
                continue;
            w.println(linePrefix + "// " + className);
            w.print(linePrefix + info.name + "[label=");
            externalTableStart(opt, className, classToUrl(className));
            innerTableStart();
            String qualifiedName = qualifiedName(opt, className);
            int startTemplate = qualifiedName.indexOf('<');
            int idx = qualifiedName.lastIndexOf('.', startTemplate < 0 ? qualifiedName.length() - 1 : startTemplate);
            if (opt.postfixPackage && idx > 0 && idx < (qualifiedName.length() - 1)) {
                String packageName = qualifiedName.substring(0, idx);
                String cn = qualifiedName.substring(idx + 1);
                tableLine(Align.CENTER, Font.CLASS.wrap(opt, escape(cn)));
                tableLine(Align.CENTER, Font.PACKAGE.wrap(opt, packageName));
            } else {
                tableLine(Align.CENTER, Font.CLASS.wrap(opt, escape(qualifiedName)));
            }
            innerTableEnd();
            externalTableEnd();
            if (className == null || className.length() == 0)
                w.print(",URL=\"" + classToUrl(className) + "\"");
            nodeProperties(opt);
        }
    }

    /**
     * Prints associations recovered from the fields of a class. An association is
     * inferred only if another relation between the two classes is not already in
     * the graph.
     * 
     * @param classes
     */
    public void printInferredRelations(TypeElement c) {
        // check if the source is excluded from inference
        if (hidden(c)) {
            return;
        }

        Options opt = optionProvider.getOptionsFor(docTrees, c);

        for (VariableElement field : ElementUtil.getFields(c)) {
            if (hidden(field)) {
                continue;
            }
            // skip statics
            if (field.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            // skip primitives
            FieldRelationInfo fri = getFieldRelationInfo(field);
            if (fri == null) {
                continue;
            }
            // check if the destination is excluded from inference
            if (hidden(fri.cd)) {
                continue;
            }

            // if source and dest are not already linked, add a dependency
            RelationPattern rp = getClassInfo(c, true).getRelation(fri.cd.toString());
            if (rp == null) {
                String destAdornment = fri.multiple ? "*" : "";
                relation(opt, opt.inferRelationshipType, c, fri.cd, "", "", destAdornment);
            }
        }
    }

    /**
     * Returns an array representing the imported classes of c. Disables the
     * deprecation warning, which is output, because the imported classed are an
     * implementation detail.
     */
    List<TypeMirror> importedClasses(TypeElement c) {
        JavaFileObject source;
        try {
            source = fileManager.getJavaFileForInput(StandardLocation.SOURCE_PATH, c.getQualifiedName().toString(), JavaFileObject.Kind.SOURCE);
        } catch (IOException e) {
            source = null;
        }
        
        if (source == null) {
            return Collections.emptyList();
        }
        return Collections.emptyList(); // c.importedClasses();
    }

    /**
     * Prints dependencies recovered from the methods of a class. A dependency is
     * inferred only if another relation between the two classes is not already in
     * the graph.
     * 
     * @param classes
     */
    public void printInferredDependencies(TypeElement c) {
        if (hidden(c)) {
            return;
        }

        Options opt = optionProvider.getOptionsFor(docTrees, c);
        Set<TypeMirror> types = new HashSet<>();
        
        // harvest method return and parameter types
        for (ExecutableElement method : filterByVisibility(ElementUtil.getMethods(c), opt.inferDependencyVisibility)) {
            types.add(method.getReturnType());
            for (VariableElement parameter : method.getParameters()) {
                types.add(parameter.asType());
            }
        }
        // and the field types
        if (!opt.inferRelationships) {
            for (VariableElement field : filterByVisibility(ElementUtil.getFields(c), opt.inferDependencyVisibility)) {
                types.add(field.asType());
            }
        }
        // see if there are some type parameters
        if (c.asType() instanceof DeclaredType) {
            DeclaredType pt = (DeclaredType) c.asType();
            types.addAll(pt.getTypeArguments());
        }
        // see if type parameters extend something
        for (TypeParameterElement tv : c.getTypeParameters()) {
            if (tv.getBounds().size() > 0) {
                types.addAll(tv.getBounds());
            }
        }

        // and finally check for explicitly imported classes (this
        // assumes there are no unused imports...)
        if (opt.useImports) {
            types.addAll(importedClasses(c));
        }

        // compute dependencies
        for (TypeMirror type : types) {
            // skip primitives and type variables, as well as dependencies
            // on the source class
            if (type.getKind().isPrimitive() || type instanceof NoType || type instanceof WildcardType || type instanceof TypeVariable
                    || c.toString().equals(ElementUtil.getTypeElement(type).toString())) {
                continue;
            }

            // check if the destination is excluded from inference
            TypeElement fc = ElementUtil.getTypeElement(type);
            if (hidden(fc)) {
                continue;
            }

            // check if source and destination are in the same package and if we are allowed
            // to infer dependencies between classes in the same package
            if (!opt.inferDepInPackage && ElementUtil.getPackageOf(elementUtils, c).equals(ElementUtil.getPackageOf(elementUtils, fc))) {
                continue;
            }

            // if source and dest are not already linked, add a dependency
            RelationPattern rp = getClassInfo(c, true).getRelation(fc.toString());
            if (rp == null || rp.matchesOne(new RelationPattern(RelationDirection.OUT))) {
                relation(opt, RelationType.DEPEND, c, fc, "", "", "");
            }

        }
    }

    /**
     * Returns all program element docs that have a visibility greater or equal than
     * the specified level
     */
    private <T extends Element> List<T> filterByVisibility(List<T> docs, Visibility visibility) {
        if (visibility == Visibility.PRIVATE) {
            return docs;
        }

        List<T> filtered = new ArrayList<>();
        for (T doc : docs) {
            if (Visibility.get(doc).compareTo(visibility) > 0) {
                filtered.add(doc);
            }
        }
        return filtered;
    }

    private FieldRelationInfo getFieldRelationInfo(VariableElement field) {
        TypeMirror type = field.asType();
        if (type.getKind().isPrimitive() || type.getKind() == TypeKind.WILDCARD || type.getKind() == TypeKind.TYPEVAR) {
            return null;
        }

        if (ElementUtil.dimensions(type).endsWith("[]")) {
            return new FieldRelationInfo(ElementUtil.getTypeElement(type), true);
        }

        Options opt = optionProvider.getOptionsFor(docTrees, ElementUtil.getTypeElement(type));
        if (opt.matchesCollPackageExpression(ElementUtil.getQualifiedName(types, type))) {
            List<? extends TypeMirror> argTypes = getInterfaceTypeArguments(collectionClassDoc, type);
            if (argTypes != null && argTypes.size() == 1 && !argTypes.get(0).getKind().isPrimitive()) {
                return new FieldRelationInfo(ElementUtil.getTypeElement(argTypes.get(0)), true);
            }

            argTypes = getInterfaceTypeArguments(mapClassDoc, type);
            if (argTypes != null && argTypes.size() == 2 && !argTypes.get(1).getKind().isPrimitive()) {
                return new FieldRelationInfo(ElementUtil.getTypeElement(argTypes.get(1)), true);
            }
        }

        return new FieldRelationInfo(ElementUtil.getTypeElement(type), false);
    }

    private List<? extends TypeMirror> getInterfaceTypeArguments(TypeElement iface, TypeMirror t) {
        if (t instanceof DeclaredType) {
            DeclaredType pt = (DeclaredType) t;
            if (iface != null && iface.equals(pt.asElement())) {
                return pt.getTypeArguments();
            } else {
                for (TypeMirror pti : ElementUtil.getInterfacesTypes(iface)) {
                    List<? extends TypeMirror> result = getInterfaceTypeArguments(iface, pti);
                    if (result != null) {
                        return result;
                    }
                }
                if (ElementUtil.getSuperclassType(pt) != null) {
                    return getInterfaceTypeArguments(iface, ElementUtil.getSuperclassType(pt));
                }
            }
        }
        return null;
    }

    /** Convert the class name into a corresponding URL */
    public String classToUrl(TypeElement cd, boolean rootClass) {
        // building relative path for context and package diagrams
        if (contextPackageName != null && rootClass) {
            return buildRelativePathFromClassNames(
                    contextPackageName, ElementUtil.getPackageOf(elementUtils, cd).getQualifiedName().toString()) + cd.getSimpleName() + ".html";
        }
        return classToUrl(cd.getQualifiedName());
    }

    /** Convert the class name into a corresponding URL */
    public String classToUrl(Name className) {
        TypeElement classDoc = rootClassdocs.get(className);
        if (classDoc != null) {
            String docRoot = optionProvider.getGlobalOptions().apiDocRoot;
            if (docRoot == null) {
                return null;
            }
            return new StringBuilder(docRoot.length() + className.length() + 10).append(docRoot) //
                    .append(ElementUtil.getPackageOf(elementUtils, classDoc).getQualifiedName().toString().replace('.', '/')) //
                    .append('/').append(classDoc.getSimpleName()).append(".html").toString();
        }
        String docRoot = optionProvider.getGlobalOptions().getApiDocRoot(className);
        if (docRoot == null) {
            return null;
        }
        int split = splitPackageClass(className);
        StringBuilder buf = new StringBuilder(docRoot.length() + className.length() + 10).append(docRoot);
        if (split > 0) { // Avoid -1, and the extra slash then.
            buf.append(className.toString().substring(0, split).replace('.', '/')).append('/');
        }
        return buf.append(className, Math.min(split + 1, className.length()), className.length()) //
                .append(".html").toString();
    }

    /**
     * Dot prologue
     * 
     * @throws IOException
     */
    public void prologue() throws IOException {
        Options opt = optionProvider.getGlobalOptions();
        OutputStream os;

        if (opt.outputFileName.equals("-")) {
            os = System.out;
        } else {
            // prepare output file. Use the output file name as a full path unless the
            // output
            // directory is specified
            File file = new File(opt.outputDirectory, opt.outputFileName);
            // make sure the output directory are there, otherwise create them
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            os = new FileOutputStream(file);
        }

        // print prologue
        w = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), opt.outputEncoding));
        w.println("#!/usr/local/bin/dot\n" + "#\n" + "# Class diagram \n" + "# Generated by UMLGraph version "
                + Version.VERSION + " (http://www.spinellis.gr/umlgraph/)\n" + "#\n\n" + "digraph G {\n" + linePrefix
                + "graph [fontnames=\"svg\"]\n" + linePrefix + "edge [fontname=\"" + opt.edgeFontName + "\",fontsize="
                + fmt(opt.edgeFontSize) + ",labelfontname=\"" + opt.edgeFontName + "\",labelfontsize="
                + fmt(opt.edgeFontSize) + ",color=\"" + opt.edgeColor + "\"];\n" + linePrefix + "node [fontname=\""
                + opt.nodeFontName + "\",fontcolor=\"" + opt.nodeFontColor + "\",fontsize=" + fmt(opt.nodeFontSize)
                + ",shape=plaintext,margin=0,width=0,height=0];");

        w.println(linePrefix + "nodesep=" + opt.nodeSep + ";");
        w.println(linePrefix + "ranksep=" + opt.rankSep + ";");
        if (opt.horizontal) {
            w.println(linePrefix + "rankdir=LR;");
        }
        if (opt.bgColor != null) {
            w.println(linePrefix + "bgcolor=\"" + opt.bgColor + "\";\n");
        }
    }

    /** Dot epilogue */
    public void epilogue() {
        w.println("}\n");
        w.flush();
        w.close();
    }

    private void externalTableStart(Options opt, Name name, String url) {
        String bgcolor = opt.nodeFillColor == null ? "" : (" bgcolor=\"" + opt.nodeFillColor + "\"");
        String href = url == null ? "" : (" href=\"" + url + "\" target=\"_parent\"");
        w.print("<<table title=\"" + name + "\" border=\"0\" cellborder=\"" + opt.shape.cellBorder()
                + "\" cellspacing=\"0\" " + "cellpadding=\"2\"" + bgcolor + href + ">" + linePostfix);
    }

    private void externalTableEnd() {
        w.print(linePrefix + linePrefix + "</table>>");
    }

    private void innerTableStart() {
        w.print(linePrefix + linePrefix + "<tr><td><table border=\"0\" cellspacing=\"0\" " + "cellpadding=\"1\">"
                + linePostfix);
    }

    /**
     * Start the first inner table of a class.
     */
    private void firstInnerTableStart(Options opt) {
        w.print(linePrefix + linePrefix + "<tr>" + opt.shape.extraColumn()
                + "<td><table border=\"0\" cellspacing=\"0\" " + "cellpadding=\"1\">" + linePostfix);
    }

    private void innerTableEnd() {
        w.print(linePrefix + linePrefix + "</table></td></tr>" + linePostfix);
    }

    /**
     * End the first inner table of a class.
     */
    private void firstInnerTableEnd(Options opt) {
        w.print(linePrefix + linePrefix + "</table></td>" + opt.shape.extraColumn() + "</tr>" + linePostfix);
    }

    private void tableLine(Align align, CharSequence text) {
        w.print(linePrefix + linePrefix //
                + "<tr><td align=\"" + align.lower + "\" balign=\"" + align.lower + "\"> " //
                + text // MAY contain markup!
                + " </td></tr>" + linePostfix);
    }

    private static class FieldRelationInfo {
        TypeElement cd;
        boolean multiple;

        public FieldRelationInfo(TypeElement cd, boolean multiple) {
            this.cd = cd;
            this.multiple = multiple;
        }
    }
}
