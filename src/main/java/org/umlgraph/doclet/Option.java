package org.umlgraph.doclet;

import java.util.List;

import jdk.javadoc.doclet.Doclet;

/**
 * A base class for declaring options. Subtypes for specific options should
 * implement the {@link #process(String,List) process} method to handle
 * instances of the option found on the command line.
 */
public abstract class Option implements Doclet.Option {
    private final String name;
    private final int argumentCount;
    private final String description;
    private final String parameters;

    public Option(String name, boolean hasArg, String description, String parameters) {
        this(name, hasArg ? 1 : 0, description, parameters);
    }
    
    public Option(String name, int argumentCount, String description, String parameters) {
        this.name = name;
        this.argumentCount = argumentCount;
        this.description = description;
        this.parameters = parameters;
    }

    @Override
    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
        return List.of(name);
    }

    @Override
    public String getParameters() {
        return argumentCount == 0 ? "" : parameters;
    }
}