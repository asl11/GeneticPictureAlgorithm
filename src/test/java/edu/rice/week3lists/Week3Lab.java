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

package edu.rice.week3lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Week3Lab {
  @Test
  public void testFill() {
    var emptyList = GList.<String>empty();
    var list5 = GList.of("Hello", "Hello", "Hello", "Hello", "Hello");
    assertEquals(emptyList, GList.fill(0, "Hello"));
    assertEquals(emptyList, GList.fill(-5, "Hello"));
    assertEquals(list5, GList.fill(5, "Hello"));
  }

  @Test
  public void testTake() {
    var list1 = GList.of(5, 4, 3, 2, 1);
    assertEquals(5, list1.length()); // sanity test

    var firstThree = list1.take(3);
    assertEquals(3, firstThree.length());
    assertEquals(GList.of(5, 4, 3), firstThree);

    // now for some crazier cases
    var firstTen = list1.take(10);
    assertEquals(list1.toString(), firstTen.toString());

    var shouldBeEmpty = list1.take(0);
    assertTrue(shouldBeEmpty.isEmpty());

    var shouldAlsoBeEmpty = list1.take(-3);
    assertTrue(shouldAlsoBeEmpty.isEmpty());
  }
}
