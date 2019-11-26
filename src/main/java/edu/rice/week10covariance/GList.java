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

package edu.rice.week10covariance;

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

  /** Returns a new list with the given value in the front. */
  GList<T> prepend(T value);

  /** Returns a new list equal to the old list without its head() element. */
  GList<T> tail();

  /** Returns a new list equal to all the elements in the old list satisfying the predicate. */
  GList<T> filter(Predicate<? super T> predicate);

  /** Returns a new list equal to the old list with the function applied to each value. */
  <R> GList<R> map(Function<? super T, ? extends R> f);

  /**
   * Returns a value of type U equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from left-to-right (i.e., from head() to tail()). The
   * zero value is used to the left of the list's head. If the list is empty, the zero value is
   * returned.
   *
   * <p>Example, to join a list of strings together, you might write: <br>
   * String result = listOfStrings.foldLeft("", (x,y)-&gt;x+y); <br>
   * The lambda concatenates two strings, and the zero is the empty-string.
   */
  default <U> U foldLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operator) {
    return match(
        emptyList -> zero, (head, tail) -> tail.foldLeft(operator.apply(zero, head), operator));
  }

  /**
   * Returns a value of type U equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from right-to-left (i.e., from tail() to head()). The
   * zero value is used to the right of the list's last non-empty value. If the list is empty, the
   * zero value is returned.
   *
   * <p>Example, to join a list of strings together, you might write: <br>
   * String result = listOfStrings.foldRight("", (x,y)-&gt;x+y); <br>
   * The lambda concatenates two strings, and the zero is the empty-string.
   */
  default <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> operator) {
    return match(
        emptyList -> zero, (head, tail) -> operator.apply(head, tail.foldRight(zero, operator)));
  }

  /** Returns the value of the first element in the list. */
  T head();

  /** Computes the number of elements in the list. */
  int length();

  /** Returns whether the list is empty or not. */
  boolean isEmpty();

  /** Returns whether the value o is somewhere in the list. */
  boolean contains(T o);

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well.
   *
   * @param emptyFunc called if the list is empty
   * @param nonEmptyFunc called if the list has at least one value within
   * @param <R> the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of invoking whichever function matches
   */
  default <R> R match(
      Function<? super GList<T>, ? extends R> emptyFunc,
      BiFunction<? super T, ? super GList<T>, ? extends R> nonEmptyFunc) {
    if (isEmpty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }

  /**
   * Returns a new list equal to the "other" list concatenated at the end of "this" list.
   *
   * <p>Examples: <br>
   * {1,2,3}.appendAll({4,5}) returns {1,2,3,4,5} <br>
   * emptyList.appendAll({1,2}) returns {1,2} <br>
   * {1,2}.appendAll(emptyList} returns {1,2}
   */
  GList<T> appendAll(GList<? extends T> other);

  /**
   * Returns a new list equal to at most the first n elements of "this" list. If n &gt; length(),
   * then the returned list will be equal to "this" list. If n &lt;= 0, an empty list will be
   * returned.
   */
  GList<T> take(int n);

  /** Narrows a list from a wildcard parameter to a regular parameter. */
  @SuppressWarnings("unchecked")
  static <T> GList<T> narrow(GList<? extends T> list) {
    return (GList<T>) list;
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
  static <T extends Comparable<? super T>> T minimum(T defaultValue, GList<? extends T> list) {
    GList<T> nlist = narrow(list);
    if (nlist.isEmpty()) {
      return defaultValue;
    } else {
      return nlist.tail().foldLeft(nlist.head(), (a, b) -> (a.compareTo(b) < 0) ? a : b);
    }
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
  static <T extends Comparable<? super T>> T maximum(T defaultValue, GList<? extends T> list) {
    GList<T> nlist = narrow(list);
    if (nlist.isEmpty()) {
      return defaultValue;
    } else {
      return nlist.tail().foldLeft(nlist.head(), (a, b) -> (a.compareTo(b) > 0) ? a : b);
    }
  }

  /** Helper method for toString(). */
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

    private Cons(T value, GList<? extends T> tailList) {
      this.headVal = value;
      this.tailVal = narrow(tailList);
    }

    @Override
    public GList<T> prepend(T value) {
      return new Cons<>(value, this);
    }

    @Override
    public GList<T> tail() {
      return tailVal;
    }

    @Override
    public GList<T> filter(Predicate<? super T> predicate) {
      if (predicate.test(headVal)) {
        return tailVal.filter(predicate).prepend(headVal);
      } else {
        return tailVal.filter(predicate);
      }
    }

    @Override
    public <R> GList<R> map(Function<? super T, ? extends R> f) {
      R head = f.apply(headVal);
      GList<R> tailList = tailVal.map(f);
      return tailList.prepend(head);
    }

    @Override
    public T head() {
      return headVal;
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
    public String toString() {
      return toStringHelper(this, "");
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof GList<?>)) {
        return false;
      }

      var otherList = (GList<?>) other;
      return head().equals(otherList.head()) && tail().equals(otherList.tail());
    }

    @Override
    public int hashCode() {
      return headVal.hashCode() + tailVal.hashCode() * 31; // a hack, but better than nothing
    }

    @Override
    public GList<T> appendAll(GList<? extends T> other) {
      return tailVal.appendAll(other).prepend(headVal);
    }

    @Override
    public GList<T> take(int n) {
      if (n < 1) {
        return empty();
      } else {
        return tailVal.take(n - 1).prepend(headVal);
      }
    }
  }

  class Empty<T> implements GList<T> {
    private Empty() {}

    private static final GList<?> SINGLETON = new Empty<>();

    @Override
    public GList<T> prepend(T val) {
      return new Cons<>(val, this);
    }

    @Override
    public GList<T> tail() {
      // An unusual design decision, but we're having the tail() of an
      // empty list be another empty list, rather than blowing up with
      // an exception or something. This might allow naïve code,
      // iterating over a list, to hit an infinite loop, but that's a
      // bug and it still needs to be fixed.
      return this;
    }

    @Override
    public GList<T> filter(Predicate<? super T> predicate) {
      return empty();
    }

    @Override
    public T head() {
      throw new NoSuchElementException("can't take head() of an empty list");
    }

    @Override
    public GList<T> appendAll(GList<? extends T> other) {
      return narrow(other);
    }

    @Override
    public GList<T> take(int n) {
      return this;
    }

    @Override
    public <R> GList<R> map(Function<? super T, ? extends R> f) {
      return empty();
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
    public String toString() {
      return toStringHelper(this, "");
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof GList<?> && ((GList<?>) other).isEmpty();
    }

    @Override
    public int hashCode() {
      return 1; // a hack, but better than nothing
    }
  }
}
