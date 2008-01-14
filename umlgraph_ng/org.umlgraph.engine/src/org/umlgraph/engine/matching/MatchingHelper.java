package org.umlgraph.engine.matching;

import org.eclipse.emf.ecore.EClass;

public class MatchingHelper {
    public static ElementMatcher matchNameAndMetaClass(String name, EClass metaClass) {
        return new AndMatcher(new NameMatcher(name, false), new MetaClassMatcher(metaClass, false));
    }
}
