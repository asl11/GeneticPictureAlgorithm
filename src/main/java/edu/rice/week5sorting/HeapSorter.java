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

import edu.rice.tree.BinaryHeap;
import io.vavr.collection.Seq;
import java.util.Comparator;

/**
 * Uses our <i>mutating</i> {@link BinaryHeap} to implement <i>functional</i> heap-sort over any
 * {@link Seq}.
 */
public interface HeapSorter {
  /**
   * Given any {@link Seq} over a {@link Comparable} type, returns another sequence in "natural"
   * sorted order.
   */
  static <T extends Comparable<? super T>> Seq<T> sort(Seq<T> input) {
    return sort(input, Comparator.naturalOrder());
  }

  /**
   * Given any {@link Seq} and a {@link java.util.Comparator}, return the contents of the sequence
   * in sorted order.
   */
  static <T> Seq<T> sort(Seq<T> input, Comparator<? super T> comparator) {
    var heap = BinaryHeap.<T>empty(comparator);
    input.forEach(heap::insert);
    return heap.drainToStream();
  }
}

// Engineering notes: HeapSort was invented in 1964 (https://en.wikipedia.org/wiki/Heapsort).
// There isn't much different between what we're doing here and how it's done classically,
// except the classic version sorts an array in-place, whereas the strategy here is that
// we're converting from list to heap and back to list again.

// As Wikipedia notes, QuickSort can be faster, but HeapSort has a guaranteed O(n log n)
// runtime, whereas QuickSort -- if it chooses its pivots poorly -- can end up running
// in O(n^2) time. Perhaps unsurprisingly, real-world implementations of QuickSort are
// careful to sample a number of values before picking a pivot. Also, once the partitioning
// is complete, QuickSort can then run in parallel, with separate CPU cores working on
// each partition. Parallelism is beyond what we generally discuss in Comp215, but you'll
// learn all about it in later classes.

// When you ask VAVR to sort a list, VAVR asks Java to do it with Java Streams (which
// we'll discuss later in the semester). If you dig all the way down, Java uses an
// algorithm called Timsort (https://en.wikipedia.org/wiki/Timsort), invented in 1993.
// The same Timsort algorithm is also used by Python and many other systems.
