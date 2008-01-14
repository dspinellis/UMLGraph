package org.umlgraph.engine.classdiagram;

import org.umlgraph.engine.Diagram;
import org.umlgraph.engine.DiagramOptions;
import org.umlgraph.engine.EdgeOptions;
import org.umlgraph.engine.NodeOptions;
import org.umlgraph.engine.matching.ElementMatcher;
import org.umlgraph.settings.Settings;

/**
 * Any features specific to class diagrams are defined here.
 */
public class ClassDiagram extends Diagram {

    public ClassDiagram(String rootNamespace,
            ElementMatcher matcher) {
        super(rootNamespace, matcher);
    }

    @Override
    protected Settings createSettings() {
        return new Settings(ClassDiagramOptions.class, ClassOptions.class, PackageOptions.class, NodeOptions.class, EdgeOptions.class, DiagramOptions.class);
    }

}
