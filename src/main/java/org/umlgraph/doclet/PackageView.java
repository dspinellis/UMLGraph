package org.umlgraph.doclet;

import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.umlgraph.doclet.util.ElementUtil;

import com.sun.source.util.DocTrees;

/**
 * A view designed for UMLDoc, filters out everything that it's not contained in
 * the specified package.
 * <p>
 * As such, can be viewed as a simplified version of a {@linkplain View} using a
 * single {@linkplain ClassMatcher}, and provides some extra configuration such
 * as output path configuration (and it is specified in code rather than in
 * javadoc comments).
 * 
 * @author wolf
 * 
 */
public class PackageView implements OptionProvider {

    private static final String[] HIDE = new String[] { "hide" };
    private PackageElement pd;
    private OptionProvider parent;
    private ClassMatcher matcher;
    private String outputPath;
    private Options opt;
    private DocTrees docTrees;

    public PackageView(String outputFolder, PackageElement pd, DocletEnvironment root, OptionProvider parent) {
        this.parent = parent;
        this.pd = pd;
        this.docTrees = root.getDocTrees();
        this.matcher = new PackageMatcher(root, pd);
        this.opt = parent.getGlobalOptions();
        this.opt.setOptions(docTrees, pd);
        ModuleElement md = ElementUtil.getModuleOf(root, pd);
        String pathPrefix = Runtime.version().major() > 10 && md != null ? md.getQualifiedName().toString() + "/" : "";
        this.outputPath = pathPrefix + pd.getQualifiedName().toString().replace('.', '/') + "/" + pd.getSimpleName().toString() + ".dot";
    }

    public String getDisplayName() {
        return "Package view for package " + pd;
    }

    public Options getGlobalOptions() {
        Options go = parent.getGlobalOptions();

        go.setOption(new String[] { "output", outputPath });
        go.setOption(HIDE);

        return go;
    }

    public Options getOptionsFor(DocTrees dt, TypeElement cd) {
        Options go = parent.getGlobalOptions();
        overrideForClass(go, cd);
        return go;
    }

    public Options getOptionsFor(CharSequence name) {
        Options go = parent.getGlobalOptions();
        overrideForClass(go, name);
        return go;
    }

    public void overrideForClass(Options opt, TypeElement cd) {
        opt.setOptions(docTrees, cd);
        boolean inPackage = matcher.matches(cd);
        if (inPackage) {
            opt.showQualified = false;
        }
        boolean included = inPackage || this.opt.matchesIncludeExpression(cd.getQualifiedName());
        if (!included || this.opt.matchesHideExpression(cd.getQualifiedName())) {
            opt.setOption(HIDE);
        }
    }

    public void overrideForClass(Options opt, CharSequence className) {
        opt.showQualified = false;
        boolean inPackage = matcher.matches(className);
        if (inPackage)
            opt.showQualified = false;
        boolean included = inPackage || this.opt.matchesIncludeExpression(className);
        if (!included || this.opt.matchesHideExpression(className)) {
            opt.setOption(HIDE);
        }
    }

}
