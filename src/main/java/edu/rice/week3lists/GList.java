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

package edu.rice.week3lists;

import static edu.rice.week3lists.Pair.pair;
import static java.lang.Math.abs;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Interface for a functional list over generic types. */
public interface GList<T> {
  // Data definition: a GList is one of two things:
  // - Cons: an element of type T, and another GList<T>
  // - Empty

  /** Create a new empty list of the given parameter type. */
  @SuppressWarnings("unchecked")
  static <T> GList<T> empty() {
    return (GList<T>) Empty.SINGLETON;
  }

  /**
   * Builder for making a list of elements.
   *
   * <p>Example: GList.of(1, 3, 5) is equivalent to
   * GList&lt;Integer&gt;.empty().prepend(5).prepend(3).prepend(1).
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  static <T> GList<T> of(T... elems) {
    return of(elems, 0);
  }

  /** Helper method for of(). */
  private static <T> GList<T> of(T[] elems, int offset) {
    if (elems.length <= offset) {
      return empty();
    } else {
      return new Cons<>(elems[offset], of(elems, offset + 1));
    }
  }

  /** Creates a list of <code>n</code> references to the value <code>elem</code>. */
  static <T> GList<T> fill(int n, T elem) {
    if (n <= 0) {
      return empty();
    } else {
      return new Cons<>(elem, fill(n - 1, elem));
    }

    //    throw new RuntimeException("fill not implemented yet");
  }

  /** Returns the value of the first element in the list. */
  T head();

  /**
   * Returns a new list equal to the old list without its head() element. If the list is empty, this
   * will throw an exception.
   */
  GList<T> tail();

  /** Returns a new list with the given value in the front. */
  default GList<T> prepend(T val) {
    return new Cons<>(val, this);
  }

  /** Computes the number of elements in the list. */
  int length();

  /** Returns whether the list is empty or not. */
  boolean isEmpty();

  /** Returns whether the value o is somewhere in the list. */
  boolean contains(T o);

  /** Returns a new list equal to all the elements in the old list satisfying the predicate. */
  GList<T> filter(Predicate<T> predicate);

  /** Returns a new list equal to the old list with the function applied to each value. */
  <R> GList<R> map(Function<T, R> f);

  /**
   * Returns a value of type U equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from left-to-right (i.e., from head() to tail()). The
   * zero value is used to the left of the list's head. If the list is empty, the zero value is
   * returned.
   */
  <U> U foldLeft(U zero, BiFunction<U, T, U> operator);

  /**
   * Returns a value of type T equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from right-to-left (i.e., from tail() to head()). The
   * zero value is used to the right of the list's last non-empty value. If the list is empty, the
   * zero value is returned.
   */
  <U> U foldRight(U zero, BiFunction<T, U, U> operator);

  /**
   * Returns a new list equal to the "other" list concatenated at the end of "this" list.
   *
   * <p>Examples: <br>
   * {1,2,3}.appendAll({4,5}) returns {1,2,3,4,5} <br>
   * emptyList.appendAll({1,2}) returns {1,2} <br>
   * {1,2}.appendAll(emptyList} returns {1,2}
   */
  GList<T> appendAll(GList<T> other);

  //////////////////////////////////////////////////////////////////////
  // the methods below will be implemented as part of the week 3 project
  // Unit tests are in Week3ProjectTest.java
  //////////////////////////////////////////////////////////////////////

  /**
   * Returns a new list equal to at most the first n elements of "this" list. If n &gt; length(),
   * then the returned list will be equal to "this" list. If n &lt;= 0, an empty list will be
   * returned.
   */
  GList<T> take(int n);

  /**
   * Returns a list of integers, beginning at start and continuing by increment until the value
   * would be outside of [start,end] (i.e., the inclusive range).
   *
   * <p>Example: rangeClosed(1,5) returns {1,2,3,4,5}
   */
  static GList<Integer> rangeClosed(int start, int end) {
    //    throw new RuntimeException("rangeClosed not implemented yet");
    return rangeClosedBy(start, end, 1);
  }

  /**
   * Returns a list of integers, beginning at start and continuing by increment until the value
   * would be outside of [start,end] (i.e., the inclusive range).
   *
   * <p>Examples: <br>
   * rangeClosedBy(1,5,1) returns {1,2,3,4,5} <br>
   * rangeClosedBy(1,5,2) returns {1,3,5} <br>
   * rangeClosedBy(5,1,-1) returns {5,4,3,2,1}<br>
   * rangeClosedBy(5,1,1) returns {}
   */
  static GList<Integer> rangeClosedBy(int start, int end, int increment) {
    //    throw new RuntimeException("rangeClosedBy not implemented yet");
    if (start < end) {
      return increment < 0 ? empty() : rangeClosedHelper(start, start, end, increment);
    } else {
      return increment > 0 ? empty() : rangeClosedHelper(start, end, start, increment);
    }
  }

  /** Helper method for rangeClosed. */
  private static GList<Integer> rangeClosedHelper(int current, int min, int max, int increment) {
    if (current < min || current > max) {
      return empty();
    } else {
      return rangeClosedHelper(current + increment, min, max, increment).prepend(current);
    }
  }

  /**
   * For lists of comparable types, it's useful to compute their "minimum" based on the comparison
   * function. This is a static method rather than a member method because not all lists are lists
   * over comparable types. If the input is an empty list, the <code>defaultValue</code> should be
   * returned.
   *
   * <p>Examples: GList.minimum(0, {5,2,9,3,7}) returns 2 <br>
   * GList.minimum(0, emptyList) returns 0 <br>
   * GList.minimum("", {"Charlie", "Alice", "Bob"}) returns "Alice"
   */
  static <T extends Comparable<T>> T minimum(T defaultValue, GList<T> list) {
    if (list.isEmpty()) {
      return defaultValue;
    } else {
      return list.tail().foldLeft(list.head(), (a, b) -> (a.compareTo(b) < 0) ? a : b);
    }
    //    throw new RuntimeException("minimum not implemented yet");
  }

  /**
   * For lists of comparable types, it's useful to compute their "maximum" based on the comparison
   * function. This is a static method rather than a member method because not all lists are lists
   * over comparable types. If the input is an empty list, the <code>defaultValue</code> should be
   * returned.
   *
   * <p>Examples: GList.maximum(0, {5,2,9,3,7}) returns 9 <br>
   * GList.maximum(0, emptyList) returns 0 <br>
   * GList.maximum("", {"Charlie", "Alice", "Bob"}) returns "Charlie"
   */
  static <T extends Comparable<T>> T maximum(T defaultValue, GList<T> list) {
    if (list.isEmpty()) {
      return defaultValue;
    } else {
      return list.tail().foldLeft(list.head(), (a, b) -> (a.compareTo(b) > 0) ? a : b);
    }
    //    throw new RuntimeException("maximum not implemented yet");
  }

  /**
   * For a list of numbers, computes their sum with repeated addition. Might not give accurate
   * answers when floating-point precision is lost.
   */
  static double naiveSum(GList<Double> list) {
    return list.foldLeft(0.0, (a, b) -> a + b);
    //    throw new RuntimeException("naiveSum not implemented yet");
  }

  /**
   * For a list of numbers, computes their sum. Uses Neumaier's algorithm to compensate for error.
   */
  static double sum(GList<Double> list) {
    // Neumaier summation per Wikipedia:
    // https://en.wikipedia.org/wiki/Kahan_summation_algorithm#Further_enhancements

    // function NeumaierSum(input)
    //   var sum = 0.0
    //   var c = 0.0                   // A running compensation for lost low-order bits.
    //   for i = 1 to input.length do
    //     var t = sum + input[i]
    //     if |sum| >= |input[i]| then
    //       c += (sum - t) + input[i] // If sum is bigger, low-order digits of input[i] are lost.
    //     else
    //       c += (input[i] - t) + sum // Else low-order digits of sum are lost
    //     endif
    //     sum = t
    //   next i
    //   return sum + c                // Correction only applied once in the very end

    // Rather than using a for-loop and mutating the variables sum and
    // c, you should implement this using a foldLeft, using a Pair of
    // values to track the state of the computation (sum, c). You will
    // also need to use Math.abs() for computing the absolute value.

    // You will do this without any mutation.

    var startingState = pair(0.0, 0.0); // sum, c
    var finalState =
        list.foldLeft(
            startingState,
            (state, input) -> {
              var sum = state._1;
              var c = state._2;
              var t = sum + input;
              var delta = abs(sum) >= abs(input) ? (sum - t) + input : (input - t) + sum;
              return pair(t, c + delta);
            });
    return finalState._1 + finalState._2;
    //    throw new RuntimeException("sum not implemented yet");
  }

  /**
   * For lists of numbers, it's useful to compute their average. This is a static method rather than
   * a member method because not all types allow you to do arithmetic with them. If the list is
   * empty, <code>defaultValue</code> should be returned.
   *
   * <p>Examples: <br>
   * GList.average(0, {5,2,9,3,7}) returns 5.2 <br>
   * GList.average(0, emptyList) returns 0
   */
  static double average(double defaultValue, GList<Double> list) {
    if (list.isEmpty()) {
      return defaultValue;
    } else {
      return sum(list) / list.length();
    }
    //    throw new RuntimeException("average not implemented yet");
  }

  /** Helper method for toString. */
  private static <T> String toStringHelper(GList<T> list, String prefix) {
    if (list.isEmpty()) {
      return "GList(" + prefix + ")";
    } else if (prefix.isEmpty()) {
      return toStringHelper(list.tail(), list.head().toString());
    } else {
      return toStringHelper(list.tail(), prefix + ", " + list.head().toString());
    }
  }

  class Cons<T> implements GList<T> {
    private final T headVal;
    private final GList<T> tailVal;

    private Cons(T value, GList<T> tailList) {
      this.headVal = value;
      this.tailVal = tailList;
    }

    @Override
    public T head() {
      return headVal;
    }

    @Override
    public GList<T> tail() {
      return tailVal;
    }

    @Override
    public int length() {
      return 1 + tailVal.length();
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean contains(T o) {
      if (o == headVal) {
        return true; // if they're pointing to the exact same object
      } else if (o.equals(headVal)) {
        return true; // we found it
      }

      // we didn't find it, so let's look recursively
      return tail().contains(o);
    }

    @Override
    public GList<T> filter(Predicate<T> predicate) {
      if (predicate.test(headVal)) {
        return tailVal.filter(predicate).prepend(headVal);
      } else {
        return tailVal.filter(predicate);
      }
    }

    @Override
    public <R> GList<R> map(Function<T, R> f) {
      return tailVal.map(f).prepend(f.apply(headVal));
    }

    @Override
    public <U> U foldLeft(U zero, BiFunction<U, T, U> operator) {
      // Engineering note: foldLeft() can be written recursively, as
      // with foldRight(), but we're instead writing it here with a
      // traditional Java loop. This means that foldLeft() won't run
      // out of memory when you're running it on very long lists. It's
      // not possible to do the same transformation to foldRight().
      // We'll discuss this in lecture.

      // Also of note: try replacing GList<T> with var. The inferred
      // type of currentList will be GList.Cons<T>, which will then
      // cause an error when we overwrite currentList inside the while
      // loop. Sometimes Java var's type inference is what we want,
      // and other times it's too specific, so we need to explicitly
      // declare out types!

      GList<T> currentList = this;

      while (!currentList.isEmpty()) {
        zero = operator.apply(zero, currentList.head());
        currentList = currentList.tail();
      }
      return zero;
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<T, U, U> operator) {
      return operator.apply(headVal, tailVal.foldRight(zero, operator));
    }

    @Override
    public String toString() {
      return toStringHelper(this, "");
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof Cons<?>)) {
        return false;
      }

      var otherList = (Cons<?>) other;

      return headVal.equals(otherList.headVal) && tailVal.equals(otherList.tailVal);
    }

    @Override
    public int hashCode() {
      return headVal.hashCode() + tailVal.hashCode() * 31; // a hack, but better than nothing
    }

    @Override
    public GList<T> appendAll(GList<T> other) {
      return tailVal.appendAll(other).prepend(headVal);
      //    throw new RuntimeException("appendAll not implemented yet");
    }

    @Override
    public GList<T> take(int n) {
      if (n < 1) {
        return empty();
      } else {
        return tailVal.take(n - 1).prepend(headVal);
      }
      //    throw new RuntimeException("take not implemented yet");
    }
  }

  class Empty<T> implements GList<T> {
    private Empty() {}

    private static final GList<?> SINGLETON = new Empty<>();

    @Override
    public T head() {
      throw new NoSuchElementException("can't take head() of an empty list");
    }

    @Override
    public GList<T> tail() {
      throw new NoSuchElementException("can't take tail() of an empty list");
    }

    @Override
    public int length() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean contains(T t) {
      return false;
    }

    @Override
    public GList<T> filter(Predicate<T> predicate) {
      return empty();
    }

    @Override
    public GList<T> appendAll(GList<T> other) {
      return other;
      //      throw new RuntimeException("appendAll not implemented yet");
    }

    @Override
    public GList<T> take(int n) {
      return this;
      //      throw new RuntimeException("take not implemented yet");
    }

    @Override
    public <R> GList<R> map(Function<T, R> f) {
      return empty();
    }

    @Override
    public <U> U foldLeft(U zero, BiFunction<U, T, U> operator) {
      return zero;
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<T, U, U> operator) {
      return zero;
    }

    @Override
    public String toString() {
      return toStringHelper(this, "");
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Empty<?>;
    }

    @Override
    public int hashCode() {
      return 1; // a hack, but better than nothing
    }
  }
}
