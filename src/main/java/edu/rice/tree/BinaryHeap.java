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

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.util.Strings;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

/**
 * BinaryHeap is a classic <b>mutating</b> priority queue, where the priorities come from the
 * objects being inserted, using the optionally specified <code>lessThanFunc</code>. If you want a
 * <b>functional</b> priority queue, then use VAVR's {@link io.vavr.collection.PriorityQueue}.
 */
public interface BinaryHeap<T> {
  /**
   * This static function is a shorthand for building a heap by inserting the given values. The
   * given values are required to be Comparable; their internal compareTo function is used for
   * ordering.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T extends Comparable<? super T>> BinaryHeap<T> of(T... array) {
    var result = empty(Comparator.<T>naturalOrder());

    Stream.of(array).forEach(result::insert); // mutation!

    return result;
  }

  /**
   * This make-method lets you define a binary heap over any {@link Comparable} type using it's
   * "natural" ordering.
   */
  static <T extends Comparable<? super T>> BinaryHeap<T> empty() {
    return empty(Comparator.naturalOrder());
  }

  /**
   * This make-method lets you define a binary heap over any type at all, so long as you can define
   * an ordering with {@link Comparator}.
   */
  static <T> BinaryHeap<T> empty(Comparator<? super T> comparator) {
    return empty((T a, T b) -> comparator.compare(a, b) < 0);
  }

  /**
   * This make-method lets you define a binary heap over any type at all, so long as you can define
   * lessThanFunction (e.g., (a,b) -&gt; a &lt; b) over the type to give it an ordering.
   */
  static <T> BinaryHeap<T> empty(BiPredicate<? super T, ? super T> lessThanFunc) {
    return new BinaryHeapImpl<>(lessThanFunc);
  }

  /**
   * Inserts a new element into the priority queue. <b>Warning: THIS IS A MUTATING OPERATION.</b>
   */
  void insert(T val);

  /**
   * Returns a list of the elements in sorted order. <b>Warning: THIS IS A MUTATING OPERATION.</b>
   * When this is complete, the priority queue will be empty. If you insert to the queue while also
   * iterating on the stream, the results will be undefined. This method is best used if you've
   * added everything you're ever going to add to the queue and you just want to get its values out
   * in sorted order and then discard the queue. Of course, the resulting list is functional and
   * will have all the usual properties of any functional list.
   *
   * <p>If you call {@link Seq#toList()} on the resulting stream, you will then be guaranteed that
   * the priority queue is empty.
   */
  default Stream<T> drainToStream() {
    return Stream.iterate(() -> isEmpty() ? none() : some(getMin()));
  }

  /** Returns the number of elements in the queue. */
  int size();

  /** Returns whether the queue has elements or not. */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns the lowest-priority item in the queue, and removes it from the queue. <b>Warning: THIS
   * IS A MUTATING OPERATION.</b>
   */
  T getMin();

  /**
   * Checks to make sure that the internal structure of the priority queue is consistent. Useful for
   * unit tests.
   */
  boolean isValid();

  /**
   * A standard binary-heap implementation of a mutating priority queue. Among other things, this
   * provides a relatively straightforward O(n log n) way of sorting a list of things. Just insert
   * them here then drain them out again in order.
   */
  class BinaryHeapImpl<T> implements BinaryHeap<T> {
    // Engineering note: Java's ArrayList provides us with a
    // mutating array-like structure with constant-time insert and
    // fetch, as well as automatically growing the array when
    // we call the add() method, saving us from having to
    // worry about making bigger arrays and copying the data.
    private final ArrayList<T> storage;

    private final BiPredicate<? super T, ? super T> lessThanFunc;
    private int elements;

    /**
     * Private constructor, forcing users to instead use the public {@link
     * BinaryHeap#of(Comparable[])}, {@link BinaryHeap#empty()}, etc., all of which force the return
     * type to be the BinaryHeap interface.
     */
    private BinaryHeapImpl(BiPredicate<? super T, ? super T> lessThanFunc) {
      storage = new ArrayList<>();
      elements = 0;
      this.lessThanFunc = lessThanFunc;
    }

    @Override
    public void insert(T val) {
      elements++;

      // the new value starts off at the end of the array
      if (storage.size() < elements) {
        storage.add(val);
      } else {
        storage.set(elements - 1, val);
      }

      // time to heapify!
      for (var index = elements - 1; index > 0; index = parent(index)) {
        var currentElem = storage.get(index);
        var parentElem = storage.get(parent(index));

        if (lessThanFunc.test(currentElem, parentElem)) {
          // we need to swap and continue onward

          storage.set(index, parentElem);
          storage.set(parent(index), currentElem);
        } else {
          // the heap property is now satisfied and we're done
          return;
        }
      }
    }

    @Override
    public int size() {
      return elements;
    }

    @Override
    public T getMin() {
      if (elements == 0) {
        throw new NoSuchElementException("can't call getMin() on an empty binary heap");
      }

      // this is the result that the user actually wants
      final var result = storage.get(0);

      // remove the last thing and stick it in front
      storage.set(0, storage.get(elements - 1));
      elements--;

      int index = 0;

      // and now, heapify!
      while (child1(index) < elements) {
        var currentElem = storage.get(index);
        var child1elem = storage.get(child1(index));

        if (child2(index) < elements) {
          // we have to decide whether child1 or child2 is smaller, and then compare
          // that to the current element

          var child2elem = storage.get(child2(index));

          if (lessThanFunc.test(child1elem, child2elem)) {
            // child1 is "less than" child2

            if (lessThanFunc.test(currentElem, child1elem)) {
              // current is smaller, so we're done

              break; // out of the while loop
            }

            // swap the two elements
            storage.set(index, child1elem);
            storage.set(child1(index), currentElem);

            // continue with child1
            index = child1(index);
          } else {
            if (lessThanFunc.test(currentElem, child2elem)) {
              // current is smaller, so we're done
              break; // out of the while loop
            }

            // swap the two elements
            storage.set(index, child2elem);
            storage.set(child2(index), currentElem);

            // continue with child2
            index = child2(index);
          }
        } else {
          // child2 doesn't exist, but we still have to compare child1 to the current element

          if (lessThanFunc.test(currentElem, child1elem)) {
            // current is smaller, so we're done

            break; // out of the while loop
          }

          // swap the two elements
          storage.set(index, child1elem);
          storage.set(child1(index), currentElem);

          // continue with child1
          index = child1(index);
        }
      }

      // at this point, we've properly heapified things, so we're ready to return the result
      return result;
    }

    private static int child1(int index) {
      return (index * 2) + 1;
    }

    private static int child2(int index) {
      return (index * 2) + 2;
    }

    private static int parent(int index) {
      return (index - 1) / 2;
    }

    /** Returns whether or not the heap property is satisfied; useful for unit tests. */
    @Override
    public boolean isValid() {
      var result = true;
      for (var i = 0; i < elements; i++) {
        var valParent = storage.get(i);
        if (child1(i) < elements) {
          var valChild1 = storage.get(child1(i));
          // either the parent is less than the child or they're equal
          result =
              result && (lessThanFunc.test(valParent, valChild1) || valParent.equals(valChild1));
        }
        if (child2(i) < elements) {
          var valChild2 = storage.get(child2(i));
          // either the parent is less than the child or they're equal
          result =
              result && (lessThanFunc.test(valParent, valChild2) || valParent.equals(valChild2));
        }
      }

      return result;
    }

    private Seq<T> storageAsList() {
      return Stream.ofAll(storage).take(elements);
    }

    @Override
    public String toString() {
      // this returns the heap in its *internal* order, not in sorted order
      return storageAsList().map(Strings::objectToEscapedString).mkString("BinaryHeap(", ", ", ")");
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BinaryHeapImpl)) {
        return false;
      }

      final var oHeap = (BinaryHeapImpl) o;

      // Philosophical question: what does it mean to compare equality
      // of two binary heaps? Should we test whether they contain the
      // same *set* of entries, regardless of order, or should we test
      // whether they contain the same entries in the same order? Since
      // a BinaryHeap is not a Set, we'll go with an ordering-specific
      // solution.

      return storageAsList().equals(oHeap.storageAsList());
    }

    @Override
    public int hashCode() {
      return storageAsList().hashCode();
    }
  }
}
