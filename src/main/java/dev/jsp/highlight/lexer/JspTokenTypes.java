package dev.jsp.highlight.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import dev.jsp.highlight.JspLanguage;

/**
 * Token types produced by {@link JspLexer}.
 *
 * <p>Four families:
 * <ul>
 *   <li>{@code HTML_*} — template text and markup, which JSP shares with HTML.</li>
 *   <li>{@code JSP_*} — JSP delimiters, directives and comments.</li>
 *   <li>{@code JAVA_*} — Java code inside scriptlets, expressions and declarations.</li>
 *   <li>{@code EL_*} — Expression Language inside {@code ${...}} and {@code #{...}}.</li>
 * </ul>
 */
public final class JspTokenTypes {

    private JspTokenTypes() {
    }

    private static IElementType token(String name) {
        return new IElementType(name, JspLanguage.INSTANCE);
    }

    // ---------------------------------------------------------------- HTML

    public static final IElementType HTML_TEXT = token("HTML_TEXT");
    public static final IElementType HTML_TAG_START = token("HTML_TAG_START");
    public static final IElementType HTML_TAG_END = token("HTML_TAG_END");
    public static final IElementType HTML_TAG_NAME = token("HTML_TAG_NAME");
    public static final IElementType HTML_ATTRIBUTE_NAME = token("HTML_ATTRIBUTE_NAME");
    public static final IElementType HTML_ATTRIBUTE_ASSIGN = token("HTML_ATTRIBUTE_ASSIGN");
    public static final IElementType HTML_ATTRIBUTE_VALUE = token("HTML_ATTRIBUTE_VALUE");
    public static final IElementType HTML_COMMENT = token("HTML_COMMENT");
    public static final IElementType HTML_DECLARATION = token("HTML_DECLARATION");
    public static final IElementType HTML_ENTITY = token("HTML_ENTITY");

    // ----------------------------------------------------------------- JSP

    public static final IElementType JSP_SCRIPTLET_START = token("JSP_SCRIPTLET_START");
    public static final IElementType JSP_EXPRESSION_START = token("JSP_EXPRESSION_START");
    public static final IElementType JSP_DECLARATION_START = token("JSP_DECLARATION_START");
    public static final IElementType JSP_DIRECTIVE_START = token("JSP_DIRECTIVE_START");
    public static final IElementType JSP_END = token("JSP_END");
    public static final IElementType JSP_COMMENT = token("JSP_COMMENT");
    public static final IElementType JSP_DIRECTIVE_NAME = token("JSP_DIRECTIVE_NAME");
    public static final IElementType JSP_DIRECTIVE_ATTRIBUTE = token("JSP_DIRECTIVE_ATTRIBUTE");
    public static final IElementType JSP_DIRECTIVE_VALUE = token("JSP_DIRECTIVE_VALUE");
    public static final IElementType JSP_DIRECTIVE_ASSIGN = token("JSP_DIRECTIVE_ASSIGN");

    // ---------------------------------------------------------------- Java

    public static final IElementType JAVA_KEYWORD = token("JAVA_KEYWORD");
    public static final IElementType JAVA_IDENTIFIER = token("JAVA_IDENTIFIER");
    public static final IElementType JAVA_NUMBER = token("JAVA_NUMBER");
    public static final IElementType JAVA_STRING = token("JAVA_STRING");
    public static final IElementType JAVA_CHAR = token("JAVA_CHAR");
    public static final IElementType JAVA_LINE_COMMENT = token("JAVA_LINE_COMMENT");
    public static final IElementType JAVA_BLOCK_COMMENT = token("JAVA_BLOCK_COMMENT");
    public static final IElementType JAVA_OPERATOR = token("JAVA_OPERATOR");
    public static final IElementType JAVA_PUNCTUATION = token("JAVA_PUNCTUATION");

    // ------------------------------------------------------------------ EL

    public static final IElementType EL_START = token("EL_START");
    public static final IElementType EL_END = token("EL_END");
    public static final IElementType EL_KEYWORD = token("EL_KEYWORD");
    public static final IElementType EL_IDENTIFIER = token("EL_IDENTIFIER");
    public static final IElementType EL_NUMBER = token("EL_NUMBER");
    public static final IElementType EL_STRING = token("EL_STRING");
    public static final IElementType EL_OPERATOR = token("EL_OPERATOR");
    public static final IElementType EL_PUNCTUATION = token("EL_PUNCTUATION");

    // ------------------------------------------------------------ JavaScript

    public static final IElementType JS_KEYWORD = token("JS_KEYWORD");
    public static final IElementType JS_IDENTIFIER = token("JS_IDENTIFIER");
    public static final IElementType JS_NUMBER = token("JS_NUMBER");
    public static final IElementType JS_STRING = token("JS_STRING");
    public static final IElementType JS_TEMPLATE = token("JS_TEMPLATE");
    public static final IElementType JS_LINE_COMMENT = token("JS_LINE_COMMENT");
    public static final IElementType JS_BLOCK_COMMENT = token("JS_BLOCK_COMMENT");
    public static final IElementType JS_OPERATOR = token("JS_OPERATOR");
    public static final IElementType JS_PUNCTUATION = token("JS_PUNCTUATION");

    // -------------------------------------------------------------- groups

    public static final TokenSet COMMENTS = TokenSet.create(
            HTML_COMMENT, JSP_COMMENT, JAVA_LINE_COMMENT, JAVA_BLOCK_COMMENT,
            JS_LINE_COMMENT, JS_BLOCK_COMMENT);

    public static final TokenSet STRINGS = TokenSet.create(
            JAVA_STRING, JAVA_CHAR, EL_STRING, HTML_ATTRIBUTE_VALUE, JSP_DIRECTIVE_VALUE,
            JS_STRING, JS_TEMPLATE);
}
