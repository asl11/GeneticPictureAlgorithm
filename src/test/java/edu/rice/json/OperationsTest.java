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

import static edu.rice.json.Builders.jnumber;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.json.Builders.jstring;
import static edu.rice.json.Operations.getPathOption;
import static edu.rice.json.Operations.getPathOptionArray;
import static edu.rice.json.Operations.getPathOptionBoolean;
import static edu.rice.json.Operations.getPathOptionNumber;
import static edu.rice.json.Operations.getPathOptionString;
import static edu.rice.json.Operations.getPathsMatching;
import static edu.rice.json.Operations.getValuesMatchingPathRegex;
import static edu.rice.json.Operations.updatePath;
import static edu.rice.json.Operations.updateValuesMatchingPathRegex;
import static edu.rice.json.Parser.parseJsonObject;
import static edu.rice.util.Strings.regexToPredicate;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.io.Files;
import io.vavr.Tuple;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

@GradeProject(
    name = "Week09",
    description = "Database Operations",
    coveragePoints = 1.0,
    coveragePercentage = 85,
    warningPoints = 1.0)
@GradeTopic(project = "Week09", topic = "getPathOption")
@GradeTopic(project = "Week09", topic = "Operations")
public class OperationsTest {
  static final Value.JObject BIG_JSON_ALL_CAPS_TITLE =
      parseJsonObject(Files.readResource("bigJsonAllCapsTitle.json").getOrElse("")).get();

  static final Value.JObject BIG_JSON_QTY_PLUS_TEN =
      parseJsonObject(Files.readResource("bigJsonQtyPlusTen.json").getOrElse("")).get();

  // This JSON file has a collection of puppies that we're going to
  // use for queries and such; it notably has no JSON arrays in it, so
  // the tests against this will work even when the tests against the
  // "bigJson" objects will fail, assuming that you've got objects
  // working and arrays not working yet.
  static final Value.JObject PUPPIES =
      parseJsonObject(Files.readResource("puppies.json").getOrElse("")).get();

  @Test
  @Grade(project = "Week09", topic = "getPathOption", points = 1.0)
  public void getPathTest() {
    // Engineering note: many of these examples are directly calling
    // get() on an Option, without any proof that it's
    // Option.some. That's normally a bad engineering practice for
    // production code, but it's great for test code, because it
    // encodes an assertion that the call will succeed. If it fails,
    // then the test fails.

    assertEquals("$10.00", getPathOptionString(ParserTest.BIG_COMPARISON, "items/0/price").get());
    assertFalse(
        getPathOptionNumber(ParserTest.BIG_COMPARISON, "items/0/price")
            .isDefined()); // it's a string, not a number
    assertFalse(
        getPathOptionBoolean(ParserTest.BIG_COMPARISON, "items/0/price")
            .isDefined()); // it's a string, not a boolean
    assertFalse(
        getPathOptionArray(ParserTest.BIG_COMPARISON, "items/0/price")
            .isDefined()); // it's a string, not a JSON array

    assertEquals("$5.50", getPathOptionString(ParserTest.BIG_COMPARISON, "items/1/price").get());
    assertFalse(getPathOption(ParserTest.BIG_COMPARISON, "items/2/price").isDefined());
    assertFalse(getPathOption(ParserTest.BIG_COMPARISON, "items/green/price").isDefined());
    assertFalse(getPathOption(ParserTest.BIG_COMPARISON, "items/1/green").isDefined());
    assertEquals((Double) 2.0, getPathOptionNumber(ParserTest.BIG_COMPARISON, "itemCount").get());
    assertFalse(getPathOption(ParserTest.BIG_COMPARISON, "itemCount/1").isDefined());
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void jObjectGetMatching() {
    final var puppies = PUPPIES.get("puppies").get().asJObject();

    assertEquals(
        List.of(Tuple.of("puppies", puppies)), PUPPIES.getMatching(regexToPredicate("puppies")));

    assertEquals(List.empty(), PUPPIES.getMatching(regexToPredicate("birds")));

    assertEquals(
        List.of(Tuple.of("Charlie", puppies.get("Charlie").get())),
        puppies.getMatching(regexToPredicate("Charlie")));

    // make sure our regex helper function is working correctly, while we're at it
    assertTrue(regexToPredicate("Char.*").test("Charlie"));
    assertTrue(regexToPredicate(".*").test("Charlie"));

    assertEquals(
        List.of(Tuple.of("Charlie", puppies.get("Charlie").get())),
        puppies.getMatching(regexToPredicate("Char.*")));

    assertEquals(
        HashSet.of(
            Tuple.of("Alice", puppies.get("Alice").get()),
            Tuple.of("Charlie", puppies.get("Charlie").get())),
        puppies.getMatching(regexToPredicate(".*li.*")).toSet());

    assertEquals(
        HashSet.of(
            Tuple.of("Alice", puppies.get("Alice").get()),
            Tuple.of("Bob", puppies.get("Bob").get()),
            Tuple.of("Charlie", puppies.get("Charlie").get())),
        puppies.getMatching(regexToPredicate(".*")).toSet());
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void regexSearchTest() {
    var prices =
        getValuesMatchingPathRegex(ParserTest.BIG_COMPARISON, List.of(".*", ".*", "pr\\w+"));
    assertEquals(List.of(jstring("$10.00"), jstring("$5.50")), prices);

    // filtering out the zero...
    var pricesFiltered =
        getValuesMatchingPathRegex(
            ParserTest.BIG_COMPARISON, List.of(".*", "[123456789]", "price"));
    assertEquals(List.of(jstring("$5.50")), pricesFiltered);
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void testUpdate() {
    var tmp1 =
        updatePath(
                ParserTest.BIG_COMPARISON,
                "items/0/qty",
                oval -> oval.map(val -> jnumber(val.asJNumber().get() + 10)))
            .get();

    var tmp2 =
        updatePath(
                tmp1, "items/1/qty", oval -> oval.map(val -> jnumber(val.asJNumber().get() + 10)))
            .get();

    assertEquals(BIG_JSON_QTY_PLUS_TEN, tmp2);
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void testBogusArrayIndices() {
    // if you pass a string that isn't an integer, when dealing with a JArray, the result
    // of a getter should be Option.none(), and the result of a setter should be a no-op.

    assertEquals(none(), getPathOption(ParserTest.BIG_COMPARISON, "items/zero/qty"));

    assertEquals(
        some(ParserTest.BIG_COMPARISON),
        updatePath(ParserTest.BIG_COMPARISON, "items/zero/qty", oval -> some(jnumber(300))));
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void testUpdateNewDepth() {
    var basics = jobject(jpair("itemCount", 2), jpair("subtotal", "$15.50"));

    var testVal = updatePath(basics, "a/b/c/d", oval -> some(jstring("Hello!"))).get();

    var expected =
        jobject(
            jpair("itemCount", 2),
            jpair("subtotal", "$15.50"),
            jpair("a", jobject(jpair("b", jobject(jpair("c", jobject(jpair("d", "Hello!"))))))));

    assertEquals(expected, testVal);

    // should nuke the whole a/b/c/d stack
    var nowRemoveItVal = updatePath(expected, "a", oval -> none()).get();
    assertEquals(basics, nowRemoveItVal);
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void testPathSearchesNoArrays() {
    assertEquals(
        HashSet.of(
            List.of("puppies", "Alice", "hat"),
            List.of("puppies", "Bob", "hat"),
            List.of("puppies", "Charlie", "hat")),
        getPathsMatching(
                PUPPIES,
                List.of(
                    regexToPredicate("puppies"), regexToPredicate(".*"), regexToPredicate("hat")))
            .toSet());
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void testUpdatePathMatchesRegex() {
    var testVal =
        updateValuesMatchingPathRegex(
                ParserTest.BIG_COMPARISON,
                List.of(".*", ".*", "title"),
                oval -> oval.map(val -> jstring(val.asJString().toUnescapedString().toUpperCase())))
            .get();

    assertEquals(BIG_JSON_ALL_CAPS_TITLE, testVal);

    // nothing should actually match this time, so the result should be unchanged
    var testVal2 =
        updateValuesMatchingPathRegex(
                ParserTest.BIG_COMPARISON,
                List.of("foo", "bar", "baz", "whee"),
                oval -> oval.map(val -> jstring(val.asJString().toUnescapedString().toUpperCase())))
            .get();

    assertEquals(ParserTest.BIG_COMPARISON, testVal2);
  }

  @Test
  @Grade(project = "Week09", topic = "Operations", points = 0.5)
  public void testMissing() {
    // First, we're starting a a simple object having one name/value
    // pair ("name" -> "Alice"). We're going to update this to add
    // another name ("age" -> 10). The first assertion, as part of the
    // update, asserts that there was nothing previously there for the
    // "field" key. The second assertion verifies that the new thing
    // that we inserted landed where we expected.

    var obj =
        updatePath(
            jobject(jpair("name", "Alice")),
            "age",
            val -> {
              assertFalse(val.isDefined());
              return some(jnumber(10));
            });

    assertEquals(jobject(jpair("name", "Alice"), jpair("age", 10)), obj.get());
  }
}
