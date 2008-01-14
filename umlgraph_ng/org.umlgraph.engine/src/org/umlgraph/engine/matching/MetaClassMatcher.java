package org.umlgraph.engine.matching;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Element;

public class MetaClassMatcher implements ElementMatcher {

    private boolean exact;
    private EClass metaClass;

    public MetaClassMatcher(EClass metaClass, boolean exact) {
        this.metaClass = metaClass;
        this.exact = exact;
    }

    public boolean matches(Element element) {
        return exact ? metaClass == element.eClass() : metaClass
                .isInstance(element);
    }

}
