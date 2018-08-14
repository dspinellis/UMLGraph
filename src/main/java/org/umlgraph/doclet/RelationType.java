package org.umlgraph.doclet;

/**
 * The type of relation that links two entities
 * @author wolf
 * 
 */
public enum RelationType {
    ASSOC(), NAVASSOC(), HAS(), NAVHAS(), COMPOSED(), NAVCOMPOSED(), DEPEND(), EXTENDS(), IMPLEMENTS();

    public final String lower;

    private RelationType() {
        this.lower = toString().toLowerCase();
    }
}
