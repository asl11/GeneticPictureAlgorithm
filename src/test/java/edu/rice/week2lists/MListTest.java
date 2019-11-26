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

package edu.rice.week2lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MListTest {

  @Test
  public void testBasics() {
    var ml = new MList();
    assertTrue(ml.isEmpty());
    ml.push("Hello");
    ml.push("Rice");
    ml.push("Owls");
    assertFalse(ml.isEmpty());
    assertTrue(ml.contains("Rice"));
    assertFalse(ml.contains("Harvard"));
    assertEquals("Owls", ml.pop());
    assertEquals("Rice", ml.pop());
    assertEquals("Hello", ml.pop());
  }
}
