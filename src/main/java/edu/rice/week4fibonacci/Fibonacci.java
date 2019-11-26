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

package edu.rice.week4fibonacci;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;

/**
 * Four different implementations of the Fibonacci function.
 *
 * <p>More on Leonardo Bigollo (a.k.a., Fibonacci):
 * https://www.bbvaopenmind.com/en/fibonacci-and-his-magic-numbers/
 */
public interface Fibonacci {
  /** Traditional, recursive Fibonacci. */
  static int fib(int n) {
    if (n <= 1) {
      return 1;
    } else {
      return fib(n - 1) + fib(n - 2);
    }
  }

  /** Tail-recursive Fibonacci. */
  static int fibTailRecursive(int n) {
    return fibTailRecursiveHelper(1, 1, 0, n);
  }

  private static int fibTailRecursiveHelper(int p1, int p2, int start, int end) {
    if (start < end) {
      return fibTailRecursiveHelper(p2, p1 + p2, start + 1, end);
    } else {
      return p1;
    }
  }

  /** Looping Fibonacci. */
  static int fibLoop(int n) {
    int p1 = 1;
    int p2 = 1;
    int start = 0;

    while (start < n) {
      int newP2 = p1 + p2;
      p1 = p2;
      p2 = newP2;
      start = start + 1;
    }
    return p1;
  }

  /** Lazy list Fibonacci, recomputing the list every time. */
  static int fibStream(int n) {
    var allFibs =
        Stream.iterate(Tuple.of(1, 1), state -> state.map((p1, p2) -> Tuple.of(p2, p1 + p2)))
            .map(Tuple2::_1);
    return allFibs.get(n);
  }
}
