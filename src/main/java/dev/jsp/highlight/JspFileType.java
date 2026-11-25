package dev.jsp.highlight;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * File type for {@code .jsp}, {@code .jspf}, {@code .jspx}, {@code .tag} and {@code .tagx} files.
 *
 * <p>Registered through the {@code com.intellij.fileType} extension point with the {@code language}
 * attribute, so the file type is only materialised together with {@link JspLanguage}.
 */
public final class JspFileType extends LanguageFileType {

    public static final JspFileType INSTANCE = new JspFileType();

    private JspFileType() {
        super(JspLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "JSP (highlight)";
    }

    @Override
    public @NotNull String getDescription() {
        return "JavaServer Pages file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "jsp";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.FileTypes.Java;
    }
}
