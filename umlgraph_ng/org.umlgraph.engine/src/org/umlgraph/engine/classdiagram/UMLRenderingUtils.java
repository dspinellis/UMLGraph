package org.umlgraph.engine.classdiagram;

import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.VisibilityKind;

public class UMLRenderingUtils {

	public static String ID = UMLRenderingUtils.class.getPackage().getName();

	public static String addGuillemots(String original) {
		return "&laquo; " + original + " &raquo;";
	}

	public static void logUnexpected(String message, Exception e) {
	    //TODO adopt logging
	    System.err.println(message);
	    e.printStackTrace();
	}

	public static String renderMultiplicity(MultiplicityElement multiple, boolean brackets) {
		if (!multiple.isMultivalued())
			return "";
		if (multiple.lowerBound() == multiple.upperBound()) {
			if (multiple.upperBound() == -1)
				return wrapInBrackets("*", brackets);
			else if (multiple.upperBound() != 1) {
				return wrapInBrackets(Integer.toString(multiple.upperBound()), brackets);
			}
			return "";
		}
		StringBuffer interval = new StringBuffer();
		interval.append(multiple.lowerBound());
		interval.append("..");
		interval.append(multiple.upperBound() == -1 ? "*" : multiple.upperBound());
		return wrapInBrackets(interval.toString(), brackets);
	}

	public static String renderVisibility(VisibilityKind visibility) {
		switch (visibility) {
		case PACKAGE_LITERAL:
			return "~";
		case PRIVATE_LITERAL:
			return "-";
		case PROTECTED_LITERAL:
			return "#";
		case PUBLIC_LITERAL:
			return "+";
		}
		return "";
	}

	private static String wrapInBrackets(String original, boolean useBrackets) {
		return useBrackets ? "[" + original + "]" : " " + original;
	}
}
