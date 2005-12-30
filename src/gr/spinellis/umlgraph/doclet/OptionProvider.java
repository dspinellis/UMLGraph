package gr.spinellis.umlgraph.doclet;

import com.sun.javadoc.ClassDoc;

public interface OptionProvider {
    public Options getOptionsFor(ClassDoc cd);
    
    public Options getOptionsFor(String name);
    
    public Options getGlobalOptions();
}
