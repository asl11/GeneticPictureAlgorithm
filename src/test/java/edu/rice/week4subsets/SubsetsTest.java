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

package edu.rice.week4subsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class SubsetsTest {
  @Test
  public void subsetsListEmpty() {
    Seq<Integer> emptyList = List.empty();
    var results = Subsets.subsetsList(emptyList);

    // should be exactly one entry: an empty list
    assertEquals(1, results.length());
  }

  @Test
  public void subsetsStreamEmpty() {
    Seq<Integer> emptyList = List.empty();
    var results = Subsets.subsetsStream(emptyList);

    // should be exactly one entry: an empty list
    assertEquals(1, results.length());
  }

  @Test
  public void subsetsListFive() {
    var fiveEntries = List.of(0, 1, 2, 3, 4);
    var results = Subsets.subsetsList(fiveEntries);

    //    System.out.println("Results: " + results); // uncomment this line to see all the results

    assertEquals(32, results.length());

    assertTrue(results.contains(List.empty()));

    // we expects results to be in the same order as the original
    assertTrue(results.contains(List.of(0, 2, 4)));

    // we expects results not to be reversed
    assertFalse(results.contains(List.of(4, 2, 0)));
  }

  @Test
  public void subsetsStreamFive() {
    var fiveEntries = List.of(0, 1, 2, 3, 4);
    var results = Subsets.subsetsStream(fiveEntries);

    //    System.out.println("Results: " + results); // uncomment this line to see all the results

    assertEquals(32, results.length());

    assertTrue(results.contains(List.empty()));

    // we expects results to be in the same order as the original
    assertTrue(results.contains(List.of(0, 2, 4)));

    // we expects results not to be reversed
    assertFalse(results.contains(List.of(4, 2, 0)));
  }

  /** Checks that everything in subset is also in full. */
  private static <T> void assertSubsetOf(Seq<T> subset, Seq<T> full) {
    subset.forEach(
        entry ->
            assertTrue(
                full.contains(entry),
                String.format(
                    "entry (%s) in subset (%s) not found in full list (%s)", entry, subset, full)));
  }

  /** Checks all pairs of entries, each of which must differ from the rest. */
  private static <T> void assertAllDifferent(Seq<T> list) {
    list.forEach(
        entry -> {
          var numCopies = list.filter(x -> x.equals(entry)).length();
          assertEquals(
              1, numCopies, String.format("list (%s) has more than one copy of (%s)", list, entry));
        });
  }

  @Test
  public void subsetsShouldBeLazy() {
    var hugeList = Stream.rangeClosed(0, 200);

    // if this runs forever or has a stack overflow, then your subsetsStream method isn't lazy!

    var results =
        assertTimeoutPreemptively(
            Duration.ofSeconds(10),
            () -> Subsets.subsetsStream(hugeList),
            "Lazy operations should be fast");

    var firstTwenty =
        assertTimeoutPreemptively(
            Duration.ofSeconds(10), () -> results.take(20), "Lazy operations should be fast");

    assertAllDifferent(firstTwenty);
    firstTwenty.forEach(entry -> assertSubsetOf(entry, hugeList));
  }

  @Test
  public void subsetsBehavesLikeVavrCombinations() {
    var input = Stream.rangeClosed(0, 10);

    var ourResults = Subsets.subsetsList(input);
    var vavrResults = input.combinations(); // built into VAVR!

    // converting to sets: we want equality even if the order of the subsets is different
    assertEquals(vavrResults.toSet(), ourResults.toSet());
  }
}
