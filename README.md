# JSP Highlight

Plugin de IntelliJ IDEA **2025.3 (unificada, serie de build 253)** que añade resaltado de sintaxis
para ficheros JavaServer Pages.

> **Contexto.** En la línea unificada, el soporte JSP de JetBrains vive en los plugins de la parte
> de pago. Sin suscripción, un `.jsp` se abre como texto plano. Este plugin cubre ese hueco con un
> lexer y un esquema de color propios.

[Licencia: MIT](LICENSE) · Requiere IntelliJ IDEA 2025.3 (build `253`) o superior

---

## Qué resalta

| Constructo | Ejemplo |
|---|---|
| Texto de plantilla y HTML | `<p class="x">hola</p>` |
| Comentario JSP | `<%-- no se renderiza --%>` |
| Scriptlet | `<% int x = 0; %>` |
| Expresión | `<%= x * 2 %>` |
| Declaración | `<%! private int n = 0; %>` |
| Directiva | `<%@ page contentType="text/html" %>` |
| EL | `${usuario.nombre}`, `#{carrito.total}` |
| JavaScript dentro de `<script>` | `var`, `function`, `//`, `/* */`, strings, template literals, operadores |
| Comentarios HTML/Java | `<!-- -->`, `//`, `/* */` |

Extensiones asociadas: `.jsp`, `.jspf`, `.jspx`, `.tag`, `.tagx`.

## Requisitos

- IntelliJ IDEA **2025.3** (build `253`).
- JDK **21** para compilar.

## Compilar

Por defecto la plataforma se resuelve del **repositorio de JetBrains**:

```powershell
$env:JAVA_HOME = "<ruta a un JDK 21>"
.\gradlew.bat buildPlugin
```

El ZIP instalable queda en `build/distributions/`.

### Modo offline (plataforma local)

Si la máquina no puede resolver el API de releases de JetBrains (es el caso que originó este
proyecto), indica un IDE ya descomprimido con la propiedad `idePath` o la variable `IDE_PATH`:

```powershell
$cache = "<carpeta de caché>"
curl.exe -L --retry 5 -o "$cache\ideaIC-253.33813.120.zip" `
  "https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/253.33813.120/ideaIC-253.33813.120.zip"
tar.exe -xf "$cache\ideaIC-253.33813.120.zip" -C "$cache\ide253"   # local() exige el IDE descomprimido

.\gradlew.bat buildPlugin "-PidePath=$cache\ide253"
```

Para no repetir la ruta, guárdala en `%USERPROFILE%\.gradle\gradle.properties` (fuera del repo):

```properties
idePath=C:/ruta/al/ide253
```

El `build.gradle.kts` no contiene rutas de máquina: o hay `idePath`/`IDE_PATH`, o se resuelve
`intellijIdeaCommunity("253.33813.120")` desde el repositorio.

## Instalar

1. `Settings > Plugins > ⚙ > Install Plugin from Disk…`
2. Selecciona `build/distributions/*.zip`
3. Reinicia el IDE.

Para probar sin instalar: `.\gradlew.bat runIde`.

## Ejecutar los tests

```powershell
.\gradlew.bat test
```

`JspLexerTest` es **JUnit puro**, a propósito: el lexer solo necesita `Lexer`, `IElementType` y el
objeto de lenguaje, así que los tests corren sin el *platform test framework* (nada que descargar,
funciona offline y en CI).

Es de tipo *golden file*: cada `.jsp` de `src/test/testData/lexer` se lexa y se compara con el
volcado de tokens de su `.txt` homónimo. Si el `.txt` no existe, el test lo escribe y falla — hay
que revisarlo a mano y volver a ejecutar. **No se acepta un `.txt` generado sin revisar**: es el
contrato del lexer, no una salida cualquiera.

Tras un cambio intencionado en el lexer, regenera los goldens y revísalos:

```powershell
.\gradlew.bat test "-Djsp.golden.update=true"
```

## Diseño

JSP es un lenguaje anfitrión: texto y HTML por fuera, Java y EL por dentro. Por eso el lexer
(`dev.jsp.highlight.lexer.JspLexer`) es una **máquina de estados explícita**, no un conjunto plano
de reglas:

```
S_HTML → S_JSP_OPEN → S_JAVA | S_DIRECTIVE_* | (comentario)
S_HTML → S_TAG_NAME → S_TAG_BODY → S_TAG_VALUE_* → (EL | JSP) → vuelta al valor
S_HTML / S_TAG_VALUE_* → S_EL_OPEN → S_EL → vuelta al estado de origen
S_TAG_NAME ("script") → S_TAG_BODY → ">" → S_JS → "</script" → S_TAG_NAME
```

Cada estado recuerda a dónde volver (`myJspReturnState`, `myElReturnState`) y `getState()` empaqueta
esos datos en un `int`, de modo que el relexado incremental del editor no pierde contexto.

**Sin JFlex en la v1.** El lexer está escrito a mano para no depender de un generador de código ni
de un plugin Gradle adicional; el contrato (`Lexer`, tokens, tests golden) es idéntico. Migrar a
JFlex es un cambio local a esta clase y su build.

**Sin PSI real.** `JspParserDefinition` produce un único nodo plano `JSP_FILE`. El editor necesita
una definición de parser para construir el árbol PSI, pero el resaltado no depende de él. No hay
navegación, referencias ni inspecciones.

**IDs propios para no colisionar.** El lenguaje se registra como `JSP_HIGHLIGHT` y el file type como
`JSP (highlight)`. Así, si algún día se activa una licencia con el soporte JSP de JetBrains, los dos
plugins conviven sin `IllegalArgumentException` por IDs duplicados. La extensión `.jsp` sí es
compartida: el IDE decide qué file type la atiende y se puede forzar en
`Settings > Editor > File Types`.

## Limitaciones del entorno (por qué el build es raro)

- Esta máquina **no tiene ruta IPv6 por defecto** y `data.services.jetbrains.com`, el API que usa el
  plugin Gradle para resolver releases, solo responde por IPv6 para el JVM → `UnknownHostException`.
- Consecuencia: `intellijIdeaCommunity(...)`, `intellijIdea(...)` y `verifyPlugin` no funcionan aquí.
  De ahí el `local(...)` con ZIP descargado por `curl`.
- `gradle-intellij-plugin` 2.20.1 exige **Gradle 9+**; el wrapper fija 9.1.0.

## Límites conocidos (v1)

- No resuelve taglibs ni valida atributos de directivas.
- No analiza tipos Java: todo scriptlet es léxico, no semántico.
- EL anidado dentro de cadenas EL no se interpreta.
- **JavaScript**: no reconoce literales regex (`/[a-z]+/`), así que `/` se lexa como operador;
  los template literals son un token único (el `${}` de dentro no se separa); dentro de strings
  JS no se separa el EL (`'${x}'` es un string completo, como corresponde léxicamente).
- **CSS dentro de `<style>`**: se pinta como texto.
- Un `${...}` *sin* comillas dentro de `<script>` sí se reconoce como EL, porque JSP lo evalúa.
  Con comillas (`var u = '${url}';`) es un string JS y se pinta como tal.

## Roadmap sugerido

1. Migrar el lexer a JFlex (`jflex` + `org.jetbrains.grammarkit`).
2. Parser/PSI real: nodos de tag, directiva y scriptlet; `BraceMatcher` y `Commenter`.
3. `CompletionContributor` para directivas y atributos conocidos.
4. `GotoDeclaration` de `<%@ taglib %>` al TLD del classpath.
5. Publicar en JetBrains Marketplace (requiere icono 40×40/80×80 y licencia).

## Licencia

MIT — ver [LICENSE](LICENSE). Puedes usar, modificar y redistribuir el plugin manteniendo el aviso
de copyright.
