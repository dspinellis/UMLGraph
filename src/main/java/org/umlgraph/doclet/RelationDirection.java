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
	if (this == NONE)
	    return d;

	if ((this == IN && d == OUT) || (this == OUT && d == IN) || this == BOTH || d == BOTH)
	    return BOTH;
	return this;
    }

    /**
     * Returns true if this direction "contains" the specified one, that is,
     * either it's equal to it, or this direction is {@link #BOTH}
     * @param d
     * @return
     */
    public boolean contains(RelationDirection d) {
	if (this == BOTH)
	    return true;
	else
	    return d == this;
    }

    /**
     * Inverts the direction of the relation. Turns IN into OUT and vice-versa, NONE and BOTH
     * are not changed
     * @return
     */
    public RelationDirection inverse() {
	if (this == IN)
	    return OUT;
	else if (this == OUT)
	    return IN;
	else
	    return this;
    }

};
