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

package edu.rice.week2subsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week02", topic = "Subsets", maxPoints = 1.0)
public class SubsetsTest {

  @Test
  @Grade(project = "Week02", topic = "Subsets", points = 1.0)
  public void subsetsEmpty() {
    var emptyList = new ArrayList<Integer>();
    var results = Subsets.subsets(emptyList);
    assertEquals(1, results.size()); // should be exactly one entry: an empty list
  }

  @Test
  @Grade(project = "Week02", topic = "Subsets", points = 1.0)
  public void subsetsFive() {
    var fiveEntries = List.of(0, 1, 2, 3, 4);

    var results = Subsets.subsets(fiveEntries);
    //    System.out.println("Results: " + results); // uncomment this line to see all the results

    assertEquals(32, results.size());

    assertTrue(results.contains(new ArrayList<Integer>())); // empty list should be included

    var zeroTwoFour = List.of(0, 2, 4);
    assertTrue(
        results.contains(
            zeroTwoFour)); // we expects results to be in the same order as the original

    var fourTwoZero = List.of(4, 2, 0);
    assertFalse(results.contains(fourTwoZero)); // we expects results not to be reversed
  }
}
