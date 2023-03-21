package org.umlgraph.doclet;

/**
 * Class to represent a font for graphviz.
 * <p>
 * This is a fairly complicated model, because it is rather an API into graphviz
 * formatting strings rather than a standalone thing. <p Some fonts (edge, node,
 * abstract) are set on the top level elements, whereas others (class name, tag,
 * package) are inserted as {@code <font>} tags, and these can be omitted if not
 * set. Inheritance of properties then happens in graphviz.
 *
 * @author Erich Schubert
 */
public enum Font {
    /** Edge label font */
    EDGE, //
    /** Node basic */
    NODE, //
    /** Normal operation font */
    NORMAL, //
    /** Abstract operation font */
    ABSTRACT, //
    /** Class name. Inherits from null. */
    CLASS, //
    /** Class name of abstract classes (usually italic) */
    CLASS_ABSTRACT, //
    /** Package names Inherits from null. */
    PACKAGE, //
    /** Tags Inherits from null. */
    TAG;

    public static final String DEFAULT_FONT;

    // Static initialization of further values.
    static {
        // use an appropriate font depending on the current operating system
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            DEFAULT_FONT = "Arial";
        } else {
            DEFAULT_FONT = "Helvetica"; // TODO: can we use just "sans"?
        }
    }

    /**
     * Wraps the text with the appropriate font according to the specified font type
     *
     * @param opt  Options
     * @param text Text to wrap
     * @return Wrapped text
     */
    public String wrap(Options opt, String text) {
        if (text.isEmpty() || this == NORMAL)
            return text;
        String face = null;
        double size = -1;
        boolean italic = false;
        switch (this) {
        case EDGE:
        case NODE:
            // Not used with the wrap function.
            throw new UnsupportedOperationException();
        case ABSTRACT:
            italic = opt.nodeFontAbstractItalic;
        case NORMAL:
            break;
        case CLASS_ABSTRACT:
            italic = opt.nodeFontAbstractItalic;
        case CLASS:
            face = opt.nodeFontClassName;
            size = opt.nodeFontClassSize;
            break;
        case PACKAGE:
            face = opt.nodeFontPackageName;
            size = opt.nodeFontPackageSize;
            break;
        case TAG:
            face = opt.nodeFontTagName;
            size = opt.nodeFontTagSize;
            break;
        }
        if (face == null && size < 0 && !italic)
            return text;
        StringBuilder buf = new StringBuilder(text.length() + 100);
        if (face != null || size > 0) {
            buf.append("<font");
            if (face != null)
                buf.append(" face=\"").append(face).append('"');
            if (size > 0)
                buf.append(" point-size=\"").append(size).append('"');
            buf.append('>');
        }
        if (italic)
            buf.append("<i>");
        buf.append(text);
        if (italic)
            buf.append("</i>");
        if (face != null || size > 0)
            buf.append("</font>");
        return buf.toString();
    }
}
