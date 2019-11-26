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
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.util.Strings.json;
import static edu.rice.util.Strings.stringToUnixLinebreaks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.io.Files;
import edu.rice.regex.Token;
import edu.rice.util.Log;
import io.vavr.collection.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

@GradeProject(
    name = "Week06",
    description = "Regular expressions / property-based testing",
    warningPoints = 1.0)
@GradeTopic(project = "Week06", topic = "Thursday")
@GradeTopic(project = "Week06", topic = "Correctness", maxPoints = 4.0)
public class ScannerTest {
  private static final String TAG = "ScannerTest";

  @Test
  @Grade(project = "Week06", topic = "Thursday", points = 2.0)
  public void testBasicRegexs() {
    final Pattern stringPattern = Pattern.compile(STRING.pattern);
    assertFalse(stringPattern.matcher("hello").matches());
    assertTrue(stringPattern.matcher("\"hello\"").matches());

    // TODO: Your "Thursday" unit tests for week06 go here.
  }

  // This file is an example of a completely legal JSON expression.
  private static final String bigJson =
      stringToUnixLinebreaks(Files.readResource("bigJson.json").getOrElse(""));

  // This file has the sort of thing shows up sometimes in JavaScript
  // programs, but is *not* legal JSON and should be rejected.
  private static final String noQuotesJson =
      stringToUnixLinebreaks(Files.readResource("bigJsonMalformed.notjson").getOrElse(""));

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testJsonExamples() {
    assertNotEquals("", bigJson); // make sure the file read operations succeeded
    assertNotEquals("", noQuotesJson);

    final var tokenList = scanJson(bigJson);

    final var expectedTokens =
        List.of(
            new Token<>(OPENCURLY, "{"),
            new Token<>(STRING, "itemCount"),
            new Token<>(COLON, ":"),
            new Token<>(NUMBER, "2"),
            new Token<>(COMMA, ","),
            new Token<>(STRING, "subtotal"),
            new Token<>(COLON, ":"),
            new Token<>(STRING, "$15.50"));

    // there are more tokens after this, so we're only testing that
    // the first ones are what we expect
    assertEquals(expectedTokens, tokenList.take(expectedTokens.length()));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void testJsonBadExamples() {
    // now, switch to the input version that's missing quotation
    // marks. This should cause the lexer to fail to find a token on
    // the first non-quoted string.

    Log.i(TAG, "Testing scanning of non-compliant JSON tokens: expect logs of failures!");
    assertEquals(List.of(new Token<>(FAIL, "")), scanJson("####"));

    final var tokenListNoQuotes = scanJson(noQuotesJson);
    final var expectedTokensNoQuotes = List.of(new Token<>(OPENCURLY, "{"), new Token<>(FAIL, ""));

    assertEquals(expectedTokensNoQuotes, tokenListNoQuotes);
  }

  private final Token<Scanner.JsonPatterns> failToken = new Token<>(FAIL, "");

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void malformedNumbers() {
    final var uglyNumbers = scanJson("2 0000 33 11");

    assertEquals(new Token<>(NUMBER, "2"), uglyNumbers.head());
    assertTrue(uglyNumbers.contains(failToken));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void malformedNulls() {
    final var realNulls = scanJson("null,null,null");
    assertEquals(
        List.of(
            new Token<>(NULL, "null"),
            new Token<>(COMMA, ","),
            new Token<>(NULL, "null"),
            new Token<>(COMMA, ","),
            new Token<>(NULL, "null")),
        realNulls);

    final var nullnullnullList = scanJson("nullnullnull");
    assertTrue(nullnullnullList.contains(failToken));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void zeroDotZeroDotZero() {
    final var tokenList = scanJson("0.0 0.0"); // should succeed!

    final var expectedTokens = List.of(new Token<>(NUMBER, "0.0"), new Token<>(NUMBER, "0.0"));

    assertEquals(expectedTokens, tokenList);

    final var tokenList2 = scanJson("0.11.0"); // should fail!

    assertTrue(tokenList2.contains(failToken));
  }

  @Test
  @Grade(project = "Week06", topic = "Correctness", points = 0.2)
  public void whitespaceOptionalForStructuralTokens() {
    final var tokenList = scanJson(json("{\"a\":[],\"b\":true,\"c\":{},\"d\":false}"));

    final var expectedTokens =
        List.of(
            new Token<>(OPENCURLY, "{"),
            new Token<>(STRING, "a"),
            new Token<>(COLON, ":"),
            new Token<>(OPENSQUARE, "["),
            new Token<>(CLOSESQUARE, "]"),
            new Token<>(COMMA, ","),
            new Token<>(STRING, "b"),
            new Token<>(COLON, ":"),
            new Token<>(TRUE, "true"),
            new Token<>(COMMA, ","),
            new Token<>(STRING, "c"),
            new Token<>(COLON, ":"),
            new Token<>(OPENCURLY, "{"),
            new Token<>(CLOSECURLY, "}"),
            new Token<>(COMMA, ","),
            new Token<>(STRING, "d"),
            new Token<>(COLON, ":"),
            new Token<>(FALSE, "false"),
            new Token<>(CLOSECURLY, "}"));

    assertEquals(expectedTokens, tokenList);
  }

  @Test
  public void veryLongStringsShouldStillWork() {
    // Engineering note: When we use the regular-expression engine
    // inside Java, it has a stack-overflow when matching very long
    // strings. This test exercises that specific bug and ultimately
    // led us to use the JFlex-based scanner instead. See
    // src/main/jflex/edu/rice/json/FlexScanner.jflex for details.

    final var tokenList = scanJson(Files.readResource("veryLongString.json").get());
    assertEquals(5, tokenList.length());
  }
}
