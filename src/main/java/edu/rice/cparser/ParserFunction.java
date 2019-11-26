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

import static edu.rice.cparser.Expression.exprNothing;
import static edu.rice.cparser.Expression.exprPair;
import static edu.rice.cparser.Expression.exprTerminal;
import static edu.rice.cparser.Result.ParserError;
import static edu.rice.cparser.Result.resultError;
import static edu.rice.cparser.Result.resultOk;
import static edu.rice.vavr.Sequences.seqMatch;

import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import edu.rice.util.Log;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * ParserFunctions are the building blocks that you use to make parser-combinators. You start with
 * primitives like {@link #parseTerminal(Token)} and then combine them with operators like {@link
 * #list()} or {@link #exactlyOneOf(ParserFunction, ParserFunction[])}. The results from running a
 * parser are variations on the {@link Expression} interface; if you want to be clever and map those
 * to your own data classes, you should use {@link #mapExpression(UnaryOperator)}. You do this as
 * part of defining your parser. It's used, for example, by the {@link #between(ParserFunction,
 * ParserFunction)} combinator, to say that you're expecting two things beforehand and afterward of
 * the thing you care about (e.g., open and close-parentheses around a value), but you only need the
 * thing in the middle once the parser has finished its work.
 *
 * <p>To actually run a parser on some input, just use any ParserFunction's {@link #parse(Seq)}
 * method on a list of tokens, and you'll get back a {@link Result}.
 *
 * <p>Note also that ParserFunctions generally say something useful if you call their {@link
 * Object#toString()} method. Those are composed together as well. If you want to be able to specify
 * a composition-string as part of your own parser-combinator, then you can use {@link
 * #pfWithString(ParserFunction, Supplier)}, or just specify a name with {@link #named(Supplier)}.
 *
 * @param <T> Every ParserFunction has a type parameter, which matches up to the type parameter of
 *     the tokenizer you're using. See {@link edu.rice.regex.RegexScanner} for more details.
 */
@FunctionalInterface
public interface ParserFunction<T extends Enum<T> & TokenPatterns> {
  /**
   * Every parser takes in a list of tokens and returns a {@link Result}, which internally has
   * behavior analogous to {@link io.vavr.control.Option}, in that it can indicate success or
   * failure as well as the resulting {@link Expression} in the successful case.
   */
  Result<T> parse(Seq<Token<T>> tokens);

  /**
   * When combining parsers, we want to make sure that the resulting parsers not only combine the
   * parsing functionality but also combine the internal {@link Object#toString()} methods, because
   * we want parsers to be usefully convertible to strings. (Helpful for debugging, etc.)
   *
   * <p>This utility method takes two lambdas -- a new parser function and a supplier of a string,
   * and wraps them together into another ParserFunction that behaves just like the first lambda,
   * only with the enhanced toString() method.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> pfWithString(
      ParserFunction<T> pf, Supplier<String> ss) {
    return new ParserFunction<>() {
      @Override
      public Result<T> parse(Seq<Token<T>> tokens) {
        return pf.parse(tokens);
      }

      @Override
      public String toString() {
        return ss.get();
      }

      // While we're at it, we might as well implement equals() and
      // hashCode() methods, so these parsers can be stored in lists
      // and maps and otherwise be manipulated. There's no useful way
      // to define these methods on the ParserFunction lambdas (pf),
      // so we'll just delegate to the toString() methods.

      @Override
      public boolean equals(Object o) {
        return o instanceof ParserFunction<?> && toString().equals(o.toString());
      }

      @Override
      public int hashCode() {
        return toString().hashCode();
      }
    };
  }

  /**
   * Given a sequence of ParserFunctions, returns a new parser for each of them in sequence. For
   * example, sequence(A, B, C) is equivalent to A.then(B.then(C)), and the resulting expression
   * will map nicely to a list with three expressions using {@link Expression#asList()}.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> sequence(ParserFunction<T>... list) {
    return sequence(Stream.of(list));
  }

  /**
   * Given a list of ParserFunctions, returns a new parser for each of them in sequence. For
   * example, sequence(List.of(A, B, C)) is equivalent to A.then(B.then(C)), and the resulting
   * expression will map nicely to a list with three expressions using {@link Expression#asList()}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> sequence(
      Seq<ParserFunction<T>> list) {
    // Engineering notes: Since we don't really have an "identity"
    // parser here (monadic failure!), we can't just write this as a
    // foldRight -- we'd need to have the "identity" parser for the last
    // step of the fold -- so instead we build it recursively
    // ourselves.

    // Exercise 1 (easy): can you reverse the list then use foldLeft
    // instead of the recursion? If you did, under what conditions
    // might that be a preferable solution to the code below?

    // Exercise 2 (hard): if you wanted to invent an "identity"
    // parser, how would that work? What would it do? Would you need
    // to add a special case for it to all the parser-combinators or
    // could you make it work without special-case handling?

    return seqMatch(
            list,
            emptyList -> Log.ethrow("ParserFunction.sequence", "requires at least one argument!"),
            (head, emptyTail) -> head,
            (head, second, tail) -> head.then(sequence(list.tail())))
        .named(() -> list.mkString("(sequence: ", ",", ")"));
  }

  /**
   * Given the current parser (A) and a second parser (B), returns a parser for (A B): A followed by
   * B.
   */
  default ParserFunction<T> then(ParserFunction<T> b) {
    return pfWithString(
        input ->
            this.parse(input)
                .flatMap(
                    resultA ->
                        b.parse(resultA.tokens)
                            .flatMap(
                                resultB ->
                                    resultOk(
                                        exprPair(resultA.production, resultB.production),
                                        resultB.tokens))),
        () -> String.format("(%s then %s)", this.toString(), b.toString()));
  }

  /**
   * Given the current parser, returns another parser that accepts the same input but requires there
   * to be no more remaining tokens. If tokens remain, then {@link ParserError} is returned.
   */
  default ParserFunction<T> thenEnd() {
    return pfWithString(
        input ->
            this.parse(input).flatMap(result -> result.tokens.isEmpty() ? result : resultError()),
        () -> this.toString() + ", EOF");
  }

  /**
   * Given the current parser (A) and a second parser (B), returns a parser for (A | B): A or B. If
   * both parsers succeed, this is considered an error. An error will be logged and the combined
   * parser will fail, returning {@link ParserError}. If you only care about B in the case when A
   * fails, then you may prefer {@link #orElse(ParserFunction)}. If you want to use "or" with three
   * or more parsers, then you may prefer {@link #exactlyOneOf(ParserFunction, ParserFunction[])}.
   */
  default ParserFunction<T> or(ParserFunction<T> b) {
    return pfWithString(
        input ->
            this.parse(input)
                .match(
                    resultA ->
                        b.parse(input)
                            .match(
                                resultB -> {
                                  Log.e(
                                      "ParserFunction.or",
                                      () -> "Ambiguous results: two parsers accept this input!");
                                  Log.e(
                                      "ParserFunction.or", () -> "-- ParserA: " + this.toString());
                                  Log.e("ParserFunction.or", () -> "-- ParserB: " + b.toString());
                                  Log.e(
                                      "ParserFunction.or",
                                      () -> "-- Input tokens: " + input.take(10).mkString(", "));
                                  return resultError();
                                },
                                // if B fails but A succeeded, then we'll take whatever A's got
                                errorB -> resultA),
                    errorA -> b.parse(input)), // if A fails, then we'll take whatever B's got
        () -> String.format("(%s or %s)", this.toString(), b.toString()));
  }

  /**
   * Give the current parser (A) and a second parser (B), returns a parser that behaves identical to
   * A when A would have succeeded. If A would have failed, then the result of B is returned. If you
   * want to ensure that exactly one of A or B will succeed but never both, then you may prefer
   * {@link #or(ParserFunction)}. If you want to use a sequence of "orElse" combinations with three
   * or more parsers, then you may prefer {@link #oneOf(ParserFunction, ParserFunction[])}.
   */
  default ParserFunction<T> orElse(ParserFunction<T> b) {
    return pfWithString(
        input ->
            this.parse(input)
                .match(
                    resultA -> resultA, // if A succeeds, then we're done
                    errorA -> b.parse(input)), // if A fails, then we'll take whatever B's got
        () -> String.format("(%s orElse %s)", this.toString(), b.toString()));
  }

  /**
   * Given at least one parser-function, returns a parser that combines all the given parsers and
   * matches one of them. Internally, they're combined with {@link #orElse(ParserFunction)}, meaning
   * that they'll be attempted sequentially, left to right.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> oneOf(
      ParserFunction<T> a, ParserFunction<T>... others) {
    Seq<ParserFunction<T>> otherList = Stream.of(others);

    return otherList
        .foldLeft(a, ParserFunction::orElse)
        .named(() -> otherList.mkString("(oneOf: ", ", ", ")"));
  }

  /**
   * Given at least one parser-function, returns a parser that combines all the given parsers and
   * matches one of them. Internally, they're combined with {@link #or(ParserFunction)}, meaning
   * that if any two succeed, the overall result is a failure.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> exactlyOneOf(
      ParserFunction<T> a, ParserFunction<T>... others) {
    Seq<ParserFunction<T>> otherList = Stream.of(others);

    return otherList
        .foldLeft(a, ParserFunction::or)
        .named(() -> otherList.mkString("(exactlyOneOf: ", ", ", ")"));
  }

  /**
   * Given the current parser (A), returns a parser that will accept a list of A. In the event that
   * there are no matches, the results will be an expression with an empty-list inside. This
   * composed production never returns {@link ParserError}. <b>Warning: this production has the
   * potential to consume zero tokens, and thus infinite loop if used carelessly.</b>
   */
  default ParserFunction<T> list() {
    return pfWithString(
        // try to parse the head of the list
        input ->
            this.parse(input)
                .match(

                    // recursively try to parse the tail of the list
                    headResult ->
                        this.list()
                            .parse(headResult.tokens)
                            .flatMap(
                                tailResult ->
                                    resultOk(
                                        exprPair(headResult.production, tailResult.production),
                                        tailResult.tokens)),

                    // when the recursion fails, this ok token will represent the end of the list
                    errorA -> resultOk(exprNothing(), input)),
        () -> "(list-of: " + this.toString() + ")");
  }

  /**
   * Given the current parser (A), returns a parser that will accept a list of (A)'s, separated with
   * the given separator, perhaps a <code>parseTerminal(COMMA)</code> or equivalent. In the event
   * that there are no matches, the results will be an expression with an empty-list inside. This
   * composed production never returns {@link ParserError}. <b>Warning: this production has the
   * potential to consume zero tokens, and thus infinite loop if used carelessly.</b>
   *
   * <p>A useful feature of this production is that it will remove the separators. You only get the
   * list of (A)'s that you care about.
   */
  default ParserFunction<T> separatedList(ParserFunction<T> separator) {
    // This sepPlusList parser handles the sequence of
    // separator-expr-separator-expr-..., which is to say, the case
    // after the leading expression has already been parsed.

    ParserFunction<T> sepPlusList = this.withPrefix(separator).list();

    return pfWithString(
        // we'll initially parse "this" (which is the thing we're
        // trying to get a list of), and then after that go with the
        // separated list
        input ->
            this.parse(input)
                .match(
                    headResult ->
                        sepPlusList
                            .parse(headResult.tokens)
                            .flatMap(
                                tailResult ->
                                    resultOk(
                                        exprPair(headResult.production, tailResult.production),
                                        tailResult.tokens)),
                    errorA -> resultOk(exprNothing(), input)),
        () -> "(separated-list-of " + this.toString() + ")");
  }

  /**
   * Given the current parser (A) and two other parsers (Prefix) and (Suffix), returns a parser for
   * (Prefix) (A) (Suffix). When successful, the (Prefix) and (Suffix) parts are removed, and only
   * the result of (A) is returned. This might be useful for eliminating open and close parentheses,
   * or other such tokens from the results you don't need.
   */
  default ParserFunction<T> between(ParserFunction<T> prefix, ParserFunction<T> suffix) {
    return sequence(prefix, this, suffix)
        .mapExpression(expr -> expr.asList().get(1))
        .named(() -> String.format("(between: %s %s %s)", prefix, this, suffix));
  }

  /**
   * Given the current parser (A) and one other parser (Prefix), returns a parser for (Prefix) (A).
   * When successful, the (Prefix) is removed, and only the result of (A) is returned. This might be
   * useful for eliminating various tokens from the results you require in your language (e.g.,
   * separators, punctuation) but don't actually care about so long as they were present.
   */
  default ParserFunction<T> withPrefix(ParserFunction<T> header) {
    return sequence(header, this)
        .mapExpression(expr -> expr.asList().get(1))
        .named(() -> "(withPrefix: " + header + " " + this + ")");
  }

  /**
   * Allows you to add some post-processing into the parser, combining with a function that changes
   * the expression. This might be useful if you want to clean up what you've got and use a
   * different implementation of {@link Expression}.
   */
  default ParserFunction<T> mapExpression(UnaryOperator<Expression<T>> mapFunc) {
    ParserFunction<T> newParser =
        input -> this.parse(input).flatMap(result -> result.mapProduction(mapFunc));
    return newParser.named(() -> "(mapped: " + this.toString() + ")");
  }

  /** Adds a printable name to a parser-function, useful for debugging. */
  default ParserFunction<T> named(Supplier<String> supplier) {
    return pfWithString(this, supplier);
  }

  /**
   * Constructs a "terminal" parser that accepts a single token and rejects all other tokens. This
   * variant matches both the token's type (i.e., the name of the token from the enum) <b>and</b>
   * the token's value. If you want a parser that will accept any value for a given type, use {@link
   * #parseTerminal(Enum)}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> parseTerminal(
      T tokenType, String value) {
    return parseTerminal(new Token<>(tokenType, value));
  }

  /**
   * Constructs a "terminal" parser that accepts a single token and rejects all other tokens. This
   * variant matches both the token's type (i.e., the name of the token from the enum) <b>and</b>
   * the token's value. If you want a parser that will accept any value for a given type, use {@link
   * #parseTerminal(Enum)}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> parseTerminal(Token<T> token) {
    return pfWithString(
        input ->
            seqMatch(
                input,
                emptyList -> resultError(),
                (head, tail) ->
                    head.equals(token) ? resultOk(exprTerminal(token), tail) : resultError()),
        () -> "Terminal" + token);
  }

  /**
   * Constructs a "terminal" parser that accepts a single token and rejects all other tokens. This
   * variant will accept <i>any</i> token of the given token type, ignoring the token's value. If
   * you want to match a token with a specific type <b>and</b> value, then use {@link
   * #parseTerminal(Token)} or {@link #parseTerminal(Enum, String)}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> parseTerminal(T tokenType) {
    return pfWithString(
        input ->
            seqMatch(
                input,
                emptyList -> resultError(),
                (head, tail) ->
                    head.type.equals(tokenType)
                        ? resultOk(exprTerminal(head), tail)
                        : resultError()),
        () -> "Terminal(" + tokenType + ")");
  }
}
