package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Element;

/**
 * A conjunction composite matcher. 
 */
public class AndMatcher implements ElementMatcher {

    private ElementMatcher[] subMatchers;

    public AndMatcher(ElementMatcher... subMatchers) {
        this.subMatchers = subMatchers;
    }

    public boolean matches(Element element) {
        for (ElementMatcher current : subMatchers)
            if (!current.matches(element))
                return false;
        return true;
    }

}
