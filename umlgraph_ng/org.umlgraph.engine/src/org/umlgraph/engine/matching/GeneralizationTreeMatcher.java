package org.umlgraph.engine.matching;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;

public class GeneralizationTreeMatcher implements ElementMatcher {
    private String generalName;
    public GeneralizationTreeMatcher(String generalName) {
        this.generalName = generalName;
    }
    public boolean matches(Element element) {
        if (!(element instanceof Classifier))
            return false;
        Classifier classifier = (Classifier) element;
        if (classifier.getQualifiedName().equals(generalName))
            return true;
        return isDescendant(classifier);
    }
    
    private boolean isDescendant(Classifier current) {
        if (current.getGeneral(generalName) != null)
            return true;
        for (Classifier general : current.getGenerals())
            if (isDescendant(general))
                return true;
        return false;
    }

}
