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

import static edu.rice.stream.StreamHelpers.intStreamToList;
import static io.vavr.collection.Map.entry;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

import edu.rice.util.Log;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * These static functions give you lists of prime numbers. They're useful both for testing list
 * laziness (which several of them rely on for performance) and for general-purpose performance
 * observations.
 */
public class Primes {
  // Here are all the prime-number generation functions that we
  // support, in a convenient registry

  static final Map<String, Function<Integer, Seq<Integer>>> REGISTRY =
      HashMap.ofEntries(
          entry("O(n sqrt n)", Primes::primesTrialDivision),
          entry("O(n sqrt n) PARALLEL", Primes::primesTrialDivisionParallel),
          entry("O(n sqrt n) PARALLEL2", Primes::primesTrialDivisionParallel2),
          entry("O(n sqrt n / log n) PARALLEL", Primes::primesTrialDivisionTwoPhaseParallel),
          entry("O(n^2 / (log n)^2)", Primes::primesFakeEratosthenes),
          entry("O(n log log n)", Primes::primesEratosthenes),
          entry("O(n log log n) OPT", Primes::primesEratosthenesStorageOpt),
          entry("O(n log log n) SORENSON", Primes::primesSorenson),
          entry("O(n log n) NO LISTS", Primes::primesOuterProductNoLists),
          entry("O(n log n) PARALLEL", Primes::primesOuterProductParallel),
          entry("O(n log n) PARA NOLST", Primes::primesOuterProductParallelNoLists),
          entry("O(n log n)", Primes::primesOuterProduct),
          entry("O(n log n) HAMT", Primes::primesOuterProductHamtSet),
          entry("O(n log^2 n)", Primes::primesOuterProductRedBlackTreeSet),
          entry("O(n^2 log n)", Primes::primesOuterProductWithLists));

  // Some of these algorithms are slower than others. When doing
  // benchmarking or "big" tests, this mapping offers suggestions for
  // problem sizes that you'd rather not exceed.
  static final Map<String, Integer> MAX_FOR_FUNC =
      HashMap.of(
          "O(n log n) PARALLEL", 5_000_000,
          "O(n sqrt n)", 5_000_000,
          "O(n log n)", 200_000,
          "O(n log n) HAMT", 50_000,
          "O(n^2 / (log n)^2)", 50_000,
          "O(n log^2 n)", 50_000,
          "O(n^2 log n)", 5_000);

  // In the order that we'd like to report benchmark results.
  static final Seq<String> FUNCS =
      List.of(
          "O(n log log n) SORENSON",
          "O(n^2 log n)",
          "O(n^2 / (log n)^2)",
          "O(n log^2 n)",
          "O(n log n)",
          "O(n log n) HAMT",
          "O(n log n) PARALLEL",
          "O(n log n) NO LISTS",
          "O(n log n) PARA NOLST",
          "O(n sqrt n)",
          //   "O(n sqrt n) PARALLEL",    // don't run this one normally, use PARALLEL2 instead
          "O(n sqrt n) PARALLEL2",
          "O(n sqrt n / log n) PARALLEL",
          "O(n log log n)",
          "O(n log log n) OPT");

  // Engineering notes: you may wonder why we're doing everything with
  // Integer rather than Long values, especially since we're dancing
  // so close to Integer.MAX_VALUE. Turns out, Java doesn't support
  // array sizes larger than Integer.MAX_VALUE entries. You can't have
  // a contiguous array bigger than that! Yeah, sure, we could
  // simulate it, probably most easily with an array of arrays and
  // some logic in front to do getters and setters. Our benchmark runs
  // long enough, as-is, without needing to work on larger problem-set
  // sizes. Also, if we were really serious about performance, we'd do
  // bit-vectors rather than arrays of boolean. And if we were
  // *really* serious, we'd consider some form of *compressed*
  // bitmaps, since as we get to larger primes, they're spaced out
  // more.  That would cost more CPU time per query, to manage the
  // bitmap, but we might get better memory cache behavior, more than
  // making up for the added CPU cost.

  // Further reading on compressed bitmap data structures:
  //    https://github.com/RoaringBitmap/RoaringBitmap
  //    https://github.com/lemire/javaewah

  // don't instantiate this class
  private Primes() {}

  /**
   * List of all primes from 1 to maxPrime. O(n sqrt n). Tests each number individually for
   * primality. Very simple. This algorithm is sometimes called "trial division".
   */
  public static Seq<Integer> primesTrialDivision(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    // Note: we don't strictly need to "force" the output here, but
    // we're trying to make our benchmarks fair, so we're trying to
    // make sure that we do all the work here, rather than delaying it
    // until later.
    return Stream.rangeClosed(2, maxPrime).filter(Primes::isPrime).toList();
  }

  /** Searches factors to determine if query is prime. Runs in O(sqrt(n)) time. */
  public static boolean isPrime(int n) {
    if (n < 2) {
      return false;
    } else if (n < 4) {
      return true;
    } else if ((n & 1) == 0) {
      return false;
    }

    var max = (int) ceil(sqrt(n));

    for (var i = 3; i <= max; i += 2) {
      if (n % i == 0) {
        return false;
      }
    }

    return true;
  }

  /**
   * Searches factors to determine if query is prime. Runs in O(sqrt(n)) time, but with parallelism.
   */
  public static boolean isPrimeParallel(int n) {
    if (n < 2) {
      return false;
    } else if (n < 4) {
      return true;
    } else if ((n & 1) == 0) {
      return false;
    }

    var max = (int) ceil(sqrt(n));

    // some inspiration here: https://dzone.com/articles/whats-wrong-java-8-part-vii

    return IntStream.rangeClosed(3, max) // numbers we're going to try
        .parallel()
        .noneMatch(i -> n % i == 0); // if any of them divides evenly into n, it's not prime
  }

  /**
   * List of all primes from 1 to maxPrime. O(n^2 log n). This version uses the outer product
   * strategy, but keeps its primes in a list, which costs O(n) each time for a lookup, thus the
   * awful performance.
   */
  public static Seq<Integer> primesOuterProductWithLists(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    // We're only using the numbers less than sqrt(max) because for
    // any composite number in [1,maxPrime], there will exist a factor
    // less than or equal to sqrt(max). Note that we're taking the
    // "ceiling" of the sqrt rather than the "floor" out of a sense of
    // paranoia. Example: what if maxPrime was 25, and sqrt(25) gave
    // us 4.999999? Safer to round up than down.
    var maxFactor = (int) ceil(sqrt(maxPrime));

    var numRange = Stream.rangeClosed(2, maxPrime);
    var outerProduct =
        Stream.rangeClosed(2, maxFactor)
            .flatMap(i -> numRange.map(j -> i * j).takeWhile(n -> n <= maxPrime));

    // Engineering notes: it's essential that we use lazy lists rather
    // than eager lists, which ensures that the takeWhile() part will
    // terminate our search once the products we're looking at are
    // greater than maxPrime.  If we did this with eager lists, we'd
    // blow well past products > n, and waste a ton of work with the
    // takeWhile() throwing away results beyond maxPrime that we
    // didn't care about. That's a nice optimization.

    // The outer product here has runtime O(n sqrt n), without the
    // takeWhile(), but it's going to be smaller than that because the
    // takeWhile() will terminate each search once the results are >N
    // (and thus we don't need them).

    // How does this exactly improve the big-O complexity? Think about
    // a number like 16. We could arrive with that by computing 2*8 or
    // 4*4, so composite numbers like 16 that can appear potentially
    // multiple times in the list, so long as one of the factors is
    // less than sqrt(n). So then how big is the outerProduct list?
    // It's certainly bounded by O(n sqrt n). Now think about a number
    // that's a power of two. How many ways are there to factor such a
    // number into a pair of composites? If the number is 2^n, then we
    // have n factorizations. Stated slightly differently, the number
    // 2^n will appear in the outerProduct at most n times. This
    // generalizes to other numbers, so we can also derive an upper
    // bound on outerProduct of O(n log n). log n is smaller than sqrt
    // n, so O(n log n) wins.

    // Consequently, the code above is O(n log n) runtime, and O(n log n) list length.

    // The filter, below, is then n passes over the list above, thus
    // the total run time of O(n^2 log n).  We'll get smarter in
    // subsequent prime number sieves! Also, note that we convert
    // the resulting stream to a list only to ensure that when we
    // benchmark this function, we're actually benchmarking the
    // full cost of generating all the primes.

    return numRange.filter(i -> !outerProduct.contains(i)).toList();
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Follows an outer-product strategy, using a
   * HAMT to track prior primes, so perhaps more expensive than the boolean arrays we use elsewhere.
   */
  public static Seq<Integer> primesOuterProductHamtSet(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var numRange = Stream.rangeClosed(2, maxPrime); // list of integers from 2..maxPrime

    var outerProduct =
        HashSet.ofAll(
            Stream.rangeClosed(2, maxFactor)
                .flatMap(i -> numRange.map(j -> i * j).takeWhile(n -> n <= maxPrime)));

    // Engineering notes: basically the same as above, but we also
    // avoid the linear scans of the list, using the HAMT, with its
    // constant-time queries instead.

    // Big-O, we're doing O(n log n) inserts, as above, but duplicates
    // will be ignored, so the size of the set is just O(n), with
    // constant work for each insert.

    // Total runtime cost is O(n log n)

    return numRange.filter(i -> !outerProduct.contains(i)).toList();
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log^2 n). Follows an outer-product strategy, using a
   * red-black tree to track prior primes, so somewhat more expensive than the boolean arrays we use
   * elsewhere.
   */
  public static Seq<Integer> primesOuterProductRedBlackTreeSet(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var numRange = Stream.rangeClosed(2, maxPrime);

    var outerProduct =
        TreeSet.ofAll(
            Stream.rangeClosed(2, maxFactor)
                .flatMap(i -> numRange.map(j -> i * j).takeWhile(n -> n <= maxPrime)));

    // Engineering notes: basically the same as above, but using
    // a red-black tree rather than a HAMT. Operations are O(log n)
    // rather than O(1).

    // Big-O, we're doing O(n log n) inserts, as above, but duplicates
    // will be ignored, so the size of the set is just O(n), but it's
    // going to take O(log n) work for each insert. Set creation cost
    // is O(n log^2 n).  Subsequently, we're doing N queries of cost
    // O(log n).

    // Total runtime cost is O(n log^2 n)

    return numRange.filter(i -> !outerProduct.contains(i)).toList();
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n) following an "outer product" strategy. Feel
   * the power of mutation!
   */
  public static Seq<Integer> primesOuterProductNoLists(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var notPrimes = new boolean[maxPrime + 1]; // start off false

    for (var i = 2; i <= maxFactor; i++) {
      for (var j = 2; j <= maxPrime; j++) {
        var product = i * j;
        if (product > maxPrime) {
          break; // breaks the j-loop, continues the i-loop
        }
        notPrimes[product] = true;
      }
    }

    return boolArrayToIntSeq(notPrimes, false, maxPrime);
  }

  /** List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation! */
  public static Seq<Integer> primesOuterProduct(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var numRange = Stream.rangeClosed(2, maxPrime); // lazy list of integers from 2..maxPrime
    var numFactor = Stream.rangeClosed(2, maxFactor); // lazy list of integers from 2..maxFactor
    var notPrimes = new boolean[maxPrime + 1]; // these start off false

    numRange
        .flatMap(i -> numFactor.map(j -> i * j).takeWhile(product -> product <= maxPrime))
        .forEach(x -> notPrimes[x] = true);

    // Total runtime cost is O(n log n).

    return boolArrayToIntSeq(notPrimes, false, maxPrime);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation and parallel
   * streams!
   */
  public static Seq<Integer> primesOuterProductParallel(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var numRange = Stream.rangeClosed(2, maxPrime); // lazy list of integers from 2..maxPrime
    var notPrimes = new boolean[maxPrime + 1]; // initially false

    // Engineering note: For maximum speed, we're using IntStream's
    // rangeClosed to give us a native parallel stream which we'll
    // operate on. The forEach part then is regular sequential code.
    // A method like .takeWhile() is only meaningful when you have
    // an ordered list, and a parallel stream isn't ordered, so
    // there's no equivalent thing except for a .filter(), for which,
    // despite the parallelism, we'd still end up doing more work.

    IntStream.rangeClosed(2, maxFactor)
        .parallel() // we have to state this explicitly
        .forEach(
            i ->
                numRange // from here on down, we're operating on a lazy list of ints
                    .map(j -> i * j)
                    .takeWhile(n -> n <= maxPrime)
                    .forEach(k -> notPrimes[k] = true));

    // Total runtime cost is still O(n log n), same as the original primesOuterProduct.

    return boolArrayToIntSeq(notPrimes, false, maxPrime);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n log n). Feel the power of mutation and parallel
   * streams, with this version carefully engineered to get rid of all the Seq stuff that makes the
   * previous version slightly more readable.
   */
  public static Seq<Integer> primesOuterProductParallelNoLists(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var notPrimes = new boolean[maxPrime + 1]; // initially false

    IntStream.rangeClosed(2, maxFactor)
        .parallel() // we have to state this explicitly
        .forEach(
            i -> {
              for (int j = 2; j <= maxPrime; j++) {
                int product = i * j;
                if (product > maxPrime) {
                  break;
                }
                notPrimes[product] = true;
              }
            });

    // Total runtime cost is still O(n log n), same as the original primesOuterProduct.

    return boolArrayToIntSeq(notPrimes, false, maxPrime);
  }

  /** List of all primes from 1 to maxPrime. O(n sqrt n) with parallelism. */
  public static Seq<Integer> primesTrialDivisionParallel(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    return Stream.rangeClosed(2, maxPrime).filter(Primes::isPrimeParallel).toList();
  }

  /** List of all primes from 1 to maxPrime. O(n sqrt n) with parallelism. */
  public static Seq<Integer> primesTrialDivisionParallel2(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    // Engineering note: let's compare primesTrialDivisionParallel
    // vs. primesTrialDivisionParallel2.  The former has a *sequential* list
    // of numbers, each of which is tested for primality using a
    // *parallelized* test. The latter has a *parallel* list of
    // numbers, each of which is tested using a *sequential* test. We
    // would expect the latter design (i.e., the code right below
    // here) to run radically faster since the tasks being performed
    // are relatively heavyweight and are completely independent of
    // one another. This is, indeed, what happens, which is why we're
    // only running the latter during our big benchmark runs. Feel
    // free to play with the settings and compare them yourself.

    return intStreamToList(
        IntStream.rangeClosed(2, maxPrime).parallel().filter(Primes::isPrime), true);
  }

  /**
   * List of all primes from 1 to maxPrime. O(n sqrt n) with parallelism + array output. Unlike our
   * other prime sieves, this one does not output an Seq, which might lead to a significant
   * performance improvement. We use this internally for some of the other sieves.
   */
  public static int[] primesTrialDivisionParallelArrayOutput(int maxPrime) {
    if (maxPrime < 2) {
      return new int[] {}; // array of size zero
    }

    return IntStream.rangeClosed(2, maxPrime).parallel().filter(Primes::isPrime).toArray();
  }

  /**
   * Just like {@link #isPrime(int)}, but only considers the list of known primes as possible
   * factors to check.
   */
  private static boolean isPrimeKnownFactors(int n, int[] knownPrimes) {
    if (n < 2) {
      return false;
    } else if (n == 2) {
      return true;
    }

    var max = (int) ceil(sqrt(n));

    for (int currentPrime : knownPrimes) {
      if (currentPrime > max) {
        return true; // no point looking at anything bigger than sqrt(n)
      }

      if (n % currentPrime == 0) {
        return false;
      }
    }

    // we didn't find a factor, so it must be true
    return true;
  }

  /**
   * The fastest trial-division prime number sieve we've got: O((n sqrt n) / (log n)) with
   * parallelism.
   */
  public static Seq<Integer> primesTrialDivisionTwoPhaseParallel(int maxPrime) {
    // We pre-compute all of the primes below sqrt(maxPrime) and then
    // use that to speedup the primality testing (via
    // isPrimeKnownFactors()). This runs in time O(n^{.75} + n *
    // pi(sqrt n)), where pi(n) is the number of primes less than n.

    // According to the Riemann Hypothesis: pi(n) = O(n / log n).
    // Simplified, this algorithm then runs in
    // O((n sqrt n) / (log n)), which is a non-trivial speedup
    // relative to primesTrialDivisionParallel()'s O(n sqrt n).

    if (maxPrime < 2) {
      return List.empty();
    }

    var maxFactor = (int) ceil(sqrt(maxPrime));

    var primesBelowMaxFactor = primesTrialDivisionParallelArrayOutput(maxFactor);

    return intStreamToList(
        IntStream.concat(
            IntStream.of(primesBelowMaxFactor),
            IntStream.rangeClosed(maxFactor + 1, maxPrime)
                .parallel()
                .filter(n -> isPrimeKnownFactors(n, primesBelowMaxFactor))),
        true);
  }

  /**
   * Uses {@link Stream} parallelism to rapidly convert from an array of bools to a sequence of
   * Integers, corresponding to the indices whose value are equal to the <code>desired</code> value.
   */
  static Seq<Integer> boolArrayToIntSeq(boolean[] input, boolean desired, int maxPrime) {
    return intStreamToList(
        IntStream.rangeClosed(2, input.length - 1)
            .parallel()
            .filter(i -> input[i] == desired)
            .takeWhile(x -> x <= maxPrime),
        true);
  }

  /**
   * The classic Sieve of Eratosthenes. O(n log log n), written in a fully mutating style for
   * maximum speed (and minimum comprehensibility).
   */
  public static Seq<Integer> primesEratosthenes(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    }

    var notPrime = new boolean[maxPrime + 1]; // these start off initialized to false

    var maxFactor = (int) ceil(sqrt(maxPrime));

    // special case for 2, then standard case afterward
    for (var i = 4; i <= maxPrime; i += 2) {
      notPrime[i] = true;
    }

    for (var i = 3; i <= maxFactor; i += 2) {
      if (!notPrime[i]) {
        var skip = 2 * i; // optimization: odd + odd = even, so we can avoid half of the work
        for (var j = i * i; j <= maxPrime; j += skip) {
          notPrime[j] = true;
        }
      }
    }

    return boolArrayToIntSeq(notPrime, false, maxPrime);
  }

  /**
   * An improved Sieve of Eratosthenes. O(n log log n), written in a fully mutating style for
   * maximum speed (and minimum comprehensibility). Internally, we never store entries for even
   * numbers to save space.
   */
  public static Seq<Integer> primesEratosthenesStorageOpt(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    } else if (maxPrime < 3) {
      return List.of(2);
    }

    // Engineering note: To keep this code as close to the original as
    // possible, we're using the two helper functions below, rather
    // than trying to have those expressions appear inline, which
    // would be harder to debug. We're counting on the compiler to
    // notice that it can do the inlining for us. Note the absence of
    // the first for-loop which sets half of the notPrime[] array to
    // true?  We got rid of all those entries, so there's no need to
    // initialize them!

    // these start off initialized to false
    var notPrime = new boolean[intToIndex(maxPrime) + 1];

    var maxFactor = (int) ceil(sqrt(maxPrime));

    for (var i = 3; i <= maxFactor; i += 2) {
      if (!notPrime[intToIndex(i)]) {
        int skip = 2 * i; // optimization: odd + odd = even, so we can avoid half of the work
        for (var j = i * i; j <= maxPrime; j += skip) {
          notPrime[intToIndex(j)] = true;
        }
      }
    }

    // We can't just use boolArrayToIntSeq because our bool array
    // is indexed using the helper methods below.
    var primes =
        IntStream.rangeClosed(1, notPrime.length - 1)
            .parallel()
            .filter(i -> !notPrime[i])
            .map(Primes::indexToInt)
            .takeWhile(n -> n <= maxPrime);

    return intStreamToList(primes, true).prepend(2);
  }

  // Helper functions for primesEratosthenesStorageOpt(). See engineering note, above.
  private static int intToIndex(int i) {
    return i >> 1;
  }

  private static int indexToInt(int index) {
    return index * 2 + 1;
  }

  /**
   * Here's a seemingly simple, and lazy, version that is sometimes mistaken for being a Sieve of
   * Eratosthenes, but is actually much worse. The idea is that we have a lazy list of *all* prime
   * numbers, which starts off as just a lazy list of the integers, and we keep filtering it to
   * remove composites as we discover earlier primes. Runs in O( n^2 / (log n)^2 ).
   */
  public static Seq<Integer> primesFakeEratosthenes(int maxPrime) {
    if (maxPrime < 2) {
      return List.empty();
    } else {
      return nextPrimeFakeEratosthenes(Stream.iterate(2, x -> x + 1))
          .takeWhile(x -> x <= maxPrime)
          .toList(); // otherwise we won't actually do the work!
    }
  }

  private static Stream<Integer> nextPrimeFakeEratosthenes(Stream<Integer> nextPrimes) {
    // Note that this method returns an *infinite* but *lazy* list of all primes. If you think about
    // what it means to "filter" a lazy list, you'll realize that all of these filters will pile
    // up, so every time you ask for the next entry in the list, more and more of these filter
    // lambdas will be executing.
    var head = nextPrimes.head();
    var tail = nextPrimes.tail();
    return Stream.cons(head, () -> nextPrimeFakeEratosthenes(tail.filter(x -> x % head != 0)));
  }

  static class Sorenson {
    // This code is pretty much literally the code from Sorenson's paper, with some tweaks because
    // we're using Java's ArrayList to give us our "array of stacks". Within that, we're using
    // Java's ArrayDeque class to implement each stack. We originally used Seq, but ArrayDeque
    // turns out to be twice as fast, saving us from the cost of allocating new Cons cells as
    // well as saving us from needing to ever need to call t.set().

    private int pos;
    private int n;
    private int r;
    private int s;
    private int delta;
    private final ArrayList<ArrayDeque<Integer>> t;

    Sorenson(int start, int[] p) {
      r = 1 + (int) floor(sqrt(start));
      s = r * r;

      delta = r + 2;

      t = new ArrayList<>();

      for (int i = 0; i < delta; i++) {
        // Java's ArrayLists have to be initialized before they can be used
        t.add(i, new ArrayDeque<>());
      }

      for (int pp : p) {
        if (pp > r) {
          break; // we have more primes in P than we need for this initial setup routine
        }

        var j = (pp - (start % pp)) % pp;
        t.get(j).push(pp);
      }

      pos = 0;
      n = start;
    }

    void logState() {
      Log.iformat("SORENSON", "r(%d), s(%d), delta(%d), pos(%d), n(%d)", r, s, delta, pos, n);
    }

    boolean nextIsPrime() {
      boolean isPrime = true;
      while (!t.get(pos).isEmpty()) {
        // process prime divisors
        var p = t.get(pos).pop();

        var posPlusP = (pos + p) % delta;
        t.get(posPlusP).push(p);
        isPrime = false;
      }

      if (n == s) {
        // n is a square
        if (isPrime) {
          // r is a prime
          var tmp = (pos + r) % delta;
          t.get(tmp).push(r);
          isPrime = false;
        }

        r = r + 1;
        s = r * r;
      }

      n = n + 1;
      pos = (pos + 1) % delta;
      if (pos == 0) {
        // We need to grow the array of stacks.
        t.add(delta, new ArrayDeque<>());
        t.add(delta + 1, new ArrayDeque<>());

        delta = delta + 2;
      }
      return isPrime;
    }

    /** Gets the next prime number from Sorenson's algorithm. Internally mutates state. */
    @SuppressWarnings("StatementWithEmptyBody")
    public int nextPrime() {
      while (!nextIsPrime()) {}
      return n - 1;
    }
  }

  /**
   * Here's an implementation of a compact, incremental prime number sieve from Jonathan Sorenson
   * (2015): <a href="https://arxiv.org/pdf/1503.02592.pdf">Two Compact Incremental Prime Sieves</a>
   * (see section 3, starting on page 4). It does some pre-computation to get things rolling, then
   * runs in O(n log log n) time with O(sqrt(n) * log n) space.
   *
   * <p>Despite having the same runtime complexity as the Sieve of Eratosthenes, it seems to run a
   * factor of 10x slower. What's interesting, nonetheless, is that this returns an infinite, lazy,
   * list of all primes, with nice incremental efficiency guarantees on generating each additional
   * prime.
   */
  public static IntStream primesSorenson() {
    // 100 is a pretty arbitrary place to start, but that's what Sorenson suggests.
    var start = primesTrialDivisionParallelArrayOutput(100);

    var sorenson = new Sorenson(100, start);

    // We're using Java IntStreams, which are reasonably fast at this. Note that
    // there's no way to get parallelism, since the generation process is
    // inherently sequential.
    var initialPrimes = IntStream.of(start).takeWhile(x -> x < 100);
    var sorensonPrimes = IntStream.generate(sorenson::nextPrime);
    return IntStream.concat(initialPrimes, sorensonPrimes);
  }

  /**
   * Just like {@link #primesSorenson()}, except with a maximum prime specified and does all the
   * work eagerly. Suitable for benchmarking.
   */
  public static Seq<Integer> primesSorenson(int maxPrime) {
    return intStreamToList(primesSorenson().takeWhile(n -> n <= maxPrime), true);
  }
}

// Here's a fun paper to read about computing prime numbers and the confusion about what,
// exactly, is a Sieve of Eratosthenes:
// https://www.cs.hmc.edu/~oneill/papers/Sieve-JFP.pdf

// And, here's a fun 22 minute YouTube video on some surprising patterns that show up
// when you make radial plots of prime numbers. You could easily use the code here to
// generate these plots.
// https://www.youtube.com/watch?v=EK32jo7i5LQ
