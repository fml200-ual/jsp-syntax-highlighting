package dev.jsp.highlight;

import com.intellij.lang.Language;

/**
 * The single language instance for JavaServer Pages.
 *
 * <p>No parser beyond a flat token stream is registered in this version: the language exists so
 * that a file type, a lexer and a syntax highlighter can be attached to it.
 */
public final class JspLanguage extends Language {

    public static final JspLanguage INSTANCE = new JspLanguage();

    public static final String ID = "JSP_HIGHLIGHT";

    private JspLanguage() {
        super(ID);
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
