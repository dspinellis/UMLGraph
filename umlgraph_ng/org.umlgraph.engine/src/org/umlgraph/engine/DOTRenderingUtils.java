/*
 * (C) Copyright 2008 Abstratt Technologies
 *
 * Permission to use, copy, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * $Id$
 *
 */
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
