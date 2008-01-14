package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Element;

/**
 * Protocol for element matchers.
 */
public interface ElementMatcher {
    public boolean matches(Element element);
}
