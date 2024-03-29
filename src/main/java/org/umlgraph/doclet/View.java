/*
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
package org.umlgraph.doclet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.element.TypeElement;

import org.umlgraph.doclet.util.TagUtil;

import jdk.javadoc.doclet.DocletEnvironment;
import com.sun.source.util.DocTrees;

/**
 * Contains the definition of a View. A View is a set of option overrides that
 * will lead to the creation of a UML class diagram. Multiple views can be
 * defined on the same source tree, effectively allowing to create multiple
 * class diagram out of it.
 * 
 * @author wolf
 * 
 * @depend - - - Options
 * @depend - - - ClassMatcher
 * @depend - - - InterfaceMatcher
 * @depend - - - PatternMatcher
 * @depend - - - SubclassMatcher
 * @depend - - - ContextMatcher
 * 
 */
public class View implements OptionProvider {
    Map<ClassMatcher, List<String[]>> optionOverrides = new LinkedHashMap<>();
    TypeElement viewDoc;
    OptionProvider provider;
    List<String[]> globalOptions;
    DocletEnvironment root;

    /**
     * Builds a view given the class that contains its definition
     */
    public View(DocletEnvironment root, TypeElement c, OptionProvider provider) {
        this.viewDoc = c;
        this.provider = provider;
        this.root = root;
        Map<String, List<String>> tags = TagUtil.getTags(root, viewDoc);
        ClassMatcher currMatcher = null;
        // parse options, get the global ones, and build a map of the
        // pattern matched overrides
        globalOptions = new ArrayList<String[]>();
        if (tags.get("match") != null) {
            for (String text : tags.get("match")) {
                currMatcher = buildMatcher(text);
                if (currMatcher != null) {
                    optionOverrides.put(currMatcher, new ArrayList<>());
                }
            }
        }
        if (tags.get("opt") != null) {
            for (String text : tags.get("opt")) {
                String[] opts = StringUtil.tokenize(text);
                opts[0] = "-" + opts[0];
                if (currMatcher == null) {
                    globalOptions.add(opts);
                } else {
                    optionOverrides.get(currMatcher).add(opts);
                }
            }
        }
    }

    /**
     * Factory method that builds the appropriate matcher for @match tags
     */
    private ClassMatcher buildMatcher(String tagText) {
        // check there are at least @match <type> and a parameter
        String[] strings = StringUtil.tokenize(tagText);
        if (strings.length < 2) {
            System.err.println("Skipping uncomplete @match tag, type missing: " + tagText + " in view " + viewDoc);
            return null;
        }

        try {
            if (strings[0].equals("class")) {
                return new PatternMatcher(Pattern.compile(strings[1]));
            } else if (strings[0].equals("context")) {
                return new ContextMatcher(root, Pattern.compile(strings[1]), getGlobalOptions(), false);
            } else if (strings[0].equals("outgoingContext")) {
                return new ContextMatcher(root, Pattern.compile(strings[1]), getGlobalOptions(), false);
            } else if (strings[0].equals("interface")) {
                return new InterfaceMatcher(root, Pattern.compile(strings[1]));
            } else if (strings[0].equals("subclass")) {
                return new SubclassMatcher(root, Pattern.compile(strings[1]));
            } else {
                System.err.println("Skipping @match tag, unknown match type, in view " + viewDoc);
            }
        } catch (PatternSyntaxException pse) {
            System.err.println("Skipping @match tag due to invalid regular expression '" + tagText + "'" + " in view " + viewDoc);
        } catch (Exception e) {
            System.err.println("Skipping @match tag due to an internal error '" + tagText + "'" + " in view " + viewDoc);
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // OptionProvider methods
    // ----------------------------------------------------------------

    public Options getOptionsFor(DocTrees dt, TypeElement cd) {
        Options localOpt = getGlobalOptions();
        overrideForClass(localOpt, cd);
        localOpt.setOptions(dt, cd);
        return localOpt;
    }

    public Options getOptionsFor(CharSequence name) {
        Options localOpt = getGlobalOptions();
        overrideForClass(localOpt, name);
        return localOpt;
    }

    public Options getGlobalOptions() {
        Options go = provider.getGlobalOptions();

        boolean outputSet = false;
        for (String[] opts : globalOptions) {
            if (Options.matchOption(opts[0], "output")) {
                outputSet = true;
            }
            go.setOption(opts);
        }
        if (!outputSet) {
            go.setOption(new String[] { "output", viewDoc.getSimpleName() + ".dot" });
        }

        return go;
    }

    public void overrideForClass(Options opt, TypeElement cd) {
        provider.overrideForClass(opt, cd);
        for (ClassMatcher cm : optionOverrides.keySet()) {
            if (cm.matches(cd)) {
                for (String[] override : optionOverrides.get(cm)) {
                    opt.setOption(override);
                }
            }
        }
    }

    public void overrideForClass(Options opt, CharSequence className) {
        provider.overrideForClass(opt, className);
        for (ClassMatcher cm : optionOverrides.keySet()) {
            if (cm.matches(className)) {
                for (String[] override : optionOverrides.get(cm)) {
                    opt.setOption(override);
                }
            }
        }
    }

    public String getDisplayName() {
        return "view " + viewDoc.getSimpleName();
    }

}
