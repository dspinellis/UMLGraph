package org.umlgraph.engine.tests;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.util.UMLUtil;

public class TestStandalone extends TestCase {

    public TestStandalone(String name) {
	super(name);
    }

    public void testStandalone() throws IOException {
	ResourceSet resourceSet = TestUtils.assemble("sample.uml");
	assertNotNull(UMLUtil.findNamedElements(resourceSet, "sample"));
	assertNotNull(UMLUtil.findNamedElements(resourceSet, "sample::SampleClass"));
    }

    public static Test suite() {
	return new TestSuite(TestStandalone.class);
    }
}
