package org.umlgraph.doclet;

/**
 * The possibile directions of a relation given a reference class (used in
 * context diagrams)
 */
public enum RelationDirection {
    NONE, IN, OUT, BOTH;

    /**
     * Adds the current direction
     * @param d
     * @return
     */
    public RelationDirection sum(RelationDirection d) {
	// Handle same and nones first:
	return (this == d || d == NONE) ? this : //
		this == NONE ? d : BOTH; // They are different and not none.
    }

    /**
     * Returns true if this direction "contains" the specified one, that is,
     * either it's equal to it, or this direction is {@link #BOTH}
     * @param d
     * @return
     */
    public boolean contains(RelationDirection d) {
	return this == BOTH ? true : (d == this);
    }

    /**
     * Inverts the direction of the relation. Turns IN into OUT and vice-versa, NONE and BOTH
     * are not changed
     * @return
     */
    public RelationDirection inverse() {
	return this == IN ? OUT : this == OUT ? IN : this;
    }
}
