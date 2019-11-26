/*
 * This code is part of Rice Comp215 and is made available for your
 * use as a student in Comp215. You are specifically forbidden from
 * posting this code online in a public fashion (e.g., on a public
 * GitHub repository) or otherwise making it, or any derivative of it,
 * available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being
 * reported to the Honor Council, even after you've completed the
 * class, and will result in retroactive reductions to your grade. For
 * additional details, please see the Comp215 course syllabus.
 */

package edu.rice.json;

import static edu.rice.json.Scanner.JsonPatterns.FAIL;
import static edu.rice.json.Scanner.JsonPatterns.STRING;
import static edu.rice.json.Scanner.JsonPatterns.WHITESPACE;
import static edu.rice.regex.RegexScanner.scanPatterns;
import static io.vavr.control.Option.none;

import edu.rice.regex.RegexScanner;
import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.StringReader;
import org.intellij.lang.annotations.Language;

/**
 * This class takes a string and tokenizes it for JSON. {@link RegexScanner} does all the heavy
 * lifting. Note that String tokens coming from the tokenizer will <b>not</b> have quotation marks
 * around them. We remove those. They <b>may have escape characters</b> within, which you'll want to
 * deal with elsewhere.
 */
public interface Scanner {
  /** Internal helper method, gets next token from the JFlex scanner. */
  private static Option<Token<JsonPatterns>> nextToken(FlexScanner scanner) {
    // Engineering note: JFlex scanners return a new token every time
    // you call the yylex() method, mutating their internal state
    // every time, but they can also hypothetically throw an exception
    // because they're meant to directly do IO operations, like
    // reading from a file. We'll never use them that way, but Java
    // *requires* us to catch the declared exception, even though we
    // know it will never occur in practice.

    try {
      return scanner.yylex();
    } catch (IOException ioe) {
      return none();
    }
  }

  /**
   * Given a string, return a list of JSON tokens. If there's a failure in the tokenizer, there will
   * be a FAIL token at the point of the failure. Also note that whitespace tokens are filtered out.
   * You don't have to worry about them.
   *
   * @see JsonPatterns#FAIL
   */
  static Stream<Token<JsonPatterns>> scanJson(String input) {
    // Instead of our scanner built with Java's built-in regex
    // support, we're instead using an automatically generated scanner
    // made by JFlex, which runs faster and is more robust.

    FlexScanner scanner = new FlexScanner(new StringReader(input));

    // Two things going on here: if it's a string, we strip the leading and trailing quotation
    // marks. And if it's a FAIL token, then we're done, but we need to make sure we return
    // the first FAIL at the end, but not the potentially infinite stream of FAILs after
    // that one.

    // Note that whitespace is removed before we get here. It's built into our JFlex scanner.
    return Stream.iterate(() -> nextToken(scanner))
        .map(
            x ->
                x.type == STRING
                    ? new Token<>(STRING, x.data.substring(1, x.data.length() - 1))
                    : x)
        .splitAtInclusive(x -> x.type == FAIL)
        ._1;
  }

  /**
   * Given a string, return a list of JSON tokens. If there's a failure in the tokenizer, there will
   * be a FAIL token at the point of the failure. Also note that whitespace tokens are filtered out.
   * You don't have to worry about them.
   *
   * @see JsonPatterns#FAIL
   */
  static Stream<Token<JsonPatterns>> scanJsonOld(String input) {
    return scanPatterns(input, JsonPatterns.class, new Token<>(FAIL, ""))
        .filter(x -> x.type != WHITESPACE)
        .map(
            x ->
                x.type == STRING
                    ? new Token<>(STRING, x.data.substring(1, x.data.length() - 1))
                    : x);
  }

  // Engineering note: We're going to want to reuse these regular
  // expressions in many different places, not just in the scanner, so
  // we're defining them as separate Strings. Also notable, when you
  // define a constant like this, as part of an interface, it's
  // *always* final. The compiler would reject code that tried to
  // assign something to one of these strings.

  // We're taking advantage of IntelliJ's "regular expression
  // language" annotation, which allows IntelliJ to verify that these
  // complex regular expressions are well-formed. IntelliJ will even
  // detect if you repeat a pattern or otherwise have redundancies in
  // your regular expression. IntelliJ calls this "language injection"
  // and they support a number of other languages like HTML and SQL.

  String jsonStringPatternNoQuotes =
      "(" // grouping of acceptable characters:
          + "[^\"\\\\\\p{Cntrl}]" // any unicode char except " or \ or control char
          + "|\\\\" // or a backslash followed by one of:
          + "([\"\\\\/bfnrt]" // - a series of acceptable single characters
          + "|u[0123456789abcdefABCDEF]{4})" // - or the code for a unicode quad-hex thing
          + ")*"; // zero or more of the group

  String jsonStringPattern = "\"" + jsonStringPatternNoQuotes + "\"";

  String jsonNumberPattern =
      "(-)?" // optional minus sign
          + "(0|" // either a zero, or
          + "[1-9][0-9]*)" // a series of digits starting with a non-zero
          + "(\\.[0-9]+)?" // optional decimal point followed by one or more digits
          + "([eE][+-]?[0-9]+)?" // optional exponent with optional +/- sign
          + "\\b"; // word boundary checker at the end, ensures we don't terminate early

  enum JsonPatterns implements TokenPatterns {
    STRING(jsonStringPattern),
    NUMBER(jsonNumberPattern),
    TRUE("true\\b"),
    FALSE("false\\b"),
    NULL("null\\b"),
    OPENCURLY("\\{"),
    CLOSECURLY("}"),
    COLON(":"),
    COMMA(","),
    OPENSQUARE("\\["),
    CLOSESQUARE("]"),
    WHITESPACE("\\s+"),
    FAIL(""); // if the matcher fails, you get one of these

    public final String pattern;

    JsonPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }
}
