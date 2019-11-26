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

package edu.rice.week4subsets;

import static edu.rice.vavr.Sequences.seqMatch;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.function.Function;

/** This class implements subsets of lists of integers, and has a performance test as well. */
public class Subsets {
  /** Using VAVR {@link Stream}, return all subsets of the input. */
  public static <T> Seq<Seq<T>> subsetsStream(Seq<T> input) {
    // In this version, you'll use Stream methods like Stream.of() and Stream.empty()
    // as well as seqMatch() for dealing with the empty and not-empty cases.
    // Rather than concatenating two separate lists together, you will do
    // everything with flatMap().

    return seqMatch(
        input,
        emptyList -> Stream.of(Stream.empty()),
        (head, tail) ->
            subsetsStream(tail).flatMap(child -> Stream.of(child, child.prepend(head))));
  }

  /** Using VAVR {@link List}, return all subsets of the input. */
  public static <T> Seq<Seq<T>> subsetsList(Seq<T> input) {
    // In this version, you'll use List methods like List.of() and List.empty()
    // as well as seqMatch() for dealing with the empty and not-empty cases.
    // Rather than concatenating two separate lists together, you will do
    // everything with flatMap().

    return seqMatch(
        input,
        emptyList -> List.of(List.empty()),
        (head, tail) -> subsetsList(tail).flatMap(child -> List.of(child, child.prepend(head))));
  }

  public static final int MAX = 100;

  /** Helper method for running both subset functions. */
  public static void seqTest(String name, Function<Seq<Integer>, Seq<Seq<Integer>>> subsetFunc) {
    System.out.printf("================ %s subset performance test\n", name);
    var bigInput = List.rangeClosed(0, MAX);

    for (var i = 0; i < MAX; i++) {
      var startTime = System.nanoTime();
      var ignored = subsetFunc.apply(bigInput.take(i));
      var endTime = System.nanoTime();

      var delta = (endTime - startTime) / 1_000_000_000.0;

      System.out.printf("%d,%g\n", i, delta);
      if (delta > 3.0) {
        break;
      }
    }
  }

  /**
   * Very simple benchmark: tries to run the subsets function on input lists as large as 100
   * entries.
   */
  public static void main(String[] args) {
    seqTest("Vavr Stream", Subsets::subsetsStream);
    seqTest("Vavr List", Subsets::subsetsList);
  }
}
