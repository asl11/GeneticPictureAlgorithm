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

package edu.rice.week4queue;

import static edu.rice.vavr.Sequences.seqMatch;

import io.vavr.collection.Seq;
import io.vavr.collection.Stream;

/**
 * This class, as with the helpers in {@link edu.rice.vavr.Sequences}, implements functionality
 * that's missing from VAVR.
 */
public interface Merge {
  /**
   * Given two {@link Seq} sequences of a {@link Comparable} type which are in "natural" sorted
   * order -- according to how their {@link Comparable#compareTo(Object)} method would order them --
   * merge them together into a new sequence that's also in the "natural" order.
   *
   * <p>This method works equally well with finite lists and infinite streams as its inputs. The
   * result is a {@link Stream}, computed lazily as necessary.
   */
  static <T extends Comparable<? super T>> Stream<T> seqMerge(
      Seq<? extends T> a, Seq<? extends T> b) {
    // Don't use if/then/else or switch constructs when dealing with
    // whether "a" or "b" are empty. Similarly, don't use for, while,
    // or any other looping constructions, since the input sequences
    // might be infinite.
    //
    // To deal with the different empty or not-empty cases, use
    // seqMatch() from the edu.rice.vavr.Sequences helpers.  After
    // that, you will need to use if/then/else or Java's ternary
    // operator.

    // To deal with infinite streams, you have to be careful about how
    // you do recursion. If you write a recursive call like
    // seqMerge(...).prepend(...), then your computation will never
    // complete. Instead, you want to use Stream.cons().
    //
    // Note that the inputs are Seq, not Stream, so you may need to
    // also convert them to Streams. See Stream.ofAll().

    Stream<T> na = Stream.ofAll(a);
    Stream<T> nb = Stream.ofAll(b);

    return seqMatch(
        na,
        emptyA -> nb,
        (aHead, aTail) ->
            seqMatch(
                nb,
                emptyB -> na,
                (bHead, bTail) ->
                    aHead.compareTo(bHead) < 0
                        ? Stream.cons(aHead, () -> seqMerge(aTail, nb))
                        : Stream.cons(bHead, () -> seqMerge(na, bTail))));
  }
}
