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

package edu.rice.vavr;

import edu.rice.util.Log;
import edu.rice.util.TriFunction;
import io.vavr.control.Option;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper methods for working with VAVR {@link Option}.
 *
 * <p>Are you looking for a "match method" for an Option? It's already built-in. It's called {@link
 * Option#fold(Supplier, Function)}, taking two lambdas: one for the <i>none</i> case and one for
 * the <i>some</i> case.
 */
public interface Options {
  /**
   * Given an {@link Option#some(Object)}, returns the value within. If it's an {@link
   * Option#none()}, generates a logged error with the given tag and string, and the string supplied
   * by the given lambda. Also throws a {@link RuntimeException} with the same supplied string.
   */
  static <T> T optionGetOrLogAndFail(Option<T> o, String tag, Supplier<String> logStrSupplier) {
    if (o.isDefined()) {
      return o.get();
    } else {
      String logStr = logStrSupplier.get();
      Log.e(tag, logStr);
      throw new RuntimeException(logStr);
    }
  }

  /**
   * Given a function from <code>T</code> to <code>R</code>, returns another function from <code>
   * Option&lt;T&gt;</code> to <code>Option&lt;R&gt;</code>, "lifting" the original function to a
   * new one that is equivalent to running the internal function only when its argument is {@link
   * Option#some(Object)}. If the input is {@link Option#none()}, then {@link Option#none()} is
   * returned.
   */
  static <T, R> Function<Option<T>, Option<R>> optionLift(Function<? super T, ? extends R> f) {
    return ot -> ot.map(f);
  }

  /**
   * Given a two-argument function from <code>T1</code> and <code>T2</code> to <code>R</code>,
   * returns another two-argument function from <code>Option&lt;T1&gt;</code> and <code>
   * Option&lt;T2&gt;</code> to <code>Option&lt;R&gt;</code>, "lifting" the original function to a
   * new one that will run the internal function only when all its arguments are {@link
   * Option#some(Object)}. If any input is {@link Option#none()}, then {@link Option#none()} is
   * returned.
   */
  static <T1, T2, R> BiFunction<Option<T1>, Option<T2>, Option<R>> optionLift(
      BiFunction<? super T1, ? super T2, ? extends R> f) {
    return (ot1, ot2) -> ot1.flatMap(t1 -> ot2.map(t2 -> f.apply(t1, t2)));
  }

  /**
   * Given a three-argument function from <code>T1</code>, <code>T2</code>, and <code>T3</code> to
   * <code>R</code>, returns another three-argument function from <code>Option&lt;T1&gt;</code>,
   * <code>Option&lt;T2&gt;</code>, and <code>Option&lt;T3&gt;</code> to <code>Option&lt;R&gt;
   * </code>, "lifting" the original function to a new one that will run the internal function only
   * when all its arguments are {@link Option#some(Object)}. If any input is {@link Option#none()},
   * then {@link Option#none()} is returned.
   */
  static <T1, T2, T3, R> TriFunction<Option<T1>, Option<T2>, Option<T3>, Option<R>> optionLift(
      TriFunction<? super T1, ? super T2, ? super T3, ? extends R> f) {
    return (ot1, ot2, ot3) ->
        ot1.flatMap(t1 -> ot2.flatMap(t2 -> ot3.map(t3 -> f.apply(t1, t2, t3))));
  }
}
