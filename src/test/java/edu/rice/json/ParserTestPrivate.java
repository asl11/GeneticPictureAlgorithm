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

import static edu.rice.json.Parser.Result;
import static edu.rice.json.Parser.parseJsonObject;
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.util.Strings.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.autograder.annotations.Grade;
import edu.rice.regex.Token;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Function;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class ParserTestPrivate {
  private static final String TAG = "ParserTestPrivate";

  final Seq<String> invalidAnything =
      // Fun trick: try removing that empty string at the start of the list
      // and see what happens to the code coverage test.
      List.of(
          "",
          "{",
          "[[]",
          "[1,]",
          "[,]",
          "[,2]",
          "[,2[",
          "{]",
          "[}",
          "[\"k1\": \"v1\"]",
          "[\"k1\": \"v1\"]",
          "[\"k1\": \"v1\", \"k2\": \"v2\" ]",
          "[1,2,3,]",
          "[,1,2,3]",
          "[1,,2,3]",
          "[1 2 3]",
          "[1,2:3]",
          "{\"k1\" \"v1\"}",
          "{\"k1\": \"v1\", }",
          "{, \"k1\": \"v1\", }",
          "{, \"k1\": \"v1\" }",
          "{\"k1\": \"v1\",    \"k2\": }",
          "{\"k1\": \"v1\"     \"k2\": \"v2\" }",
          "{\"k1\": \"v1\" , , \"k2\": \"v2\" }",
          "{\"k1\": \"v1\" ,   \"k2\", \"v2\" }",
          "{\"k1\": \"v1\" :   \"k2\", \"v2\" }",
          "{\"k1\": \"v1\" :   \"k2\": \"v2\" }",
          "{\"k1\", \"v1\" ,   \"k2\", \"v2\" }",
          "{\"k1\" \"v1\",     \"k2\" \"v2\" }",
          "{\"k1\" \"v1\"      \"k2\" \"v2\" }",
          "{\"k1\": \"v1\" ,   \"k2\": \"v2\" ]",
          "{true: \"v1\"}",
          "{3: \"v1\"}",
          "{null: \"v1\"}",
          "{[3]: \"v1\"}");

  final Seq<String> invalidObjects =
      List.of("true", "false", "3", "27.0", "null", "[]", "\"hello\": 3");
  final Seq<String> invalidArrays = List.of("true", "false", "3", "27.0", "null", "{}", "1,2,3");
  final Seq<String> invalidStrings =
      List.of("true", "false", "3", "27.0", "null", "{}", "[\"hello\"]");
  final Seq<String> invalidNumbers =
      List.of("\"true\"", "\"false\"", "\"True\"", "[3]", "{}", "null");
  final Seq<String> invalidBooleans =
      List.of("\"true\"", "\"false\"", "\"True\"", "3", "27.0", "null", "{}", "[true]");
  final Seq<String> invalidNulls = List.of("3", "27.0", "\"null\"", "{}", "[]");

  final Seq<String> validArrays =
      List.of(
          json("[]"),
          json("[1,2,3]"),
          json("[\"a\",\"b\",\"c\"]"),
          json("[true,[false],true]"),
          json("[null,null,[null, null, [null], null]]"));

  final Seq<String> validObjects =
      List.of(
          json("{}"),
          json("{\"first\":1,\"second\":2}"),
          json("{\"first\":1,\"second\":[3,5,2,1,0,-3,{\"third\": 3}]}"),
          json("{\"1\":1,\"2\":2,\"3\":3}"),
          json("{\"x\":1.0}"));

  final Seq<String> validValues =
      List.of(
          json("12"),
          json("12.5"),
          json("true"),
          json("false"),
          json("null"),
          json("\"Hello\""),
          json("[]"),
          json("{}"));

  private static Seq<DynamicTest> makeFailureHelper(
      String testName,
      Seq<String> input,
      Function<Seq<Token<Scanner.JsonPatterns>>, Option<Result<Value>>> mapper) {

    return input.map(
        str ->
            dynamicTest(
                testName + "(\"" + str + "\") fails",
                () -> assertFalse(mapper.apply(scanJson(str)).isDefined())));
  }

  private static Seq<DynamicTest> makeSuccessHelper(
      String testName,
      Seq<String> input,
      Function<Seq<Token<Scanner.JsonPatterns>>, Option<Result<Value>>> mapper) {

    var goodTests =
        input.map(
            str ->
                dynamicTest(
                    testName + "(\"" + str + "\") good",
                    () -> assertTrue(mapper.apply(scanJson(str)).isDefined())));

    // now, we'll add another token on the end and make sure it's there when we're done
    var goodTestsPlus =
        input.map(
            str ->
                dynamicTest(
                    testName + "(\"" + str + "\") good + ignored",
                    () ->
                        assertEquals(
                            "ignored",
                            mapper
                                .apply(scanJson(str + " \"ignored\""))
                                .get()
                                .tokens
                                .head()
                                .data)));

    return goodTests.appendAll(goodTestsPlus);
  }

  /** Test makeNull with a variety of good and bad inputs. */
  @TestFactory
  public Seq<DynamicTest> testNulls() {
    return makeFailureHelper("makeNull", invalidNulls, Parser::makeNull)
        .appendAll(makeFailureHelper("makeNull", invalidAnything, Parser::makeNull));
  }

  /** Test makeObject with a variety of good and bad inputs. */
  @TestFactory
  public Seq<DynamicTest> testObjects() {
    return makeFailureHelper("makeObject", invalidObjects, Parser::makeObject)
        .appendAll(makeFailureHelper("makeObject", invalidAnything, Parser::makeObject))
        .appendAll(makeFailureHelper("makeObject", validArrays, Parser::makeObject))
        .appendAll(makeSuccessHelper("makeObject", validObjects, Parser::makeObject));
  }

  /** Test makeArray with a variety of good and bad inputs. */
  @TestFactory
  public Seq<DynamicTest> testArrays() {
    return makeFailureHelper("makeArray", invalidArrays, Parser::makeArray)
        .appendAll(makeFailureHelper("makeArray", invalidAnything, Parser::makeArray))
        .appendAll(makeSuccessHelper("makeArray", validArrays, Parser::makeArray))
        .appendAll(makeFailureHelper("makeArray", validObjects, Parser::makeArray));
  }

  /** Test makeString with a variety of good and bad inputs. */
  @TestFactory
  public Seq<DynamicTest> testStrings() {
    return makeFailureHelper("makeString", invalidStrings, Parser::makeString)
        .appendAll(makeFailureHelper("makeString", invalidAnything, Parser::makeString));
  }

  /** Test makeNumber with a variety of good and bad inputs. */
  @TestFactory
  public Seq<DynamicTest> testNumbers() {
    return makeFailureHelper("makeNumber", invalidNumbers, Parser::makeNumber)
        .appendAll(makeFailureHelper("makeNumber", invalidAnything, Parser::makeNumber));
  }

  /** Test makeObject with a variety of good and bad inputs. */
  @TestFactory
  public Seq<DynamicTest> testBooleans() {
    return makeFailureHelper("makeBoolean", invalidBooleans, Parser::makeBoolean)
        .appendAll(makeFailureHelper("makeBoolean", invalidAnything, Parser::makeBoolean));
  }

  private static Seq<DynamicTest> parserSuccessHelper(
      String parserName, Seq<String> goodInputs, Function<String, Option<?>> parser) {

    // Engineering note: We can get away with Option<?> here because
    // the only thing we're testing is whether the parser succeeded
    // (returning some(...)) or failed (returning none()).  If you're
    // reading this *after* the midterm, what would be a better type
    // constraint?

    var goodTests =
        goodInputs.map(
            str ->
                dynamicTest(
                    parserName + "(\"" + str + "\") succeeds",
                    () -> assertTrue(parser.apply(str).isDefined())));

    var goodTestsPlus =
        goodInputs.map(
            str ->
                dynamicTest(
                    parserName + "(\"" + str + "\" \\\"ignored\\\") fails",
                    () -> assertFalse(parser.apply(str + "\"ignored\"").isDefined())));

    return goodTests.appendAll(goodTestsPlus);
  }

  private static Seq<DynamicTest> parserFailureHelper(
      String parserName, Seq<String> badInputs, Function<String, Option<?>> parser) {

    return badInputs.map(
        str ->
            dynamicTest(
                parserName + "(\"" + str + "\") fails",
                () -> assertFalse(parser.apply(str).isDefined())));
  }

  /** Test parseJsonArray with a variety of good and bad inputs. */
  @TestFactory
  @Grade(project = "Week07", topic = "Correctness", points = 0.2, maxPoints = 1.0)
  public Seq<DynamicTest> testParseArray() {
    Log.e(TAG, "testing JSON array parser failures; expect many errors being logged!");

    return parserSuccessHelper("parseJsonArray", validArrays, Parser::parseJsonArray)
        .appendAll(parserFailureHelper("parseJsonArray", validObjects, Parser::parseJsonArray))
        .appendAll(parserFailureHelper("parseJsonArray", invalidAnything, Parser::parseJsonArray));
  }

  /** Test parseJsonObject with a variety of good and bad inputs. */
  @TestFactory
  @Grade(project = "Week07", topic = "Correctness", points = 0.2, maxPoints = 1.0)
  public Seq<DynamicTest> testParseObject() {
    Log.e(TAG, "testing JSON object parser failures; expect many errors being logged!");

    return parserSuccessHelper("parseJsonObject", validObjects, Parser::parseJsonObject)
        .appendAll(parserFailureHelper("parseJsonObject", validArrays, Parser::parseJsonObject))
        .appendAll(
            parserFailureHelper("parseJsonObject", invalidAnything, Parser::parseJsonObject));
  }

  /** Test parseJsonValue with a variety of good and bad inputs. */
  @TestFactory
  @Grade(project = "Week07", topic = "Correctness", points = 0.2, maxPoints = 1.0)
  public Seq<DynamicTest> testParseValue() {
    Log.e(TAG, "testing JSON value parser failures; expect many errors being logged!");

    var allValidInputs = validObjects.appendAll(validArrays).appendAll(validValues);
    return parserSuccessHelper("parseJsonValue", allValidInputs, Parser::parseJsonValue)
        .appendAll(parserFailureHelper("parseJsonValue", invalidAnything, Parser::parseJsonValue));
  }

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.2)
  public void testDuplicateKeys() {
    var obj =
        parseJsonObject(json("{\"first\": 1, \"second\": 2, \"third\": 3}")).get().asJObject();
    assertEquals(1, obj.apply("first").asJNumber().get());
    assertEquals(2, obj.apply("second").asJNumber().get());
    assertEquals(3, obj.apply("third").asJNumber().get());

    // The JSON spec doesn't define this as an error; we require that
    // the last entry overrides an earlier one.  If you add json()
    // around this string, the IntelliJ syntax highlighter will flag
    // the repeating key here.  Clearly, it's not desirable to repeat
    // a key, so it's sensible for them to flag it.

    var obj2 = parseJsonObject("{\"first\": 1, \"second\": 2, \"first\": 3}").get().asJObject();
    assertEquals(3, obj2.apply("first").asJNumber().get());
    assertEquals(2, obj2.apply("second").asJNumber().get());
  }
}
