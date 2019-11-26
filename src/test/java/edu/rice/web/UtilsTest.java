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

package edu.rice.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

import org.junit.jupiter.api.Test;

class UtilsTest {
  @Test
  void bigRandomReturnsDigits() {
    qt().forAll(integers().from(0).upTo(200))
        .asWithPrecursor(Utils::bigRandom)
        .checkAssert(
            (nDigits, s) -> {
              assertTrue(s.matches("\\d*"));
              assertEquals(nDigits, s.length());
            });
  }
}
