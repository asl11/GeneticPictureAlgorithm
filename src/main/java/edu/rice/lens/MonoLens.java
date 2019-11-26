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

package edu.rice.lens;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A mono-typed version of a {@link Lens}, analogous to a {@link java.util.function.UnaryOperator}.
 * Simply, a <code>MonoLens&lt;A&gt;</code> is interchangeable with a <code>Lens&lt;A, A&gt;</code>.
 * Using MonoLens can make your code cleaner if you're using lenses on recursive data types, where
 * the outer data class and inner data class are the same type (e.g., trees).
 *
 * <p>Note that MonoLens defines {@link #andThenMono(MonoLens)} and you can also use {@link
 * Lens#andThen(Lens)}. The former lets you compose two mono-lenses and get another mono-lens. The
 * latter lets you compose a mono-lens with any other lens and the result is a Lens, but no longer a
 * mono-lens.
 */
public interface MonoLens<A> extends Lens<A, A> {
  // Engineering notes: if Java let us declare that MonoLens<A> was a
  // "type alias" for for Lens<A,A>, then this whole file could be
  // compressed to that one single declaration. We've run into this
  // limitation of the Java programming language before and we'll
  // run into it again before the semester ends. Many other statically
  // typed programming languages have this, but Java does not.

  // Also, what exactly should you *name* a Lens from a type to
  // itself? Java lets you use UnaryOperator<A> in place of
  // Function<A, A>, but would it makes sense to say UnaryLens or
  // UnaryOperatorLens?  That feels a bit confusing. Likewise, if "a
  // monad is just a monoid on the category of endofunctors", maybe we
  // could say EndoLens? That sounds like a snowboarding move. We're
  // instead settling on MonoLens, implying a mono-typed lens.

  /**
   * Makes a lens, given the getter function (fget) and setter function (fset). The outer data class
   * and inner data class are both of type A.
   */
  static <A> MonoLens<A> monoLens(UnaryOperator<A> fget, BinaryOperator<A> fset) {
    return new MonoLensImpl<>(fget, fset);
  }

  /**
   * Lenses can be composed together. If you've got a lens from A to B and another lens from B to C,
   * you can compose them together to get a lens from A to B to C all in one go. Analogous to
   * composing functions with {@link Function#andThen(Function)}. Since a MonoLens is also a Lens,
   * you can use {@link Lens#andThen(Lens)} to compose with a lens that has different types for the
   * outer data class and the inner data class.
   *
   * @param that A lens from B to C
   * @return a lens from A through B to C
   */
  default MonoLens<A> andThenMono(MonoLens<A> that) {
    return new MonoLensImpl<>(
        a -> that.get(this.get(a)), (a, c) -> this.update(a, b -> that.set(b, c)));
  }

  /**
   * Since we can compose lenses all we want, it's useful to have an "identity" lens that you can
   * compose with anything, where x.andThen(identity) is the same as x.
   */
  @SuppressWarnings("unchecked")
  static <T> MonoLens<T> identity() {
    // We're using a singleton for the identity lens. Just like we did
    // when doing singleton empty lists or empty trees, this is safe
    // because the code in question (the lambdas defined in
    // SINGLETON_IDENTITY) never call methods on their
    // arguments. After type erasure, they're just Objects, and all we
    // do is return one of the arguments without calling anything.

    return (MonoLens<T>) MonoLensImpl.SINGLETON_IDENTITY;
  }

  class MonoLensImpl<A> implements MonoLens<A> {
    static final MonoLens<?> SINGLETON_IDENTITY =
        new MonoLensImpl<>(
            // Getter of an identity lens is straightforward: we
            // return the value, since we're not exactly zooming in.
            a -> a,

            // Setter of an identity lens is a bit of a brain twister:
            // should we return oldA or newA? Logically, this lambda
            // is updating oldA (the original value) with newA (the
            // updated value), rather than zooming in, which makes
            // sense. Rather than updating a part of an object, we're
            // updating the entire object.
            (oldA, newA) -> newA);

    private final UnaryOperator<A> fget;
    private final BinaryOperator<A> fset;

    private MonoLensImpl(UnaryOperator<A> fget, BinaryOperator<A> fset) {
      this.fget = fget;
      this.fset = fset;
    }

    @Override
    public Function<A, A> getter() {
      return fget;
    }

    @Override
    public BiFunction<A, A, A> setter() {
      return fset;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof MonoLens.MonoLensImpl)) {
        return false;
      }

      MonoLensImpl<?> oImpl = (MonoLensImpl<?>) o;

      return fget.equals(oImpl.fget) && fset.equals(oImpl.fset);
    }

    @Override
    public int hashCode() {
      return fget.hashCode() * 31 + fset.hashCode();
    }

    @Override
    public String toString() {
      return String.format("MonoLens(%s, %s)", fget.toString(), fset.toString());
    }
  }
}
