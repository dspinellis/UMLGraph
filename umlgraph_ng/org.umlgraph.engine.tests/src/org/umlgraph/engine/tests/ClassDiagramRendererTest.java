package org.umlgraph.engine.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.umlgraph.engine.classdiagram.ClassDiagram;
import org.umlgraph.engine.classdiagram.dot.ClassDiagramRenderer;

public class ClassDiagramRendererTest extends TestCase {

    public ClassDiagramRendererTest(String name) {
	super(name);
    }
    
    public void testBasic() throws IOException {
	ResourceSet resourceSet = TestUtils.assemble("payment.uml");
	ClassDiagram diagram = new ClassDiagram("payment", null);
	ByteArrayOutputStream output = new ByteArrayOutputStream();
	new ClassDiagramRenderer().render(resourceSet, diagram, output);
	//TODO adopt dot comparison utilities from UMLGraph
	System.out.println(new String(output.toByteArray()));
    }

    public static Test suite() {
	return new TestSuite(ClassDiagramRendererTest.class);
    }
}
