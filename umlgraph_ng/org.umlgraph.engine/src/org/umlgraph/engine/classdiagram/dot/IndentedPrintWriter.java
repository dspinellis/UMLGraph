/*
 * (C) Copyright 2007-2008 Abstratt Technologies
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
package org.umlgraph.engine.classdiagram.dot;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A print writer with an extended protocol that allows increasing/descreasing
 * the level of indentation.
 */
public class IndentedPrintWriter extends PrintWriter {
    public static final String DEFAULT_TAB_STRING = "    ";
    private int indentationLevel = 0;
    private char[] tabString;
    private boolean indented = false;

    public IndentedPrintWriter(OutputStream os) {
        this(os, false);
    }

    public IndentedPrintWriter(Writer w) {
        this(w, false);
    }

    public IndentedPrintWriter(OutputStream os, boolean flush) {
        super(os, flush);
        setTabString(DEFAULT_TAB_STRING);
    }

    public IndentedPrintWriter(Writer w, boolean flush) {
        super(w, flush);
        setTabString(DEFAULT_TAB_STRING);
    }

    public void setTabString(String tabString) {
        this.tabString = tabString.toCharArray();
    }

    public String getTabString() {
        return new String(this.tabString);
    }

    public void enterLevel() {
        indentationLevel++;
    }

    public void exitLevel() {
        if (indentationLevel == 0)
            throw new IllegalStateException("No more levels to exit");
        indentationLevel--;
    }

    @Override
    public void println() {
        super.println();
        indented = false;
    }

    @Override
    public void write(String s, int off, int len) {
        indentIfNeeded();
        super.write(s, off, len);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        indentIfNeeded();
        super.write(buf, off, len);
    }

    @Override
    public void write(int c) {
        indentIfNeeded();
        super.write(c);
    }

    private void indentIfNeeded() {
        if (!indented) {
            indented = true;
            indent();
        }
    }

    private void indent() {
        for (int i = 0; i < indentationLevel; i++)
            super.print(tabString);
    }
}
