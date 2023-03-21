package org.umlgraph.doclet;

import java.util.regex.Pattern;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Matches every class that extends (directly or indirectly) a class matched by
 * the regular expression provided.
 */
public class SubclassMatcher implements ClassMatcher {

    protected RootDoc root;
    protected Pattern pattern;

    public SubclassMatcher(RootDoc root, Pattern pattern) {
        this.root = root;
        this.pattern = pattern;
    }

    public boolean matches(ClassDoc cd) {
        // if it's the class we're looking for return
        if (pattern.matcher(cd.toString()).matches())
            return true;

        // recurse on supeclass, if available
        return cd.superclass() == null ? false : matches(cd.superclass());
    }

    public boolean matches(String name) {
        ClassDoc cd = root.classNamed(name);
        return cd == null ? false : matches(cd);
    }

}
