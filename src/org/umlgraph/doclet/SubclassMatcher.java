package org.umlgraph.doclet;

import java.util.regex.Pattern;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Matches every class that extends (directly or indirectly) a class
 * matched by the regular expression provided.
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
	if(pattern.matcher(cd.toString()).matches())
	    return true;
	
	// recurse on supeclass, if available
	if(cd.superclass() != null)
	    return matches(cd.superclass());
	
	return false;
    }

    public boolean matches(String name) {
	ClassDoc cd = root.classNamed(name);
	if(cd == null)
	    return false;
	return matches(cd);
    }

}
