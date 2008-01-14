package org.umlgraph.engine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

public class Util {

    public static String getNamespace(Class<?> class_) {
        return class_.getPackage().getName();
    }

    private static void registerUML(ResourceSet resourceSet) {
        resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI,
                UMLPackage.eINSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
        Map<URI, URI> uriMap = resourceSet.getURIConverter().getURIMap();
        URL umlMetamodelURL = Util.class.getResource("/metamodels/UML.metamodel.uml");
        if (umlMetamodelURL == null)
            throw new IllegalStateException(
                    "UML metamodel not found in the classpath");
        URL base;
        try {
            base = new URL(umlMetamodelURL, "..");
        } catch (MalformedURLException e) {
            // should never happen,   
            // the URL is an existing valid URL + '..'
            throw new IllegalStateException("Could not register packages", e);
        }
        URI uri = URI.createURI(base.toString());
        uriMap.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP), uri
                .appendSegment("libraries").appendSegment(""));
        uriMap.put(URI.createURI(UMLResource.METAMODELS_PATHMAP), uri
                .appendSegment("metamodels").appendSegment(""));
        uriMap.put(URI.createURI(UMLResource.PROFILES_PATHMAP), uri
                .appendSegment("profiles").appendSegment(""));
    }

    public static ResourceSet createResourceSet() {
        ResourceSet newResourceSet = new ResourceSetImpl();
        if (!Activator.isRunningOSGi())
            registerUML(newResourceSet);
        return newResourceSet;
    }
}
