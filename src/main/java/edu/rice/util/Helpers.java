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

import io.vavr.Function2;
import io.vavr.Function3;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/** These helper methods are handy when manipulating functions. */
public interface Helpers {
  /**
   * This helper takes a function with multiple arguments and returns it as a function on the first
   * argument returning another function on the second function.
   *
   * <pre>
   * BiFunction&lt;Integer, Integer, Integer&gt; add = (a, b) -&gt; a + b;
   * Function&lt;Integer, Function&lt;Integer, Integer&gt;&gt; cadd = curry(add);
   * Function&lt;Integer, Integer&gt; threePlus = cadd.apply(3);
   * assertEquals(5, threePlus.apply(2));
   * </pre>
   *
   * <p>More on currying: <a
   * href="https://en.wikipedia.org/wiki/Currying">https://en.wikipedia.org/wiki/Currying</a>
   *
   * <p>See also, VAVR's {@link Function2#curried()}.
   */
  static <T1, T2, R> Function<T1, Function<T2, R>> curry(BiFunction<T1, T2, R> f) {
    return t1 -> t2 -> f.apply(t1, t2);
  }

  /**
   * This helper takes a function with multiple arguments and returns it as a function on the first
   * argument returning another function on the second function, and so on for the third argument.
   *
   * <p>More on currying: <a
   * href="https://en.wikipedia.org/wiki/Currying">https://en.wikipedia.org/wiki/Currying</a>
   *
   * <p>See also, VAVR's {@link Function3#curried()}
   */
  static <T1, T2, T3, R> Function<T1, Function<T2, Function<T3, R>>> curry(
      TriFunction<T1, T2, T3, R> f) {
    return t1 -> t2 -> t3 -> f.apply(t1, t2, t3);
  }

  /**
   * This helper converts from curried form (as output by {@link #curry(BiFunction)} back to a
   * standard Java {@link BiFunction}.
   *
   * <p>VAVR doesn't have an equivalent function.
   */
  static <T1, T2, R> BiFunction<T1, T2, R> uncurry2(Function<T1, Function<T2, R>> f) {
    return (t1, t2) -> f.apply(t1).apply(t2);
  }

  /**
   * This helper converts from curried form (as output by {@link #curry(BiFunction)} back to a
   * standard Java {@link BiFunction}.
   *
   * <p>VAVR doesn't have an equivalent function.
   */
  static <T1, T2, T3, R> TriFunction<T1, T2, T3, R> uncurry3(
      Function<T1, Function<T2, Function<T3, R>>> f) {
    return (t1, t2, t3) -> f.apply(t1).apply(t2).apply(t3);
  }

  /**
   * This helper is a shorthand for the common case where you need to compute some value, and then
   * return some complicated function of that value where you want to reuse that computed value more
   * than once. For example, where you might otherwise write code to say
   *
   * <pre>
   * return someList.map(x -&gt; f(x) * f(x));
   * </pre>
   *
   * <p>but <code>f(x)</code> is expensive, you'll instead typically save the result in a variable
   * with uglier code like
   *
   * <pre>
   * return someList.map(x -&gt; {
   *   var y = f(x);
   *   return y * y;
   * });
   * </pre>
   *
   * <p>Now you can write:
   *
   * <pre>
   *   return someList.map(x -&gt; with(f(x), y -&gt; y * y));
   * </pre>
   *
   * <p>and the parameter to the lambda, <code>y</code>, will have the result of <code>f(x)</code>,
   * and the body of the lambda, <code>y * y</code>, will be executed and the result ultimately
   * returned.
   */
  static <T, R> R with(T val, Function<T, R> f) {
    return f.apply(val);
  }

  /**
   * This helper, much like {@link #with}, allows you to compute a value once, and then reuse it
   * multiple times, while ultimately returning that value again. As an example, consider the
   * insertion of print statements into a lambda, where you might normally write something like
   *
   * <pre>
   * return someList.map(x -&gt; {
   *     var y = f(x);
   *     System.out.println(x + " -&gt; " + y);
   *     return y;
   * })
   * </pre>
   *
   * <p>Now you can write:
   *
   * <pre>
   * return someList.map(x -&gt; also(f(x), y -&gt; System.out.println(x + " -&gt; " + y)));
   * </pre>
   *
   * <p>where the value in the first parameter is passed to all of the {@link Consumer} lambdas in
   * sequence and then returned.
   */
  @SafeVarargs
  static <T> T also(T val, Consumer<T>... consumers) {
    for (var consumer : consumers) {
      consumer.accept(val);
    }

    return val;
  }

  /**
   * This version of also works similarly to {@link #also(Object, Consumer[])}, except rather than
   * calling the lambdas with the val from the first argument, each lambda takes no arguments and
   * returns nothing. This has proven useful on occasion when the lambda parameters would have
   * otherwise been ignored anyway.
   */
  static <T> T also(T val, Runnable... runners) {
    for (var runner : runners) {
      runner.run();
    }

    return val;
  }

  // Engineering notes: With and Also are very small, and we never
  // really *need* them. They're really just a bit of "sugar" to make
  // other methods cleaner. They might even have a significant
  // performance cost. We're *hoping* that the Java optimizer sees
  // these things in use and rewrites the code to avoid allocating
  // lambda objects and calling them. But if the JVM isn't smart
  // enough to do that, then we could have a real performance
  // impact. So why do it anyway?

  // 1) Because low-level optimization of performance is not a goal in
  // Comp215. It's useful. It's fun, but it's not our goal. Our goal
  // is clean and readable code.

  // 2) Because in other languages closely related to Java, like
  // Kotlin, you can write little functions like this and declare them
  // to be "inline", which *requires* the compiler to do the necessary
  // rewriting and performance optimization. Kotlin has a whole family
  // of little functions like this (see links below), and they make
  // code cleaner and simpler.

  // https://kotlinlang.org/docs/reference/scope-functions.html
}
