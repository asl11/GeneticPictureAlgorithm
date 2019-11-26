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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

class FibonacciTest {
  @Test
  void fibClassicTest() {
    testAnyFib(Fibonacci::fib);
  }

  @Test
  void fibLoopTest() {
    testAnyFib(Fibonacci::fibLoop);
  }

  @Test
  void fibTailRecursiveTest() {
    testAnyFib(Fibonacci::fibTailRecursive);
  }

  @Test
  void fibStreamTest() {
    testAnyFib(Fibonacci::fibStream);
  }

  static void testAnyFib(Function<Integer, Integer> fibFunc) {
    assertEquals(1, fibFunc.apply(0));
    assertEquals(1, fibFunc.apply(1));
    assertEquals(2, fibFunc.apply(2));
    assertEquals(3, fibFunc.apply(3));
    assertEquals(5, fibFunc.apply(4));
    assertEquals(8, fibFunc.apply(5));
    assertEquals(13, fibFunc.apply(6));
  }
}
