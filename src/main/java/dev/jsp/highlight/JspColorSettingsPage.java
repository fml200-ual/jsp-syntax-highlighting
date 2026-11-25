package dev.jsp.highlight;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

/**
 * Settings page under <em>Editor &gt; Color Scheme &gt; JSP</em>, so the JSP colours can be tuned
 * without editing the theme. Not listed in <em>Plugins</em> settings; it is a colour page, not an
 * application-level settings page.
 */
public final class JspColorSettingsPage implements ColorSettingsPage {

    private static final String DEMO_TEXT = """
            <%@ page language="java" contentType="text/html; charset=UTF-8" %>
            <%-- JSP comment: this is not rendered --%>
            <!DOCTYPE html>
            <html>
            <head><title>${pageTitle}</title></head>
            <body>
            <%!
                private int visitas = 0;
            %>
            <h1 class="titulo">Hola, ${usuario.nombre}</h1>
            <%
                visitas++;
                String saludo = "Visitas: " + visitas;
                System.out.println(saludo);
            %>
            <p>Total: <%= visitas %></p>
            <a href="/detalle?id=${usuario.id}">Ver detalle</a>
            <ul>
            <%-- iteration over ${items} is a runtime concern, not the lexer's --%>
            </ul>
            </body>
            </html>
            """;

    private static final AttributesDescriptor[] DESCRIPTORS = {
            new AttributesDescriptor("JSP delimiters", JspSyntaxHighlighter.JSP_DELIMITER),
            new AttributesDescriptor("JSP comment", JspSyntaxHighlighter.JSP_COMMENT),
            new AttributesDescriptor("Directive name", JspSyntaxHighlighter.JSP_DIRECTIVE_NAME),
            new AttributesDescriptor("Directive attribute", JspSyntaxHighlighter.JSP_DIRECTIVE_ATTRIBUTE),
            new AttributesDescriptor("Directive value", JspSyntaxHighlighter.JSP_DIRECTIVE_VALUE),
            new AttributesDescriptor("Directive assignment", JspSyntaxHighlighter.JSP_DIRECTIVE_ASSIGN),
            new AttributesDescriptor("Java keyword", JspSyntaxHighlighter.JAVA_KEYWORD),
            new AttributesDescriptor("Java identifier", JspSyntaxHighlighter.JAVA_IDENTIFIER),
            new AttributesDescriptor("Java number", JspSyntaxHighlighter.JAVA_NUMBER),
            new AttributesDescriptor("Java string", JspSyntaxHighlighter.JAVA_STRING),
            new AttributesDescriptor("Java line comment", JspSyntaxHighlighter.JAVA_LINE_COMMENT),
            new AttributesDescriptor("Java block comment", JspSyntaxHighlighter.JAVA_BLOCK_COMMENT),
            new AttributesDescriptor("Java operator", JspSyntaxHighlighter.JAVA_OPERATOR),
            new AttributesDescriptor("Java punctuation", JspSyntaxHighlighter.JAVA_PUNCTUATION),
            new AttributesDescriptor("EL delimiters", JspSyntaxHighlighter.EL_DELIMITER),
            new AttributesDescriptor("EL identifier", JspSyntaxHighlighter.EL_IDENTIFIER),
            new AttributesDescriptor("EL keyword", JspSyntaxHighlighter.EL_KEYWORD),
            new AttributesDescriptor("EL number", JspSyntaxHighlighter.EL_NUMBER),
            new AttributesDescriptor("EL string", JspSyntaxHighlighter.EL_STRING),
            new AttributesDescriptor("EL operator", JspSyntaxHighlighter.EL_OPERATOR),
            new AttributesDescriptor("EL punctuation", JspSyntaxHighlighter.EL_PUNCTUATION),
            new AttributesDescriptor("JavaScript keyword", JspSyntaxHighlighter.JS_KEYWORD),
            new AttributesDescriptor("JavaScript identifier", JspSyntaxHighlighter.JS_IDENTIFIER),
            new AttributesDescriptor("JavaScript number", JspSyntaxHighlighter.JS_NUMBER),
            new AttributesDescriptor("JavaScript string", JspSyntaxHighlighter.JS_STRING),
            new AttributesDescriptor("JavaScript template literal", JspSyntaxHighlighter.JS_TEMPLATE),
            new AttributesDescriptor("JavaScript line comment", JspSyntaxHighlighter.JS_LINE_COMMENT),
            new AttributesDescriptor("JavaScript block comment", JspSyntaxHighlighter.JS_BLOCK_COMMENT),
            new AttributesDescriptor("JavaScript operator", JspSyntaxHighlighter.JS_OPERATOR),
            new AttributesDescriptor("JavaScript punctuation", JspSyntaxHighlighter.JS_PUNCTUATION),
            new AttributesDescriptor("HTML text", JspSyntaxHighlighter.HTML_TEXT_KEY),
            new AttributesDescriptor("HTML tag name", JspSyntaxHighlighter.HTML_TAG_NAME_KEY),
            new AttributesDescriptor("HTML tag delimiter", JspSyntaxHighlighter.HTML_TAG_DELIMITER),
            new AttributesDescriptor("HTML attribute name", JspSyntaxHighlighter.HTML_ATTRIBUTE_NAME_KEY),
            new AttributesDescriptor("HTML attribute value", JspSyntaxHighlighter.HTML_ATTRIBUTE_VALUE_KEY),
            new AttributesDescriptor("HTML comment", JspSyntaxHighlighter.HTML_COMMENT_KEY),
            new AttributesDescriptor("HTML declaration", JspSyntaxHighlighter.HTML_DECLARATION_KEY),
            new AttributesDescriptor("HTML entity", JspSyntaxHighlighter.HTML_ENTITY_KEY),
    };

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.FileTypes.Java;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new JspSyntaxHighlighter();
    }

    @Override
    public @NotNull String getDemoText() {
        return DEMO_TEXT;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "JSP (highlight)";
    }
}
