package org.umlgraph.doclet;

import java.util.regex.Pattern;

import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.TypeElement;

import org.umlgraph.doclet.util.ElementUtil;

/**
 * Matches every class that extends (directly or indirectly) a class matched by
 * the regular expression provided.
 */
public class SubclassMatcher implements ClassMatcher {

    protected DocletEnvironment root;
    protected Pattern pattern;

    public SubclassMatcher(DocletEnvironment root, Pattern pattern) {
        this.root = root;
        this.pattern = pattern;
    }

    public boolean matches(TypeElement cd) {
        // if it's the class we're looking for return
        if (pattern.matcher(cd.toString()).matches()) {
            return true;
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
