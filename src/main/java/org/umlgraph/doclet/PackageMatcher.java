package org.umlgraph.doclet;

import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.umlgraph.doclet.util.ElementUtil;

public class PackageMatcher implements ClassMatcher {
    protected DocletEnvironment root;
    protected PackageElement packageDoc;

    public PackageMatcher(DocletEnvironment root, PackageElement packageDoc) {
        this.root = root;
        this.packageDoc = packageDoc;
    }

    public boolean matches(TypeElement cd) {
        return packageDoc.equals(ElementUtil.getPackageOf(root, cd));
    }

    public boolean matches(CharSequence name) {
        for (Element cd : packageDoc.getEnclosedElements()) {
            if (cd instanceof TypeElement && ((TypeElement) cd).getQualifiedName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
