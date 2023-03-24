package org.umlgraph.doclet.util;

import static com.sun.source.doctree.DocTree.Kind.CODE;

import java.util.List;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.AttributeTree.ValueKind;
import com.sun.source.util.SimpleDocTreeVisitor;

public class CommentHelper {
    public static final String SPACER = " ";
    
    public static String getText(List<? extends DocTree> list) {
        StringBuilder sb = new StringBuilder();
        for (DocTree dt : list) {
            sb.append(getText0(dt));
        }
        return sb.toString();
    }

    public static String getText(DocTree dt) {
        return getText0(dt).toString();
    }

    private static StringBuilder getText0(DocTree dt) {
        final StringBuilder sb = new StringBuilder();
        new SimpleDocTreeVisitor<Void, Void>() {
            @Override
            public Void visitAttribute(AttributeTree node, Void p) {
                sb.append(SPACER).append(node.getName());
                if (node.getValueKind() == ValueKind.EMPTY) {
                    return null;
                }

                sb.append("=");
                String quote;
                switch (node.getValueKind()) {
                    case DOUBLE:
                        quote = "\"";
                        break;
                    case SINGLE:
                        quote = "\'";
                        break;
                    default:
                        quote = "";
                        break;
                }
                sb.append(quote);
                node.getValue().stream().forEach((dt) -> {
                    dt.accept(this, null);
                });
                sb.append(quote);
                return null;
            }

            @Override
            public Void visitEndElement(EndElementTree node, Void p) {
                sb.append("</")
                        .append(node.getName())
                        .append(">");
                return null;
            }

            @Override
            public Void visitEntity(EntityTree node, Void p) {
                sb.append(node.toString());
                return null;
            }

            @Override
            public Void visitLink(LinkTree node, Void p) {
                if (node.getReference() == null) {
                    return null;
                }

                node.getReference().accept(this, null);
                node.getLabel().stream().forEach((dt) -> {
                    dt.accept(this, null);
                });
                return null;
            }

            @Override
            public Void visitLiteral(LiteralTree node, Void p) {
                if (node.getKind() == CODE) {
                    sb.append("<").append(node.getKind().tagName).append(">");
                }
                sb.append(node.getBody().toString());
                if (node.getKind() == CODE) {
                    sb.append("</").append(node.getKind().tagName).append(">");
                }
                return null;
            }

            @Override
            public Void visitReference(ReferenceTree node, Void p) {
                sb.append(node.getSignature());
                return null;
            }

            @Override
            public Void visitSee(SeeTree node, Void p) {
                node.getReference().stream().forEach((dt) -> {
                    dt.accept(this, null);
                });
                return null;
            }

            @Override
            public Void visitSerial(SerialTree node, Void p) {
                node.getDescription().stream().forEach((dt) -> {
                    dt.accept(this, null);
                });
                return null;
            }

            @Override
            public Void visitStartElement(StartElementTree node, Void p) {
                sb.append("<");
                sb.append(node.getName());
                node.getAttributes().stream().forEach((dt) -> {
                    dt.accept(this, null);
                });
                sb.append((node.isSelfClosing() ? "/>" : ">"));
                return null;
            }

            @Override
            public Void visitText(TextTree node, Void p) {
                sb.append(node.getBody());
                return null;
            }

            @Override
            public Void visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
                node.getContent().stream().forEach((dt) -> {
                    dt.accept(this, null);
                });
                return null;
            }

            @Override
            public Void visitValue(ValueTree node, Void p) {
                return node.getReference().accept(this, null);
            }

            @Override
            protected Void defaultAction(DocTree node, Void p) {
                sb.append(node.toString());
                return null;
            }
        }.visit(dt, null);
        return sb;
    }
}
