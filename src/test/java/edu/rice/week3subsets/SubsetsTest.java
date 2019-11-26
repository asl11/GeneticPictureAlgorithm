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

package edu.rice.week3subsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.week3lists.GList;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week03", topic = "Subsets", maxPoints = 1.0)
public class SubsetsTest {
  @Test
  @Grade(project = "Week03", topic = "Subsets", points = 0.5)
  public void subsetsEmptyWeek3() {
    var emptyList = GList.<Integer>empty();
    var results = Subsets.subsets(emptyList);

    // should be exactly one entry: an empty list
    assertEquals(1, results.length());
  }

  @Test
  @Grade(project = "Week03", topic = "Subsets", points = 0.5)
  public void subsetsFiveWeek3() {
    var fiveEntries = GList.<Integer>empty().prepend(4).prepend(3).prepend(2).prepend(1).prepend(0);
    var results = Subsets.subsets(fiveEntries);

    //    System.out.println("Results: " + results); // uncomment this line to see all the results

    assertEquals(32, results.length());

    assertTrue(results.contains(GList.empty()));

    // we expects results to be in the same order as the original
    assertTrue(results.contains(GList.<Integer>empty().prepend(4).prepend(2).prepend(0)));

    // we expects results not to be reversed
    assertFalse(results.contains(GList.<Integer>empty().prepend(0).prepend(2).prepend(4)));
  }
}
