package dev.jsp.highlight;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import dev.jsp.highlight.lexer.JspLexer;
import dev.jsp.highlight.lexer.JspTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal parser definition.
 *
 * <p>The editor needs a parser definition to build a PSI file for the language; highlighting itself
 * is driven by {@link JspSyntaxHighlighterFactory} and does not depend on the PSI tree. The parser
 * therefore produces a single flat {@code JSP_FILE} node over the whole token stream. A real PSI
 * (tags, attributes, scriptlets as nodes) is out of scope for this version.
 */
public final class JspParserDefinition implements ParserDefinition {

    public static final IFileElementType JSP_FILE =
            new IFileElementType("JSP_FILE", JspLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new JspLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return (root, builder) -> {
            var marker = builder.mark();
            while (!builder.eof()) {
                builder.advanceLexer();
            }
            marker.done(root);
            return builder.getTreeBuilt();
        };
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return JSP_FILE;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.create(TokenType.WHITE_SPACE);
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return JspTokenTypes.COMMENTS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull TokenSet getStringLiteralElements() {
        return JspTokenTypes.STRINGS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new JspPsiFile(viewProvider);
    }

    /** Flat PSI file: the whole file is one {@code JSP_FILE} node. */
    public static final class JspPsiFile extends PsiFileBase {
        public JspPsiFile(@NotNull FileViewProvider viewProvider) {
            super(viewProvider, JspLanguage.INSTANCE);
        }

        @Override
        public @NotNull FileType getFileType() {
            return JspFileType.INSTANCE;
        }

        @Override
        public String toString() {
            return "JSP file";
        }
    }
}
