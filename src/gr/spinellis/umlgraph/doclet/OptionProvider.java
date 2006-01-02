package gr.spinellis.umlgraph.doclet;

import com.sun.javadoc.ClassDoc;

/**
 * A factory class that builds Options object for general use or for a
 * specific class
 */
public interface OptionProvider {
    /**
     * Returns the options for the specified class. 
     */
    public Options getOptionsFor(ClassDoc cd);
    
    /**
     * Returns the options for the specified class. 
     */
    public Options getOptionsFor(String name);
    
    /**
     * Returns the global options (the class independent definition) 
     */
    public Options getGlobalOptions();
    
    /**
     * Gets a base Options and applies the overrides for the specified class 
     */
    public void overrideForClass(Options opt, ClassDoc cd);
    
    /**
     * Gets a base Options and applies the overrides for the specified class 
     */
    public void overrideForClass(Options opt, String className);
}
