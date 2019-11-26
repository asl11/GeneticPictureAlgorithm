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

package edu.rice.week5knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import io.vavr.Tuple;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week05", topic = "Knapsack")
class KnapsackTest {

  @Test
  @Grade(project = "Week05", topic = "Knapsack", points = 0.5)
  void simpleQueries() {
    int[] sampleSizes = {3, 4, 1, 9};
    int[] sampleValues = {2, 7, 4, 1};
    var k = new Knapsack(sampleSizes, sampleValues);

    assertEquals(0, k.computeValue(0));
    assertEquals(4, k.computeValue(1));
    assertEquals(13, k.computeValue(10));

    // this assertion checks that we not only got the right results, but also in the required order
    assertEquals(List.of(Tuple.of(3, 2), Tuple.of(4, 7), Tuple.of(1, 4)), k.knapsackContents(10));

    int[] sampleSizes2 = {3, 4, 1, 9};
    int[] sampleValues2 = {2, 7, 4, 15}; // unlike above, we're expecting the (9, 15) to win
    var k2 = new Knapsack(sampleSizes2, sampleValues2);
    assertEquals(List.of(Tuple.of(1, 4), Tuple.of(9, 15)), k2.knapsackContents(10));
  }

  @Test
  @Grade(project = "Week05", topic = "Knapsack", points = 0.5)
  void multipleCopies() {
    int[] sampleSizes = {4, 2, 3, 3, 1, 9};
    int[] sampleValues = {10, 3, 7, 7, 3, 1};

    var k = new Knapsack(sampleSizes, sampleValues);

    // the two (3, 7) copies beat the (4, 10) + (2, 3) alternative when we're querying size=6
    assertEquals(List.of(Tuple.of(3, 7), Tuple.of(3, 7)), k.knapsackContents(6));

    // but with only size=5, we're going to pick up (4, 10) and (1, 4)
    assertEquals(List.of(Tuple.of(4, 10), Tuple.of(1, 3)), k.knapsackContents(5));
  }
}
