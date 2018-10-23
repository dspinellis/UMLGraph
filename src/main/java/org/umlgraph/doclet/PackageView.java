package org.umlgraph.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

/**
 * A view designed for UMLDoc, filters out everything that it's not contained in
 * the specified package.
 * <p>
 * As such, can be viewed as a simplified version of a {@linkplain View} using a
 * single {@linkplain ClassMatcher}, and provides some extra configuration such
 * as output path configuration (and it is specified in code rather than in
 * javadoc comments).
 * @author wolf
 * 
 */
public class PackageView implements OptionProvider {

    private static final String[] HIDE = new String[] { "-hide" };
    private PackageDoc pd;
    private OptionProvider parent;
    private ClassMatcher matcher;
    private String outputPath;
    private Options opt;

    public PackageView(String outputFolder, PackageDoc pd, RootDoc root, OptionProvider parent) {
	this.parent = parent;
	this.pd = pd;
	this.matcher = new PackageMatcher(pd);
	this.opt = parent.getGlobalOptions();
	this.opt.setOptions(pd);
	this.outputPath = pd.name().replace('.', '/') + "/" + pd.name() + ".dot";
    }

    public String getDisplayName() {
	return "Package view for package " + pd;
    }

    public Options getGlobalOptions() {
	Options go = parent.getGlobalOptions();

	go.setOption(new String[] { "-output", outputPath });
	go.setOption(HIDE);

	return go;
    }

    public Options getOptionsFor(ClassDoc cd) {
	Options go = parent.getGlobalOptions();
	overrideForClass(go, cd);
	return go;
    }

    public Options getOptionsFor(String name) {
	Options go = parent.getGlobalOptions();
	overrideForClass(go, name);
	return go;
    }

    public void overrideForClass(Options opt, ClassDoc cd) {
	opt.setOptions(cd);
	boolean inPackage = matcher.matches(cd);
	if (inPackage)
	    opt.showQualified = false;
	if (!(inPackage || this.opt.matchesIncludeExpression(cd.qualifiedName()))
		|| this.opt.matchesHideExpression(cd.qualifiedName()))
	    opt.setOption(HIDE);
    }

    public void overrideForClass(Options opt, String className) {
	opt.showQualified = false;
	boolean inPackage = matcher.matches(className);
	if (inPackage)
	    opt.showQualified = false;
	if (!(inPackage || this.opt.matchesIncludeExpression(className))
		|| this.opt.matchesHideExpression(className))
	    opt.setOption(HIDE);
    }

}
