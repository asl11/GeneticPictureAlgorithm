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

import static edu.rice.vavr.Sequences.iteratorToSeq;
import static edu.rice.vavr.Sequences.seqGetOption;
import static edu.rice.vavr.Sequences.seqIsSorted;
import static edu.rice.vavr.Sequences.seqMatch;
import static edu.rice.vavr.Sequences.seqUpdateOption;
import static edu.rice.vavr.Tries.tryOfNullable;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class VavrHelperTests {
  @Test
  void testIteratorToSeq() {
    assertEquals(List.empty(), iteratorToSeq(Collections.emptyIterator()));

    var jlist = java.util.List.of(1, 3, 5, 7, 9);
    var list = List.of(1, 3, 5, 7, 9);

    assertEquals(list, iteratorToSeq(jlist.iterator()));
  }

  @Test
  void testSeqNth() {
    var list = List.of(1, 3, 5, 7, 9);
    assertEquals(none(), seqGetOption(list, -5));
    assertEquals(none(), seqGetOption(list, 20));
    assertEquals(some(1), seqGetOption(list, 0));
    assertEquals(some(3), seqGetOption(list, 1));
    assertEquals(some(5), seqGetOption(list, 2));
    assertEquals(some(7), seqGetOption(list, 3));
    assertEquals(some(9), seqGetOption(list, 4));
    assertEquals(none(), seqGetOption(list, 5));
  }

  @Test
  void testUpdateList() {
    var list = List.of(1, 3, 5, 7, 9);
    assertEquals(list, seqUpdateOption(list, -5, x -> some(x + 1)));
    assertEquals(list, seqUpdateOption(list, 5, x -> some(x + 1)));
    assertEquals(List.of(2, 3, 5, 7, 9), seqUpdateOption(list, 0, x -> some(x + 1)));
    assertEquals(List.of(1, 4, 5, 7, 9), seqUpdateOption(list, 1, x -> some(x + 1)));
    assertEquals(List.of(1, 3, 6, 7, 9), seqUpdateOption(list, 2, x -> some(x + 1)));
    assertEquals(List.of(1, 3, 7, 9), seqUpdateOption(list, 2, x -> none()));
  }

  @Test
  void testSeqIsSorted() {
    Seq<Integer> list0 = List.empty();
    var list1 = List.of(1, 3, 5, 7, 9);
    var list2 = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    assertTrue(seqIsSorted(list0));
    assertTrue(seqIsSorted(list1));
    assertTrue(seqIsSorted(list2));
    assertFalse(seqIsSorted(list1.reverse()));
    assertFalse(seqIsSorted(list2.reverse()));
  }

  @Test
  void testTryWithNulls() {
    // This test exercises a corner cases of VAVR: converting Try<T> to Option<T>,
    // when the Try might be success(null). VAVR allows this, creating what we might
    // think of as surprising behavior.

    Try<String> ts = Try.of(() -> null);
    Option<String> os = ts.toOption(); // becomes Option.some(null)
    Option<String> ns = Option.of(null); // becomes Option.none()

    assertTrue(ts.isSuccess());
    assertTrue(os.isDefined());
    assertFalse(ns.isDefined());
    assertNull(os.get());
    assertNull(ts.get());
    assertThrows(
        NoSuchElementException.class,
        () -> {
          var ignored = ns.get();
        });

    // Our own helper function brings a bit of sanity to this issue.

    Try<String> ts2 = tryOfNullable(() -> null);
    assertEquals(ns, ts2.toOption());
  }

  /**
   * This code demonstrates how {@link Stream#cons(Object, Supplier)} can be used to implement a
   * lazy list filter. You would never use this in real code. For that, you'd use {@link
   * Stream#filter}. We have two versions here. One uses {@link Sequences#seqMatch} and the other
   * uses ternary operators. They both run the same. Both are lazy.
   */
  static <T> Stream<T> seqLazyFilter(Seq<T> seq, Predicate<T> predicate) {
    return seqMatch(
        seq,
        emptySeq -> Stream.empty(),
        (head, tail) ->
            predicate.test(head)
                ? Stream.cons(head, () -> seqLazyFilter(tail, predicate))
                : seqLazyFilter(tail, predicate));
  }

  static <T> Stream<T> seqLazyFilter2(Seq<T> seq, Predicate<T> predicate) {
    if (seq.isEmpty()) {
      return Stream.empty();
    } else {
      var head = seq.head();
      var tail = seq.tail();

      return predicate.test(head)
          ? Stream.cons(head, () -> seqLazyFilter2(tail, predicate))
          : seqLazyFilter2(tail, predicate);
    }
  }

  @Test
  public void testSeqLazyFilter() {
    var wholeNumbers = Stream.from(0);
    var evens = Stream.from(0, 2);

    var alsoEvens0 = wholeNumbers.filter(x -> x % 2 == 0);
    var alsoEvens1 = seqLazyFilter(wholeNumbers, x -> x % 2 == 0);
    var alsoEvens2 = seqLazyFilter2(wholeNumbers, x -> x % 2 == 0);

    assertEquals(evens.take(20), alsoEvens0.take(20));
    assertEquals(evens.take(20), alsoEvens1.take(20));
    assertEquals(evens.take(20), alsoEvens2.take(20));
  }
}
