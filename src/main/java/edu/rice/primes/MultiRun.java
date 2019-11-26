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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.rice.util.Strings;
import io.vavr.collection.Stream;

/**
 * This class runs a requested prime-sieve benchmark a given number of times with a given size and
 * reports the nanoseconds for each run. Useful for understanding the distribution of performance
 * over multiple runs.
 */
public class MultiRun {
  // Engineering note: Parsing Unix-style command-line arguments can
  // chew up a lot of code, and nobody wants to write that every
  // time. Instead, there are a ton of libraries out there that do it
  // for you.  This particular library, JCommander, leverages Java
  // "annotations" (those @Parameter fields).  JCommander declares
  // these annotations (you can hit command-B to follow them into the
  // JCommander source code) and it then inspects the object passed to
  // it to look for these annotations. We don't really talk about how
  // annotations work, in Comp215, but this is a good example of how
  // handy they can be.

  // JCommander home page: http://jcommander.org/

  static class Params {
    @Parameter(
        names = {"--sizes", "-s"},
        required = true,
        description = "Comma-separated list of problem sizes to run")
    String sizes;

    @Parameter(
        names = {"--runs", "-r"},
        required = true,
        description = "Number of runs to conduct")
    int runs;

    @Parameter(
        names = {"--benchmark", "-b"},
        required = true,
        description = "Benchmark to run (e.g., \"O(n sqrt n / log n) PARALLEL\")")
    String benchmark;
  }

  /**
   * Entry point for starting benchmarks from the command-line. Supported parameters include <code>
   * --sizes</code> to specify a comma-separated list of problem sizes, <code>--runs</code> to
   * specify the number of runs to conduct at each size, and <code>--benchmark</code> to specify
   * which benchmark to run, using the benchmark names that can be found in {@link Primes#REGISTRY}.
   */
  public static void main(String... args) {
    var params = new Params();
    JCommander.newBuilder().addObject(params).build().parse(args);

    var benchmark =
        Primes.REGISTRY
            .get(params.benchmark)
            .getOrElse(
                () -> {
                  System.out.printf(
                      "Unknown benchmark: %s\nAvailable benchmarks are:\n", params.benchmark);
                  Primes.FUNCS.forEach(name -> System.out.printf("  %s\n", name));
                  System.exit(1);
                  throw new RuntimeException("Won't happen");
                });

    if (params.runs < 1) {
      System.out.println("Number of runs must be positive");
      System.exit(1);
    }

    // We're using Java's string splitting. JCommander has its own way
    // of doing it, but this is a bit more concise. If one of the sizes
    // isn't an integer, it will be quietly ignored.
    var sizes = Stream.of(params.sizes.split(",")).flatMap(Strings::stringToOptionInteger);

    System.out.println("Benchmark: " + params.benchmark);
    System.out.println("Sizes: " + sizes.mkString(", "));
    System.out.println("Number of runs: " + params.runs);
    System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

    System.out.println("Run#,Benchmark,Size,Time(ns),Time per prime (ns)");
    Stream.range(0, params.runs)
        .forEach(
            i ->
                sizes.forEach(
                    size -> {
                      var nanoSecs = PrimeBenchmarking.nanoSecs(benchmark, size);
                      System.out.printf(
                          "%d,%s,%d,%d,%.3f\n",
                          i, params.benchmark, size, nanoSecs, nanoSecs / (double) size);
                    }));
  }
}
