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

import java.util.function.BiFunction;

/**
 * This class implements a general-purpose pair of two values. To create a pair, use the
 * helper-method {@link #pair(Object, Object)} and to get the values within, simply reference {@link
 * #_1} or {@link #_2} or use {@link #apply(BiFunction)}.
 *
 * <p>Once we start using VAVR, you should use VAVR's Tuple2 rather than Pair.
 */
public class Pair<A, B> {
  public final A _1;
  public final B _2;

  private Pair(A a, B b) {
    _1 = a;
    _2 = b;
  }

  /** Make a new pair from the two given values. */
  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<>(a, b);
  }

  /** Apply the given bifunction to the elements of the pair, returning the result. */
  public <R> R apply(BiFunction<A, B, R> func) {
    return func.apply(_1, _2);
  }

  @Override
  public String toString() {
    return String.format("(%s, %s)", _1.toString(), _2.toString());
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Pair<?, ?>)
        && ((Pair<?, ?>) o)._1.equals(_1)
        && ((Pair<?, ?>) o)._2.equals(_2);
  }

  @Override
  public int hashCode() {
    return _1.hashCode() * 7 + _2.hashCode();
  }
}
