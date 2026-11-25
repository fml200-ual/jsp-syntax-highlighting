package dev.jsp.highlight.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static dev.jsp.highlight.lexer.JspTokenTypes.*;

/**
 * Hand-written lexer for JavaServer Pages.
 *
 * <p>JSP is a host language: template text and HTML surround embedded Java and Expression
 * Language. This lexer is therefore a small state machine rather than a single flat rule set.
 *
 * <p>States, and what each one starts inside of:
 * <pre>
 *   S_HTML              markup / template text
 *   S_JSP_OPEN          at "&lt;%" — decides comment, directive, expression, declaration, scriptlet
 *   S_TAG_NAME          after "&lt;" or "&lt;/", before the element name
 *   S_TAG_BODY          inside a tag, among attributes
 *   S_TAG_VALUE_START   right after "=", looking for the value
 *   S_TAG_VALUE_*       inside a quoted or unquoted attribute value (may host EL / scriptlets)
 *   S_JAVA              inside "&lt;% %&gt;", "&lt;%= %&gt;", "&lt;%! %&gt;"
 *   S_DIRECTIVE_NAME    inside "&lt;%@ %&gt;", before the directive name
 *   S_DIRECTIVE_BODY    inside "&lt;%@ %&gt;", among the directive attributes
 *   S_EL_OPEN           at "${" or "#{"
 *   S_EL                inside ${...} / #{...}
 * </pre>
 *
 * <p>Deliberate limits of this version: no tag-library resolution, no Java type analysis, and no
 * nested-quote handling inside EL strings. The output is a flat token stream, which is all a
 * syntax highlighter needs.
 */
public final class JspLexer extends LexerBase {

    // ------------------------------------------------------------------ states

    private static final int S_HTML = 0;
    private static final int S_JSP_OPEN = 1;
    private static final int S_TAG_NAME = 2;
    private static final int S_TAG_BODY = 3;
    private static final int S_TAG_VALUE_START = 4;
    private static final int S_TAG_VALUE_DQ = 5;
    private static final int S_TAG_VALUE_DQ_CONT = 6;
    private static final int S_TAG_VALUE_SQ = 7;
    private static final int S_TAG_VALUE_SQ_CONT = 8;
    private static final int S_TAG_VALUE_UNQUOTED = 9;
    private static final int S_JAVA = 10;
    private static final int S_DIRECTIVE_NAME = 11;
    private static final int S_DIRECTIVE_BODY = 12;
    private static final int S_EL_OPEN = 13;
    private static final int S_EL = 14;
    private static final int S_JS = 15;

    /** States fit in 4 bits, so the return addresses and EL nesting travel with {@link #getState()}. */
    private static final int STATE_BITS = 4;
    private static final int STATE_MASK = (1 << STATE_BITS) - 1;

    /** Set while an opened {@code <script>} tag has not reached its closing {@code >} yet. */
    private static final int PENDING_SCRIPT_BIT = 20;
    /** Set between an opening {@code <} / closing {@code </} and the tag name that follows. */
    private static final int CLOSING_TAG_BIT = 21;

    // ---------------------------------------------------------------- keywords

    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "var", "void", "volatile", "while", "yield",
            "record", "sealed", "permits", "true", "false", "null");

    private static final Set<String> EL_KEYWORDS = Set.of(
            "and", "or", "not", "eq", "ne", "lt", "gt", "le", "ge", "true", "false", "null",
            "empty", "div", "mod", "instanceof");

    private static final String EL_OPERATOR_CHARS = "+-*/%=<>!&|^?:";
    private static final String EL_PUNCTUATION_CHARS = "()[].,";
    private static final String JS_OPERATOR_CHARS = "+-*/%=<>!&|^~?:";

    private static final Set<String> JS_KEYWORDS = Set.of(
            "async", "await", "break", "case", "catch", "class", "const", "continue", "debugger",
            "default", "delete", "do", "else", "export", "extends", "false", "finally", "for",
            "from", "function", "get", "if", "import", "in", "instanceof", "let", "new", "null",
            "of", "return", "set", "static", "super", "switch", "this", "throw", "true", "try",
            "typeof", "undefined", "var", "void", "while", "with", "yield");

    // ------------------------------------------------------------------ fields

    private CharSequence myBuffer = "";
    private int myBufferEnd;
    private int myTokenStart;
    private int myTokenEnd;
    private IElementType myTokenType;

    private int myState = S_HTML;
    /** Where to continue after "%&gt;" ends an embedded JSP construct. */
    private int myJspReturnState = S_HTML;
    /** Where to continue after the closing "}" of an EL expression. */
    private int myElReturnState = S_HTML;
    private int myElDepth;
    private boolean myScriptPending;
    private boolean myClosingTag;

    // ----------------------------------------------------------------- lexer API

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer = buffer;
        myBufferEnd = endOffset;
        myTokenStart = startOffset;
        myTokenEnd = startOffset;
        myTokenType = null;

        myState = initialState & STATE_MASK;
        myJspReturnState = (initialState >> STATE_BITS) & STATE_MASK;
        myElReturnState = (initialState >> (2 * STATE_BITS)) & STATE_MASK;
        myElDepth = Math.max(0, (initialState >> (3 * STATE_BITS)) & 0xFF);
        myScriptPending = (initialState & (1 << PENDING_SCRIPT_BIT)) != 0;
        myClosingTag = (initialState & (1 << CLOSING_TAG_BIT)) != 0;

        // Lexer contract: after start(), getTokenType() must already expose the first token.
        advance();
    }

    @Override
    public int getState() {
        return myState
                | (myJspReturnState << STATE_BITS)
                | (myElReturnState << (2 * STATE_BITS))
                | (Math.min(myElDepth, 0xFF) << (3 * STATE_BITS))
                | (myScriptPending ? 1 << PENDING_SCRIPT_BIT : 0)
                | (myClosingTag ? 1 << CLOSING_TAG_BIT : 0);
    }

    @Override
    public IElementType getTokenType() {
        return myTokenType;
    }

    @Override
    public int getTokenStart() {
        return myTokenStart;
    }

    @Override
    public int getTokenEnd() {
        return myTokenEnd;
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return myBuffer;
    }

    @Override
    public int getBufferEnd() {
        return myBufferEnd;
    }

    @Override
    public void advance() {
        myTokenStart = myTokenEnd;
        myTokenType = null;

        // Some states only change state (for example S_HTML on "<%"), so re-dispatch until a token
        // is produced. The guard turns an inconsistent state machine into visible bad characters
        // instead of an endless loop.
        for (int guard = 0; myTokenType == null; guard++) {
            if (myTokenStart >= myBufferEnd) {
                myTokenEnd = myBufferEnd;
                return;
            }
            if (guard > 8) {
                myTokenEnd = myTokenStart + 1;
                myTokenType = TokenType.BAD_CHARACTER;
                return;
            }

            int stateBefore = myState;
            dispatch();

            if (myTokenType == null && myState == stateBefore) {
                myTokenEnd = myTokenStart + 1;
                myTokenType = TokenType.BAD_CHARACTER;
            }
        }

        if (myTokenEnd <= myTokenStart) {
            myTokenEnd = myTokenStart + 1;
        }
    }

    private void dispatch() {
        switch (myState) {
            case S_HTML -> lexHtml();
            case S_JSP_OPEN -> lexJspOpen();
            case S_TAG_NAME -> lexTagName();
            case S_TAG_BODY -> lexTagBody();
            case S_TAG_VALUE_START -> lexTagValueStart();
            case S_TAG_VALUE_DQ -> scanTagValue('"', myTokenStart + 1, S_TAG_VALUE_DQ_CONT);
            case S_TAG_VALUE_DQ_CONT -> scanTagValue('"', myTokenStart, S_TAG_VALUE_DQ_CONT);
            case S_TAG_VALUE_SQ -> scanTagValue('\'', myTokenStart + 1, S_TAG_VALUE_SQ_CONT);
            case S_TAG_VALUE_SQ_CONT -> scanTagValue('\'', myTokenStart, S_TAG_VALUE_SQ_CONT);
            case S_TAG_VALUE_UNQUOTED -> lexTagValueUnquoted();
            case S_JAVA -> lexJava();
            case S_DIRECTIVE_NAME -> lexDirective(true);
            case S_DIRECTIVE_BODY -> lexDirective(false);
            case S_EL_OPEN -> lexElOpen();
            case S_EL -> lexEl();
            case S_JS -> lexJs();
            default -> {
                myTokenEnd = myTokenStart + 1;
                myTokenType = TokenType.BAD_CHARACTER;
                myState = S_HTML;
            }
        }
    }

    // ------------------------------------------------------------------ markup

    private void lexHtml() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }

        if (c == '<') {
            if (startsWith(i, "<%")) {
                myJspReturnState = S_HTML;
                myState = S_JSP_OPEN;
                return;
            }
            if (startsWith(i, "<!--")) {
                myTokenEnd = indexOrEnd(i, "-->", 4);
                myTokenType = HTML_COMMENT;
                return;
            }
            if (startsWith(i, "<!")) {
                myTokenEnd = indexOrEnd(i, ">", 2);
                myTokenType = HTML_DECLARATION;
                return;
            }
            if (startsWith(i, "</")) {
                if (i + 2 < myBufferEnd && Character.isLetter(at(i + 2))) {
                    myTokenEnd = i + 2;
                    myTokenType = HTML_TAG_START;
                    myState = S_TAG_NAME;
                    myClosingTag = true;
                    return;
                }
            } else if (i + 1 < myBufferEnd && Character.isLetter(at(i + 1))) {
                myTokenEnd = i + 1;
                myTokenType = HTML_TAG_START;
                myState = S_TAG_NAME;
                myClosingTag = false;
                return;
            }
        }

        if (c == '&' && isEntityStart(i)) {
            myTokenEnd = entityEnd(i);
            myTokenType = HTML_ENTITY;
            return;
        }

        if (elStartAt(i)) {
            myElReturnState = S_HTML;
            myState = S_EL_OPEN;
            return;
        }

        int j = i;
        while (j < myBufferEnd) {
            char ch = at(j);
            if (ch == '<' || ch == '&' || elStartAt(j)) {
                break;
            }
            j++;
        }
        if (j == i) {
            j = i + 1;
        }
        myTokenEnd = j;
        myTokenType = HTML_TEXT;
    }

    private void lexJspOpen() {
        int i = myTokenStart;

        if (startsWith(i, "<%--")) {
            myTokenEnd = indexOrEnd(i, "--%>", 4);
            myTokenType = JSP_COMMENT;
            myState = myJspReturnState;
            return;
        }
        if (startsWith(i, "<%@")) {
            myTokenEnd = i + 3;
            myTokenType = JSP_DIRECTIVE_START;
            myState = S_DIRECTIVE_NAME;
            return;
        }
        if (startsWith(i, "<%=")) {
            myTokenEnd = i + 3;
            myTokenType = JSP_EXPRESSION_START;
            myState = S_JAVA;
            return;
        }
        if (startsWith(i, "<%!")) {
            myTokenEnd = i + 3;
            myTokenType = JSP_DECLARATION_START;
            myState = S_JAVA;
            return;
        }
        if (startsWith(i, "<%")) {
            myTokenEnd = i + 2;
            myTokenType = JSP_SCRIPTLET_START;
            myState = S_JAVA;
            return;
        }

        // Reached only on malformed input; keep it visible but harmless.
        myTokenEnd = i + 1;
        myTokenType = HTML_TEXT;
        myState = myJspReturnState;
    }

    private void lexTagName() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '>') {
            myTokenEnd = i + 1;
            myTokenType = HTML_TAG_END;
            endTag();
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = HTML_TAG_END;
            endTag();
            return;
        }
        if (isNameChar(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isNameChar(at(j))) {
                j++;
            }
            if (!myClosingTag && myBuffer.subSequence(i, j).toString().equalsIgnoreCase("script")) {
                myScriptPending = true;
            }
            myClosingTag = false;
            myTokenEnd = j;
            myTokenType = HTML_TAG_NAME;
            myState = S_TAG_BODY;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = HTML_TEXT;
        myState = S_TAG_BODY;
    }

    private void lexTagBody() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '>') {
            myTokenEnd = i + 1;
            myTokenType = HTML_TAG_END;
            endTag();
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = HTML_TAG_END;
            endTag();
            return;
        }
        if (c == '=') {
            myTokenEnd = i + 1;
            myTokenType = HTML_ATTRIBUTE_ASSIGN;
            myState = S_TAG_VALUE_START;
            return;
        }
        if (c == '"') {
            myState = S_TAG_VALUE_DQ;
            scanTagValue('"', i + 1, S_TAG_VALUE_DQ_CONT);
            return;
        }
        if (c == '\'') {
            myState = S_TAG_VALUE_SQ;
            scanTagValue('\'', i + 1, S_TAG_VALUE_SQ_CONT);
            return;
        }
        if (startsWith(i, "<%")) {
            myJspReturnState = S_TAG_BODY;
            myState = S_JSP_OPEN;
            return;
        }
        if (elStartAt(i)) {
            myElReturnState = S_TAG_BODY;
            myState = S_EL_OPEN;
            return;
        }
        if (isAttributeNameChar(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isAttributeNameChar(at(j))) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = HTML_ATTRIBUTE_NAME;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = HTML_TEXT;
    }

    private void lexTagValueStart() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '"') {
            myState = S_TAG_VALUE_DQ;
            scanTagValue('"', i + 1, S_TAG_VALUE_DQ_CONT);
            return;
        }
        if (c == '\'') {
            myState = S_TAG_VALUE_SQ;
            scanTagValue('\'', i + 1, S_TAG_VALUE_SQ_CONT);
            return;
        }
        if (c == '>') {
            myTokenEnd = i + 1;
            myTokenType = HTML_TAG_END;
            endTag();
            return;
        }

        myState = S_TAG_VALUE_UNQUOTED;
    }

    /**
     * Scans an attribute value. Within it, EL expressions and JSP scriptlets are handed over to
     * their own states, and control returns to {@code continuationState} afterwards.
     */
    private void scanTagValue(char quote, int from, int continuationState) {
        int i = from;

        while (i < myBufferEnd) {
            char c = at(i);

            if (c == quote) {
                myTokenEnd = i + 1;
                myTokenType = HTML_ATTRIBUTE_VALUE;
                myState = S_TAG_BODY;
                return;
            }
            if (elStartAt(i)) {
                if (i > myTokenStart) {
                    myTokenEnd = i;
                    myTokenType = HTML_ATTRIBUTE_VALUE;
                }
                myElReturnState = continuationState;
                myState = S_EL_OPEN;
                return;
            }
            if (c == '<' && startsWith(i, "<%")) {
                if (i > myTokenStart) {
                    myTokenEnd = i;
                    myTokenType = HTML_ATTRIBUTE_VALUE;
                }
                myJspReturnState = continuationState;
                myState = S_JSP_OPEN;
                return;
            }
            i++;
        }

        myTokenEnd = myBufferEnd;
        myTokenType = HTML_ATTRIBUTE_VALUE;
        myState = S_TAG_BODY;
    }

    private void lexTagValueUnquoted() {
        int i = myTokenStart;

        while (i < myBufferEnd) {
            char c = at(i);
            if (isWhitespace(c) || c == '>') {
                break;
            }
            if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
                break;
            }
            if (elStartAt(i) || (c == '<' && startsWith(i, "<%"))) {
                break;
            }
            i++;
        }

        if (i > myTokenStart) {
            myTokenEnd = i;
            myTokenType = HTML_ATTRIBUTE_VALUE;

            if (elStartAt(i)) {
                myElReturnState = S_TAG_BODY;
                myState = S_EL_OPEN;
                return;
            }
            if (i < myBufferEnd && at(i) == '<' && startsWith(i, "<%")) {
                myJspReturnState = S_TAG_BODY;
                myState = S_JSP_OPEN;
                return;
            }
            myState = S_TAG_BODY;
            return;
        }

        myState = S_TAG_BODY;
    }

    // -------------------------------------------------------------------- java

    private void lexJava() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '%' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = JSP_END;
            myState = myJspReturnState;
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '/') {
            myTokenEnd = lineCommentEnd(i);
            myTokenType = JAVA_LINE_COMMENT;
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '*') {
            myTokenEnd = indexOrEnd(i, "*/", 2);
            myTokenType = JAVA_BLOCK_COMMENT;
            return;
        }
        if (c == '"') {
            myTokenEnd = stringEnd(i, '"');
            myTokenType = JAVA_STRING;
            return;
        }
        if (c == '\'') {
            myTokenEnd = stringEnd(i, '\'');
            myTokenType = JAVA_CHAR;
            return;
        }
        if (Character.isDigit(c)) {
            myTokenEnd = numberEnd(i);
            myTokenType = JAVA_NUMBER;
            return;
        }
        if (isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isIdentifierPart(at(j))) {
                j++;
            }
            String word = myBuffer.subSequence(i, j).toString();
            myTokenEnd = j;
            myTokenType = JAVA_KEYWORDS.contains(word) ? JAVA_KEYWORD : JAVA_IDENTIFIER;
            return;
        }
        if (EL_OPERATOR_CHARS.indexOf(c) >= 0) {
            int j = i + 1;
            while (j < myBufferEnd && EL_OPERATOR_CHARS.indexOf(at(j)) >= 0) {
                if (at(j - 1) == '%' && at(j) == '>') {
                    break;
                }
                j++;
            }
            myTokenEnd = j;
            myTokenType = JAVA_OPERATOR;
            return;
        }
        if ("(){}[];,.".indexOf(c) >= 0) {
            myTokenEnd = i + 1;
            myTokenType = JAVA_PUNCTUATION;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = TokenType.BAD_CHARACTER;
    }

    // --------------------------------------------------------------- directive

    private void lexDirective(boolean nameExpected) {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '%' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = JSP_END;
            myState = myJspReturnState;
            return;
        }
        if (c == '=') {
            myTokenEnd = i + 1;
            myTokenType = JSP_DIRECTIVE_ASSIGN;
            myState = S_DIRECTIVE_BODY;
            return;
        }
        if (c == '"' || c == '\'') {
            myTokenEnd = stringEnd(i, c);
            myTokenType = JSP_DIRECTIVE_VALUE;
            myState = S_DIRECTIVE_BODY;
            return;
        }
        if (isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isIdentifierPart(at(j))) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = nameExpected ? JSP_DIRECTIVE_NAME : JSP_DIRECTIVE_ATTRIBUTE;
            myState = S_DIRECTIVE_BODY;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = TokenType.BAD_CHARACTER;
    }

    // ---------------------------------------------------------------------- el

    private void lexElOpen() {
        if (elStartAt(myTokenStart)) {
            myTokenEnd = myTokenStart + 2;
            myTokenType = EL_START;
            myElDepth = 1;
            myState = S_EL;
            return;
        }

        myTokenEnd = myTokenStart + 1;
        myTokenType = TokenType.BAD_CHARACTER;
        myState = S_EL;
    }

    private void lexEl() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '}') {
            myTokenEnd = i + 1;
            myTokenType = EL_END;
            myElDepth--;
            if (myElDepth <= 0) {
                myElDepth = 0;
                myState = myElReturnState;
            }
            return;
        }
        if (c == '"' || c == '\'') {
            myTokenEnd = stringEnd(i, c);
            myTokenType = EL_STRING;
            return;
        }
        if (Character.isDigit(c)) {
            myTokenEnd = numberEnd(i);
            myTokenType = EL_NUMBER;
            return;
        }
        if (isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isIdentifierPart(at(j))) {
                j++;
            }
            String word = myBuffer.subSequence(i, j).toString();
            myTokenEnd = j;
            myTokenType = EL_KEYWORDS.contains(word) ? EL_KEYWORD : EL_IDENTIFIER;
            return;
        }
        if (EL_OPERATOR_CHARS.indexOf(c) >= 0) {
            int j = i + 1;
            while (j < myBufferEnd && EL_OPERATOR_CHARS.indexOf(at(j)) >= 0) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = EL_OPERATOR;
            return;
        }
        if (EL_PUNCTUATION_CHARS.indexOf(c) >= 0) {
            myTokenEnd = i + 1;
            myTokenType = EL_PUNCTUATION;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = TokenType.BAD_CHARACTER;
    }

    // ------------------------------------------------------------ javascript

    private void lexJs() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (startsWithIgnoreCase(i, "</script")) {
            myTokenEnd = i + 2;
            myTokenType = HTML_TAG_START;
            myState = S_TAG_NAME;
            myScriptPending = false;
            myClosingTag = true;
            return;
        }
        if (elStartAt(i)) {
            myElReturnState = S_JS;
            myState = S_EL_OPEN;
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '/') {
            myTokenEnd = lineCommentEnd(i);
            myTokenType = JS_LINE_COMMENT;
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '*') {
            myTokenEnd = indexOrEnd(i, "*/", 2);
            myTokenType = JS_BLOCK_COMMENT;
            return;
        }
        if (c == '"' || c == '\'') {
            myTokenEnd = stringEnd(i, c);
            myTokenType = JS_STRING;
            return;
        }
        if (c == '`') {
            myTokenEnd = templateEnd(i);
            myTokenType = JS_TEMPLATE;
            return;
        }
        if (Character.isDigit(c) || (c == '.' && i + 1 < myBufferEnd && Character.isDigit(at(i + 1)))) {
            myTokenEnd = numberEnd(i);
            myTokenType = JS_NUMBER;
            return;
        }
        if (isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isIdentifierPart(at(j))) {
                j++;
            }
            String word = myBuffer.subSequence(i, j).toString();
            myTokenEnd = j;
            myTokenType = JS_KEYWORDS.contains(word) ? JS_KEYWORD : JS_IDENTIFIER;
            return;
        }
        if (JS_OPERATOR_CHARS.indexOf(c) >= 0) {
            int j = i + 1;
            while (j < myBufferEnd && JS_OPERATOR_CHARS.indexOf(at(j)) >= 0) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = JS_OPERATOR;
            return;
        }
        if ("(){}[];,.".indexOf(c) >= 0) {
            myTokenEnd = i + 1;
            myTokenType = JS_PUNCTUATION;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = TokenType.BAD_CHARACTER;
    }

    private int templateEnd(int start) {
        int i = start + 1;
        while (i < myBufferEnd) {
            char c = at(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == '`') {
                return i + 1;
            }
            i++;
        }
        return myBufferEnd;
    }

    private void endTag() {
        myState = myScriptPending ? S_JS : S_HTML;
        myScriptPending = false;
        myClosingTag = false;
    }

    // ---------------------------------------------------------------- helpers

    private char at(int index) {
        return myBuffer.charAt(index);
    }

    private boolean startsWith(int offset, String text) {
        if (offset + text.length() > myBufferEnd) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (myBuffer.charAt(offset + i) != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean startsWithIgnoreCase(int offset, String text) {
        if (offset + text.length() > myBufferEnd) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char actual = myBuffer.charAt(offset + i);
            char expected = text.charAt(i);
            if (actual != expected
                    && Character.toLowerCase(actual) != Character.toLowerCase(expected)) {
                return false;
            }
        }
        return true;
    }

    private boolean elStartAt(int index) {
        return index + 1 < myBufferEnd
                && (at(index) == '$' || at(index) == '#')
                && at(index + 1) == '{';
    }

    private int indexOrEnd(int from, String terminator, int searchFromOffset) {
        int length = terminator.length();
        for (int i = from + searchFromOffset; i + length <= myBufferEnd; i++) {
            boolean match = true;
            for (int k = 0; k < length; k++) {
                if (myBuffer.charAt(i + k) != terminator.charAt(k)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i + length;
            }
        }
        return myBufferEnd;
    }

    private int lineCommentEnd(int from) {
        int i = from;
        while (i < myBufferEnd && at(i) != '\n' && at(i) != '\r') {
            i++;
        }
        return i;
    }

    private int stringEnd(int start, char quote) {
        int i = start + 1;
        while (i < myBufferEnd) {
            char c = at(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            if (c == '\n' || c == '\r') {
                return i;
            }
            i++;
        }
        return myBufferEnd;
    }

    private int numberEnd(int start) {
        int i = start;

        if (at(i) == '0' && i + 1 < myBufferEnd && (at(i + 1) == 'x' || at(i + 1) == 'X')) {
            i += 2;
            while (i < myBufferEnd && isHexDigit(at(i))) {
                i++;
            }
            if (i < myBufferEnd && (at(i) == 'L' || at(i) == 'l')) {
                i++;
            }
            return i;
        }

        boolean seenDot = false;
        while (i < myBufferEnd) {
            char c = at(i);
            if (Character.isDigit(c)) {
                i++;
                continue;
            }
            if (c == '.' && !seenDot && i + 1 < myBufferEnd && Character.isDigit(at(i + 1))) {
                seenDot = true;
                i++;
                continue;
            }
            if ((c == 'e' || c == 'E') && i + 1 < myBufferEnd) {
                int k = i + 1;
                if (at(k) == '+' || at(k) == '-') {
                    k++;
                }
                if (k < myBufferEnd && Character.isDigit(at(k))) {
                    seenDot = true;
                    i = k;
                    continue;
                }
            }
            break;
        }
        if (i < myBufferEnd && "lLfFdD".indexOf(at(i)) >= 0) {
            i++;
        }
        return i;
    }

    private boolean isEntityStart(int index) {
        if (index + 1 >= myBufferEnd) {
            return false;
        }
        char next = at(index + 1);
        return Character.isLetter(next) || next == '#';
    }

    private int entityEnd(int start) {
        int limit = Math.min(start + 12, myBufferEnd);
        for (int i = start + 1; i < limit; i++) {
            char c = at(i);
            if (c == ';') {
                return i + 1;
            }
            if (!Character.isLetterOrDigit(c) && c != '#') {
                return start + 1;
            }
        }
        return start + 1;
    }

    private void emitWhitespace() {
        int i = myTokenStart + 1;
        while (i < myBufferEnd && isWhitespace(at(i))) {
            i++;
        }
        myTokenEnd = i;
        myTokenType = TokenType.WHITE_SPACE;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }

    private static boolean isHexDigit(char c) {
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':' || c == '.';
    }

    private static boolean isAttributeNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':' || c == '.'
                || c == '@' || c == '[' || c == ']';
    }
}
