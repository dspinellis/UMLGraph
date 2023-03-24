package org.umlgraph.doclet;

import java.util.regex.Pattern;

import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.umlgraph.doclet.util.ElementUtil;

/**
 * Matches every class that implements (directly or indirectly) an interfaces
 * matched by regular expression provided.
 */
public class InterfaceMatcher implements ClassMatcher {

    protected DocletEnvironment root;
    protected Pattern pattern;

    public InterfaceMatcher(DocletEnvironment root, Pattern pattern) {
        this.root = root;
        this.pattern = pattern;
    }

    public boolean matches(TypeElement cd) {
        // if it's the interface we're looking for, match
        if (cd.getKind() == ElementKind.INTERFACE && pattern.matcher(cd.toString()).matches()) {
            return true;
        }

        // for each interface, recurse, since classes and interfaces
        // are treated the same in the doclet API
        for (TypeMirror type : cd.getInterfaces()) {
            TypeElement iType = ElementUtil.getTypeElement(type);
            if (iType != null && matches(iType)) {
                return true;
            }
        }

        // recurse on superclass, if available
        TypeElement scd = ElementUtil.getTypeElement(cd.getSuperclass());
        return scd == null ? false : matches(scd);
    }

    public boolean matches(CharSequence name) {
        TypeElement cd = root.getElementUtils().getTypeElement(name);
        return cd == null ? false : matches(cd);
    }

}
