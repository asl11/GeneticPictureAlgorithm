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

package edu.rice.tree;

import static edu.rice.qt.QtHelpers.qtGenOnce;
import static edu.rice.qt.SequenceGenerators.sequences;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quicktheories.generators.SourceDSL.strings;

import edu.rice.util.Log;
import edu.rice.util.TriFunction;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TreePerformance {
  private static final String TAG = "TreePerformance";

  private static final int SIZE = 10_000;
  private static final int RANGE_QUERIES = 10;

  // These are initialized in the static block below.
  private static final Seq<String> seq;
  private static final Seq<String> fail;
  private static final Seq<Tuple2<String, String>> ranges;
  private static final Seq<Seq<String>> rangeExpectedAnswers;

  static {
    Log.iformat(TAG, "Computing seq test sequence, size = %d", SIZE);
    var x = qtGenOnce(sequences().of(strings().basicLatinAlphabet().ofLength(10)).ofSize(SIZE));
    seq = x.toList();

    ranges =
        x.zip(x.tail())
            .take(RANGE_QUERIES)
            .map(tuple -> (tuple._1.compareTo(tuple._2) > 0) ? tuple.swap() : tuple)
            .toList();

    rangeExpectedAnswers =
        ranges.map(
            tuple ->
                seq.filter(i -> i.compareTo(tuple._1) >= 0 && i.compareTo(tuple._2) <= 0).sorted());

    Log.iformat(TAG, "Computing fail test sequence, size = %d", SIZE);
    fail =
        qtGenOnce(sequences().of(strings().basicLatinAlphabet().ofLength(9)).ofSize(SIZE)).toList();
  }

  static <T> void testAlgorithm(
      String name,
      Supplier<T> emptyMaker,
      BiFunction<T, String, T> inserter,
      BiPredicate<T, String> querier,
      BiFunction<T, String, T> remover,
      TriFunction<T, String, String, Iterable<String>> ranger) {

    T empty = emptyMaker.get();

    //    System.out.printf("================== warming up %-12s ==================\n", name);
    var shortSeq = seq.take(1000);
    var shortFail = fail.take(1000);
    var shortRanges = ranges.take(1000);
    var tmp1 = shortSeq.foldLeft(empty, inserter);
    var tmp2 = shortSeq.map(v -> querier.test(tmp1, v)).toList();
    var tmp3 = shortFail.map(v -> querier.test(tmp1, v)).toList();
    var tmp4 = shortRanges.map(pair -> ranger.apply(tmp1, pair._1, pair._2));
    var tmp5 = shortSeq.foldLeft(tmp1, remover);

    System.out.printf(
        "================== testing %-12s with %d elements ==================\n", name, SIZE);

    var insertionResult = nanoBenchmarkVal(() -> seq.foldLeft(empty, inserter));

    System.out.printf(
        "Insertion time    : %12.3f µs / element\n", insertionResult._1 / (SIZE * 1000.0));

    var querySuccessResult =
        nanoBenchmarkVal(() -> seq.map(v -> querier.test(insertionResult._2, v)).toList());
    assertEquals(SIZE, querySuccessResult._2.filter(x -> x).length());

    System.out.printf(
        "Success query time: %12.3f µs / element\n", querySuccessResult._1 / (SIZE * 1000.0));

    var queryFailureResult =
        nanoBenchmarkVal(() -> fail.map(v -> querier.test(insertionResult._2, v)).toList());
    assertEquals(SIZE, queryFailureResult._2.filter(x -> !x).length());

    System.out.printf(
        "Failure query time: %12.3f µs / element\n", queryFailureResult._1 / (SIZE * 1000.0));

    var rangeResult =
        nanoBenchmarkVal(
            () -> ranges.map(pair -> ranger.apply(insertionResult._2, pair._1, pair._2)));

    System.out.printf(
        "Range time        : %12.3f µs / query\n", rangeResult._1 / (RANGE_QUERIES * 1000.0));

    // Note that we're sorting the results, since we cannot guarantee the order that comes
    // out of some of these data structures, and we're looking for set equality.
    rangeResult
        ._2
        .zip(rangeExpectedAnswers)
        .forEach(tuple -> assertEquals(tuple._2, Stream.ofAll(tuple._1).sorted()));

    var removerResult = nanoBenchmarkVal(() -> seq.foldLeft(insertionResult._2, remover));
    assertEquals(empty, removerResult._2);

    System.out.printf(
        "Removal time      : %12.3f µs / element\n", removerResult._1 / (SIZE * 1000.0));
  }

  @Test
  public void testTreap() {
    testAlgorithm(
        "Comp215 Treap",
        Treap::<String>empty,
        ITree::add,
        (treap, e) -> treap.find(e).isDefined(),
        ITree::remove,
        (treap, min, max) -> treap.greaterThan(min, true).lessThan(max, true));
  }

  @Test
  public void testHamt() {
    testAlgorithm(
        "VAVR HAMT",
        HashSet::<String>empty,
        HashSet::add,
        HashSet::contains,
        HashSet::remove,
        (hs, min, max) -> hs.filter(x -> x.compareTo(min) >= 0 && x.compareTo(max) <= 0));
  }

  @Test
  public void testRedBlackTree() {
    testAlgorithm(
        "VAVR TreeSet",
        TreeSet::<String>empty,
        TreeSet::add,
        TreeSet::contains,
        TreeSet::remove,
        (tree, min, max) -> tree.filter(x -> x.compareTo(min) >= 0 && x.compareTo(max) <= 0));
  }

  @Test
  public void testJavaTree() {
    testAlgorithm(
        "Java TreeSet",
        java.util.TreeSet<String>::new,
        (tree, v) -> {
          tree.add(v);
          return tree;
        },
        java.util.TreeSet::contains,
        (tree, v) -> {
          tree.remove(v);
          return tree;
        },
        (tree, min, max) -> tree.subSet(min, true, max, true));
  }

  @Test
  public void testJavaHashSet() {
    testAlgorithm(
        "Java HashSet",
        java.util.HashSet<String>::new,
        (h, v) -> {
          h.add(v);
          return h;
        },
        java.util.HashSet::contains,
        (h, v) -> {
          h.remove(v);
          return h;
        },
        (h, min, max) ->
            h.stream()
                .filter(x -> x.compareTo(min) >= 0 && x.compareTo(max) <= 0)
                .collect(Collectors.toList()));
  }
}

// Here's some output from running these tests with SIZE=1_000_000 (one million):

// ================== testing VAVR TreeSet with 1000000 elements ==================
// Insertion time    :        2.312 µs / element
// Success query time:        1.050 µs / element
// Failure query time:        1.261 µs / element
// Range time        :   484635.519 µs / query
// Removal time      :        1.778 µs / element
// ================== testing Comp215 Treap with 1000000 elements ==================
// Insertion time    :        1.745 µs / element
// Success query time:        1.439 µs / element
// Failure query time:        1.319 µs / element
// Range time        :       21.595 µs / query
// Removal time      :        1.386 µs / element
// ================== testing VAVR HAMT    with 1000000 elements ==================
// Insertion time    :        0.511 µs / element
// Success query time:        0.326 µs / element
// Failure query time:        0.252 µs / element
// Range time        :   269703.383 µs / query
// Removal time      :        0.565 µs / element
// ================== testing Java TreeSet with 1000000 elements ==================
// Insertion time    :        1.108 µs / element
// Success query time:        0.985 µs / element
// Failure query time:        1.258 µs / element
// Range time        :       58.450 µs / query
// Removal time      :        0.801 µs / element
// ================== testing Java HashSet with 1000000 elements ==================
// Insertion time    :        0.228 µs / element
// Success query time:        0.120 µs / element
// Failure query time:        0.130 µs / element
// Range time        :   137667.174 µs / query
// Removal time      :        0.126 µs / element

// Engineering notes: Comp215 teaches you how hash tables, binary trees, and treaps work,
// but what about red-black trees or hash array mapped tries (HAMT) -- the core data structures
// that VAVR uses? The java.util.TreeSet also uses a red-black tree.

// HAMTs: You might start by understanding tries:
// https://en.wikipedia.org/wiki/Trie

// And then here's a paper about how HAMTs work:
// https://infoscience.epfl.ch/record/64398/files/idealhashtrees.pdf

// And here are some links to introduce you to the classic red-black tree:
// https://en.wikipedia.org/wiki/Red%E2%80%93black_tree
// https://www.cs.auckland.ac.nz/software/AlgAnim/red_black.html

// Of particular note, red-black trees were invented in 1978 by Guibas and Sedgewick, but it
// apparently wasn't until 1999 that Okasaki showed how to do red-black inserts in a purely
// functional fashion! And deletion turns out to be even harder:
// http://matt.might.net/papers/germane2014deletion.pdf

// Lastly, here's a link that describes a bit of the history of Java's HashMap implementation,
// which originally used a linked-list attached to each hash bucket but in Java8 (2014-ish)
// switched to having a red-black tree attached to each hash bucket:
// http://coding-geek.com/how-does-a-hashmap-work-in-java

// Based on the performance numbers you'll see from running the test code above, treaps and
// red-black trees have surprisingly similar performance. Treaps are a little faster at
// insertion and deletion, perhaps because they've got less work to do, but red-black trees
// are a little faster at queries, perhaps because they end up slightly better balanced.

// Meanwhile, HAMTs blow away treaps or red-black trees. There are a lot of reasons for this,
// but they still don't come close to Java's HashSet, which uses mutation, and is the fastest
// of all. As such, here's how you might go about deciding which data structure is the "best"
// for any given application:

// 1) Do you frequently need "range queries" (i.e., find all the elements between X and Y),
//    particularly over large sets? If so, then you could use our Treap class or Java's TreeSet.
//    You're looking at a four-order-of-magnitude difference in performance!

// 2) If not, do you need or prefer a functional API?
//    If so, then HAMTs are fantastic.

// 3) Otherwise, particularly if you need the absolute fastest performance, then Java's HashSet
//    seems to be the winner.

// There are plenty of other issues that might influence a decision like this, particularly
// if you're running with more than one concurrent thread. Needless to say, these issues
// are beyond the scope of this already over-sized engineering note.
