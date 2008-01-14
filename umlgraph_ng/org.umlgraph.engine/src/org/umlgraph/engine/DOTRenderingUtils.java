package org.umlgraph.engine;

import java.io.PrintWriter;

public class DOTRenderingUtils {
	public static void addAttribute(PrintWriter pw, String attribute, int value) {
		pw.println(attribute + " = " + value);
	}

	public static void addAttribute(PrintWriter pw, String attribute, String value) {
		pw.println(attribute + " = \"" + value + "\"");
	}

	public static void newLine(PrintWriter pw) {
		pw.print("\\n");
	}
}
