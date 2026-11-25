package dev.jsp.highlight.lexer;

import com.intellij.lexer.Lexer;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Golden-file tests for {@link JspLexer}, deliberately independent of the IntelliJ Platform test
 * framework: the lexer only needs {@code Lexer}, {@code IElementType} and the JSP language object,
 * so plain JUnit keeps the test runnable offline and in CI without extra downloads.
 *
 * <p>Each {@code .jsp} in {@code src/test/testData/lexer} is lexed and compared with the token dump
 * in the matching {@code .txt}. When the dump is missing, it is written and the test fails on
 * purpose: the dump is a reviewed contract, not an incidental output.
 */
public class JspLexerTest {

    private static final Path DATA_DIR = Path.of("src", "test", "testData", "lexer");

    @Test
    public void markup() throws IOException {
        check("markup.jsp");
    }

    @Test
    public void scriptletAndExpression() throws IOException {
        check("scriptlet.jsp");
    }

    @Test
    public void directives() throws IOException {
        check("directive.jsp");
    }

    @Test
    public void expressionLanguage() throws IOException {
        check("el.jsp");
    }

    @Test
    public void jspComments() throws IOException {
        check("comments.jsp");
    }

    @Test
    public void realWorldLiferayStyle() throws IOException {
        check("liferay.jsp");
    }

    @Test
    public void javaScriptInsideScriptTag() throws IOException {
        check("script.jsp");
    }

    private void check(String fileName) throws IOException {
        String text = read(DATA_DIR.resolve(fileName));
        String actual = dump(text);
        boolean update = Boolean.getBoolean("jsp.golden.update");

        Path expectedFile = DATA_DIR.resolve(fileName + ".txt");
        if (!Files.exists(expectedFile) || update) {
            Files.writeString(expectedFile, actual, StandardCharsets.UTF_8);
            if (update) {
                System.out.println("Golden updated: " + expectedFile);
                return;
            }
            fail("Golden file generated: " + expectedFile + " — review it, then re-run the tests");
        }

        assertEquals("Token dump of " + fileName, read(expectedFile), actual);
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String dump(String text) {
        Lexer lexer = new JspLexer();
        lexer.start(text);

        StringBuilder out = new StringBuilder();
        while (lexer.getTokenType() != null) {
            String tokenText = text.substring(lexer.getTokenStart(), lexer.getTokenEnd());
            out.append(lexer.getTokenType())
                    .append(" '")
                    .append(tokenText
                            .replace("\\", "\\\\")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t"))
                    .append("'\n");
            lexer.advance();
        }
        return out.toString();
    }
}
