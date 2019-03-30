package org.umlgraph.doclet;

import java.io.IOException;
import java.util.regex.Pattern;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * A view designed for UMLDoc, filters out everything that it's not directly
 * connected to the center class of the context.
 * <p>
 * As such, can be viewed as a simplified version of a {@linkplain View} using a
 * single {@linkplain ContextMatcher}, but provides some extra configuration
 * such as context highlighting and output path configuration (and it is
 * specified in code rather than in javadoc comments).
 * @author wolf
 * 
 */
public class ContextView implements OptionProvider {

    private ClassDoc cd;
    private ContextMatcher matcher;
    private Options globalOptions;
    private Options myGlobalOptions;
    private Options hideOptions;
    private Options centerOptions;
    private Options packageOptions;
    private static final String[] HIDE_OPTIONS = new String[] { "hide" };

    public ContextView(String outputFolder, ClassDoc cd, RootDoc root, Options parent)
	    throws IOException {
	this.cd = cd;
	String outputPath = cd.containingPackage().name().replace('.', '/') + "/" + cd.name()
		+ ".dot";

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

	this.matcher = new ContextMatcher(root, Pattern.compile(Pattern.quote(cd.toString())),
		myGlobalOptions, true);

    }

    public void setContextCenter(ClassDoc contextCenter) {
	this.cd = contextCenter;
	String outputPath = cd.containingPackage().name().replace('.', '/') + "/" + cd.name()
		+ ".dot";
	this.myGlobalOptions.setOption(new String[] { "output", outputPath });
	matcher.setContextCenter(Pattern.compile(Pattern.quote(cd.toString())));
    }

    public String getDisplayName() {
	return "Context view for class " + cd;
    }

    public Options getGlobalOptions() {
	return myGlobalOptions;
    }

    public Options getOptionsFor(ClassDoc cd) {
	Options opt;
	if (globalOptions.matchesHideExpression(cd.qualifiedName())
		|| !(matcher.matches(cd) || globalOptions.matchesIncludeExpression(cd.qualifiedName()))) {
		opt = hideOptions;
	} else if (cd.equals(this.cd)) {
		opt = centerOptions;
	} else if(cd.containingPackage().equals(this.cd.containingPackage())){
		opt = packageOptions;
	} else {
		opt = globalOptions;
	}
	Options optionClone = (Options) opt.clone();
	overrideForClass(optionClone, cd);
	return optionClone;
    }

    public Options getOptionsFor(String name) {
	Options opt;
	if (!matcher.matches(name))
		opt = hideOptions;
	else if (name.equals(cd.name()))
		opt = centerOptions;
	else
		opt = globalOptions;
	Options optionClone = (Options) opt.clone();
	overrideForClass(optionClone, name);
	return optionClone;
    }

    public void overrideForClass(Options opt, ClassDoc cd) {
	opt.setOptions(cd);
	if (opt.matchesHideExpression(cd.qualifiedName())
		|| !(matcher.matches(cd) || opt.matchesIncludeExpression(cd.qualifiedName())))
	    opt.setOption(HIDE_OPTIONS);
	if (cd.equals(this.cd)) {
	    opt.nodeFillColor = "lemonChiffon";
        } else {
            // If the class is not the class on the Javadoc page, hide all the detail.
            // In order for attributes and operations to be properly hidden, 
            // all of these need to be set to false.
            opt.showAttributes = false;
            opt.showOperations = false;
            opt.showConstructors = false;
            opt.showEnumConstants = false;
        }
    }

    public void overrideForClass(Options opt, String className) {
	if (!(matcher.matches(className) || opt.matchesIncludeExpression(className)))
	    opt.setOption(HIDE_OPTIONS);
    }

}
