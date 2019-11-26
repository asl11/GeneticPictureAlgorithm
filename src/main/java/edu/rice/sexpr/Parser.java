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

import static edu.rice.sexpr.Scanner.SexprPatterns;
import static edu.rice.sexpr.Scanner.SexprPatterns.CLOSE;
import static edu.rice.sexpr.Scanner.SexprPatterns.OPEN;
import static edu.rice.sexpr.Scanner.SexprPatterns.WORD;
import static edu.rice.sexpr.Scanner.scanSexpr;
import static edu.rice.sexpr.Value.sexpr;
import static edu.rice.sexpr.Value.word;
import static edu.rice.vavr.Sequences.seqMatch;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.regex.Token;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Function;

/**
 * S-Expression Recursive-Descent Parser. The way we're doing it here is a bit like: <br>
 * Value ::= Word | SExpr <br>
 * SExpr ::= ( ValueList ) <br>
 * ValueList ::= Value ValueList || *Nothing*
 *
 * <p>If you look carefully, you'll notice that the definition of a ValueList is basically identical
 * to the definition of a linked list, and we'll internally store our ValueLists as exactly that:
 * Seq&lt;Value&gt;.
 */
public class Parser {
  private static final String TAG = "SexprParser";

  private Parser() {}

  /**
   * Given a String input, this will attempt to parse it and give you back an S-Expression which can
   * then be interrogated for its internal contents.
   *
   * @see Value.Sexpr#apply(Integer)
   * @return Option.some of the S-Expression, if the parse operation succeeded, or option.none if it
   *     failed
   */
  public static Option<Value> parseSexpr(String input) {
    // Engineering note: we're not exposing Result outside of this
    // file. From the outside, you say parseSexpr and you get back an
    // optional Value. The details of what kind of Value can be found
    // within, and the remaining tokens are dealt with here. If there
    // *are* remaining tokens, then the input might well have
    // *started* with a valid s-expression, but the string, as a
    // whole, is *not* an s-expression, so we'll return Option.none().

    // Also, you might be wondering about the need to explicitly state
    // a type for the Option.none().  You could actually replace
    // Option.<Value>none() with none(result.production), which just
    // ignores its argument.  What's going on here? Welcome to the
    // limits of Java's type inference. Option.some() can look at the
    // type of its input and infer the type of its
    // output. Option.none() has no input, and Java sadly isn't clever
    // enough to notice that both lambdas here need to return the
    // exact same type.

    // We saw this issue earlier with List.empty() when we first
    // introduced lists with parameterized types.

    return makeValue(scanSexpr(input))
        .flatMap(
            result ->
                seqMatch(
                        result.tokens,
                        // this is what we want: no remaining tokens after we're done parsing the
                        // Value
                        emptyList -> some(result.production),
                        (head, tail) -> Option.<Value>none())
                    // adding explicit logging because otherwise the programmer may get
                    // really confused wondering why
                    .onEmpty(
                        () ->
                            Log.e(
                                TAG,
                                "tokens remaining in the stream after end of the s-expression; parser failure")));
  }

  /**
   * This internal class is the result of calling each production. It's got a type parameter,
   * because each production returns something different, but they always return the resulting
   * production, and a list of remaining tokens. That pairing is handled here. Yes, we could have
   * used {@link io.vavr.Tuple2} instead, but then the type parameters would start getting really
   * ugly. Better to be specific for our needs.
   */
  static class Result<T> {
    public final T production;
    public final Seq<Token<SexprPatterns>> tokens;

    Result(T production, Seq<Token<SexprPatterns>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return String.format(
          "Result(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }
  }

  private static final Seq<Function<Seq<Token<SexprPatterns>>, Option<Result<Value>>>> MAKERS =
      List.of(Parser::makeSexpr, Parser::makeWord);

  /**
   * General-purpose maker for all value types; will internally try all the concrete JSON builders
   * and return the result of whichever one succeeds.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeValue(Seq<Token<SexprPatterns>> tokenList) {
    // Engineering notes:

    // If we just said MAKERS.map(), below, we'd get a list of options,
    // but we only want the some() options. But we're using flatMap()?
    // Indeed, with flatMap(), something magical happens. flatMap()
    // expects the lambda to return something that's Iterable, which
    // might normally just be another Seq, but Option is also
    // iterable! That means that flatMap() treats an Option exactly
    // the same way it would treat a list with zero or one entries,
    // and it just concatenates them all together.

    // Anyway, given that list of successful results, we're checking
    // how many we got. If there's nothing, then the parser failed.
    // If there's exactly one, then that's our answer. If there are
    // more than one, then we have an ambiguous parser (i.e., it has
    // more than one way of parsing the same message), which indicates
    // that the parser is broken.

    return seqMatch(
        MAKERS.flatMap(x -> x.apply(tokenList)),
        // none of the builders succeeded, so we'll pass that along
        emptyList -> none(),

        // we got exactly one success, which is exactly what we want
        (head, emptyTail) -> some(head),

        // oops, multiple successful builders!
        (first, second, remainder) ->
            Log.ethrow(TAG, "Ambiguous parser! Only one production should be successful."));
  }

  /**
   * Attempts to construct a S-Expression from a list of tokens.
   *
   * @return Option.some of the Result, which includes the S-Expression Value and a list of the
   *     remaining tokens; option.none if it failed.
   */
  static Option<Result<Value>> makeSexpr(Seq<Token<SexprPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            (token.type == OPEN)
                // next, we recursively add together the list; this
                // will consume the close square and return to us a
                // list of tokens, which we'll then convert into an
                // Sexpr.
                ? makeValueList(remainingTokens)
                    .map(result -> new Result<>(sexpr(result.production), result.tokens))
                : none());
  }

  /**
   * This helper function deals with everything after the open-paren, recursively gobbling tokens
   * until it hits the close-paren, and then building an Seq of values on the way back out.
   */
  private static Option<Result<Seq<Value>>> makeValueList(Seq<Token<SexprPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            (token.type == CLOSE)
                ? some(new Result<>(List.empty(), remainingTokens))

                // recursively continue consuming the rest of the
                // input and then prepend the current value to the
                // front of the list that's returned from the
                // recursive call and pass along the remaining
                // unconsumed tokens
                : makeValue(tokenList)
                    .flatMap(
                        headResult ->
                            makeValueList(headResult.tokens)
                                .map(
                                    tailResults ->
                                        new Result<>(
                                            tailResults.production.prepend(headResult.production),
                                            tailResults.tokens))));
  }

  /**
   * Attempts to construct a Word from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeWord(Seq<Token<SexprPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            Option.when(token.type == WORD, () -> new Result<>(word(token.data), remainingTokens)));
  }
}
