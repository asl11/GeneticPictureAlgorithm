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

package edu.rice.regex;

import static edu.rice.regex.RegexScanner.scanPatterns;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.CLOSECURLY;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.EQUALS;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.FAIL;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.OPENCURLY;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.SEMICOLON;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.WHITESPACE;
import static edu.rice.regex.RegexScannerTest.SimpleTokenPatterns.WORD;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.util.Log;
import io.vavr.collection.List;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

public class RegexScannerTest {
  private static final String TAG = "RegexScannerTest";

  @Test
  public void testGetNamedGroupMatches() {
    var results =
        scanPatterns(
                "{ hello = fun; world=aw3some; }", SimpleTokenPatterns.class, new Token<>(FAIL, ""))
            .filter(token -> token.type != WHITESPACE);

    var expectedResult =
        List.of(
            new Token<>(OPENCURLY, "{"),
            new Token<>(WORD, "hello"),
            new Token<>(EQUALS, "="),
            new Token<>(WORD, "fun"),
            new Token<>(SEMICOLON, ";"),
            new Token<>(WORD, "world"),
            new Token<>(EQUALS, "="),
            new Token<>(WORD, "aw3some"),
            new Token<>(SEMICOLON, ";"),
            new Token<>(CLOSECURLY, "}"));

    assertEquals(expectedResult, results);
  }

  @Test
  public void testGetNamedGroupMatchesWithFail() {
    Log.i(
        TAG, "Testing scanning of tokens that don't match the patterns: expect logs of failures!");
    var results =
        scanPatterns(
                "{ hello = fun; !!! world=aw3some; }",
                SimpleTokenPatterns.class,
                new Token<>(FAIL, ""))
            .filter(token -> token.type != WHITESPACE);

    var expectedResult =
        List.of(
            new Token<>(OPENCURLY, "{"),
            new Token<>(WORD, "hello"),
            new Token<>(EQUALS, "="),
            new Token<>(WORD, "fun"),
            new Token<>(SEMICOLON, ";"),
            new Token<>(FAIL, ""));

    assertEquals(expectedResult, results);
  }

  enum SimpleTokenPatterns implements TokenPatterns {
    OPENCURLY("\\{"),
    CLOSECURLY("}"),
    WHITESPACE("\\s+"),
    EQUALS("="),
    SEMICOLON(";"),
    WORD("\\p{Alnum}+"),
    FAIL("");

    public final String pattern;

    SimpleTokenPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }
}
