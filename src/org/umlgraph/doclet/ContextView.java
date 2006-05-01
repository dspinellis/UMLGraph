package gr.spinellis.umlgraph.doclet;

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
    private OptionProvider parent;
    private ContextMatcher matcher;
    private String outputPath;

    public ContextView(String outputFolder, ClassDoc cd, RootDoc root, OptionProvider parent)
	    throws IOException {
	this.parent = parent;
	this.cd = cd;
	this.matcher = new ContextMatcher(root, Pattern.compile(cd.qualifiedName()),
		getGlobalOptions());
	this.outputPath = cd.containingPackage().name().replace('.', '/') + "/" + cd.name()
		+ ".dot";
    }

    public String getDisplayName() {
	return "Context view for class " + cd;
    }

    public Options getGlobalOptions() {
	Options go = parent.getGlobalOptions();

	go.setOption(new String[] { "-output", outputPath });
	go.setOption(new String[] { "-hide" });

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
	if (parent.getGlobalOptions().matchesHideExpression(cd.name()) || !matcher.matches(cd))
	    opt.setOption(new String[] { "-hide" });
	if (cd.equals(this.cd))
	    opt.nodeFillColor = "lemonChiffon";
    }

    public void overrideForClass(Options opt, String className) {
	if (!matcher.matches(className))
	    opt.setOption(new String[] { "-hide" });
    }

}
