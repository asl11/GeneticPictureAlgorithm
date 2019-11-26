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

import static edu.rice.json.Builders.jstring;

import edu.rice.json.Value;
import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import edu.rice.util.Log;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Every {@link ParserFunction} returns a Result. Those results have two subtypes: <i>Ok</i>, which
 * corresponds to a successful parsing of the input and then contains the resulting {@link
 * Expression} and list of tokens. The other subtype is <i>ParserError</i>, which corresponds to an
 * inability to parser the input.
 *
 * <p>This Result class is a bit different from the {@link edu.rice.json.Parser}'s <code>Result
 * </code> or {@link edu.rice.sexpr.Parser}'s <code>Result</code>. Those classes are typically
 * wrapped in an {@link Option}, while we instead roll Option's some/none functionality directly
 * into this Result class. This yields cleaner code than returning something really painful like
 * <code>Option&lt;Tuple2&lt;Expression&lt;T&gt;,Seq&lt;Token&lt;T&gt;&gt;&gt;&gt;</code>.
 */
public interface Result<T extends Enum<T> & TokenPatterns> {
  /**
   * Make an "ok" parser result.
   *
   * @param production The expression resulting from the parser.
   * @param tokens The remaining unread tokens in the input stream
   */
  static <T extends Enum<T> & TokenPatterns> Result<T> resultOk(
      Expression<T> production, Seq<Token<T>> tokens) {
    return new Ok<>(production, tokens);
  }

  /** Make an "error" parser result. */
  @SuppressWarnings("unchecked")
  static <T extends Enum<T> & TokenPatterns> Result<T> resultError() {
    return (ParserError<T>) ParserError.SINGLETON;
  }

  /**
   * Make an "error" parser result, ignoring the input token, but passing through its type. (This
   * helps with inadequate type inference when using {@link #resultError()}.
   */
  @SuppressWarnings("unused")
  static <T extends Enum<T> & TokenPatterns> Result<T> resultError(Expression<T> ignored) {
    return resultError();
  }

  /**
   * Make an "error" parser result, ignoring the input token, but passing through its type. (This
   * helps with inadequate type inference when using {@link #resultError()}.
   */
  @SuppressWarnings("unused")
  static <T extends Enum<T> & TokenPatterns> Result<T> resultError(Token<T> ignored) {
    return resultError();
  }

  /**
   * General-purpose structural pattern matching, taking two lambdas: one for if the result is "ok"
   * and the other if it's an "error".
   */
  default <R> R match(Function<Ok<T>, R> okFunc, Function<ParserError<T>, R> errorFunc) {
    if (this instanceof Ok) {
      return okFunc.apply((Ok<T>) this);
    } else if (this instanceof ParserError) {
      return errorFunc.apply((ParserError<T>) this);
    } else {
      throw new RuntimeException("unexpected type for Result.match: " + this.getClass().getName());
    }
  }

  /**
   * Getter for {@link Result.Ok}, throws an exception if it's not that type. Useful for tests and
   * such when you're absolutely sure you don't have a parsing failure.
   */
  default Ok<T> asOk() {
    return match(ok -> ok, err -> Log.ethrow("CParser", "Result isn't Ok"));
  }

  /**
   * Getter for {@link ParserError}, throws an exception if it's not that type. Useful for tests and
   * such when you're absolutely sure you had a parsing failure.
   */
  default ParserError<T> asError() {
    return match(ok -> Log.ethrow("CParser", "Result isn't ParserError"), err -> err);
  }

  /** Returns whether the result is {@link Result.Ok}. */
  default boolean isOk() {
    return match(ok -> true, err -> false);
  }

  /** Returns whether the result is {@link ParserError}. */
  default boolean isError() {
    return match(ok -> false, err -> true);
  }

  /**
   * This method is analogous to {@link Option#flatMap(Function)}. The lambda is applied to "Ok"
   * Results to generate another Result. "Error" results as passed through. If you need something
   * more general, then {@link #match(Function, Function)} lets you match on both "Ok" and "Error"
   * Results. If you only want to operate on the production inside an "Ok" Result, then {@link
   * #mapProduction(UnaryOperator)} does what you need.
   */
  default Result<T> flatMap(Function<Ok<T>, Result<T>> flatMapFunc) {
    return match(flatMapFunc, err -> err);
  }

  /**
   * Makes it easy to change the produced expression of a successful run to a different type of
   * expression. Analogous to {@link Option#map(Function)}, in that the <code>mapFunc</code> is run
   * on "Ok" Result productions. Errors are passed through. Similarly, any remaining tokens in the
   * "Ok" Result are passed through.
   */
  default Result<T> mapProduction(UnaryOperator<Expression<T>> mapFunc) {
    return match(ok -> resultOk(mapFunc.apply(ok.production), ok.tokens), err -> err);
  }

  /** Returns a JSON representation of the parser result. */
  default Value toJson() {
    return match(ok -> ok.production.toJson(), err -> jstring("ParserError"));
  }

  // Engineering notes: We're doing things slightly differently here than how we've done the
  // "Comp215 standard style" in the past. This time, we're exposing these implementation
  // classes to our clients, who can then just directly reference their contents. Using
  // getters or making the match method do destructuring isn't particularly helpful.

  class Ok<T extends Enum<T> & TokenPatterns> implements Result<T> {
    public final Expression<T> production;
    public final Seq<Token<T>> tokens;

    private Ok(Expression<T> production, Seq<Token<T>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return String.format(
          "Result.Ok(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Ok<?>)) {
        return false;
      }

      var otherResult = (Ok<?>) o;

      return production.equals(otherResult.production) && tokens.equals(otherResult.tokens);
    }

    @Override
    public int hashCode() {
      return production.hashCode() * 7 + tokens.hashCode() * 31;
    }
  }

  class ParserError<T extends Enum<T> & TokenPatterns> implements Result<T> {
    private static final ParserError<?> SINGLETON = new ParserError<>();

    private ParserError() {}

    @Override
    public String toString() {
      return "Result.ParserError()";
    }

    @Override
    public boolean equals(Object o) {
      return (o instanceof Result.ParserError);
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }
}
