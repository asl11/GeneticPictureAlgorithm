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

/**
 * A DTreap is a deterministic treap. Unlike a traditional {@link Treap} where the priority is
 * derived at random, here the priority comes from the hash of the value being inserted. Assuming
 * the hash function is "good", then the results will still be probabilistically balanced. However,
 * now the shape of the tree will be *identical* regardless of insertion order. The benefit of this
 * is that the equals() and hashCode() methods now becomes useful, regardless of insertion order.
 *
 * <p>Because a DTreap is so similar to a Treap, this code is really an exercise in how much code we
 * can reuse from Treap to DTreap.
 */
public interface DTreap<T extends Comparable<? super T>> extends ITree<T> {
  // Engineering note: notice how thin DTreap can be? We're reusing
  // the same node layout as in Treap. All we're doing is changing how
  // the priorities are set. Everything else is the same!

  // We do need to copy the static methods (empty() and of()),
  // since those also have dependencies that change, and inheritance
  // doesn't do anything for static methods.

  /**
   * Construct an empty deterministic treap of the given type parameter.
   *
   * @param <T> any comparable type
   */
  @SuppressWarnings("unchecked")
  static <T extends Comparable<? super T>> ITree<T> empty() {
    return (ITree<T>) DEmpty.SINGLETON;
  }

  /**
   * Given a bunch of values passed as varargs to this function, returns a deterministic treap with
   * those values.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T extends Comparable<? super T>> ITree<T> of(T... values) {
    return of(Stream.of(values));
  }

  /** Given a sequence of values, returns a deterministic treap with those values. */
  static <T extends Comparable<? super T>> ITree<T> of(Seq<T> values) {
    return DTreap.<T>empty().addAll(values);
  }

  class DEmpty<T extends Comparable<? super T>> extends Tree.Empty<T> implements DTreap<T> {
    private static final ITree<?> SINGLETON = new DEmpty<>();

    // external user: don't call this; instead call empty()
    private DEmpty() {}

    @Override
    public ITree<T> add(T value) {
      // Engineering note: This is the exactly one place where a
      // DTreap is different from a Treap, because this was the one
      // place, in the original Treap code, where priorities were
      // generated.  Everywhere else that priorities are handled,
      // they're either read-only, or they're copied-verbatim.

      // Also, when we add "extends Tree.Empty<T>", suddenly "new
      // Node" means "new Tree.Node", which isn't what we want. Have
      // to be careful!
      return new Treap.Node<>(value, this, this, value.hashCode());
    }
  }
}
