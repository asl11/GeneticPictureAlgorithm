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

package edu.rice.week5sorting;

import static edu.rice.qt.QtHelpers.qtGenOnce;
import static edu.rice.qt.SequenceGenerators.sequences;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static edu.rice.vavr.Sequences.seqIsSorted;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.generators.SourceDSL.strings;

import edu.rice.util.Log;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class SortPerformance {
  private static final String TAG = "SortPerformance";

  private static final Seq<Integer> sizes = List.of(20, 1_000, 4_000, 16_000);
  private static final Seq<Tuple2<Integer, Seq<String>>> testData =
      sizes.map(
          size -> {
            Log.iformat(TAG, "Computing seq test sequence, size = %8d", size);
            return Tuple.of(
                size,
                qtGenOnce(sequences().of(strings().basicLatinAlphabet().ofLength(10)).ofSize(size))
                    .toList());
          });

  static void testAlgorithm(String name, Function<Seq<String>, Seq<String>> sorter) {
    System.out.printf("================== testing %-16s ==================\n", name);

    testData.forEach(
        t -> {
          var size = t._1;
          var seq = t._2;

          var result = nanoBenchmarkVal(() -> sorter.apply(seq));

          System.out.printf(
              "Sort time: %12.3f µs / element with %8d elements\n",
              result._1 / (size * 1000.0), size);

          assertTrue(seqIsSorted(result._2));
        });
  }

  @Test
  public void testVavrSort() {
    testAlgorithm("Timsort", Seq::sorted);
  }

  @Test
  public void testHeapSortLazy() {
    testAlgorithm("Heapsort (lazy)", HeapSorter::sort);
  }

  @Test
  public void testHeapSortEager() {
    testAlgorithm("Heapsort (eager)", x -> HeapSorter.sort(x).toList());
  }
}

// Engineering notes: Here's what happens on my computer with
// some larger test sizes than we put in the normal test.

// ================== testing Timsort          ==================
// Sort time:        0.790 µs / element with   100000 elements
// Sort time:        0.567 µs / element with   400000 elements
// Sort time:        0.669 µs / element with  1600000 elements
//
//
// ================== testing Heapsort (lazy)  ==================
// Sort time:        0.277 µs / element with   100000 elements
// Sort time:        0.107 µs / element with   400000 elements
// Sort time:        0.122 µs / element with  1600000 elements
//
//
// ================== testing Heapsort (eager) ==================
// Sort time:        0.813 µs / element with   100000 elements
// Sort time:        1.380 µs / element with   400000 elements
// Sort time:        1.986 µs / element with  1600000 elements

// What's the difference between the lazy and eager heapsort? The lazy
// version inserted everything, but hasn't taken anything out yet from
// the heap when we stop the clock. This laziness can clearly be a
// performance win if we only want the first few entries in the sorted
// list. Otherwise, if we want everything sorted and the problem size is
// large enough, then Timsort looks like it can win big. Also, we're
// using random inputs here. Timsort does even better when it finds
// sorted subsequences.

// Fun fact: notice how we first run the benchmark with a list of size
// 20 and then discard the result? The very first time the Java VM sees
// some code, it has to do a bunch of work (the "just-in-time compiler").
// We don't want to accidentally include that time in our benchmarking
// time, so we do some irrelevant work up front which we can throw away
// before benchmarking what we really care about.
