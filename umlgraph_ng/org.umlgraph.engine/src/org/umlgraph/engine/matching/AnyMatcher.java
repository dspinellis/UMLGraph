package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Element;

public class AnyMatcher implements ElementMatcher {
    public final static ElementMatcher INSTANCE = new AnyMatcher(); 
    public boolean matches(Element element) {
        return true;
    }
}
