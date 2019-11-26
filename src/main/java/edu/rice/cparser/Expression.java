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

package edu.rice.cparser;

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jstring;
import static edu.rice.vavr.Sequences.seqMatch;

import edu.rice.json.Value;
import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.function.Function;

/**
 * All {@link ParserFunction} parsers return an Expression, which could be one of the
 * implementations within this interface (terminals, pairs, nothings), or could be any other class
 * that implements this same interface.
 */
public interface Expression<T extends Enum<T> & TokenPatterns> {
  // Data definition: an expression can be:

  //   a terminal (i.e., a single token)
  //   a pair of expressions
  //   a "nothing" expression
  //   --or--
  //   anything else, anywhere else, that implements this Expression interface

  // A simple parser made by combining ParserFunction's together will ultimately yield
  // pairs of expressions bottoming out at terminals and nothings.

  // A fancier parser will use Result.mapExpression() to replace these expressions with other
  // expressions (still implementing the same interface) that correspond to specific
  // value types for that parser's particular language.

  /**
   * Gets the results as a list, no matter what it is. A terminal expression is converted to a list
   * with one element. An {@link ExprPair} is treated much like our original definition of a {@link
   * List}, where we defined a list as a head and a tail-list or empty-list, interpreted here as the
   * "nothing" expression.
   *
   * <p>Build your grammars in the same fashion, and this method will be useful for processing the
   * result of the parser.
   */
  default Seq<Expression<T>> asList() {
    // This is a pretty lame default, and most implementers of this interface
    // will override it. But if they don't care, we might as well have something
    // here that will always work, even if it's not useful.
    return Stream.of(this);
  }

  /**
   * Convenience function. When you know, from its position in the grammar, that an expression is
   * really an ExprPair, this is somewhat more pleasant to use than a typecast.
   */
  default ExprPair<T> asExprPair() {
    if (this instanceof ExprPair<?>) {
      return (ExprPair<T>) this;
    } else {
      throw new IllegalArgumentException("Expression isn't an ExprPair: " + toString());
    }
  }

  /**
   * Convenience function. When you know, from its position in the grammar, that an expression is
   * really a Terminal, this is somewhat more pleasant to use than a typecast.
   */
  default Terminal<T> asTerminal() {
    if (this instanceof Terminal<?>) {
      return (Terminal<T>) this;
    } else {
      throw new IllegalArgumentException("Expression isn't a Terminal: " + toString());
    }
  }

  /**
   * Convenience function. When you know, from its position in the grammar, that an expression is
   * really a Nothing, this is somewhat more pleasant to use than a typecast.
   */
  default Nothing<T> asNothing() {
    if (this instanceof Nothing<?>) {
      return (Nothing<T>) this;
    } else {
      throw new IllegalArgumentException("Expression isn't a Nothing: " + toString());
    }
  }

  /**
   * General-purpose matching function, including four lambdas: one of the Expression is a {@link
   * Terminal} one if it's a {@link Nothing}, one if it's an {@link ExprPair}, and lastly one lambda
   * for any other possible type which may occur since the Expression interface is going to be
   * extended by many other possible production types.
   */
  default <R> R match(
      Function<Terminal<T>, R> terminalFunc,
      Function<Nothing<T>, R> nothingFunc,
      Function<ExprPair<T>, R> pairFunc,
      Function<Expression<T>, R> otherFunc) {
    if (this instanceof Terminal<?>) {
      return terminalFunc.apply(asTerminal());
    } else if (this instanceof Nothing<?>) {
      return nothingFunc.apply(asNothing());
    } else if (this instanceof ExprPair<?>) {
      return pairFunc.apply(asExprPair());
    } else {
      return otherFunc.apply(this);
    }
  }

  /** Makes a "nothing" expression, containing no tokens within. */
  @SuppressWarnings("unchecked")
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprNothing() {
    return (Expression<T>) Nothing.SINGLETON;
  }

  /** Makes a "terminal" expression, containing exactly one token within. */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprTerminal(Token<T> token) {
    return new Terminal<>(token);
  }

  /** Makes a "terminal" expression, containing exactly one token within. */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprTerminal(T tokenType, String value) {
    return new Terminal<>(new Token<>(tokenType, value));
  }

  /**
   * Makes a pair of expressions, used by various parser combinators. If expressions are combined in
   * the same fashion as lists (having a head and tail-list or nothing), then they can be easily
   * converted back to a {@link Seq} using {@link #asList()}.
   */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprPair(
      Expression<T> exprA, Expression<T> exprB) {
    return new ExprPair<>(exprA, exprB);
  }

  /**
   * Given a sequence of expressions, returns them using {@link #exprPair(Expression, Expression)}'s
   * such that they can be suitable converted back to a list later on using {@link #asList()}.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprSequence(Expression<T>... exprs) {
    return (exprs.length < 1)
        ? Log.ethrow("exprSequence", "internal error: shouldn't have empty input for exprSequence")
        : exprList(Stream.of(exprs));
  }

  /**
   * Given a list of expressions, returns them using {@link #exprPair(Expression, Expression)}'s
   * such that they can be suitable converted back to a list later on using {@link #asList()}.
   */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprList(Seq<Expression<T>> exprs) {
    return seqMatch(
        exprs,
        emptyList ->
            Log.ethrow("exprList", "internal error: shouldn't have empty input for exprList"),
        (head, noTail) -> head,
        (head, middle, tail) -> new ExprPair<>(head, exprList(exprs.tail())));
  }

  /** Returns a JSON representation of the parser result. */
  Value toJson();

  class Terminal<T extends Enum<T> & TokenPatterns> implements Expression<T> {
    public final Token<T> token;

    private Terminal(Token<T> token) {
      this.token = token;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Terminal<?>)) {
        return false;
      }

      Terminal<?> other = (Terminal<?>) o;
      return token.equals(other.token);
    }

    @Override
    public Value toJson() {
      return token.toJson();
    }

    @Override
    public String toString() {
      return "Terminal" + token; // printing the token brings its own parentheses
    }

    @Override
    public int hashCode() {
      return token.hashCode();
    }
  }

  class ExprPair<T extends Enum<T> & TokenPatterns> implements Expression<T> {
    public final Expression<T> exprA;
    public final Expression<T> exprB;

    private ExprPair(Expression<T> exprA, Expression<T> exprB) {
      this.exprA = exprA;
      this.exprB = exprB;
    }

    @Override
    public Value toJson() {
      return jarray(asList().map(Expression::toJson));
    }

    @Override
    public Seq<Expression<T>> asList() {
      return exprB.asList().prepend(exprA);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ExprPair<?>)) {
        return false;
      }

      ExprPair<?> other = (ExprPair<?>) o;
      return exprA.equals(other.exprA) && exprB.equals(other.exprB);
    }

    @Override
    public String toString() {
      return "Pair(" + exprA + ", " + exprB + ")";
    }

    @Override
    public int hashCode() {
      return exprA.hashCode() * 31 + exprB.hashCode();
    }
  }

  class Nothing<T extends Enum<T> & TokenPatterns> implements Expression<T> {
    private static final Expression<?> SINGLETON = new Nothing<>();

    private Nothing() {}

    @Override
    public Value toJson() {
      return jstring("∅");
    }

    @Override
    public Seq<Expression<T>> asList() {
      return Stream.empty();
    }

    @Override
    public boolean equals(Object o) {
      return o == this;
    }

    @Override
    public String toString() {
      return "∅";
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }
}
