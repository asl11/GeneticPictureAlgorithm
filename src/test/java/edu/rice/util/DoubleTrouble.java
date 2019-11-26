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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * This unit test exercises many of the wonderful and strange properties of IEEE float-point
 * arithmetic.
 *
 * <p>What's special about +0, -0, and NaN and why are they treated differently by == than
 * .equals()? That's because the == method is trying to do "the right thing for mathematics", where
 * +0 and -0 might as well be the same thing, right? The .equals() method, on the other hand, is
 * trying to the right thing for treating these numbers as keys in a hashtable, where +0 and -0 are
 * two separate keys. Consequently, the <code>equals()</code> method just looks at the bits, while
 * the == operator has some special cases to manage: +0 and -0 should be equal to each other, but
 * not-a-number, which you get from dividing by zero, is different. <code>NaN != NaN</code> because
 * it's really representative of a set of different ways a computation could fail, and they're not
 * necessarily the same thing.
 *
 * <p>Want to learn a lot more about how IEEE floating point arithmetic works? Here's an interview
 * with William Kahan, a Berkeley CS professor who was central to the IEEE floating point standard:
 *
 * <ul>
 *   <li><a href="https://people.eecs.berkeley.edu/~wkahan/ieee754status/754story.html">An Interview
 *       with the Old Man of Floating-Point</a>
 * </ul>
 *
 * <p>Here's an essay by David Goldberg, a math PhD from Princeton with a long career in industry:
 *
 * <ul>
 *   <li><a href="https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html">What Every
 *       Computer Scientist Should Know About Floating-Point Arithmetic</a>
 * </ul>
 *
 * <p>And, here's a StackOverflow answer explaining why NaN != NaN by Stephen Canon, a member of the
 * IEEE committee that standardized on how floating point arithmetic works:
 *
 * <ul>
 *   <li><a
 *       href="https://stackoverflow.com/questions/1565164/what-is-the-rationale-for-all-comparisons-returning-false-for-ieee754-nan-values/1573715#1573715">What
 *       is the rationale for all comparisons returning false for IEEE754 NaN values?</a>
 * </ul>
 *
 * @see Double#equals(Object)
 * @see Double#isInfinite(double)
 * @see Double#isNaN(double)
 */
public class DoubleTrouble {
  // Engineering note: Normally, ErrorProne would scream about some of
  // the things we're doing in the code below, because it's easy to
  // get this stuff wrong. We're disabling those warnings because (a)
  // we know what we're doing and (b) we want you to see how all these
  // dark corners of Java "equality" really work.
  @SuppressWarnings({"ReferenceEquality", "EqualsNaN"})
  @Test
  public void assertSameVsAssertEqualVsDoubleEquals() {
    // Let's start off by understanding the weird world of autoboxing.
    final double one = 1.0;
    final double one2 = 1.0;
    final Double dOne = 1.0; // autoboxed
    final Double dOne2 = 1.0; // also autoboxed

    assertEquals(one, one2); // as expected
    assertTrue(one == one2); // equivalent to the previous assertion

    assertEquals(dOne, dOne2); // calls the equals() method, which will check the internal values
    assertTrue(dOne.equals(dOne2)); // equivalent to the previous assertion

    assertNotSame(
        one,
        one2); // calls assertNotSame(Object, Object), so autoboxes; the objects will be different
    assertNotSame(dOne, dOne2); // again, different objects, despite having the same value within
    assertFalse(dOne == dOne2); // equivalent to the previous assertion

    // Engineering note: you may notice that we have two different
    // kids of zeros here. Let's do some exercises to test our
    // understanding of Java's zero. We'll start with 0.0 and 0.0 +
    // 0.0, which is of course also 0.0. When you use ==, you get
    // exactly what you expect. Otherwise, you have to pay careful
    // attention to what's "autoboxed" into a Double and what's passed
    // along as a double.

    // Try asking IntelliJ to take you to the implementations of these
    // functions (e.g., Command-B on a Mac) so you can see what's
    // going on for each assertion being checked.

    assertTrue(0.0 == 0.0 + 0.0); // calls assertTrue(boolean)
    assertEquals(0.0, 0.0 + 0.0); // calls assertEquals(double, double)
    assertNotSame(0.0, 0.0 + 0.0); // calls assertNotSame(Object, Object), so autoboxing happens

    // Now, often we'll want to say that two numbers are really close,
    // but not exactly the same. For example, we know that sin(x) is
    // approximately x when x is small. Here's how we'd test that.
    assertEquals(0.001, Math.sin(0.001), 1e-5);

    // Now, let's talk about plus and minus zero. When we use ==, Java
    // is nice enough to arrange that +0 and -0 will be == to one
    // another, since the different rarely matters.
    final double zeroPlus = 0.0;
    final double zeroMinus = -0.0;

    // Also, note that there's a subtle IntelliJ bug we found here.
    // https://youtrack.jetbrains.com/issue/IDEA-195369
    assertTrue(zeroPlus == zeroMinus);
    assertNotSame(zeroPlus, zeroMinus);

    // there's no assertNotEquals(double, double); this calls
    // assertNotEquals(Object, Object)
    assertNotEquals(zeroPlus, zeroMinus);

    // of course, plus and minus zero are *incredibly* close to each
    // other
    assertEquals(zeroPlus, zeroMinus, 1e-30);

    // When we use Double.equals(), instead of double ==, we end up
    // testing exact bit-equality, so +0 is different from -0.
    final Double dZeroPlus = 0.0;
    final Double dZeroMinus = -0.0;

    assertNotSame(dZeroPlus, dZeroMinus);
    assertFalse(dZeroPlus == dZeroMinus);
    assertNotEquals(dZeroPlus, dZeroMinus);
    assertFalse(dZeroPlus.equals(dZeroMinus));

    // In IEEE floating point, NaN has the odd definition that NaN !=
    // NaN. Why?
    final double nan = Double.NaN;
    final double nan2 = Double.NaN;

    // These seem equivalent but they're not!
    assertFalse(nan == nan2); // comparing double values directly
    assertNotSame(
        nan, nan2); // autoboxes, calls assertNotSame(Object, Object), so comparing Object pointers

    // When you use == or assertSame on Double, you're comparing the
    // pointers. You're not looking at the bits at all.
    final Double dNan = Double.NaN;
    final Double dNan2 = Double.NaN;
    assertTrue(dNan == dNan);
    assertSame(dNan, dNan);

    // Different boxes, different pointers.
    assertFalse(dNan2 == dNan);
    assertNotSame(dNan, dNan2);

    // When you use the boxed version, the equals() method compares
    // the bits inside the boxes for being exactly the same. Even
    // though nan != nan2, dNan.equals(dNan2).
    assertTrue(dNan.equals(dNan2));
    assertEquals(
        dNan,
        dNan2); // calls assertEquals(Object, Object), which ultimately calls the equals() method
    assertEquals(
        nan,
        nan2); // calls assertEquals(double, double) which internally tests for exact bit equality

    // And if you really need to know about NaN-ness:
    assertTrue(dNan.isNaN());
    assertTrue(Double.isNaN(nan));
  }
}
