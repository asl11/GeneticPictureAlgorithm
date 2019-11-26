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

package edu.rice.qt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;

import edu.rice.autograder.annotations.Grade;
import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;

public class MapAndSetTheories {

  @Test
  @Grade(project = "Week06", topic = "Properties", points = 0.2)
  public void unrelatedKeysDontCollide() {
    // "There and back again": create an arbitrary HeapMap
    // using reasonableStringMaps(), then add another key/value where
    // the key isn't already present in the map, then remove that
    // same key/value. Check that the resulting map is the same
    // as the original.

    qt().forAll(
            QtHelpers.stringMaps(), QtHelpers.reasonableStrings(), QtHelpers.reasonableStrings())
        .assuming((map, k, v) -> !map.containsKey(k))
        .checkAssert(
            (map, k, v) -> {
              var map2 = map.put(k, v).remove(k);
              assertEquals(map, map2);
            });

    //    fail("unrelatedKeysDontCollide: not implemented yet");
  }

  @Test
  @Grade(project = "Week06", topic = "Properties", points = 0.2)
  public void updateValueGetNewValue() {
    // "Hard to prove, easy to verify": create an arbitrary HeapMap,
    // as above, then add another key/value, which might or might
    // not already be there. Check that the resulting value for
    // the key is what you added.

    qt().forAll(
            QtHelpers.stringMaps(), QtHelpers.reasonableStrings(), QtHelpers.reasonableStrings())
        .checkAssert(
            (map, k, v) -> {
              var map2 = map.put(k, v);
              assertEquals(v, map2.get(k).get());
            });

    //    fail("updateValueGetNewValue: not implemented yet");
  }

  @Test
  public void missingKeysHaveNoValues() {
    // "There and back again": VAVR maps can be treated as if they're
    // sequences of key/value tuples. Build a new HashMap using that
    // sequence and a fold operation, then check that the resulting
    // map is equal to the original.

    qt().forAll(QtHelpers.stringMaps())
        .checkAssert(
            map -> {
              var newMap = map.foldLeft(HashMap.empty(), HashMap::put);
              assertEquals(map, newMap);
            });
  }

  @Test
  public void setIntersectionElementsAreInBothSources() {
    // "Hard to prove, easy to verify": Make two VAVR HashSets, compute
    // their set intersection, then verify that every entry in the
    // intersection appears in both original sets.

    qt().forAll(QtHelpers.reasonableStringSets(), QtHelpers.reasonableStringSets())
        .checkAssert(
            (s1, s2) -> {
              var intersection = s1.intersect(s2);
              intersection.forEach(
                  elem -> {
                    assertTrue(s1.contains(elem));
                    assertTrue(s2.contains(elem));
                  });
            });
  }

  @Test
  @Grade(project = "Week06", topic = "Properties", points = 0.2)
  public void setUnionElementsAreInAtLeastOneSource() {
    // "Hard to prove, easy to verify": Make two VAVR HashSets, compute
    // their set union, then verify that every entry in the
    // union appears in at least one of the original sets *and*
    // that every entry in the original sets appears in the union.

    qt().forAll(QtHelpers.reasonableStringSets(), QtHelpers.reasonableStringSets())
        .checkAssert(
            (s1, s2) -> {
              var union = s1.union(s2);
              union.forEach(elem -> assertTrue(s1.contains(elem) || s2.contains(elem)));
              s1.forEach(elem -> assertTrue(union.contains(elem)));
              s2.forEach(elem -> assertTrue(union.contains(elem)));
            });
  }
  //    fail("setUnionElementsAreInAtLeastOneSource: not implemented yet");
}
