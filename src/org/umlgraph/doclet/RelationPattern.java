package org.umlgraph.doclet;

/**
 * A map from relation types to directions
 * @author wolf
 * 
 */
public class RelationPattern {
    /**
     * A map from RelationType (indexes) to Direction objects
     */
    RelationDirection[] directions;

    /**
     * Creates a new pattern using the same direction for every relation kind
     * @param defaultDirection The direction used to initialize this pattern
     */
    public RelationPattern(RelationDirection defaultDirection) {
	directions = new RelationDirection[RelationType.values().length];
	for (int i = 0; i < directions.length; i++) {
	    directions[i] = defaultDirection;
	}
    }

    /**
     * Adds, eventually merging, a direction for the specified relation type
     * @param relationType
     * @param direction
     */
    public void addRelation(RelationType relationType, RelationDirection direction) {
	int idx = relationType.ordinal();
	directions[idx] = directions[idx].sum(direction);
    }

    /**
     * Returns true if this patterns matches at least the direction of one
     * of the relations in the other relation patterns. Matching is defined
     * by {@linkplain RelationDirection#contains(RelationDirection)}
     * @param relationPattern
     * @return
     */
    public boolean matchesOne(RelationPattern relationPattern) {
	for (int i = 0; i < directions.length; i++) {
	    if (directions[i].contains(relationPattern.directions[i]))
		return true;
	}
	return false;
    }

}
