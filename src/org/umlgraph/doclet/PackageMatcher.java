package org.umlgraph.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;

public class PackageMatcher implements ClassMatcher {
    protected PackageDoc packageDoc;

    public PackageMatcher(PackageDoc packageDoc) {
	super();
	this.packageDoc = packageDoc;
    }

    public boolean matches(ClassDoc cd) {
	return cd.containingPackage().equals(packageDoc);
    }

    public boolean matches(String name) {
	for (ClassDoc cd : packageDoc.allClasses()) {
	    if (cd.qualifiedName().equals(name))
		return true;
	}
	return false;
    }

}
