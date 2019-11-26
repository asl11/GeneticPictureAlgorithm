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

package edu.rice.sexpr;

import static edu.rice.sexpr.Scanner.SexprPatterns.CLOSE;
import static edu.rice.sexpr.Scanner.SexprPatterns.FAIL;
import static edu.rice.sexpr.Scanner.SexprPatterns.OPEN;
import static edu.rice.sexpr.Scanner.SexprPatterns.WHITESPACE;
import static edu.rice.sexpr.Scanner.SexprPatterns.WORD;
import static edu.rice.sexpr.Scanner.scanSexpr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.regex.Token;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class SimpleScannerTest {
  private static final String TAG = "SimpleScannerTest";

  @Test
  public void testSimpleScannerExamples() {
    final var tokenList = scanSexpr("(add (multiply 3 4) 5)");
    final var expectedTokens =
        List.of(
            new Token<>(OPEN, "("),
            new Token<>(WORD, "add"),
            new Token<>(OPEN, "("),
            new Token<>(WORD, "multiply"),
            new Token<>(WORD, "3"),
            new Token<>(WORD, "4"),
            new Token<>(CLOSE, ")"),
            new Token<>(WORD, "5"),
            new Token<>(CLOSE, ")"));

    assertEquals(expectedTokens, tokenList);

    // Let's also make sure that we deal with empty-sexprs properly

    final var tokenList2 = scanSexpr("(() hello)");
    final var expectedTokens2 =
        List.of(
            new Token<>(OPEN, "("),
            new Token<>(OPEN, "("),
            new Token<>(CLOSE, ")"),
            new Token<>(WORD, "hello"),
            new Token<>(CLOSE, ")"));

    assertEquals(expectedTokens2, tokenList2);

    // And let's test that the failure token shows up at the right time

    final var tokenList3 = scanSexpr("(() hello]");
    final var expectedTokens3 =
        List.of(
            new Token<>(OPEN, "("),
            new Token<>(OPEN, "("),
            new Token<>(CLOSE, ")"),
            new Token<>(WORD, "hello"),
            new Token<>(FAIL, ""));

    assertEquals(expectedTokens3, tokenList3);
  }

  private static Seq<DynamicTest> patternTests(
      String name, Pattern pattern, Seq<String> matches, Seq<String> fails) {

    var goodTests =
        matches.map(
            str ->
                dynamicTest(name + ": " + str, () -> assertTrue(pattern.matcher(str).matches())));
    var badTests =
        fails.map(
            str ->
                dynamicTest(name + ": " + str, () -> assertFalse(pattern.matcher(str).matches())));

    return goodTests.appendAll(badTests);
  }

  /**
   * JUnit5 magic to produce a large list of tests which will be evaluated independently of each
   * other.
   */
  @TestFactory
  public Seq<DynamicTest> testRegexes() {
    final var openPattern = Pattern.compile(OPEN.pattern);
    final var closePattern = Pattern.compile(CLOSE.pattern);
    final var wordPattern = Pattern.compile(WORD.pattern);
    final var whitespacePattern = Pattern.compile(WHITESPACE.pattern);

    final var goodOpen = List.of("(");
    final var badOpen = List.of("[", "{", "hello", "((", ")");

    final var goodClose = List.of(")");
    final var badClose = List.of("]", "}", "hello", "))", "(");

    final var goodWord = List.of("10", "hello", "foo_bar");
    final var badWord = List.of("-1", "{", "]");

    final var goodSpace = List.of(" ", "\t", "   ", " \n ");
    final var badSpace = List.of("hello", "{", "]");

    // Engineering note: when you normally have a sequence of
    // assertTrue/False/Equals statements, JUnit will stop when it
    // finds the first test that fails. We want to actually try
    // *everything* even when one of them fails. This ensures that we
    // output a full list of all failing patterns, which will then
    // help you get them all fixed with hopefully fewer attempts at
    // getting it right.  We saw this earlier with our TreeSuite
    // tests.

    // Note that JUnit5 knows nothing about Seq. All it knows
    // is that it's iterable (as in, java.util.Iterable), therefore
    // it can extract the DyanamicTests, one by one.

    Log.e(TAG, "about to run many malformed Sexpr tests; expect many logged errors");

    return patternTests("open", openPattern, goodOpen, badOpen)
        .appendAll(patternTests("close", closePattern, goodClose, badClose))
        .appendAll(patternTests("word", wordPattern, goodWord, badWord))
        .appendAll(patternTests("space", whitespacePattern, goodSpace, badSpace));
  }
}
