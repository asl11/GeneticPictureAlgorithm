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

package edu.rice.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.doubles;

import org.junit.jupiter.api.Test;

/**
 * This unit test shows the limits of QuickTheories's ability to find bugs. Even when we tell QT to
 * try a million different pairs of numbers (versus it's default, which is more like 100), and even
 * when we restrict it to the range [-1,1] using <code>doubles().between(-1,1)</code> rather than
 * <code>doubles().any()</code>, QT never manages to discover that doubles behave really weird when
 * dealing with +0 and -0 as well as NaN (not-a-number).
 *
 * <p>For more examples showing how weird floating-point arithmetic can be, see {@link
 * DoubleTrouble}.
 */
public class DoubleTheories {
  // helper method used below
  private static boolean equalsMethodSameAsPrimitiveEqualityTest(double a, double b) {
    Double dA = a;
    Double dB = b;

    boolean primitivesMatch = a == b;
    boolean objectsMatch = dA.equals(dB);

    return (primitivesMatch && objectsMatch) || (!primitivesMatch && !objectsMatch);
  }

  @Test
  public void doublePrimitivesVsBoxedValuesHaveSameEquality() {
    // Engineering note: we're asking QuickTheories to make a million
    // attempts at this test -- orders of magnitude more than it
    // otherwise attempts. And yet it's never going to happen in
    // practice that it discovers the two counterexamples shown below.
    qt().withGenerateAttempts(1_000_000)
        .forAll(doubles().any(), doubles().any())
        .checkAssert((a, b) -> assertTrue(equalsMethodSameAsPrimitiveEqualityTest(a, b)));

    assertFalse(equalsMethodSameAsPrimitiveEqualityTest(-0.0, 0.0));
    assertFalse(equalsMethodSameAsPrimitiveEqualityTest(Double.NaN, Double.NaN));
  }
}
