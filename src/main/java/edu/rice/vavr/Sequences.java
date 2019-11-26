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

package edu.rice.vavr;

import static edu.rice.lens.Lens.lens;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.lens.Lens;
import edu.rice.util.TriFunction;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Helper methods for working with VAVR {@link Seq} types, including {@link List} and {@link
 * Stream}.
 */
public interface Sequences {
  /**
   * General-purpose pattern matching on a {@link Seq} (i.e., a {@link io.vavr.collection.List} or
   * {@link io.vavr.collection.Stream}) with three lambdas: one for the empty-list case, one for a
   * list with a single entry, and one for a list with two or more entries.
   */
  static <T, R> R seqMatch(
      Seq<T> input,
      Function<Seq<T>, ? extends R> emptyF,
      BiFunction<T, Seq<T>, ? extends R> oneElemF,
      TriFunction<T, T, Seq<T>, ? extends R> twoOrMoreF) {

    if (input.isEmpty()) {
      return emptyF.apply(input);
    }

    var headVal = input.head();
    var tailVal = input.tail();
    if (tailVal.isEmpty()) {
      return oneElemF.apply(headVal, tailVal);
    }

    return twoOrMoreF.apply(headVal, tailVal.head(), tailVal.tail());
  }

  /**
   * General-purpose pattern matching on a {@link Seq} (i.e., a {@link io.vavr.collection.List} or
   * {@link io.vavr.collection.Stream}) with two lambdas: one for the empty-list case, one for a
   * list with one or more entries.
   */
  static <T, R> R seqMatch(
      Seq<T> input,
      Function<Seq<T>, ? extends R> emptyF,
      BiFunction<T, Seq<T>, ? extends R> oneOrMoreElemF) {

    if (input.isEmpty()) {
      return emptyF.apply(input);
    } else {
      return oneOrMoreElemF.apply(input.head(), input.tail());
    }
  }

  /**
   * Given a {@link java.util.Iterator}, returns a lazy {@link Seq} view of the iterator. No
   * guarantees are made about when the iterator is actually iterated. If you externally mutate the
   * iterator or the underlying data, after calling this function, the results are unpredictable.
   *
   * <p>If you have an {@link Iterable}, you probably want to use {@link
   * io.vavr.collection.List#ofAll(Iterable)} or {@link io.vavr.collection.Stream#ofAll(Iterable)}
   * rather than this helper method. If you have a VAVR {@link io.vavr.collection.Iterator}, you
   * should just directly use {@link Stream#ofAll(Iterable)}.
   */
  static <T> Seq<T> iteratorToSeq(java.util.Iterator<T> i) {
    // Oddly, VAVR doesn't support Stream.ofAll(Iterator).
    return Stream.ofAll(Iterator.ofAll(i));
  }

  /**
   * Given a {@link java.util.Enumeration}, returns a lazy {@link Seq} view of the enumeration. No
   * guarantees are made about when the enumeration is actually iterated. If you externally
   * manipulate the enumeration or the underlying data, after calling this function, the results are
   * unpredictable.
   */
  static <T> Seq<T> enumerationToSeq(java.util.Enumeration<T> e) {
    // Efficiency note: this will generate O(n) garbage for all the calls to some(). We only
    // encounter Java's Enumeration class in a handful of places, and the size isn't likely
    // to matter.

    return Stream.iterate(() -> e.hasMoreElements() ? some(e.nextElement()) : none());
  }

  /**
   * Checks whether the given {@link Seq} is sorted in "natural" order. Requires the sequence type
   * <code>T</code> to implement the {@link Comparable} interface.
   */
  static <T extends Comparable<? super T>> boolean seqIsSorted(Seq<T> seq) {
    return seqIsSorted(seq, Comparator.naturalOrder());
  }

  /**
   * Checks whether the given {@link Seq} is sorted in the order defined by the given {@link
   * Comparator}.
   */
  static <T> boolean seqIsSorted(Seq<? extends T> seq, Comparator<? super T> comparator) {
    return seqMatch(
        seq,
        emptyList -> true,
        (head, tail) ->
            seq.zip(tail) // pairs of adjacent values from the list to compare
                .map(t -> comparator.compare(t._1, t._2))
                .find(x -> x > 0) // if >0, then out of order
                .isEmpty()); // make sure that doesn't happen
  }

  /**
   * Get the nth element in the given sequence, if it exists, returning {@link Option#some(Object)}
   * if present, otherwise {@link Option#none()} if it's not there. Avoids the exceptions that can
   * be thrown by {@link Seq#get}.
   */
  static <T> Option<T> seqGetOption(Seq<T> seq, int n) {
    // seq.isDefinedAt(n) would be useful here but it's deprecated, so we're doing
    // something a little less elegant.
    try {
      return some(seq.get(n));
    } catch (IndexOutOfBoundsException e) {
      return none();
    }
  }

  /**
   * If the sequence has an nth value, and <code>newValue</code> is {@link Option#some(Object)},
   * replaces the nth value with the contents of the Option. If <code>newValue</code> is {@link
   * Option#none()}, the nth value is removed from the sequence. If the sequence's length is less
   * than n, the sequence is returned unchanged.
   */
  static <T> Seq<T> seqSetOption(Seq<T> seq, int n, Option<? extends T> newValue) {
    return seqGetOption(seq, n)
        .fold(() -> seq, oldValue -> newValue.fold(() -> seq.removeAt(n), v -> seq.update(n, v)));
  }

  /**
   * Update the nth value in a sequence, if present, with updateFunc applied to the present value.
   * If the replacement is empty, then the nth value of the sequence is removed.
   */
  static <T> Seq<T> seqUpdateOption(
      Seq<T> seq, int n, Function<? super T, Option<? extends T>> updateFunc) {
    return seqGetOption(seq, n)
        .fold(() -> seq, oldValue -> seqSetOption(seq, n, updateFunc.apply(oldValue)));
  }

  /**
   * Gets a lens to the nth element in a {@link Seq}. The lens expects {@link Option} arguments and
   * returns {@link Option} results. For the getter, this means that any out-of-bounds query returns
   * {@link Option#none()}. For the setter, if the value being set is {@link Option#none()}, that
   * will be interpreted as removing the nth element from the sequence.
   */
  static <T> Lens<Seq<T>, Option<T>> lensSeq(int n) {
    return lens(seq -> seqGetOption(seq, n), (seq, v) -> seqSetOption(seq, n, v));
  }
}
