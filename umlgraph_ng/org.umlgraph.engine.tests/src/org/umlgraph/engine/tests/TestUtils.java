package org.umlgraph.engine.tests;

import java.io.IOException;
import java.net.URL;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.umlgraph.engine.Util;

public class TestUtils {
    private static final String BASE_PATH = "/test-models/";

    public static ResourceSet assemble(String... paths) throws IOException {
	ResourceSet resourceSet = Util.createResourceSet();
	for (int i = 0; i < paths.length; i++) {
	    URL resourceURL = TestUtils.class.getResource(BASE_PATH + paths[i]);
	    Resource resource = resourceSet.getResource(URI.createURI(resourceURL.toString()), true);
	    resource.load(null);
	}
	return resourceSet;
    }
}
