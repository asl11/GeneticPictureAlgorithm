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

package edu.rice.sexpr;

import static edu.rice.vavr.Sequences.seqGetOption;

import io.vavr.PartialFunction;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.function.Function;

/**
 * S-Expression Value. The way we're doing it here is a bit like: <br>
 * Value ::= Word | SExpr <br>
 * SExpr ::= ( ValueList ) <br>
 * ValueList ::= Value ValueList || *Nothing*
 *
 * <p>If you look carefully, you'll notice that the definition of a ValueList is basically identical
 * to the data definition of a linked list, and we'll internally store our ValueLists as exactly
 * that: Seq&lt;Value&gt;.
 */
public interface Value {
  /**
   * General-purpose structural pattern matching on an s-expression value, with one lambda per
   * concrete type of the Value. Typical usage:
   *
   * <pre>
   * Value val = ... ;
   * Option&lt;Whatever&gt; oresult = val.match(
   *     word -&gt; Option.some(word.something()),
   *     sexpr -&gt; Option.none());
   * </pre>
   */
  default <Q> Q match(Function<Word, Q> wordF, Function<Sexpr, Q> sexprF) {
    if (this instanceof Word) {
      return wordF.apply(asWord());
    } else if (this instanceof Sexpr) {
      return sexprF.apply(asSexpr());
    } else {
      // This will never actually happen, but we're just making sure.
      throw new RuntimeException(
          "this should never happen! unexpected value type: " + getClass().getName());
    }
  }

  /**
   * If you know that this Value is <b>definitely</b> a Word, this method does the casting for you
   * in a nice, pipelined fashion. If you're not sure, then use {@link #match(Function, Function)}.
   */
  default Word asWord() {
    return (Word) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a Word, this method does the casting for you
   * in a nice, pipelined fashion, giving you the String inside the Word. Equivalent to <code>
   * asWord().get()</code>.
   */
  default String asWordString() {
    return asWord().get();
  }

  /**
   * If you know that this Value is <b>definitely</b> a Sexpr, this method does the casting for you
   * in a nice, pipelined fashion.
   */
  default Sexpr asSexpr() {
    return (Sexpr) this;
  }

  /** Handy builder method for making an s-expression. */
  static Value sexpr(Seq<Value> values) {
    return new Sexpr(values);
  }

  /** Handy builder method for making an s-expression, here allowing varargs. */
  static Value sexpr(Value... values) {
    return sexpr(Stream.of(values));
  }

  /** Handy builder method for making words inside an s-expression. */
  static Value word(String str) {
    return new Word(str);
  }

  class Sexpr implements Value, PartialFunction<Integer, Value> {
    private final Seq<Value> values;

    private Sexpr(Seq<Value> values) {
      this.values = values;
    }

    /** Returns a sequence of the Values in the Sexpr. */
    public Seq<Value> getSeq() {
      return values;
    }

    /** Returns the nth value in the Sexpr in an {@link Option}. */
    public Option<Value> get(int i) {
      return seqGetOption(values, i);
    }

    @Override
    public Value apply(Integer i) {
      return values.get(i);
    }

    @Override
    public boolean isDefinedAt(Integer i) {
      return get(i).isDefined();
    }

    @Override
    public String toString() {
      return values.mkString("( ", " ", " )");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Sexpr)) {
        return false;
      }

      var sexpr = (Sexpr) o;

      return values.equals(sexpr.values);
    }

    @Override
    public int hashCode() {
      return values.hashCode();
    }
  }

  class Word implements Value {
    private final String word;

    private Word(String word) {
      this.word = word;
    }

    /** Gives you back the number inside the Word as a String. */
    public String get() {
      return word;
    }

    @Override
    public String toString() {
      return word;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Word)) {
        return false;
      }

      var ow = (Word) o;

      return ow.get().equals(word);
    }

    @Override
    public int hashCode() {
      return word.hashCode();
    }
  }
}
