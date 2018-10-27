package org.umlgraph.doclet;

/**
 * The type of relation that links two entities, and the graphviz format
 * 
 * @author Erich Schubert
 */
public enum RelationType {
    DEPEND("arrowhead=open,style=dashed,weight=0", false), // Weakest
    NAVASSOC("arrowhead=open,weight=1", false), //
    ASSOC("arrowhead=none,weight=2", false), //
    NAVHAS("arrowhead=open,arrowtail=ediamond,dir=both,weight=3", false), //
    HAS("arrowhead=none,arrowtail=ediamond,dir=back,weight=4", false), //
    NAVCOMPOSED("arrowhead=open,arrowtail=diamond,dir=both,weight=5", false), //
    COMPOSED("arrowhead=none,arrowtail=diamond,dir=back,weight=6", false), //
    IMPLEMENTS("arrowtail=empty,style=dashed,dir=back,weight=9", true), //
    EXTENDS("arrowtail=empty,dir=back,weight=10", true);

    /** Lower case version of the label */
    public final String lower;

    /** Graphviz style */
    public final String style;

    /** Backwards edges with respect to node ranking */
    public final boolean backorder;

    /** Enum constructors must be private */
    private RelationType(String style, boolean backorder) {
	this.lower = toString().toLowerCase();
	this.style = style;
	this.backorder = backorder;
    }
}
