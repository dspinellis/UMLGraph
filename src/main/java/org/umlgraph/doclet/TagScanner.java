package org.umlgraph.doclet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.umlgraph.doclet.util.CommentHelper;

import com.sun.source.doctree.*;
import com.sun.source.util.SimpleDocTreeVisitor;

/**
 * A visitor to gather the block tags found in a comment.
 */
public class TagScanner extends SimpleDocTreeVisitor<Void, Void> {

    private final Map<String, List<String>> tags;

    public TagScanner(Map<String, List<String>> tags) {
        this.tags = tags;
    }

    @Override
    public Void visitDocComment(DocCommentTree tree, Void p) {
        if (tree == null) {
            return null;
        }
        String body = CommentHelper.getText(tree.getFullBody());
        tags.put("comment", new ArrayList<>(List.of(body)));
        return visit(tree.getBlockTags(), null);
    }

    @Override
    public Void visitAuthor(AuthorTree node, Void p) {
        String name = node.getTagName();
        String content = node.getName().toString();
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitDeprecated(DeprecatedTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getBody());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitProvides(ProvidesTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getDescription());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitSince(SinceTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getBody());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitVersion(VersionTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getBody());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitParam(ParamTree node, Void p) {
        String name = node.getTagName();
        String content = node.getName().toString() + " " + CommentHelper.getText(node.getDescription());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getDescription());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitSee(SeeTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getReference());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitThrows(ThrowsTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getDescription());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }

    @Override
    public Void visitUses(UsesTree node, Void p) {
        String name = node.getTagName();
        String content = CommentHelper.getText(node.getDescription());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }


    @Override
    public Void visitUnknownBlockTag(UnknownBlockTagTree tree, Void p) {
        String name = tree.getTagName();
        String content = CommentHelper.getText(tree.getContent());
        tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
        return null;
    }
}