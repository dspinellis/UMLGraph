package org.umlgraph.doclet;

/**
 * The type of relation that links two entities, and the graphviz format
 * 
 * @author Erich Schubert
 */
public enum RelationType {
    ASSOC("arrowhead=none"), //
    NAVASSOC("arrowhead=open"), //
    HAS("arrowhead=none, arrowtail=ediamond, dir=both"), //
    NAVHAS("arrowhead=open, arrowtail=ediamond, dir=both"), //
    COMPOSED("arrowhead=none, arrowtail=diamond, dir=both"), //
    NAVCOMPOSED("arrowhead=open, arrowtail=diamond, dir=both"), //
    DEPEND("arrowhead=open, style=dashed"), //
    EXTENDS("arrowtail=empty, dir=back"), //
    IMPLEMENTS("arrowtail=empty, dir=back, style=dashed");

    public final String lower, style;

    /** Enum constructors must be private */
    private RelationType(String style) {
	this.lower = toString().toLowerCase();
	this.style = style;
    }
}
