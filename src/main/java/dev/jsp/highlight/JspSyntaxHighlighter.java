package dev.jsp.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import dev.jsp.highlight.lexer.JspLexer;
import dev.jsp.highlight.lexer.JspTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Maps every token produced by {@link JspLexer} to a text attribute.
 *
 * <p>Each key has an explicit fallback to a platform key, so the plugin inherits the user's colour
 * scheme (including accessibility schemes and dark themes) out of the box. The dedicated keys only
 * exist so that JSP constructs can be tuned independently when the user wants to.
 */
public final class JspSyntaxHighlighter extends SyntaxHighlighterBase {

    // ------------------------------------------------------------------- JSP

    public static final TextAttributesKey JSP_DELIMITER = createTextAttributesKey(
            "JSP_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey JSP_COMMENT = createTextAttributesKey(
            "JSP_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey JSP_DIRECTIVE_NAME = createTextAttributesKey(
            "JSP_DIRECTIVE_NAME", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey JSP_DIRECTIVE_ATTRIBUTE = createTextAttributesKey(
            "JSP_DIRECTIVE_ATTRIBUTE", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey JSP_DIRECTIVE_VALUE = createTextAttributesKey(
            "JSP_DIRECTIVE_VALUE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey JSP_DIRECTIVE_ASSIGN = createTextAttributesKey(
            "JSP_DIRECTIVE_ASSIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    // ------------------------------------------------------------------ Java

    public static final TextAttributesKey JAVA_KEYWORD = createTextAttributesKey(
            "JSP_JAVA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey JAVA_IDENTIFIER = createTextAttributesKey(
            "JSP_JAVA_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey JAVA_NUMBER = createTextAttributesKey(
            "JSP_JAVA_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey JAVA_STRING = createTextAttributesKey(
            "JSP_JAVA_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey JAVA_LINE_COMMENT = createTextAttributesKey(
            "JSP_JAVA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey JAVA_BLOCK_COMMENT = createTextAttributesKey(
            "JSP_JAVA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey JAVA_OPERATOR = createTextAttributesKey(
            "JSP_JAVA_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey JAVA_PUNCTUATION = createTextAttributesKey(
            "JSP_JAVA_PUNCTUATION", DefaultLanguageHighlighterColors.BRACES);

    // -------------------------------------------------------------------- EL

    public static final TextAttributesKey EL_DELIMITER = createTextAttributesKey(
            "JSP_EL_DELIMITER", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey EL_IDENTIFIER = createTextAttributesKey(
            "JSP_EL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey EL_KEYWORD = createTextAttributesKey(
            "JSP_EL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey EL_NUMBER = createTextAttributesKey(
            "JSP_EL_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey EL_STRING = createTextAttributesKey(
            "JSP_EL_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey EL_OPERATOR = createTextAttributesKey(
            "JSP_EL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey EL_PUNCTUATION = createTextAttributesKey(
            "JSP_EL_PUNCTUATION", DefaultLanguageHighlighterColors.BRACES);

    // ------------------------------------------------------------ JavaScript

    public static final TextAttributesKey JS_KEYWORD = createTextAttributesKey(
            "JSP_JS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey JS_IDENTIFIER = createTextAttributesKey(
            "JSP_JS_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey JS_NUMBER = createTextAttributesKey(
            "JSP_JS_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey JS_STRING = createTextAttributesKey(
            "JSP_JS_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey JS_TEMPLATE = createTextAttributesKey(
            "JSP_JS_TEMPLATE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey JS_LINE_COMMENT = createTextAttributesKey(
            "JSP_JS_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey JS_BLOCK_COMMENT = createTextAttributesKey(
            "JSP_JS_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey JS_OPERATOR = createTextAttributesKey(
            "JSP_JS_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey JS_PUNCTUATION = createTextAttributesKey(
            "JSP_JS_PUNCTUATION", DefaultLanguageHighlighterColors.BRACES);

    // ------------------------------------------------------------------ HTML

    // Markup deliberately maps to core keys, not to DefaultLanguageHighlighterColors.MARKUP_*:
    // those MARKUP keys are only defined by JetBrains' paid HTML/JavaEE plugins, so in an
    // installation without them every <tag> would render as plain text. The keys below are
    // defined by the platform's own default schemes, so markup is always visible.
    public static final TextAttributesKey HTML_TEXT_KEY = createTextAttributesKey(
            "JSP_HTML_TEXT", HighlighterColors.TEXT);
    public static final TextAttributesKey HTML_TAG_NAME_KEY = createTextAttributesKey(
            "JSP_HTML_TAG_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey HTML_TAG_DELIMITER = createTextAttributesKey(
            "JSP_HTML_TAG_DELIMITER", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey HTML_ATTRIBUTE_NAME_KEY = createTextAttributesKey(
            "JSP_HTML_ATTRIBUTE_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey HTML_ATTRIBUTE_VALUE_KEY = createTextAttributesKey(
            "JSP_HTML_ATTRIBUTE_VALUE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey HTML_ATTRIBUTE_ASSIGN_KEY = createTextAttributesKey(
            "JSP_HTML_ATTRIBUTE_ASSIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey HTML_COMMENT_KEY = createTextAttributesKey(
            "JSP_HTML_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey HTML_DECLARATION_KEY = createTextAttributesKey(
            "JSP_HTML_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey HTML_ENTITY_KEY = createTextAttributesKey(
            "JSP_HTML_ENTITY", DefaultLanguageHighlighterColors.NUMBER);

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        // JSP delimiters
        ATTRIBUTES.put(JspTokenTypes.JSP_SCRIPTLET_START, JSP_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.JSP_EXPRESSION_START, JSP_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.JSP_DECLARATION_START, JSP_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.JSP_DIRECTIVE_START, JSP_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.JSP_END, JSP_DELIMITER);

        // JSP comments and directives
        ATTRIBUTES.put(JspTokenTypes.JSP_COMMENT, JSP_COMMENT);
        ATTRIBUTES.put(JspTokenTypes.JSP_DIRECTIVE_NAME, JSP_DIRECTIVE_NAME);
        ATTRIBUTES.put(JspTokenTypes.JSP_DIRECTIVE_ATTRIBUTE, JSP_DIRECTIVE_ATTRIBUTE);
        ATTRIBUTES.put(JspTokenTypes.JSP_DIRECTIVE_VALUE, JSP_DIRECTIVE_VALUE);
        ATTRIBUTES.put(JspTokenTypes.JSP_DIRECTIVE_ASSIGN, JSP_DIRECTIVE_ASSIGN);

        // Embedded Java
        ATTRIBUTES.put(JspTokenTypes.JAVA_KEYWORD, JAVA_KEYWORD);
        ATTRIBUTES.put(JspTokenTypes.JAVA_IDENTIFIER, JAVA_IDENTIFIER);
        ATTRIBUTES.put(JspTokenTypes.JAVA_NUMBER, JAVA_NUMBER);
        ATTRIBUTES.put(JspTokenTypes.JAVA_STRING, JAVA_STRING);
        ATTRIBUTES.put(JspTokenTypes.JAVA_CHAR, JAVA_STRING);
        ATTRIBUTES.put(JspTokenTypes.JAVA_LINE_COMMENT, JAVA_LINE_COMMENT);
        ATTRIBUTES.put(JspTokenTypes.JAVA_BLOCK_COMMENT, JAVA_BLOCK_COMMENT);
        ATTRIBUTES.put(JspTokenTypes.JAVA_OPERATOR, JAVA_OPERATOR);
        ATTRIBUTES.put(JspTokenTypes.JAVA_PUNCTUATION, JAVA_PUNCTUATION);

        // Expression Language
        ATTRIBUTES.put(JspTokenTypes.EL_START, EL_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.EL_END, EL_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.EL_IDENTIFIER, EL_IDENTIFIER);
        ATTRIBUTES.put(JspTokenTypes.EL_KEYWORD, EL_KEYWORD);
        ATTRIBUTES.put(JspTokenTypes.EL_NUMBER, EL_NUMBER);
        ATTRIBUTES.put(JspTokenTypes.EL_STRING, EL_STRING);
        ATTRIBUTES.put(JspTokenTypes.EL_OPERATOR, EL_OPERATOR);
        ATTRIBUTES.put(JspTokenTypes.EL_PUNCTUATION, EL_PUNCTUATION);

        // JavaScript inside <script>
        ATTRIBUTES.put(JspTokenTypes.JS_KEYWORD, JS_KEYWORD);
        ATTRIBUTES.put(JspTokenTypes.JS_IDENTIFIER, JS_IDENTIFIER);
        ATTRIBUTES.put(JspTokenTypes.JS_NUMBER, JS_NUMBER);
        ATTRIBUTES.put(JspTokenTypes.JS_STRING, JS_STRING);
        ATTRIBUTES.put(JspTokenTypes.JS_TEMPLATE, JS_TEMPLATE);
        ATTRIBUTES.put(JspTokenTypes.JS_LINE_COMMENT, JS_LINE_COMMENT);
        ATTRIBUTES.put(JspTokenTypes.JS_BLOCK_COMMENT, JS_BLOCK_COMMENT);
        ATTRIBUTES.put(JspTokenTypes.JS_OPERATOR, JS_OPERATOR);
        ATTRIBUTES.put(JspTokenTypes.JS_PUNCTUATION, JS_PUNCTUATION);

        // Markup
        ATTRIBUTES.put(JspTokenTypes.HTML_TEXT, HTML_TEXT_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_TAG_START, HTML_TAG_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.HTML_TAG_END, HTML_TAG_DELIMITER);
        ATTRIBUTES.put(JspTokenTypes.HTML_TAG_NAME, HTML_TAG_NAME_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_ATTRIBUTE_NAME, HTML_ATTRIBUTE_NAME_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_ATTRIBUTE_ASSIGN, HTML_ATTRIBUTE_ASSIGN_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_ATTRIBUTE_VALUE, HTML_ATTRIBUTE_VALUE_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_COMMENT, HTML_COMMENT_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_DECLARATION, HTML_DECLARATION_KEY);
        ATTRIBUTES.put(JspTokenTypes.HTML_ENTITY, HTML_ENTITY_KEY);
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new JspLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}
