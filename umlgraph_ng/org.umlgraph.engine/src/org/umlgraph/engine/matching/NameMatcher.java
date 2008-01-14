package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class NameMatcher implements ElementMatcher {

    private boolean caseSensitive;
    private String name;

    public NameMatcher(String name, boolean caseSensitive) {
        this.name = name;
        this.caseSensitive = caseSensitive;
    }

    public boolean matches(Element element) {
        if (!(element instanceof NamedElement))
            return false;
        NamedElement named = (NamedElement) element;
        String elementName = named.getQualifiedName();
        return caseSensitive ? name.equals(elementName) : name
                .equalsIgnoreCase(elementName);
    }

}
