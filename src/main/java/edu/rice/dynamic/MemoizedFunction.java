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

package edu.rice.dynamic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * MemoizedFunction for use in dynamic programming. You build one of these with a lambda for the
 * function you're trying to evaluate, and it will store all pairs of input/output, so the lambda
 * never get evaluated twice for the same input.
 */
public interface MemoizedFunction {
  /**
   * Given a lambda that maps from inputs to outputs, returns a memoized version of that lambda. If
   * you need recursion, use {@link #ofRecursive(BiFunction)} instead.
   */
  static <T, R> Function<T, R> of(Function<T, R> f) {
    // We make the cache once, and then it's captured by the lambda.
    var cache = new ConcurrentHashMap<T, R>();
    return arg -> safeComputeIfAbsent(cache, arg, f);
  }

  /**
   * The regular {@link Map#computeIfAbsent(Object, Function)} fails if the function is recursive.
   * (See {@link ConcurrentHashMap#computeIfAbsent(Object, Function)}, which states that <i>the
   * computation should be short and simple, and must not attempt to update any other mappings of
   * this map</i>.) To work around this restriction, we'll first check if the map contains the key
   * <code>arg</code> and return the associated value if present. If it's absent, we'll evaluate
   * <code>f</code>, which could well be recursive. Once we have a result, we'll store the mapping
   * from <code>arg</code> to that result in <code>map</code>.
   *
   * <p>When using this as part of a recursive computation, this means that we'll have regular
   * recursion "on the way down" until the recursion bottoms out, and we'll memoized the results on
   * the way back up as the recursion unwinds.
   */
  private static <T, R> R safeComputeIfAbsent(Map<T, R> cache, T arg, Function<T, R> f) {
    // Advanced engineering notes (YOU'RE NOT EXPECTED TO UNDERSTAND
    // THIS FOR COMP215):

    // Why ConcurrentHashMap? Why are we worried about concurrency?
    // Because even though everything we write in Comp215 is
    // sequential code, we sometimes have parallelism anyway, often
    // because we've got a web server servicing requests in parallel
    // from the browser. Our #1 goal then is to make sure that nothing
    // "surprising" happens here. Probably the biggest surprise we
    // want to avoid at all costs is that one thread comes through
    // here, computes a result missing from the Map, and saves it, and
    // that new mapping is not observed by a second thread that comes
    // through afterward.  Could that happen? Unfortunately, with the
    // way the Java memory model is defined, the answer is "perhaps".
    // Side-effects from one thread are only *guaranteed* to be
    // visible to other threads when locking is used, or when the
    // variable in question is marked "volatile" (forcing a write from
    // one thread to be immediately propagated to other threads).

    // By using a ConcurrentHashMap, we're guaranteed that all the
    // necessary work to ensure visibility of side-effects is done for
    // us. But, we still haven't solved one other issue: what if two
    // threads arrive here, simultaneously, and both of them are
    // trying to evaluate f.apply(arg) for the same exact argument
    // which isn't yet in the map?

    // The way we've written the code, both will discover its absence,
    // and both will then proceed to compute the same value at the
    // same time. This seems wasteful, but because we're coding
    // everything functionally (no side effects, no mutation!), we're
    // at least guaranteed they'll get the same answer. The first one
    // to complete will have its result memoized. The second one to
    // complete will have no side-effects on the cache and will return
    // whatever result it got.

    // Even more advanced: what if we were in a multithreaded world,
    // and we wanted to ensure that f was called *exactly* once for
    // any given input argument? Needless to say, that would require
    // leaving behind some kind of a marker to say "we're working on
    // it", so the first thread could get crunching on the computation
    // while the second thread would block until it was done.  That
    // means needing a mechanism for one thread to notify the other.
    // Even more complicated, we have to make sure we never have
    // circular dependencies, such that we have two threads each
    // waiting for the other one to compute a value it needs. That's
    // called "deadlock". Needless to say, addressing deadlocks gets
    // us *way* beyond the scope of what Comp215 tries to teach, but
    // you might want to read up on this on your own.

    // How might you do it? See these relevant classes from VAVR as
    // well as the Java Collections classes:
    // - io.vavr.concurrent.Future
    // - io.vavr.concurrent.Promise
    // - java.util.concurrent.CompletableFuture

    // More on the underlying theory of promises and futures:
    // https://en.wikipedia.org/wiki/Futures_and_promises

    // VAVR has its own memoized function support, which is roughly
    // the same as the code below in terms of how it works.
    // See: io.vavr.Function1

    if (cache.containsKey(arg)) {
      return cache.get(arg);
    }

    R result = f.apply(arg);
    cache.putIfAbsent(arg, result);
    return result;
  }

  /**
   * If you want to memoize a recursive function, then your function needs something to call to make
   * the recursion happen. So, rather than using a lambda that maps from one input to one output,
   * here we instead expect a lambda that takes two arguments, the first of which will be something
   * you can call when you want to be recursive. The second argument is the usual argument to your
   * function.
   *
   * <p>As an example, say you were using this to implement a memoized Fibonacci function, you could
   * define that function like so:
   *
   * <pre>
   * Function&lt;Long, Long&gt; memoFibonacci =
   *     MemoizedFunction.ofRecursive((self, n) -&gt; {
   *
   *   if (n &lt; 2) {
   *     return 1L;
   *   } else {
   *     return self.apply(n - 1) + self.apply(n - 2);
   *   }
   * });
   * </pre>
   *
   * <p>Calls to <code>memoFibonacci</code> will run in linear time because the underlying recursive
   * calls are memoized, versus exponential time for a naïve Fibonacci implementation.
   *
   * <p>More on Leonardo Bigollo (a.k.a., Fibonacci):
   * https://www.bbvaopenmind.com/en/fibonacci-and-his-magic-numbers/
   */
  static <T, R> Function<T, R> ofRecursive(BiFunction<Function<T, R>, T, R> f) {
    return new Recursive<>(f);
  }

  class Recursive<T, R> implements Function<T, R> {
    private final BiFunction<Function<T, R>, T, R> f;
    private final Map<T, R> cache = new ConcurrentHashMap<>();

    private Recursive(BiFunction<Function<T, R>, T, R> f) {
      this.f = f;
    }

    @Override
    public R apply(T arg) {
      // Engineering note: "this" is a recursive reference back to
      // exactly this same method, because this class implements the
      // Function interface, which goes to the apply() method, right
      // here. Rather than getting our cache map from the lexical
      // scope, as we did in with of(), we instead get the cache map
      // from the current instance of the Recursive class.

      return safeComputeIfAbsent(cache, arg, x -> f.apply(this, x));
    }
  }

  // Advanced engineering notes (YOU'RE NOT EXPECTED TO UNDERSTAND
  // THIS FOR COMP215 from here to the bottom of the file):

  // Notice how when we implemented MemoizedFunction.of(), we didn't
  // have to do anything involving Java classes? We just created a new
  // lambda and returned it. That lambda "captured" the map which we
  // use to cache the results of the lambda so we didn't have to
  // explicitly create a member variable. Simple. Elegant!

  // Doing this for MemoizedFunction.ofRecursive() would be harder.
  // We'd still need a way to create the recursive lambda's "self"
  // parameter. We got around this problem by creating an instance of
  // 'class Recursive' to hold the original lambda and the map cache
  // as well as provide the apply() method that *is* the new lambda
  // for the recursive calls that passes along the "self"
  // function. This gets the job done, but it's not exactly elegant.
  // Surely there's a better way.

  // Say hello to the Y Combinator! Here are two blog posts that
  // explain this, using JavaScript syntax, but you shouldn't have too
  // much trouble. The first blog post is a bit more readable, and the
  // second post is a bit deeper.
  // https://medium.com/@dkeout/why-you-must-actually-understand-the-%CF%89-and-y-combinators-c9204241da7a
  // http://matt.might.net/articles/implementation-of-recursive-fixed-point-y-combinator-in-javascript-for-memoization/

  // In short, when you have the Y combinator, here rigged up to also
  // do the caching/memoization that we want, the Y combinator is
  // equivalent to what we did above with the Recursive class, except
  // we again take advantage of Java's lexical scope, and the Y
  // combinator does all the recursion for us. No more need for 'class
  // Recursive'.

  // Before you try to read the code for yCached(), check out
  // yUncached(), at the bottom, which is basically the same thing but
  // without the extra complexity of maintaining the memoization cache.

  // yUncached() shows you more clearly how the Y combinator works.
  // Why doesn't yUncached() have an infinite loop? Because the
  // recursive call is delayed inside the lambda, only expanding when
  // the original function, f, calls self.apply(), and then it
  // only expands once.

  // Once you understand yUncached(), you're ready to read the code
  // for yCached(), which uses our safeComputeIfAbsent() helper to
  // handle the caching. And *then*, go back and read those two blog
  // posts and dig deeper into Haskell Curry's cool mathematical
  // invention. (Perhaps unsurprisingly, the "Haskell" programming
  // language borrowed its name from Haskell Curry and we also talk
  // about "currying" a function -- see e.g.,
  // io.vavr.Function2.curried().)
  // https://en.wikipedia.org/wiki/Haskell_Curry

  // And, yes, the the venture-capital organization "Y Combinator"
  // also got its name from this.
  // https://en.wikipedia.org/wiki/Y_Combinator

  private static <T, R> Function<T, R> yCached(
      BiFunction<Function<T, R>, T, R> f, Map<T, R> cache) {
    return arg -> safeComputeIfAbsent(cache, arg, x -> f.apply(y -> yCached(f, cache).apply(y), x));
  }

  /**
   * Works in a similar fashion to {@link #ofRecursive(BiFunction)}, using a Y combinator for its
   * implementation.
   */
  static <T, R> Function<T, R> yCached(BiFunction<Function<T, R>, T, R> f) {
    return yCached(f, new ConcurrentHashMap<>());
  }

  /**
   * Works in a similar fashion to {@link #ofRecursive(BiFunction)}, using a Y combinator for its
   * implementation. <b>This function does not do memoization, it's only for demonstration.</b>
   */
  static <T, R> Function<T, R> yUncached(BiFunction<Function<T, R>, T, R> f) {
    return arg -> f.apply(y -> yUncached(f).apply(y), arg);
  }
}
