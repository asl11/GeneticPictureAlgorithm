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

import static edu.rice.tree.Tree.Node.equalsHelper;
import static edu.rice.tree.Tree.Node.hashCodeHelper;
import static edu.rice.tree.Tree.Node.toStringHelper;
import static io.vavr.control.Option.some;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.Random;

/**
 * General-purpose randomly balanced tree, based on <a
 * href="https://faculty.washington.edu/aragon/treaps.html">Aragon and Seidel's Treap data
 * structure</a>.
 */
public interface Treap<T extends Comparable<? super T>> extends ITree<T> {
  /**
   * Construct an empty treap of the given type parameter.
   *
   * @param <T> any comparable type
   */
  @SuppressWarnings("unchecked")
  static <T extends Comparable<? super T>> ITree<T> empty() {
    return (ITree<T>) Empty.SINGLETON;
  }

  /**
   * Given a bunch of values passed as varargs to this function, returns a treap with those values.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T extends Comparable<? super T>> ITree<T> of(T... values) {
    return ofAll(Stream.of(values));
  }

  /** Given any VAVR {@link io.vavr.collection.Seq}, returns a treap with those values. */
  static <T extends Comparable<? super T>> ITree<T> ofAll(Seq<T> values) {
    return Treap.<T>empty().addAll(values);
  }

  class Node<T extends Comparable<? super T>> implements Treap<T> {
    final int priority;
    final T value;
    final ITree<T> left;
    final ITree<T> right;

    // external users: don't use this
    Node(T value, ITree<T> left, ITree<T> right, int priority) {
      this.value = value;
      this.left = left;
      this.right = right;
      this.priority = priority;
    }

    @Override
    public T getValue() {
      return value;
    }

    @Override
    public ITree<T> getLeft() {
      return left;
    }

    @Override
    public ITree<T> getRight() {
      return right;
    }

    Node<T> updateLeft(ITree<T> newLeft) {
      return new Node<>(value, newLeft, right, priority); // warning: doesn't automatically heapify!
    }

    Node<T> updateRight(ITree<T> newRight) {
      return new Node<>(value, left, newRight, priority); // warning: doesn't automatically heapify!
    }

    Node<T> updateValue(T newValue) {
      return new Node<>(newValue, left, right, priority);
    }

    @Override
    public ITree<T> add(T newbie) {
      var comparison = newbie.compareTo(value);
      if (comparison < 0) {
        return updateLeft(left.add(newbie)).heapify();
      }
      if (comparison > 0) {
        return updateRight(right.add(newbie)).heapify();
      }

      // if it's exactly the same object that's already there, then no
      // merge necessary
      if (this.value == newbie) {
        return this;
      }

      // This is a curious case. If we're equal (this.equals(newbie)),
      // but not the same (this.value != newbie), then we're going to
      // update the value in place. This will be useful for key/value
      // stores where the equals method operates on the keys.

      // note that we use the same priority as before, and the merged
      // so there's no need for rotation
      return updateValue(newbie);
    }

    /** Returns a new tree that satisfies the min-heap property (smallest priority on top). */
    ITree<T> heapify() {
      var leftP = left.getPriority();
      var rightP = right.getPriority();

      // if we're satisfying the heap property, then we're done
      if (priority <= leftP && priority <= rightP) {
        return this;
      }

      // if we're here, then we know the current node is not the
      // smallest, therefore either the left or right is smaller;
      // choose the smallest and rotate that way
      if (leftP < rightP) {
        return rotateRight();
      } else {
        return rotateLeft();
      }
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    ITree<T> rotateRight() {
      return left.match(
          emptyTree -> this,
          (lValue, lLeft, lRight) -> ((Node<T>) left).updateRight(this.updateLeft(lRight)));
    }

    ITree<T> rotateLeft() {
      return right.match(
          emptyTree -> this,
          (rValue, rLeft, rRight) -> ((Node<T>) right).updateLeft(this.updateRight(rLeft)));
    }

    @Override
    public ITree<T> remove(T deadValue) {
      var comparison = deadValue.compareTo(this.value);
      if (comparison == 0) {
        // we need to remove the tree head; first see if we have an easy out
        if (left.isEmpty()) {
          return right;
        }
        if (right.isEmpty()) {
          return left;
        }

        // okay, both left and right are present, so we'll rotate and try again, trying
        // to preserve the treap property while we're at it
        if (left.getPriority() < right.getPriority()) {
          return rotateRight().remove(deadValue);
        } else {
          return rotateLeft().remove(deadValue);
        }

      } else if (comparison < 0) {
        // it's to the left
        return updateLeft(left.remove(deadValue));
      } else {
        // it's to the right
        return updateRight(right.remove(deadValue));
      }
    }

    @Override
    public int hashCode() {
      return hashCodeHelper(this);
    }

    @Override
    public ITree<T> greaterThan(T floor, boolean inclusive) {
      var comparison = floor.compareTo(value);
      if (comparison == 0 && !inclusive) {
        return right;
      }
      if (comparison == 0) {
        return updateLeft(empty());
      }

      // if the floor is entirely to the right
      if (comparison > 0) {
        return right.greaterThan(floor, inclusive);
      }

      // the floor is somewhere to the left
      return updateLeft(left.greaterThan(floor, inclusive));
    }

    @Override
    public ITree<T> lessThan(T ceiling, boolean inclusive) {
      var comparison = ceiling.compareTo(value);
      if (comparison == 0 && !inclusive) {
        return left;
      }
      if (comparison == 0) {
        return updateRight(empty());
      }

      // if the ceiling is entirely to the left
      if (comparison < 0) {
        return left.lessThan(ceiling, inclusive);
      }

      // the ceiling is somewhere to the right
      return updateRight(right.lessThan(ceiling, inclusive));
    }

    @Override
    public int getPriority() {
      return priority;
    }

    @Override
    public boolean isValid() {
      var leftGood =
          left.isEmpty()
              || (left.getPriority() >= priority && left.getValue().compareTo(value) <= 0);
      var rightGood =
          right.isEmpty()
              || (right.getPriority() >= priority && value.compareTo(right.getValue()) <= 0);

      return leftGood && rightGood && left.isValid() && right.isValid();
    }

    @Override
    public Option<Tuple2<T, ITree<T>>> removeMin() {
      // Recursively, removeMin() will never return an Option.none(),
      // unless it's called on on an empty tree, and that's handled in
      // ITreeEmpty.  Consequently, we don't have to worry about
      // removeMin.get() failing.

      return left.isEmpty()
          // if there are no left-subchildren, then we've found the
          // minimum value, and the tree without the minimum value is
          // just the right subtree
          ? some(Tuple.of(value, right))
          : left.removeMin()
              .get()
              .apply(
                  (minValue, remainingTree) -> some(Tuple.of(minValue, updateLeft(remainingTree))));
    }

    @SuppressWarnings(
        "EqualsWhichDoesntCheckParameterClass") // checked in the helper method, so okay here
    @Override
    public boolean equals(Object o) {
      return equalsHelper(this, o);
    }

    @Override
    public String toString() {
      return toStringHelper(this);
    }
  }

  class Empty<T extends Comparable<? super T>> implements Treap<T>, ITree.Empty<T> {
    private static final ITree<?> SINGLETON = new Treap.Empty<>();
    private static final Random RNG = new Random();

    // external user: don't call this; instead call empty()
    Empty() {}

    @Override
    public ITree<T> add(T value) {
      return new Node<>(value, this, this, RNG.nextInt());
    }

    @Override
    public String toString() {
      return toStringHelper(this);
    }

    @SuppressWarnings(
        "EqualsWhichDoesntCheckParameterClass") // checked in the helper method, so okay here
    @Override
    public boolean equals(Object o) {
      return equalsHelper(this, o);
    }

    @Override
    public int hashCode() {
      return hashCodeHelper(this);
    }
  }
}
