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

package edu.rice.week5knapsack;

import static io.vavr.collection.List.empty;
import static java.lang.Integer.max;

import edu.rice.dynamic.MemoizedBiFunction;
import edu.rice.util.Log;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import java.util.function.BiFunction;

public class Knapsack {
  private static final String TAG = "Knapsack";

  // Problem definition: In the knapsack problem we are given a set of
  // n items, where each item i is specified by a size s_i and a value
  // v_i. We are also given a size bound S (the size of our knapsack).
  // The goal is to find the subset of items of maximum total value
  // such that sum of their sizes is at most S (i.e., they all fit into
  // the knapsack).

  // Useful lecture notes:
  // https://www.cs.cmu.edu/~avrim/451f09/lectures/lect1001.pdf

  // Pseudocode from the lecture notes:

  // Value(n,S) // S = space left, n = # items still to choose from
  // {
  //   if (n == 0) return 0;
  //   if (s_n > S) result = Value(n-1,S); // can’t use nth item
  //   else result = max{v_n + Value(n-1, S-s_n), Value(n-1, S)};
  //   return result;
  // }

  private final BiFunction<Integer, Integer, Integer> memoizedComputeValue;
  private final int[] sizes;
  private final int[] values;

  /**
   * Creates an instance of a Knapsack solver for a given array of sizes and values, which must be
   * of the same length.
   */
  public Knapsack(int[] sizes, int[] values) {
    if (sizes.length != values.length) {
      throw new RuntimeException("sizes and values are not the same length");
    }

    Log.iformat(TAG, "initializing Knapsack with %d items", sizes.length);

    // having the class member variables aren't necessary for the
    // lambda below, since it can capture the constructor arguments
    // from lexical context, but they're necessary for computing the
    // function knapsackContents() below.
    this.sizes = sizes;
    this.values = values;

    memoizedComputeValue =
        MemoizedBiFunction.ofRecursive(
            (self, maxIndex, spaceLeft) -> {
              if (maxIndex <= 0 || spaceLeft <= 0) {
                return 0;
              } else if (sizes[maxIndex - 1] > spaceLeft) {
                return self.apply(maxIndex - 1, spaceLeft);
              } else {
                int valueWithCurrent =
                    values[maxIndex - 1]
                        + self.apply(maxIndex - 1, spaceLeft - sizes[maxIndex - 1]);
                int valueWithoutCurrent = self.apply(maxIndex - 1, spaceLeft);
                return max(valueWithCurrent, valueWithoutCurrent);
              }
            });
  }

  /**
   * For a given set of sizes and values, initialized with the constructor, this will return the
   * highest possible total value of any items whose sizes sum up to at most totalSpace.
   */
  public int computeValue(int totalSpace) {
    return computeValue(sizes.length, totalSpace);
  }

  private int computeValue(int maxIndex, int totalSpace) {
    return memoizedComputeValue.apply(maxIndex, totalSpace);
  }

  /**
   * For a given set of sizes and values, initialized with the constructor, this will return a set
   * of pairs of sizes and values corresponding to the highest possible value of any items whose
   * sizes sum up to at most totalSpace. The ordering of (size, value) pairs in the result will be
   * the same as the ordering in the original arrays passed to the Knapsack constructor.
   */
  public Seq<Tuple2<Integer, Integer>> knapsackContents(int totalSpace) {
    return knapsackContents(sizes.length, totalSpace).reverse();
  }

  private Seq<Tuple2<Integer, Integer>> knapsackContents(int maxIndex, int totalSpace) {
    // From the lecture notes:

    // So far we have only discussed computing the value of the
    // optimal solution. How can we get the items? As usual for
    // Dynamic Programming, we can do this by just working backwards:
    // if arr[n][S] = arr[n-1][S] then we didn’t use the nth item so
    // we just recursively work backwards from arr[n-1][S]. Otherwise,
    // we did use that item, so we just output the nth item and
    // recursively work backwards from arr[n-1][S-s n]

    if (maxIndex <= 0) {
      return empty();
    } else if (computeValue(maxIndex, totalSpace) == computeValue(maxIndex - 1, totalSpace)) {
      return knapsackContents(maxIndex - 1, totalSpace);
    } else {
      return knapsackContents(maxIndex - 1, totalSpace - sizes[maxIndex - 1])
          .prepend(Tuple.of(sizes[maxIndex - 1], values[maxIndex - 1]));
    }
  }
}
