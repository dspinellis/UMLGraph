package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Element;

/**
 * A disjunction composite matcher.
 *
 */
public class OrMatcher implements ElementMatcher {

    private ElementMatcher[] subMatchers;

    public OrMatcher(ElementMatcher[] subMatchers) {
        super();
        this.subMatchers = subMatchers;
    }

    public boolean matches(Element element) {
        for (ElementMatcher current : subMatchers)
            if (current.matches(element))
                return true;
        return false;
    }

}
