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
import static edu.rice.util.Strings.objectToEscapedString;
import static io.vavr.control.Option.some;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;

/** General-purpose unbalanced binary tree, parameterized over any comparable type. */
public interface Tree<T extends Comparable<? super T>> extends ITree<T> {

  /**
   * Returns an empty tree of the given type parameter.
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
    return Tree.<T>empty().addAll(values);
  }

  class Node<T extends Comparable<? super T>> implements ITree<T> {
    protected final ITree<T> left;
    protected final ITree<T> right;
    protected final T value;

    // external tree users: don't use this; instead, insert to an empty tree
    protected Node(T value, ITree<T> left, ITree<T> right) {
      this.left = left;
      this.right = right;
      this.value = value;
    }

    @Override
    public T getValue() {
      return value;
    }

    /** Gets the left subtree. */
    @Override
    public ITree<T> getLeft() {
      return left;
    }

    @Override
    public ITree<T> getRight() {
      return right;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public ITree<T> add(T newbie) {
      var comparison = newbie.compareTo(value);
      if (comparison < 0) {
        return updateLeft(left.add(newbie));
      }
      if (comparison > 0) {
        return updateRight(right.add(newbie));
      }

      // If the newbie is exactly the same as what's there, then we don't need to update anything.
      if (this.value == newbie) {
        return this;
      }

      // This is a curious case. If we're equal (this.value.equals(newbie)),
      // but not the same (this.value != newbie), then we're going to update
      // the value in place. This could be useful if we were using the tree
      // as a key/value stores where the equals method operates on the keys.

      return updateValue(newbie);
    }

    @Override
    public int hashCode() {
      return hashCodeHelper(this);
    }

    @SuppressWarnings(
        "EqualsWhichDoesntCheckParameterClass") // checked in the helper method, so okay here
    @Override
    public boolean equals(Object o) {
      return equalsHelper(this, o);
    }

    // Engineering note: We're doing something a bit interesting with
    // these updateLeft/updateRight/updateValue methods.  There are a
    // number of places where we need to say "return the current tree,
    // only with the left-subtree updated to the result of this
    // recursive call" and so forth. Without these update methods, we
    // originally had calls to create new Node<>(...) spread
    // throughout this file. Doing it this way, the code gets
    // cleaner. When you read it, it's clear which part we're updating
    // and which parts are staying the same. Code that's easier to
    // read is code that's easier to debug.

    // Additionally, notice how these methods are neither public nor
    // private? They're "package" scope, which means we can write unit
    // tests for them, so long as those unit tests are within
    // edu.rice.tree. Why not just make them public?  Because they're
    // not part of the ITree interface. We want external users of
    // Tree, or any other implementation of ITree, to stick with the
    // methods defined by ITree. That gives us the flexibility, in
    // turn, to redesign the guts of this Tree class. For example, if
    // an external client could call updateValue(), then they could
    // create a Tree that violates the "tree property" (that
    // everything to the left is "less than" the current value and
    // everything to the right is "greater than" the current value).

    // Engineering rule of thumb: any method for which you might want
    // the freedom to change in the future, or that could be used to
    // violate your data structure's invariants, should never be
    // public.

    Node<T> updateLeft(ITree<T> newLeft) {
      return new Node<>(value, newLeft, right);
    }

    Node<T> updateRight(ITree<T> newRight) {
      return new Node<>(value, left, newRight);
    }

    Node<T> updateValue(T newValue) {
      return new Node<>(newValue, left, right);
    }

    ITree<T> rotateRight() {
      // we could also write this by asking left.empty() and having a
      // ternary operator, but it's arguably cleaner to do the same
      // thing with the match method. Notice how the deconstructing
      // aspect of the second lambda is somewhat unhelpful, since
      // we're using left.updateRight().  If we instead were writing
      // new Node<>(...), then we'd use those deconstructed values.

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
    public String toString() {
      return toStringHelper(this);
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

        // we could arbitrarily decide about rotating right or left at this point
        // (for treaps, it matters; here it doesn't)
        return rotateRight().remove(deadValue);
      } else if (comparison < 0) {
        // it's to the left
        return updateLeft(left.remove(deadValue));
      } else {
        // it's to the right
        return updateRight(right.remove(deadValue));
      }
    }

    @Override
    public boolean isValid() {
      // it's a start; doesn't validate that *every* left-val is less than the current-val, but
      // at least recursively checks basic tree properties
      return (left.isEmpty() || left.getValue().compareTo(value) < 0)
          && (right.isEmpty() || value.compareTo(right.getValue()) < 0)
          && left.isValid()
          && right.isValid();
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

    /**
     * This is the internal function that we'll use to convert all trees to strings. This will work
     * over all ITree implementations; we're making it static so it's easier to call from the
     * outside.
     */
    static <T extends Comparable<? super T>> String toStringHelper(ITree<T> tree) {
      return tree.match(
          emptyTree -> "Tree()",
          (elem, leftTree, rightTree) -> {
            // if there's a priority (i.e., if we're dealing with a
            // treap) then we want to include it
            var priority = tree.getPriority();
            var priorityString =
                (priority != Integer.MAX_VALUE) ? String.format("priority=%d, ", priority) : "";

            // special handling for leaf nodes, so they're easier to read
            if (leftTree.isEmpty() && rightTree.isEmpty()) {
              return String.format("Tree(%s%s)", priorityString, objectToEscapedString(elem));
            } else {
              return String.format(
                  "Tree(%s%s, %s, %s)",
                  priorityString,
                  toStringHelper(leftTree),
                  objectToEscapedString(elem),
                  toStringHelper(rightTree));
            }
          });
    }

    /**
     * Deep structural equality of the trees. If you want set-equality, then convert to a list
     * first. This will work over all ITree implementations; we're making it static so it's easier
     * to call from the outside.
     */
    static <T extends Comparable<? super T>> boolean equalsHelper(ITree<T> tree, Object o) {
      if (!(o instanceof ITree<?>)) {
        return false;
      }

      // Note: we don't actually care what the type parameter
      // is. Ultimately, we're just calling Object.equals(), which for
      // all types should work, since every good equals() method
      // should behave as with the lines above, rejecting something of
      // an incompatible type.
      var otherTree = (ITree<?>) o;

      return tree.match(
          treeEmpty -> otherTree.isEmpty(),
          (treeVal, treeLeft, treeRight) ->
              otherTree.match(
                  otherEmpty -> false,
                  (otherVal, otherLeft, otherRight) ->
                      treeVal.equals(otherVal)
                          && treeLeft.equals(otherLeft)
                          && treeRight.equals(otherRight)));
    }

    /** Computing hashes over a tree. */
    static <T extends Comparable<? super T>> int hashCodeHelper(ITree<T> tree) {
      return tree.match(
          emptyTree -> 1,
          (elem, leftTree, rightTree) ->
              elem.hashCode() * 71
                  + leftTree.hashCode() * 31
                  + rightTree.hashCode()); // a kludge, but it's something
    }
  }

  /**
   * This class implements the case where a Tree might be empty. External users will never use
   * Tree.Empty directly but will instead use the public interface (ITree).
   */
  class Empty<T extends Comparable<? super T>> implements Tree<T>, ITree.Empty<T> {
    private static final ITree<?> SINGLETON = new Tree.Empty<>();

    // external user: don't call this; instead use empty()
    protected Empty() {}

    @Override
    public ITree<T> add(T value) {
      return new Node<>(value, this, this);
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
