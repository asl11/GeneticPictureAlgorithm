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

import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.Random;

/**
 * Reimplementation of {@link Treap} extending {@link Tree}, so it can reuse all the code in Tree.
 */
public interface ETreap<T extends Comparable<? super T>> extends ITree<T> {
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
    return of(Stream.of(values));
  }

  /** Given a sequence of values, returns a treap with those values. */
  static <T extends Comparable<? super T>> ITree<T> of(Seq<T> values) {
    return ETreap.<T>empty().addAll(values);
  }

  class Node<T extends Comparable<? super T>> extends Tree.Node<T> {
    final int priority;

    // external users: don't use this
    Node(T value, ITree<T> left, ITree<T> right, int priority) {
      super(value, left, right);
      this.priority = priority;
    }

    @Override
    Node<T> updateLeft(ITree<T> newLeft) {
      // Engineering note: These update methods are just so slightly
      // different here and in Tree.Node, so we can't inherit them. In
      // an earlier version of our Treap code, we didn't have these
      // update methods at all, which means that code like rotateLeft
      // and rotateRight ended up calling the constructors, which
      // means they couldn't be reused like we see here today.
      return new Node<>(value, newLeft, right, priority);
    }

    @Override
    Node<T> updateRight(ITree<T> newRight) {
      return new Node<>(value, left, newRight, priority);
    }

    @Override
    Node<T> updateValue(T newValue) {
      return new Node<>(newValue, left, right, priority);
    }

    @Override
    public ITree<T> add(T newbie) {
      // In Tree.Node's insert, it doesn't know about heapify(), so
      // again, things are just slightly different enough that we
      // can't inherit.
      var comparison = newbie.compareTo(value);
      if (comparison < 0) {
        return updateLeft(left.add(newbie)).heapify();
      }
      if (comparison > 0) {
        return updateRight(right.add(newbie)).heapify();
      }

      // if it's exactly the same object that's already there, then no merge necessary
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
    public ITree<T> remove(T deadValue) {
      // We can't inherit the remove method, because here, unlike
      // Tree, we have to preserve the heap property.

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
  }

  class Empty<T extends Comparable<? super T>> extends Tree.Empty<T> implements ETreap<T> {
    private static final ITree<?> SINGLETON = new ETreap.Empty<>();
    private static final Random RNG = new Random();

    // external user: don't call this; instead call empty()
    Empty() {}

    @Override
    public ITree<T> add(T value) {
      return new ETreap.Node<>(value, this, this, RNG.nextInt());
    }
  }
}
