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

package edu.rice.primes;

import static edu.rice.primes.Primes.REGISTRY;
import static edu.rice.primes.Primes.boolArrayToIntSeq;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.function.Function;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Primality testers with various different runtime performance. These turn out to be an excellent
 * stress test on the lazy list system, because if it's not actually lazy, these computations will
 * run big-O slower. As you crank up the size of the search, you can also see how the big-O
 * complexity drives the wall-clock time for each algorithm. You may find the results surprising!
 */
public class PrimalityTest {
  private static final String TAG = "PrimalityTest";

  /**
   * Container for a mutable counter value, used in some of our tests to verify laziness and/or
   * correct big-O behavior.
   */
  static class Counter {
    public int counter = 0;

    public Counter() {}
  }

  private static void basicTests(String name, Function<Integer, Seq<Integer>> primeFunc) {
    Log.i(TAG, () -> "checking " + name);
    assertEquals(List.empty(), primeFunc.apply(1));
    assertEquals(List.of(2), primeFunc.apply(2));
    assertEquals(List.of(2, 3, 5, 7, 11), primeFunc.apply(12));
    assertEquals(List.of(2, 3, 5, 7, 11, 13, 17, 19, 23), primeFunc.apply(23));
    assertEquals(List.of(2, 3, 5, 7, 11, 13, 17, 19, 23), primeFunc.apply(25));
    assertEquals(
        List.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53), primeFunc.apply(56));
  }

  /**
   * JUnit5 magic that coverts the above basicTests() method, which works on one prime number
   * generator, into a "factory" of tests that do this on every prime number generator, and which
   * will be executed independently.
   */
  @TestFactory
  public Seq<DynamicTest> basicTestsForAll() {
    return REGISTRY
        .keySet()
        .toList()
        .map(name -> dynamicTest(name, () -> basicTests(name, REGISTRY.apply(name))));
  }

  @Test
  public void testLaziness() {
    int maxPrime = 40_000;
    Counter iterations = new Counter();

    int maxFactor = (int) ceil(sqrt(maxPrime));

    // Here's an excerpt from one of the O(n log n) versions that uses
    // takeWhile(). We want to ensure that only one value in each
    // internal run through the loop is greater than maxPrime, which
    // will cause takeWhile() to truncate that list.  If the system
    // were to have run the map() on its entire input first, then the
    // iteration counter would be much higher.

    // We could have done this without the Counter, instead using
    // Mockito, but then we wouldn't have the nice lexical closures of
    // the lambda, wherein the inner lambda can still see i from the
    // outer lambda. A Mockito version would have been much uglier,
    // and worse, would be sufficiently *different* that a laziness
    // issue in the "real" code versus the "mock" code might be hard
    // to spot.

    var outerProduct =
        Stream.rangeClosed(2, maxFactor)
            .flatMap(
                i ->
                    Stream.rangeClosed(2, maxPrime)
                        .map(
                            j -> {
                              if (i * j > maxPrime) {
                                // By making queries against the
                                // outerProduct lazy-list then looking
                                // at this counter, we can see how
                                // much of the lazy-list was realized.
                                iterations.counter++;
                              }
                              return i * j;
                            })
                        .takeWhile(n -> n <= maxPrime));

    assertEquals(0, iterations.counter); // initial conditions, nothing evaluated yet...

    // the first time through, i = 2, so once j > maxPrime/2, we
    // should have bumped the counter by only one
    int desiredLength2 = (maxPrime / 2) + 10;
    assertEquals(desiredLength2, outerProduct.take(desiredLength2).length());
    assertEquals(1, iterations.counter);

    // let's do it again for i=3, and we should bump the counter only one more time
    int desiredLength3 = (maxPrime / 3) + 10;
    assertEquals(
        desiredLength2 + desiredLength3,
        outerProduct.take(desiredLength2 + desiredLength3).length());
    assertEquals(2, iterations.counter);

    // now, we're going to run the whole thing out, for which we
    // should increment the counter one time per loop
    var ignored = outerProduct.toList();
    assertEquals(maxFactor - 1, iterations.counter);
  }

  /** Compares every prime number generator to our boring O(n sqrt n) generator. */
  @TestFactory
  public Seq<DynamicTest> testBiggerPrimes() {
    final int MAX = 5000;
    Log.i(TAG, "Testing bigger primes, max(" + MAX + ")");

    // Engineering note: if there's a failure that only happens with
    // larger sizes, we want to induce that.  We're not concerned with
    // *performance*, since that's evaluated in the PrimeBenchmarking
    // class. Here, we just want the maxPrime to be big enough that
    // size-related bugs (e.g., stack overflows) might be exposed, yet
    // small enough that we won't have a significant impact on the
    // overall runtime of our unit test suite.

    var allFuncs = REGISTRY.keySet().toList();

    var result = allFuncs.toMap(name -> name, name -> REGISTRY.apply(name).apply(MAX));

    // Of all our prime number generators, this is the simplest and,
    // thus, most likely to be correct.
    var reference = result.apply("O(n sqrt n)");

    return allFuncs.map(
        funcName -> dynamicTest(funcName, () -> assertEquals(reference, result.apply(funcName))));
  }

  @Test
  public void testBoolArrayToIntSeq() {
    final boolean[] allTrue = {true, true, true, true, true};
    final boolean[] allFalse = {false, false, false};
    final boolean[] mixed = {true, false, true, false};

    // Notably, boolArrayToIntSeq ignores its first two arguments, which are presumed to
    // correspond to the numbers 0 and 1. The first prime happens in place #2.
    assertEquals(List.of(2, 3, 4), boolArrayToIntSeq(allTrue, true, 10));
    assertEquals(List.of(2), boolArrayToIntSeq(allTrue, true, 2));
    assertEquals(List.of(2), boolArrayToIntSeq(allFalse, false, 2));
    assertEquals(List.empty(), boolArrayToIntSeq(allTrue, false, 10));
    assertEquals(List.of(2), boolArrayToIntSeq(mixed, true, 5));
    assertEquals(List.of(3), boolArrayToIntSeq(mixed, false, 5));
  }
}
