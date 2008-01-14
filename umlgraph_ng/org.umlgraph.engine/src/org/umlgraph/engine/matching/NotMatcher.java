package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Element;

/**
 * A negation matcher.
 */
public class NotMatcher implements ElementMatcher {

    private ElementMatcher baseMatcher;

    public boolean matches(Element element) {
        return !baseMatcher.matches(element);
    }

    public NotMatcher(ElementMatcher baseMatcher) {
        super();
        this.baseMatcher = baseMatcher;
    }

}
