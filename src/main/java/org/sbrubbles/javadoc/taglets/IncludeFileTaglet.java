package org.sbrubbles.javadoc.taglets;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Inserts the content of a text file in the documentation, inline and verbatim.
 * <p/>
 * <h3>Usage example</h3>
 * <blockquote><pre>{@include.file #cs:utf-8 resources/usage.txt}</pre></blockquote>
 * <p/>
 * <h3>Syntax</h3>
 * This tag takes one or more arguments, separated by spaces: a path and
 * optionally one of more <i>flags</i>, which must come before the path. The
 * syntax for the arguments is as follows:
 * <blockquote><pre>
 * arguments ::= flag* path
 * path ::= &lt;a filesystem path>
 * flag ::= '#' STRING ':' STRING
 * </pre></blockquote>
 * <p/>
 * Paths may be absolute or relative, with relative paths resolved at the root
 * of the Javadoc directory. Flags are distinguished from the path by a leading
 * <code>#</code> and trailing whitespace. Therefore, all tokens, starting from
 * the first one lacking a leading <code>#</code>, are concatenated and parsed as
 * a single path. For example, <code>{{@literal @}include.file resources/Mussum Ipsum.txt}</code>
 * reads the path as <code>resources/Mussum Ipsum.txt</code>.
 * <p/>
 *
 * @author Humberto Anjos
 * @see #toString(Tag)
 * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/javadoc/taglet/overview.html">
 * Java 7: Taglet Overview</a>
 * @see <a href="https://docs.oracle.com/javase/7/docs/jdk/api/javadoc/taglet/com/sun/tools/doclets/Taglet.html">
 * Java 7: com.sun.tools.doclets.Taglet documentation</a>
 * @see <a href="https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet#Why_Can.27t_I_Just_HTML_Entity_Encode_Untrusted_Data.3F">
 * OWASP XSS Rule #1</a>
 */
public class IncludeFileTaglet implements Taglet {
  private static final String NAME = "include.file";

  /**
   * Registers this taglet, as per
   * <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/javadoc/taglet/overview.html">Taglet API</a>.
   *
   * @param tagletMap the map to register this tag to.
   * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/javadoc/taglet/overview.html">
   * Taglet API</a>
   */
  public static void register(Map tagletMap) {
    IncludeFileTaglet tag = new IncludeFileTaglet();
    Taglet t = (Taglet) tagletMap.get(tag.getName());
    if (t != null) {
      tagletMap.remove(tag.getName());
    }

    tagletMap.put(tag.getName(), tag);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Returns the content of the file given in {@code tag}, HTML-escaped as
   * in <a href="https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet#Why_Can.27t_I_Just_HTML_Entity_Encode_Untrusted_Data.3F">OWASP XSS Rule #1</a>.
   * <p/>
   * The currently accepted flags are:
   * <ul>
   * <li><b><code>cs</code></b>: the charset of the given file. Accepts anything
   * {@link Charset#forName(String)} does, defaulting to UTF-8.</li>
   * </ul>
   * <p/>
   * Using an unrecognized flag is an error. Errors are handled by printing
   * them in {@link System#err System.err} and returning the empty string.
   *
   * @param tag the {@code Tag} representation of this tag.
   * @return the text given in {@code tag}, or an empty string if there was any
   * error.
   * @see <a href="https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet#Why_Can.27t_I_Just_HTML_Entity_Encode_Untrusted_Data.3F">
   * OWASP XSS Rule #1</a>
   */
  @Override
  public String toString(Tag tag) {
    Arguments args = Arguments.eval(tag.text());

    try {
      List<String> lines = Files.readAllLines(args.path, args.charset);
      StringBuilder sb = new StringBuilder();
      for (String line : lines) {
        sb.append(escapeHtml(line)).append(System.lineSeparator());
      }

      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }

    return "";
  }

  // minimal HTML escaping to avoid script injection (OWASP XSS Rule #1)
  private String escapeHtml(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
        .replace("/", "&#x2F;");
  }

  /**
   * This method should not be called, since arrays of inline tags do not
   * exist. The method {@link #toString(Tag)} should be used to convert this
   * inline tag to a string.
   * <p/>
   * {@inheritDoc}
   *
   * @param tags the array of <code>Tag</code>s representing this custom tag.
   * @return {@code null}, since this tag is inline.
   */
  @Override
  public String toString(Tag[] tags) {
    return null;
  }

  // quick and dirty parsing; I don't want to build a grammar just for this :)
  static class Arguments {
    Path path;
    Charset charset;

    Arguments() {
      charset = Charset.forName("UTF-8");
      path = Paths.get(".");
    }

    static Arguments eval(String input) {
      Arguments result = new Arguments();

      String[] tokens = input.split("\\s");

      int i = 0; // we'll need this index later on
      for (; i < tokens.length; i++) {
        if (!tokens[i].startsWith("#")) {
          break;
        }

        result.parseFlag(tokens[i]);
      }

      StringBuilder pathB = new StringBuilder();
      for (; i < tokens.length; i++) { // same i as above
        pathB.append(tokens[i]).append(" ");
      }

      result.parsePath(pathB.toString().trim());
      return result;
    }

    void parseFlag(String token) {
      String[] elements = token.split(":", 2);
      String flag = elements[0],
          value = elements[1];

      if ("#cs".equals(flag)) {
        charset = Charset.forName(value);
      } else {
        throw new ParsingException("unknown flag: " + token);
      }
    }

    void parsePath(String filepath) {
      if (filepath == null || "".equals(filepath)) {
        return;
      }

      assert path != null;
      path = path.resolve(filepath);
    }

    @Override
    public String toString() {
      return "Arguments{" +
          "path=" + path +
          ", charset=" + charset +
          '}';
    }
  }

  // something for Arguments to throw. Should be captured and processed in the
  // taglet
  static class ParsingException extends RuntimeException {
    ParsingException(String message) {
      super(message);
    }

    ParsingException(String message, Throwable cause) {
      super(message, cause);
    }

    ParsingException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}.
   */
  @Override
  public boolean inField() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}.
   */
  @Override
  public boolean inConstructor() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}.
   */
  @Override
  public boolean inMethod() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}.
   */
  @Override
  public boolean inOverview() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}.
   */
  @Override
  public boolean inPackage() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}.
   */
  @Override
  public boolean inType() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true}.
   */
  @Override
  public boolean isInlineTag() {
    return true;
  }
}
