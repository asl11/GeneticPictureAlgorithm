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

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.function.Supplier;

/** Helper functions for measuring the performance of various functions. */
public interface Performance {
  /** Runs the given runnable, returns the number of nanoseconds it took to run. */
  static long nanoBenchmark(Runnable runnable) {
    var startTime = System.nanoTime();
    runnable.run();
    var endTime = System.nanoTime();

    return endTime - startTime;
  }

  /**
   * Runs the given lambda, returns a pair of the number of nanoseconds it took to run as well as
   * the result of the lambda.
   */
  static <T> Tuple2<Long, T> nanoBenchmarkVal(Supplier<T> supplier) {
    var startTime = System.nanoTime();
    var result = supplier.get();
    var endTime = System.nanoTime();

    return Tuple.of(endTime - startTime, result);
  }
}
