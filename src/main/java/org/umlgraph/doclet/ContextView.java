package org.umlgraph.doclet;

import java.io.IOException;
import java.util.regex.Pattern;

import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;

import org.umlgraph.doclet.util.ElementUtil;

import com.sun.source.util.DocTrees;

/**
 * A view designed for UMLDoc, filters out everything that it's not directly
 * connected to the center class of the context.
 * <p>
 * As such, can be viewed as a simplified version of a {@linkplain View} using a
 * single {@linkplain ContextMatcher}, but provides some extra configuration
 * such as context highlighting and output path configuration (and it is
 * specified in code rather than in javadoc comments).
 * 
 * @author wolf
 * 
 */
public class ContextView implements OptionProvider {

    private TypeElement cd;
    private DocletEnvironment root;
    private ContextMatcher matcher;
    private Options globalOptions;
    private Options myGlobalOptions;
    private Options hideOptions;
    private Options centerOptions;
    private Options packageOptions;
    private static final String[] HIDE_OPTIONS = new String[] { "hide" };

    public ContextView(String outputFolder, TypeElement cd, DocletEnvironment root, Options parent) throws IOException {
        this.cd = cd;
        this.root = root;
        ModuleElement md = ElementUtil.getModuleOf(root, cd);
        String pathPrefix = Runtime.version().major() > 10 && md != null ? md.getQualifiedName().toString() + "/" : "";
        String outputPath = pathPrefix + ElementUtil.getPackageOf(root, cd).getQualifiedName().toString().replace('.', '/') + "/" + cd.getSimpleName() + ".dot";

        // setup options statically, so that we won't need to change them so
        // often
        this.globalOptions = parent.getGlobalOptions();

        this.packageOptions = parent.getGlobalOptions();
        this.packageOptions.showQualified = false;

        this.myGlobalOptions = parent.getGlobalOptions();
        this.myGlobalOptions.setOption(new String[] { "output", outputPath });
        this.myGlobalOptions.setOption(HIDE_OPTIONS);

        this.hideOptions = parent.getGlobalOptions();
        this.hideOptions.setOption(HIDE_OPTIONS);

        this.centerOptions = parent.getGlobalOptions();
        this.centerOptions.nodeFillColor = "lemonChiffon";
        this.centerOptions.showQualified = false;

        this.matcher = new ContextMatcher(root, Pattern.compile(Pattern.quote(cd.toString())), myGlobalOptions, true);

    }

    public void setContextCenter(TypeElement contextCenter) {
        this.cd = contextCenter;
        ModuleElement md = ElementUtil.getModuleOf(root, contextCenter);
        String pathPrefix = Runtime.version().major() > 10 && md != null ? md.getQualifiedName().toString() + "/" : "";
        String outputPath = pathPrefix + ElementUtil.getPackageOf(root, cd).getQualifiedName().toString().replace('.', '/') + "/" + cd.getSimpleName() + ".dot";
        this.myGlobalOptions.setOption(new String[] { "output", outputPath });
        matcher.setContextCenter(Pattern.compile(Pattern.quote(cd.toString())));
    }

    public String getDisplayName() {
        return "Context view for class " + cd;
    }

    public Options getGlobalOptions() {
        return myGlobalOptions;
    }

    public Options getOptionsFor(DocTrees dt, TypeElement cd) {
        Options opt;
        if (globalOptions.matchesHideExpression(cd.getQualifiedName())
                || !(matcher.matches(cd) || globalOptions.matchesIncludeExpression(cd.getQualifiedName()))) {
            opt = hideOptions;
        } else if (cd.equals(this.cd)) {
            opt = centerOptions;
        } else if (root.getElementUtils().getPackageOf(cd).equals(root.getElementUtils().getPackageOf(this.cd))) {
            opt = packageOptions;
        } else {
            opt = globalOptions;
        }
        Options optionClone = (Options) opt.clone();
        overrideForClass(optionClone, cd);
        return optionClone;
    }

    public Options getOptionsFor(CharSequence name) {
        Options opt;
        if (!matcher.matches(name)) {
            opt = hideOptions;
        } else if (name.equals(cd.getSimpleName())) {
            opt = centerOptions;
        } else {
            opt = globalOptions;
        }
        Options optionClone = (Options) opt.clone();
        overrideForClass(optionClone, name);
        return optionClone;
    }

    public void overrideForClass(Options opt, TypeElement cd) {
        opt.setOptions(root.getDocTrees(), cd);
        if (opt.matchesHideExpression(cd.getQualifiedName())
                || !(matcher.matches(cd) || opt.matchesIncludeExpression(cd.getQualifiedName()))) {
            opt.setOption(HIDE_OPTIONS);
        }
        if (cd.equals(this.cd)) {
            opt.nodeFillColor = "lemonChiffon";
        }
    }

    public void overrideForClass(Options opt, CharSequence className) {
        if (!(matcher.matches(className) || opt.matchesIncludeExpression(className))) {
            opt.setOption(HIDE_OPTIONS);
        }
    }

}
