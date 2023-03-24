package org.umlgraph.doclet.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import org.umlgraph.doclet.TagScanner;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.DocletEnvironment;

public class TagUtil {

    public static Map<String, List<String>> getTags(DocletEnvironment root, Element c) {
        return getTags(root.getDocTrees(), c);
    }
    
    public static Map<String, List<String>> getTags(DocTrees docTrees, Element c) {
        DocCommentTree tree = docTrees.getDocCommentTree(c);
        Map<String, List<String>> tagsByName = new HashMap<>();
        new TagScanner(tagsByName).visitDocComment(tree, null);
        return tagsByName;
    }
    
    public static String getComment(DocletEnvironment root, Element c) {
        return getComment(root.getDocTrees(), c);
    }
    
    public static String getComment(DocTrees docTrees, Element c) {
        List<String> comments = getTag(docTrees, c, "comment");
        if (comments == null || comments.isEmpty()) {
            return "";
        }
        return comments.get(0);
    }
    
    public static List<String> getTag(DocletEnvironment root, Element c, String tagName) {
        return getTag(root.getDocTrees(), c, tagName);
    }
    
    public static List<String> getTag(DocTrees docTrees, Element c, String tagName) {
        List<String> tags = getTags(docTrees, c).get(tagName);
        return tags == null ? Collections.emptyList() : tags;
    }
}
