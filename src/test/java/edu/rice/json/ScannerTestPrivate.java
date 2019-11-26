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

import static edu.rice.json.Scanner.JsonPatterns.CLOSECURLY;
import static edu.rice.json.Scanner.JsonPatterns.CLOSESQUARE;
import static edu.rice.json.Scanner.JsonPatterns.COLON;
import static edu.rice.json.Scanner.JsonPatterns.COMMA;
import static edu.rice.json.Scanner.JsonPatterns.FAIL;
import static edu.rice.json.Scanner.JsonPatterns.FALSE;
import static edu.rice.json.Scanner.JsonPatterns.NULL;
import static edu.rice.json.Scanner.JsonPatterns.NUMBER;
import static edu.rice.json.Scanner.JsonPatterns.OPENCURLY;
import static edu.rice.json.Scanner.JsonPatterns.OPENSQUARE;
import static edu.rice.json.Scanner.JsonPatterns.STRING;
import static edu.rice.json.Scanner.JsonPatterns.TRUE;
import static edu.rice.json.Scanner.JsonPatterns.WHITESPACE;
import static edu.rice.json.Scanner.scanJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.autograder.annotations.Grade;
import edu.rice.regex.Token;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class ScannerTestPrivate {
  private static final String TAG = "ScannerTestPrivate";

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testSimpleStrings() {
    final var expectedTokens =
        List.of(
            new Token<>(STRING, "word"),
            new Token<>(STRING, "123"),
            new Token<>(STRING, "CaPs"),
            new Token<>(STRING, "two words"),
            new Token<>(STRING, "words and1 numbers 1234"),
            new Token<>(STRING, "p!u,n.c:t;u?ation"),
            new Token<>(STRING, "★ ❀unicode ☺❄ "));

    assertEquals(
        expectedTokens,
        scanJson(
            "\"word\" \"123\" \"CaPs\" \"two words\" \"words and1 numbers 1234\" "
                + "\"p!u,n.c:t;u?ation\" \"★ ❀unicode ☺❄ \" "));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testLegalEscapeStrings() {
    final var expectedTokens =
        List.of(
            new Token<>(STRING, "quote\\\"quote \\\" quote"),
            new Token<>(STRING, "backslash\\\\backslash \\\\ backslash"),
            new Token<>(STRING, "slash\\/slash \\/ slash"),
            new Token<>(STRING, "backspace\\bbackspace \\b backspace"),
            new Token<>(STRING, "formfeed\\fformfeed \\f formfeed"),
            new Token<>(STRING, "newline\\nnewline \\n newline"),
            new Token<>(STRING, "carriagereturn\\rcarriagereturn \\n carriagereturn"),
            new Token<>(STRING, "tab\\ttab \\t tab"),
            new Token<>(STRING, "unicode\\u1234unicode \\u5678 unicode"));
    assertEquals(
        expectedTokens,
        scanJson(
            "\"quote\\\"quote \\\" quote\" \"backslash\\\\backslash \\\\ backslash\" "
                + "\"slash\\/slash \\/ slash\" \"backspace\\bbackspace \\b backspace\" "
                + "\"formfeed\\fformfeed \\f formfeed\" \"newline\\nnewline \\n newline\" "
                + "\"carriagereturn\\rcarriagereturn \\n carriagereturn\" \"tab\\ttab \\t tab\" "
                + "\"unicode\\u1234unicode \\u5678 unicode\""));

    assertEquals(
        List.of(
            new Token<>(
                STRING,
                "all\\\"escapes\\\\all\\/escapes\\fall\\nescapes\\rall\\tescapes\\u1234all")),
        scanJson("\"all\\\"escapes\\\\all\\/escapes\\fall\\nescapes\\rall\\tescapes\\u1234all\""));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testIllegalEscapeStrings() {
    final var failTokenList = List.of(new Token<>(FAIL, ""));

    assertEquals(failTokenList, scanJson("bad\\qescape"));
    assertEquals(failTokenList, scanJson("bad\\?escape"));
    assertEquals(failTokenList, scanJson("bad\\4escape"));
    assertEquals(failTokenList, scanJson("bad\\Eescape"));
    assertEquals(failTokenList, scanJson("bad\\❄escape"));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testStringOfOtherTokens() {
    assertEquals(
        List.of(new Token<>(STRING, "nulltruefalse:,[1,2,3]{\\\"key\\\":\\\"value\\\"}fail")),
        scanJson("\"nulltruefalse:,[1,2,3]{\\\"key\\\":\\\"value\\\"}fail\""));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testSimpleNumbers() {
    final var expectedTokens =
        List.of(
            new Token<>(NUMBER, "1"),
            new Token<>(NUMBER, "0"),
            new Token<>(NUMBER, "0.1"),
            new Token<>(NUMBER, "1.0"),
            new Token<>(NUMBER, "20"),
            new Token<>(NUMBER, "24.625"),
            new Token<>(NUMBER, "0.23462"),
            new Token<>(NUMBER, "0.6435e23"),
            new Token<>(NUMBER, "-32"),
            new Token<>(NUMBER, "502.32e56"),
            new Token<>(NUMBER, "234.5236E-23"),
            new Token<>(NUMBER, "-342.124e-24"),
            new Token<>(NUMBER, "0.500"));

    var numbers =
        "1 0 0.1 1.0 20 24.625 0.23462 0.6435e23 -32 502.32e56 234.5236E-23 -342.124e-24 0.500";

    assertEquals(expectedTokens, scanJson(numbers));
  }

  private static Seq<DynamicTest> patternTests(
      String name, Pattern pattern, Seq<String> matches, Seq<String> fails) {

    // Engineering note: We're returning a sequence of dynamic tests,
    // which is to say, a list of lambdas, each of which does JUnit5
    // test assertions on the inside. You'll notice that some of the
    // methods here are marked @TestFactory rather than @Test. The
    // idea is that a TestFactory returns a sequence of tests, each
    // which is meant to run independently. Cleverly, even though
    // JUnit5 knows nothing about VAVR Sequences, it only cares that
    // it gets something that's Iterable, and all of VAVR's list-like
    // classes implement Iterable, so this works perfectly.

    // We did the same trick last week with our TreeSuite, but we're
    // going an extra distance here to build a sequence of tests
    // that pass and fail given strings that we expect to match or
    // expect not to match.

    // Below here, you'll see calls to dynamicTest. That's JUnit5's
    // factory method for making a testing lambda. You'll notice that
    // it takes two arguments: the name of the test and a lambda with
    // the body of the test.

    var goodTests =
        matches.map(
            str ->
                dynamicTest(
                    name + " accepts: " + str, () -> assertTrue(pattern.matcher(str).matches())));
    var badTests =
        fails.map(
            str ->
                dynamicTest(
                    name + " rejects: " + str, () -> assertFalse(pattern.matcher(str).matches())));

    return goodTests.appendAll(badTests);
  }

  /**
   * JUnit5 magic to produce a large list of tests which will be evaluated independently of each
   * other.
   */
  @TestFactory
  @Grade(project = "Week06", topic = "Correctness", points = 0.2, maxPoints = 2.0)
  public Seq<DynamicTest> wholeLotOfTests() {
    final var stringPattern = Pattern.compile(STRING.pattern);
    final var numberPattern = Pattern.compile(NUMBER.pattern);
    final var falsePattern = Pattern.compile(FALSE.pattern);
    final var truePattern = Pattern.compile(TRUE.pattern);
    final var nullPattern = Pattern.compile(NULL.pattern);
    final var openCurlyPattern = Pattern.compile(OPENCURLY.pattern);
    final var closeCurlyPattern = Pattern.compile(CLOSECURLY.pattern);
    final var colonPattern = Pattern.compile(COLON.pattern);
    final var commaPattern = Pattern.compile(COMMA.pattern);
    final var openSquarePattern = Pattern.compile(OPENSQUARE.pattern);
    final var closeSquarePattern = Pattern.compile(CLOSESQUARE.pattern);
    final var whiteSpacePattern = Pattern.compile(WHITESPACE.pattern);

    final var goodStrings =
        List.of(
            "\"hello\"",
            "\"\"",
            "\"\\/\"",
            "\"true\"",
            "\"\\b\"",
            "\"\\f\"",
            "\"\\n\"",
            "\"\\r\"",
            "\"\\t\"",
            "\"\'\"",
            "\"\\\\\"",
            "\"/\"",
            "\"\\b\"",
            "\"\\f\"",
            "\"\\n\"",
            "\"\\r\"",
            "\"\\t\"",
            "\"\\u0000\"",
            "\"\\u0016\"",
            "\"\\uDB99\\uDFDB\\u007F\"",
            "\"你好\"");

    final var badStrings =
        List.of(
            "hello",
            "\"",
            "",
            "/",
            "\b",
            "\\f",
            "\n",
            "\r",
            "0",
            "true",
            "'",
            "\\",
            "/",
            "\\b",
            "\\f",
            "\\n",
            "\\r",
            "\\t",
            "\\u0016",
            "\u0016",
            "\"\u0016\"",
            "\"\\u001\"",
            "你好");

    final var goodNumbers =
        List.of(
            "10", "-10", "0.1", "10.01", "-0.11", "1e10", "1e-10", "1.1e-10", "2E2", "2.5E3", "333",
            "0", "0.000", "55E10", "55E-10", "-55.0");

    final var badNumbers =
        List.of(
            "\"10\"",
            "01",
            "00.234",
            ".1",
            "2e5e6",
            "34.2e23E5",
            "--1",
            "2.3.5",
            "1.",
            "2e--23",
            "1e12.3",
            "00",
            "0.",
            ".1",
            "--1",
            "1.1.1",
            "10ee10",
            "1-1",
            "1-",
            "1..",
            ".1.",
            "\"333\"",
            "0000",
            "00.00",
            "55E0.2",
            "055",
            "55.");

    Log.e(TAG, "about to run many malformed JSON tests; expect many logged errors");

    return patternTests("string", stringPattern, goodStrings, badStrings)
        .appendAll(patternTests("number", numberPattern, goodNumbers, badNumbers))
        .appendAll(
            patternTests(
                "true",
                truePattern,
                List.of("true"),
                List.of("\"true\"", "truetruetrue", "True", "TRUE")))
        .appendAll(
            patternTests(
                "false",
                falsePattern,
                List.of("false"),
                List.of("\"false\"", "falsefalsefalse", "False", "FALSE")))
        .appendAll(
            patternTests(
                "null",
                nullPattern,
                List.of("null"),
                List.of("\"null\"", "nullnullnull", "Null", "NULL", "Nil")))
        .appendAll(
            patternTests(
                "opencurly",
                openCurlyPattern,
                List.of("{"),
                List.of("\"{\"", "{{", "[", "(", "<", "}")))
        .appendAll(
            patternTests(
                "closecurly",
                closeCurlyPattern,
                List.of("}"),
                List.of("\"}\"", "}}", "]", ")", ">", "{")))
        .appendAll(
            patternTests(
                "opensquare",
                openSquarePattern,
                List.of("["),
                List.of("\"[\"", "[[", "{", "(", "<", "]")))
        .appendAll(
            patternTests(
                "closesquare",
                closeSquarePattern,
                List.of("]"),
                List.of("\"]\"", "]]", "}", ")", ">", "[")))
        .appendAll(
            patternTests(
                "colon", colonPattern, List.of(":"), List.of("\":\"", "::", ";", ",", " ", ".")))
        .appendAll(
            patternTests(
                "comma", commaPattern, List.of(","), List.of("\",\"", ",,", ";", ":", " ", ".")))
        .appendAll(
            patternTests(
                "space",
                whiteSpacePattern,
                List.of(" ", " \n ", " \t ", "   "),
                List.of("\" \"", "-", " - ", ":", ".")));
  }
}
