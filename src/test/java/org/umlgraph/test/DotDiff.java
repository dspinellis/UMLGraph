/*
 * UmlGraph class diagram testing framework
 *
 * Contibuted by Andrea Aime
 * (C) Copyright 2005 Diomidis Spinellis
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
 *
 */

package org.umlgraph.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DotDiff {

    private List<DotNode> nodes1 = new ArrayList<DotNode>();

    private List<DotNode> nodes2 = new ArrayList<DotNode>();

    private List<DotArc> arcs1 = new ArrayList<DotArc>();

    private List<DotArc> arcs2 = new ArrayList<DotArc>();

    private List<String> extraLines1 = new ArrayList<String>();

    private List<String> extraLines2 = new ArrayList<String>();

    /**
     * Builds a dot differ on the two files
     * 
     * @param dotFirst
     * @param dotSecond
     * @throws IOException
     */
    public DotDiff(File dotFirst, File dotSecond) throws IOException {
        // gather the lines
        List<String> lines1 = readGraphLines(dotFirst);
        List<String> lines2 = readGraphLines(dotSecond);

        // parse the lines
        extraLines1 = parseLines(lines1, nodes1, arcs1);
        extraLines2 = parseLines(lines2, nodes2, arcs2);

        // diff extra lines
        for (Iterator<String> it = extraLines1.iterator(); it.hasNext();) {
            if (extraLines2.remove(it.next()))
                it.remove();
        }

        // diff nodes
        for (Iterator<DotNode> it = nodes1.iterator(); it.hasNext();) {
            if (nodes2.remove(it.next()))
                it.remove();
        }

        // diff arcs
        for (Iterator<DotArc> it = arcs1.iterator(); it.hasNext();) {
            if (arcs2.remove(it.next()))
                it.remove();
        }
    }

    /**
     * Returns true if the dot files are structurally equal, that is, if every
     * non comment and non header line of the first file appears in the second,
     * and otherwise.
     * 
     * @return
     */
    public boolean graphEquals() {
        return (extraLines1.size() + extraLines2.size() + nodes1.size() + nodes2.size()
                + arcs1.size() + arcs2.size()) == 0;
    }

    public List<DotArc> getArcs1() {
        return arcs1;
    }

    public List<DotArc> getArcs2() {
        return arcs2;
    }

    public List<String> getExtraLines1() {
        return extraLines1;
    }

    public List<String> getExtraLines2() {
        return extraLines2;
    }

    public List<DotNode> getNodes1() {
        return nodes1;
    }

    public List<DotNode> getNodes2() {
        return nodes2;
    }

    /**
     * Reads all relevant lines from the dot file
     * 
     * @param dotFile
     * @return
     * @throws IOException
     */
    private List<String> readGraphLines(File dotFile) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dotFile));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (graphDefinitionLine(line))
                    lines.add(line);
            }
        } finally {
            if (br != null)
                br.close();
        }

        return lines;
    }

    /**
     * Tells if a line is relevant or not (unrelevant lines are headers,
     * comments, "digraf G {" and the closing "}" (to simplify matters we assume
     * the file is properly structured).
     * 
     * @param line
     * @return
     */
    private boolean graphDefinitionLine(String line) {
        return !(line.startsWith("#") || line.startsWith("//") || line.equals("digraph G {") || line
                .equals("}"));
    }

    private List<String> parseLines(List<String> lines, List<DotNode> nodeList, List<DotArc> arcs)
            throws IOException {
        List<String> extraLines = new ArrayList<String>();
        List<String> arcLines = new ArrayList<String>();
        Map<String, DotNode> nodes = new HashMap<String, DotNode>();
        for (String line : lines) {
            int openBrackedIdx = line.indexOf('[');
            int closedBracketIdx = line.lastIndexOf(']');
            int arrowIdx = line.indexOf("->");
            if (openBrackedIdx < 0 && closedBracketIdx < 0 || line.startsWith("edge")
                    || line.startsWith("node"))
                extraLines.add(line);
            else if (arrowIdx > 0 && arrowIdx < openBrackedIdx) { // that's an arc
                arcLines.add(line);
            } else { // that's a node
                String attributes = line.substring(openBrackedIdx + 1, closedBracketIdx);
                Map<String, String> attMap = parseAttributes(attributes);
                String name = line.substring(0, openBrackedIdx - 1).trim();
                String label = attMap.get("label");
                DotNode node = new DotNode(name, label, attMap, line);
                nodes.put(name, node);
                nodeList.add(node);
            }
        }
        for (String line : arcLines) {
            int openBrackedIdx = line.indexOf('[');
            int closedBracketIdx = line.lastIndexOf(']');
            String attributes = line.substring(openBrackedIdx + 1, closedBracketIdx);
            String[] names = line.substring(0, openBrackedIdx).split("->");
            DotNode from = nodes.get(names[0].trim());
            DotNode to = nodes.get(names[1].trim());
            if (from == null) {
                from = new DotNode(names[0], "", new HashMap<String, String>(), "");
            }
            if (to == null) {
                to = new DotNode(names[1], "", new HashMap<String, String>(), "");
            }
            arcs.add(new DotArc(from, to, parseAttributes(attributes), line));
        }
        return extraLines;
    }

    private Map<String, String> parseAttributes(String attributes) throws IOException {
        Map<String, String> map = new HashMap<String, String>();

        StreamTokenizer st = new StreamTokenizer(new StringReader(attributes));
        st.wordChars('_', '_');
        st.wordChars('\\', '\\');
        st.wordChars('\'', '\'');
        int tokenType;
        boolean isValue = false;
        String attName = null;
        String token = null;
        while ((tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {
            switch (tokenType) {
            case StreamTokenizer.TT_NUMBER:
                token = "" + st.nval;
                break;
            case StreamTokenizer.TT_WORD:
            case '"':
            case '\'':
                token = st.sval;
                break;
            }
            tokenType = st.nextToken();

            if (isValue) {
                map.put(attName, token);
                isValue = false;
            } else {
                attName = token;
                isValue = true;
            }
        }

        return map;
    }

    private static class DotNode {
        String name;

        String label;

        Map<String, String> attributes;

        String line;

        public DotNode(String name, String label, Map<String, String> attributes, String line) {
            this.name = name;
            this.label = label.replace("\n", "\\n");
            this.attributes = attributes;
            this.line = line.replace("\n", "\\n");
        }

        public boolean equals(Object other) {
            if (!(other instanceof DotNode))
                return false;

            DotNode on = (DotNode) other;
            if (label == null) // anonymous node
                return on.label == null && on.name.equals(name) && on.attributes.equals(attributes);
            else
                return on.label.equals(label) && on.attributes.equals(attributes);
        }

        public int hashCode() {
            return name.hashCode() + 17 * attributes.hashCode();
        }

        public String toString() {
            return "Node: " + label + "; " + line;
        }

    }

    private static class DotArc {
        DotNode from;

        DotNode to;

        Map<String, String> attributes;

        String line;

        public DotArc(DotNode from, DotNode to, Map<String, String> attributes, String line) {
            this.from = from;
            this.to = to;
            this.attributes = attributes;
            this.line = line.replace("\n", "\\n");
        }

        public boolean equals(Object other) {
            if (!(other instanceof DotArc))
                return false;

            DotArc oa = (DotArc) other;
            return oa.from.equals(from) && oa.to.equals(to) && oa.attributes.equals(attributes);
        }

        public int hashCode() {
            return from.hashCode() + 17 * (to.hashCode() + 17 * attributes.hashCode());
        }

        public String toString() {
            return "Arc: " + from.label + " -> " + to.label + "; " + line;
        }
    }

}
