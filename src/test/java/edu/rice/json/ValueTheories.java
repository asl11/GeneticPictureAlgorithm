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
import static edu.rice.json.JsonGenerators.jsons;
import static edu.rice.json.Parser.parseJsonValue;
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.qt.SequenceGenerators.sequences;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.doubles;
import static org.quicktheories.generators.SourceDSL.strings;

import org.junit.jupiter.api.Test;

public class ValueTheories {
  // bigger numbers yield huge trees and slow down the test suite
  private static final int MAX_DEPTH = 3;

  @Test
  public void jsonToStringAndBackAgain() {
    // Engineering note: check out the jsons() function in
    // JsonGenerators. That allows us to generate random JSON values
    // which we can then convert to strings and back to JSON
    // again. This is incredibly powerful for bug finding.

    qt().forAll(jsons(MAX_DEPTH))
        .checkAssert(
            value -> {
              assertEquals(value, parseJsonValue(value.toString()).get());
              assertEquals(value, parseJsonValue(value.toIndentedString()).get());
            });
  }

  @Test
  public void scanJsonTokenizesWithoutFail() {
    qt().forAll(jsons(MAX_DEPTH))
        .checkAssert(
            value ->
                assertTrue(
                    scanJson(value.toString())
                        .filter(tokens -> tokens.type == Scanner.JsonPatterns.FAIL)
                        .isEmpty()));
  }

  @Test
  public void stringsParseCorrectly() {
    qt().forAll(strings().allPossible().ofLengthBetween(0, 5))
        .checkAssert(
            string ->
                assertEquals(
                    jstring(string), parseJsonValue(jstring(string).toString()).get().asJString()));
  }

  @Test
  public void numbersParseCorrectly() {
    qt().forAll(doubles().any())
        .assuming(Double::isFinite) // can't represent +/- infinity or NaN in JSON
        .checkAssert(
            number ->
                assertEquals(
                    jnumber(number), parseJsonValue(jnumber(number).toString()).get().asJNumber()));
  }

  @Test
  public void jsonStringEqualityAndEscapingWork() {
    qt().forAll(strings().allPossible().ofLengthBetween(0, 10))
        .checkAssert(
            string -> assertEquals(jstring(string), jstring(jstring(string).toUnescapedString())));
  }

  @Test
  public void jsonObjectFromOverlappingLists() {
    // Second instance of a given key should define the associated value; we're using
    // single-letter digits for the keys to make sure the "keys" and "newKeys" overlap.
    qt().forAll(
            sequences().of(strings().basicLatinAlphabet().ofLength(1)).ofSizeBetween(1, 100),
            sequences().of(strings().basicLatinAlphabet().ofLength(1)).ofSizeBetween(1, 100))
        .checkAssert(
            (keys, newKeys) -> {
              var keyMap = keys.toMap(k -> k, k -> (Value) jnumber(1.0));
              var keyMapPlusNewbies =
                  newKeys.foldLeft(keyMap, (map, key) -> map.put(key, jnumber(2.0)));
              var expected = jobject(keyMapPlusNewbies);

              var keyList = keys.map(k -> jpair(k, 1.0));
              var keyListPlusNewbies = keyList.appendAll(newKeys.map(k -> jpair(k, 2.0)));
              var actual = jobject(keyListPlusNewbies);

              assertEquals(expected, actual);
            });
  }
}
