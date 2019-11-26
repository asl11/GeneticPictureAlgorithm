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

import static edu.rice.primes.Primes.FUNCS;
import static edu.rice.primes.Primes.MAX_FOR_FUNC;
import static edu.rice.primes.Primes.REGISTRY;

import edu.rice.util.Log;
import edu.rice.util.Performance;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This code runs our prime number generators, over and over, to ultimately produce a CSV chart of
 * the performance (nanoseconds per prime) and how that changes as the number of primes we're
 * looking for grows.
 */
public class PrimeBenchmarking {
  private static final String TAG = "PrimeBenchmarking";

  // We're going to run these tests over and over, reporting the
  // *best* result of N trials. If you're doing performance profiling,
  // you can probably set this to 1 or 2.
  static final int BEST_OF_N = 10;

  // If you want a run that takes a reasonable amount of time, keep it
  // around 2 million. You can't go any larger than
  // Integer.MAX_VALUE. See the engineering notes below.
  //   static final int MAX_N = 8_000_000;

  // This number, roughly 500 million, will cause the benchmark to run
  // for many hours on a fast multi-core computer.
  static final int MAX_N = Integer.MAX_VALUE >> 2;

  // We're going for a log-log plot, so we're going to ramp up the
  // sizes in an exponential fashion.  Note the extra logic to deal
  // with integer overflow, which can happen with big values of MAX_N.
  static final Seq<Integer> SIZES =
      Stream.iterate(100, x -> (x * 3) / 2).takeWhile(x -> x < MAX_N && x > 0);

  static long nanoSecs(Function<Integer, Seq<Integer>> func, int size) {
    return Performance.nanoBenchmark(
        () -> {
          var ignored = func.apply(size);
        });
  }

  static double nanoSecsPerPrime(Function<Integer, Seq<Integer>> func, int size) {
    return nanoSecs(func, size) / (double) size;
  }

  static double nanoSecsPerPrimeBestOfN(Function<Integer, Seq<Integer>> func, int size) {
    return Stream.rangeClosed(1, BEST_OF_N)
        .map(n -> nanoSecsPerPrime(func, size))
        .foldLeft(Double.MAX_VALUE, (a, b) -> a < b ? a : b); // take min size
  }

  static Seq<Double> perfVsN(Function<Integer, Seq<Integer>> func, int max) {
    return SIZES.takeWhile(x -> x < max).map(size -> nanoSecsPerPrimeBestOfN(func, size));
  }

  static Seq<Tuple2<String, Supplier<Seq<Double>>>> perfPerFunc() {
    return FUNCS.map(
        funcName ->
            Tuple.of(
                funcName,
                () -> {
                  Log.i(TAG, () -> "starting " + funcName);
                  return perfVsN(
                      REGISTRY.apply(funcName),
                      MAX_FOR_FUNC.get(funcName).getOrElse(Integer.MAX_VALUE));
                }));
  }

  /** Runs the benchmark suite, prints CSV suitable for reading into a spreadsheet. */
  public static void main(String[] args) {
    Log.i(TAG, "Prime number generator benchmarks");
    Log.i(TAG, () -> "Available processors: " + Runtime.getRuntime().availableProcessors());

    System.out.println("Size," + SIZES.mkString(","));

    final var runTime =
        Performance.nanoBenchmark(
            () ->
                perfPerFunc()
                    .forEach(
                        kv ->
                            System.out.println(
                                kv._1
                                    + ","
                                    + kv._2
                                        .get()
                                        .map(n -> String.format("%.4f", n))
                                        .mkString(","))));

    System.out.printf("Total runtime: %.3fs\n", 1e-9 * runTime);
  }
}
