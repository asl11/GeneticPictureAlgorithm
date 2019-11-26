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

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Function;

/**
 * Helpers for VAVR's {@link io.vavr.control.Try}.
 *
 * <p>Are you looking for a "match method" for a Try? It's already built-in. It's called {@link
 * Try#fold(Function, Function)}, taking two lambdas: one for the <i>success</i> case and one for
 * the <i>failure</i> case.
 */
public interface Tries {
  /**
   * Just like {@link Try#of(CheckedFunction0)}, except if the lambda returns <code>null</code>, the
   * result is {@link Try#failed()}. Use if you're wrapping a lambda where a <code>null</code>
   * result implies a failure.
   *
   * <p>Notably, VAVR's {@link Try} is happy to have a value like <code>success(null)</code> but
   * nulls will only ever arise in Comp215 when they're returned by an external library. None of our
   * built-ins ever use null, so we don't want to allow nulls to find their way into our code.
   */
  static <T> Try<T> tryOfNullable(CheckedFunction0<? extends T> supplier) {
    return Try.narrow(
        Try.of(supplier)
            .filter(
                Objects::nonNull,
                () -> new NullPointerException("expected non-null result from supplier")));
  }
}
