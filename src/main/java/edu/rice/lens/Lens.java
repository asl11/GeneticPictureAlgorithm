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

import edu.rice.vavr.Sequences;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Don't you wish you could mutate a functional data structure? Lenses are an abstraction for doing
 * this. The broad idea is that you ask for a lens to some arbitrarily deep part of a data
 * structure, and then you've got a "set" method which lets you "mutate" it, while actually just
 * returning an entirely new copy of the data structure. The resulting APIs feel a lot like
 * "mutating" a map with {@link io.vavr.collection.Map#put(Object, Object)} or a list with {@link
 * io.vavr.collection.Seq#update(int, Object)}, where return a new instance that has the updated
 * values, while the original is completely unchanged.
 *
 * <p>To keep things clear, we'll refer to a class that has a series of final values within it as a
 * "data class". One data class might well have instances of other data classes within it. We'll use
 * the terms "outer data class" and "inner data class", respectively, to keep this clear. Each data
 * class will define Lenses that give a view toward their internals. Lenses can be composed together
 * (since they're just functions) and they ultimately give us convenient getters (that's the easy
 * part) and setters. A setter is just a function that takes the outer data class and a value you
 * want to update somewhere deep inside, and you get back a new copy of the outer and inner data
 * classes. The originals never mutate.
 *
 * <p>See the code in MonkeyExample or TimeExample (in the unit tests folder) for how this all fits
 * together. We've got a more complicated version, {@link Sequences#lensSeq(int)} for dealing with
 * lists, and in week9 you're building ({@link edu.rice.json.Operations#lensPath(String)}, for
 * dealing with JSON structures.
 */
public interface Lens<A, B> {
  // We might express a Lens as a data definition, in that a Lens is a
  // getter-function and a setter-function, but that's not
  // particularly interesting or useful. Let's instead talk about the
  // properties that a Lens should have when you use it (courtesy Seth
  // Tisue):

  // (0. if I get twice, I get the same answer)
  // 1. if I get, then set it back, nothing changes.
  // 2. if I set, then get, I get what I set.
  // 3. if I set twice then get, I get the second thing I set.

  // These properties explain how getter and setter lambdas must play nicely together.

  /**
   * Makes a lens, given the getter function (fget) and setter function (fset). The outer data class
   * is of type A and the inner data class is of type B.
   */
  static <A, B> Lens<A, B> lens(Function<A, B> fget, BiFunction<A, B, A> fset) {
    return new LensImpl<>(fget, fset);
  }

  /** Fetch the getter function from within the Lens. */
  Function<A, B> getter();

  /** Fetch the setter function from within the Lens. */
  BiFunction<A, B, A> setter();

  /**
   * Apply this function to a data class to get a value from within.
   *
   * @param a The outer data class that we're "lensing" into.
   * @return The value of the inner data class that we want.
   */
  default B get(A a) {
    return getter().apply(a);
  }

  /**
   * Apply this function to an outer data class along with a value that you want to update within
   * the data class, and you'll get back a new version of the outer data class. The original does
   * not mutate.
   *
   * @param a The outer data class that we're "lensing" into.
   * @param b A value for the inner data class that we want to update.
   * @return A new instance of the outer data class with the inner value updated.
   */
  default A set(A a, B b) {
    return setter().apply(a, b);
  }

  /**
   * This is a version of {@link #set(Object, Object)} where we apply a given function to the old
   * value to yield the new value. You can also think of this as "mapping" the function "f" over the
   * lensed data class "a".
   *
   * @param a The outer data class that we're "lensing" into.
   * @param f A unary operator to apply to the inner value
   * @return A new instance of the outer data class with the inner value updated.
   */
  default A update(A a, UnaryOperator<B> f) {
    return set(a, f.apply(get(a)));
  }

  /**
   * Lenses can be composed together. If you've got a lens from A to B and another lens from B to C,
   * you can compose them together to get a lens from A to B to C all in one go. Analogous to
   * composing functions with {@link Function#andThen(Function)}.
   *
   * @param that A lens from B to C
   * @param <C> the type of some inner data class of B
   * @return a lens from A through B to C
   */
  default <C> Lens<A, C> andThen(Lens<B, C> that) {
    return new LensImpl<>(
        a -> that.get(this.get(a)), (a, c) -> this.update(a, b -> that.set(b, c)));
  }

  /**
   * Since we can compose lenses all we want, it's useful to have an "identity" lens that you can
   * compose with anything, where x.andThen(identity) is the same as x.
   */
  static <T> Lens<T, T> identity() {
    return MonoLens.identity(); // we're using one singleton for both Lens and MonoLens
  }

  /**
   * Given a lens from A to B and a function from B to B, returns a "lifted" version of that same
   * function that now works on "A"s. Really just a nice wrapper for {@link #update(Object,
   * UnaryOperator)}.
   */
  default UnaryOperator<A> lift(UnaryOperator<B> operator) {
    return a -> this.update(a, operator);
  }

  // Engineering note: why are we hiding class LensImpl inside of
  // interface Lens rather than just directly exposing the new Lens()
  // constructor? We could really do it either way, but it might be
  // convenient to offer different ways of making a lens at some point
  // in the future, which this implementation style supports more easily.

  // This is the "standard" Comp215 style, also used throughout VAVR.

  class LensImpl<A, B> implements Lens<A, B> {
    private final Function<A, B> fget;
    private final BiFunction<A, B, A> fset;

    private LensImpl(Function<A, B> fget, BiFunction<A, B, A> fset) {
      this.fget = fget;
      this.fset = fset;
    }

    @Override
    public Function<A, B> getter() {
      return fget;
    }

    @Override
    public BiFunction<A, B, A> setter() {
      return fset;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof LensImpl)) {
        return false;
      }

      LensImpl<?, ?> oImpl = (LensImpl<?, ?>) o;

      // It's a bit unclear whether two lambdas that look the same
      // will ultimately be "equal" to one another or not, but that's
      // *not our problem*. We're just defining lens equality in terms
      // of the underlying function equality. We're *delegating* to
      // the lambdas.
      return fget.equals(oImpl.fget) && fset.equals(oImpl.fset);
    }

    @Override
    public int hashCode() {
      // It's also a bit unclear what it means to take the hashCode of
      // a lens, but we can again delegate to the lambdas, which give
      // us their own hashCode methods.
      return fget.hashCode() * 31 + fset.hashCode();
    }

    @Override
    public String toString() {
      // And lastly, we again play the delegate-to-the-lambdas game
      // when converting a lens to a string.
      return String.format("Lens(%s, %s)", fget.toString(), fset.toString());
    }
  }
}

// Engineering notes:

// This Java code was borrowed from / inspired by:
// http://davids-code.blogspot.com/2014/02/immutable-domain-and-lenses-in-java-8.html

// The concept of Lenses, as a functional alternative to mutation
// of complex structures, originated with the Haskell programming
// language, and if you do a Google search on lenses, that's where
// you'll find most of the examples. For Comp215, we've simplified
// many aspects of Lenses. Probably the fanciest Java lens library
// is in Derive4J, which includes a dynamic code generator to write
// your lenses for you!
// https://github.com/derive4j/derive4j

// Want to learn more? Here's an hour-long video from 2013 by
// Simon Peyton Jones, one of the big names of the Haskell world,
// explaining all of this. You'll have to work a bit to adjust
// your brain to the Haskell syntax in his talk, and Haskell makes
// it much easier to abstract over type parameters than Java does,
// so what he's discussing wouldn't be easy to rewrite in
// Java. Still... check it out! Note that he goes into deep waters
// then back out again several times in his talk. Don't despair if
// you're missing parts.

// https://skillsmatter.com/skillscasts/4251-lenses-compositional-data-access-and-manipulation
// (Registration required to view the video, but it's free. Sorry
// about that.)

// In short, Lenses can do many more interesting things than we
// can possibly touch on in Comp215.  If you take Comp311, or if
// you ultimately adopt a programming language like Haskell where
// lenses are quite popular, you'll need to understand these
// things.

// If you want to see a more sophisticated Lens library applied to
// processing JSON datatypes, and you don't want to slog through
// learning Haskell syntax, you might want to check out the JSON
// processing in the Monacle library, which is written for Scala
// -- a functional programming language that runs above the JVM;
// Scala syntax is much closer to Java, so the resulting programs
// are a bit easier to read.
// https://github.com/julien-truffaut/Monocle/blob/master/example/src/test/scala/monocle/JsonExample.scala

// And lastly, there are some tutorials for lenses in Scala that
// you might find useful or entertaining:
// http://eed3si9n.com/learning-scalaz/Lens.html
// https://docs.google.com/presentation/d/1jpo-glo9DU5SA57gBslLm2wFR-8kQJtEnoDT-WSsYgY/edit#slide=id.p

// (That Google Docs presentation fed into our own Lens lecture.)
