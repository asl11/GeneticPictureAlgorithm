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

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jboolean;
import static edu.rice.json.Builders.jnull;
import static edu.rice.json.Builders.jnumber;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.json.Builders.jstring;
import static edu.rice.json.Parser.makeBoolean;
import static edu.rice.json.Parser.makeNull;
import static edu.rice.json.Parser.parseJsonArray;
import static edu.rice.json.Parser.parseJsonObject;
import static edu.rice.json.Parser.parseJsonValue;
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.json.Value.JArray;
import static edu.rice.json.Value.JBoolean;
import static edu.rice.json.Value.JNull;
import static edu.rice.json.Value.JNumber;
import static edu.rice.json.Value.JObject;
import static edu.rice.json.Value.JString;
import static edu.rice.util.Strings.json;
import static edu.rice.util.Strings.stringToUnixLinebreaks;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.unbescape.json.JsonEscape.escapeJson;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.io.Files;
import edu.rice.util.Log;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

@GradeProject(
    name = "Week07",
    description = "JSON Parser",
    warningPoints = 1.0,
    coveragePoints = 1.0,
    coveragePercentage = 85)
@GradeTopic(project = "Week07", topic = "Correctness", maxPoints = 5.0)
@GradeTopic(project = "Week07", topic = "Thursday")
public class ParserTest {
  private static final String TAG = "ParserTest";

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void nullsParseCorrectly() {
    var jnullScanned = makeNull(scanJson("null"));

    assertEquals(jnull(), jnullScanned.get().production);

    List.of(
            "true",
            "false",
            "Null",
            "NULL",
            "nil",
            "Nil",
            "123",
            "[ 123 ]",
            "\"Howdy!\"",
            json("{ \"a\": 3, \"b\": 5 }"))
        .forEach(x -> assertFalse(makeNull(scanJson(x)).isDefined()));

    // Engineering note: See how we're using the json() helper function? It doesn't
    // do anything but take a string input and return it again. However, it tells
    // IntelliJ that the string is JSON, and we get nice syntax highlighting. For
    // this particular test, we want to use well-formed JSON.

    // We could also have structured this test as a JUnit5 TestFactory, like we
    // did last week with ScannerTestPrivate.wholeLotOfTests, but let's just keep
    // it simple.
  }

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void boolsParseCorrectly() {
    var jTrueScanned = makeBoolean(scanJson("true"));
    var jFalseScanned = makeBoolean(scanJson("false"));

    assertEquals(jboolean(true), jTrueScanned.get().production);
    assertEquals(jboolean(false), jFalseScanned.get().production);

    List.of(
            "True",
            "False",
            "TRUE",
            "FALSE",
            "null",
            "123",
            "[ 123 ]",
            "\"Howdy!\"",
            json("{ \"a\": 3, \"b\": 5 }"))
        .forEach(x -> assertFalse(makeBoolean(scanJson(x)).isDefined()));
  }

  @Test
  @Grade(project = "Week07", topic = "Thursday", points = 0.5)
  public void stringsParseCorrectly() {
    // Write assertions, as above, that exercise makeString. Make sure you've got
    // inputs that you expect to be parsed successfully as well as inputs you expect
    // will fail. This doesn't have to be elaborate. Just create at least five
    // inputs you expect will succeed and five inputs you expect will fail, and
    // have ten corresponding assert statements.

    //    fail("stringsParseCorrectly not implemented yet");
  }

  @Test
  @Grade(project = "Week07", topic = "Thursday", points = 0.5)
  public void numbersParseCorrectly() {
    // Write assertions, as above, that exercise makeNumber. Make sure you've got
    // inputs that you expect to be parsed successfully as well as inputs you expect
    // will fail. This doesn't have to be elaborate. Just create at least five
    // inputs you expect will succeed and five inputs you expect will fail, and
    // have ten corresponding assert statements.

    //    fail("numbersParseCorrectly not implemented yet");
  }

  @Test
  @Grade(project = "Week07", topic = "Thursday", points = 0.5)
  public void arraysParseCorrectly() {
    // Write assertions, as above, that exercise makeArray. Make sure you've got
    // inputs that you expect to be parsed successfully as well as inputs you expect
    // will fail. This doesn't have to be elaborate. Just create at least five
    // inputs you expect will succeed and five inputs you expect will fail, and
    // have ten corresponding assert statements.

    //    fail("arraysParseCorrectly not implemented yet");
  }

  @Test
  @Grade(project = "Week07", topic = "Thursday", points = 0.5)
  public void objectsParseCorrectly() {
    // Write assertions, as above, that exercise makeObject. Make sure you've got
    // inputs that you expect to be parsed successfully as well as inputs you expect
    // will fail. This doesn't have to be elaborate. Just create at least five
    // inputs you expect will succeed and five inputs you expect will fail, and
    // have ten corresponding assert statements.

    //    fail("objectsParseCorrectly not implemented yet");
  }

  static final String BIG_JSON =
      stringToUnixLinebreaks(Files.readResource("bigJson.json").getOrElse(""));

  // basically, this is what we expect when we try to pretty-print BIG_COMPARISON:
  // - object key/values are sorted by key
  static final String BIG_JSON_NORMALIZED =
      stringToUnixLinebreaks(Files.readResource("bigJsonNormalized.json").getOrElse(""));

  static final JObject BIG_COMPARISON =
      jobject(
          jpair("itemCount", 2),
          jpair("subtotal", "$15.50"),
          jpair(
              "items",
              jarray(
                  jobject(
                      jpair("title", "The Big Book of Foo"),
                      jpair("description", "Bestselling book of Foo by A.N. Other"),
                      jpair("imageUrl", "/images/books/12345.gif"),
                      jpair("price", "$10.00"),
                      jpair("qty", 1)),
                  jobject(
                      jpair("title", "Javascript Pocket Reference"),
                      jpair(
                          "description",
                          "Handy pocket-sized reference for the Javascript language"),
                      jpair("imageUrl", "/images/books/56789.gif"),
                      jpair("price", "$5.50"),
                      jpair("qty", 2)))));

  // Engineering note: when we have a Java String that we want to be a
  // well-formed JSON string, it would be handy if IntelliJ could do
  // syntax highlighting and check us if we made a mistake.  Well,
  // just like we could use @Language("RegExp") in our JSON scanner,
  // to say that each string was really a regular expression, we can
  // do the same thing here to say that each string is really
  // JSON. json() is a static helper function that comes to us from
  // edu.rice.json.Builders.

  static final String BASIC_OBJECT_JSON = json("{\n" + "  \"x\" : 1,\n" + "  \"y\" : 2\n" + "}");

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void successfulParseOfBasicObject() {
    var oresult = parseJsonObject(BASIC_OBJECT_JSON);
    assertTrue(oresult.isDefined());

    var result = oresult.get();

    assertEquals("1", result.get("x").map(Object::toString).getOrElse("fail"));
    assertEquals("2", result.get("y").map(Object::toString).getOrElse("fail"));

    assertEquals(json("{\"x\": 1, \"y\": 2}"), result.toString());
  }

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void failedParseOfCorruptBasicObject() {
    // now test that we can add tokens on the end, which shouldn't be there, and the parser will
    // fail
    Log.i(TAG, "Testing parsing of non-compliant JSON: expect logs of failures!");
    assertTrue(parseJsonObject("{ \"a\": true} 23 null").isEmpty());
    assertTrue(parseJsonValue("{ \"a\": true} 23 null").isEmpty());
    assertTrue(parseJsonArray("[true, false] 23 null").isEmpty());
    assertTrue(parseJsonValue(BASIC_OBJECT_JSON + " 23 null").isEmpty());
  }

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void successfulParseOfBigObject() {
    assertNotEquals("", BIG_JSON);
    assertNotEquals("", BIG_JSON_NORMALIZED);

    var oresult2 = parseJsonObject(BIG_JSON);
    assertTrue(oresult2.isDefined());
    var result2 = oresult2.get();
    var items =
        result2.get("items").getOrElseThrow(() -> new RuntimeException("failed to get items"));
    assertTrue(items instanceof JArray);

    var itemList = items.asJArray().getSeq();

    var item1 = itemList.get(0);
    assertTrue(item1 instanceof JObject);

    var result3 = item1.asJObject();

    assertEquals(
        "The Big Book of Foo",
        result3.get("title").map(x -> x.asJString().toUnescapedString()).getOrElse("fail"));
    assertEquals(
        "\"The Big Book of Foo\"",
        result3.get("title").map(x -> x.asJString().toString()).getOrElse("fail"));
    assertEquals(
        "$10.00",
        result3.get("price").map(x -> x.asJString().toUnescapedString()).getOrElse("fail"));
    assertEquals(
        "\"$10.00\"", result3.get("price").map(x -> x.asJString().toString()).getOrElse("fail"));
    assertEquals(1.0, result3.get("qty").map(x -> x.asJNumber().get()).getOrElse(Double.NaN), 0.01);

    // how you might write it all in one go... Note all the awful type casting!
    assertEquals(
        ((JString) result3.get("title").get()).toUnescapedString(),
        ((JString)
                ((JObject) ((JArray) result2.get("items").get()).getSeq().head())
                    .getMap()
                    .apply("title"))
            .toUnescapedString());

    // a little method sugar to make the medicine go down
    assertEquals(
        result3.get("title").map(x -> x.asJString().toUnescapedString()).getOrElse("absent title"),
        result2
            .get("items")
            .map(
                x ->
                    x.asJArray()
                        .getSeq() // returns an Seq<Value>
                        .head() // returns an Value
                        .asJObject() // which we want to treat as a JObject
                        .get("title")
                        .map(
                            y ->
                                y // from which we'll extract a specific Value
                                    .asJString() // which happens to be a JString, so cast it
                                    .toUnescapedString())
                        .getOrElse(
                            "another title fail")) // finally extract the String with escapes fixed
            .getOrElse("items fail"));

    // let's test the asOJ*() methods
    assertEquals(
        "The Big Book of Foo",
        result3
            .get("title")
            .flatMap(Value::asJStringOption)
            .fold(() -> "fail", JString::toUnescapedString));
    assertEquals(
        "fail",
        result3
            .get("title")
            .flatMap(Value::asJBooleanOption)
            .fold(() -> "fail", JBoolean::toString));

    assertEquals(
        "fail",
        result3.get("title").flatMap(Value::asJNullOption).fold(() -> "fail", JNull::toString));

    assertEquals(
        "fail",
        result3.get("title").flatMap(Value::asJArrayOption).fold(() -> "fail", JArray::toString));

    assertEquals(
        "fail",
        result3.get("title").flatMap(Value::asJObjectOption).fold(() -> "fail", JObject::toString));

    assertEquals(
        "fail",
        result3.get("title").flatMap(Value::asJNumberOption).fold(() -> "fail", JNumber::toString));
  }

  @Test
  public void expectedIndentation() {
    // we should be able to convert BIG_COMPARISON back to be exactly the same as BIG_JSON
    assertEquals(BIG_JSON_NORMALIZED, stringToUnixLinebreaks(BIG_COMPARISON.toIndentedString()));
  }

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void buildersEquivalentToParser() {
    var basicObject = parseJsonObject(BASIC_OBJECT_JSON).get();
    var basicComparison = jobject(jpair("x", 1), jpair("y", 2));

    assertEquals(basicComparison, basicObject);

    var bigObject = parseJsonObject(BIG_JSON).get();
    assertEquals(BIG_COMPARISON, bigObject);

    var otherStuffObject =
        parseJsonObject(json("{ \"stuff\" : [ \"hello\", 23, true, false, null ] }")).get();
    var otherStuffComparison =
        jobject(
            jpair(
                "stuff",
                jarray(jstring("hello"), jnumber(23), jboolean(true), jboolean(false), jnull())));

    assertEquals(otherStuffComparison, otherStuffObject);
  }

  @Test
  @Grade(project = "Week07", topic = "Correctness", points = 0.5)
  public void escapedSlashesHandledCorrectly() {
    // What's up with escaped forward slashes?
    //
    // http://stackoverflow.com/questions/1580647/json-why-are-forward-slashes-escaped
    // https://github.com/esnme/ultrajson/issues/110
    // https://code.google.com/p/json-simple/issues/detail?id=8
    //
    // In short, it's *allowed* to escape them, and there are some
    // weird cases where it's *preferable* to escape them, but it's
    // also *allowed* not to escape them. Yeah, standards! This test
    // checks to make sure that, whether escaped or not, we still end
    // up with the same result.
    //
    // What we're testing, really, is that we're using a single
    // internal representation regardless of the external differences.

    var stringObjectWithSlashes =
        parseJsonObject(json("{ \"path\" : \"\\/foo\\/bar/baz\" }")).get();
    var stringObjectWithSlashesComparison = jobject(jpair("path", "/foo/bar/baz"));

    assertEquals(stringObjectWithSlashesComparison, stringObjectWithSlashes);
  }

  @Test
  public void valueMatchCanFilterNumbers() {
    var peopleDB =
        jobject(
            jpair("Alice", 40),
            jpair("Bob", "Yuck"),
            jpair("Charlie", true),
            jpair("Dorothy", 10),
            jpair("Eve", jnull()));

    // filter out only the ones that have a jnumber (age)
    var ages =
        peopleDB
            .getContents() // we're starting with Seq<Tuple2<String,Value>>
            .flatMap(
                kv ->
                    kv._2.match(
                        jObject -> none(),
                        jArray -> none(),
                        jString -> none(),
                        jNumber -> some(jNumber.get()),
                        jBoolean -> none(),
                        jNull -> none()));

    assertEquals(2, ages.length());
    assertEquals(50.0, (double) ages.sum(), 0.01);
  }

  @Test
  public void deleteCharacterShouldBeEscaped() {
    // we had some issues with this with the Apache Commons Text
    // library; now we're using unbescape, which seems better
    var backspaceString = "Backspace: \b";
    var deleteString = "Delete: \u007F";

    assertEquals("Backspace: \\b", escapeJson(backspaceString));
    assertEquals("Delete: \\u007F", escapeJson(deleteString));
  }
}
