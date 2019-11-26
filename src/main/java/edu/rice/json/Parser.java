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

package edu.rice.json;

import static edu.rice.json.Scanner.JsonPatterns;
import static edu.rice.json.Scanner.JsonPatterns.CLOSECURLY;
import static edu.rice.json.Scanner.JsonPatterns.CLOSESQUARE;
import static edu.rice.json.Scanner.JsonPatterns.COMMA;
import static edu.rice.json.Scanner.JsonPatterns.NULL;
import static edu.rice.json.Scanner.JsonPatterns.STRING;
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.json.Value.JArray;
import static edu.rice.json.Value.JBoolean;
import static edu.rice.json.Value.JKeyValue;
import static edu.rice.json.Value.JNull;
import static edu.rice.json.Value.JNumber;
import static edu.rice.json.Value.JObject;
import static edu.rice.json.Value.JString;
import static edu.rice.util.Strings.stringToOptionDouble;
import static edu.rice.vavr.Sequences.seqMatch;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.autograder.annotations.GradeCoverage;
import edu.rice.regex.Token;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Function;

/**
 * Parser for various JSON types. Everything public is a static method; this class is never
 * instantiated. If you're trying to convert a String to an arbitrary JSON value, then you probably
 * want to use {@link #parseJsonValue(String)}. If your String is something that you require to be a
 * JSON Object or Array, then you probably want to use {@link #parseJsonObject(String)} or {@link
 * #parseJsonArray(String)}, respectively.
 */
@GradeCoverage(project = "Week07")
public class Parser {
  private static final String TAG = "JsonParsers";

  // Engineering note: There's no particular reason for this to be a
  // class vs. an interface. Either way, we just want to export a
  // bunch of static methods. If it were an interface, we wouldn't
  // need to declare the private constructor, as below, but then there
  // would be no way to have the package-scope methods that we have here.
  // (Java has private but not package-scope methods in interfaces.)
  // The approach here is pretty much the standard way of doing things
  // before Java8, so we'll use it here as well.

  // So why are some method public and others package-scope? The
  // public static methods are meant to be the *external* interface to
  // our JSON code. The package-scope static methods are internal, but
  // we want them visible to our unit tests, which are all in the same
  // edu.rice.json package.

  // On the other hand, the private methods (and the MAKERS field)
  // aren't meant to be used by anybody outside of this file. That's
  // what "private" is meant to convey.

  private Parser() {} // never instantiate this class!

  /**
   * Given a String input, this will attempt to parse it and give you back a JSON object, which can
   * then be interrogated for its internal contents. If any tokens remain after the JSON object is
   * otherwise successfully parsed, or if the input is some other JSON type, like a JSON array, the
   * parser will also reject the input.
   *
   * @see JObject#getMap()
   * @return Option.some of the JSON value, if the parse operation succeeded, or option.none if it
   *     failed
   */
  public static Option<JObject> parseJsonObject(String input) {
    return noTokensRemaining(makeObject(scanJson(input))).flatMap(Value::asJObjectOption);
  }

  /**
   * Given a String input, this will attempt to parse it and give you back a JSON value (of any JSON
   * type: object, array, string, etc.). You may then interrogate the result for its concrete type
   * and/or contents. If any token remains after the JSON value is otherwise successfully parsed,
   * the parser will also reject the input.
   *
   * @return Option.some of the JSON value, if the parse operation succeeded, or option.none if it
   *     failed
   */
  public static Option<Value> parseJsonValue(String input) {
    return noTokensRemaining(makeValue(scanJson(input)));
  }

  /**
   * Given a String input, this will attempt to parse it and give you back a JSON array which can
   * then be interrogated for its internal contents. If any token remains after the JSON array is
   * otherwise successfully parsed, or if the input is some other JSON type, like a JSON object, the
   * parser will also reject the input.
   *
   * @see JArray#get(int)
   * @return Option.some of the JSON array, if the parse operation succeeded, or option.none if it
   *     failed
   */
  public static Option<JArray> parseJsonArray(String input) {
    return noTokensRemaining(makeArray(scanJson(input))).flatMap(Value::asJArrayOption);
  }

  /**
   * Given a parser {@link Result}, of whatever type parameter, this method transforms it to an
   * option of the parameter type, getting rid of the Result wrapper, but only if there are no
   * tokens remaining in the stream of tokens. If any tokens are left, we treat this as an error and
   * return {@link Option#none()}.
   */
  private static <T> Option<T> noTokensRemaining(Option<Result<T>> o) {
    return o.flatMap(
        parseResult ->
            seqMatch(
                    parseResult.tokens,
                    emptyList -> some(parseResult.production),
                    (head, tail) -> Option.<T>none())
                .onEmpty(
                    () ->
                        Log.e(TAG, "tokens remaining after parsing was complete; parser failure")));
  }

  /**
   * Every internal make-method returns an Option&lt;Result&lt;T&gt;&gt;, which inside contains the
   * {@link Value} produced as well as an {@link Seq} of the remaining tokens. That Result is
   * parameterized. Commonly it's Result&lt;Value&gt; but some helper functions and such return
   * other things besides Value, while still returning a production of some kind and a list of
   * remaining tokens.
   */
  @GradeCoverage(project = "Week07", exclude = true)
  static class Result<T> {
    // Engineering note: We're excluding the Result class from your code coverage
    // requirements for week7. You have to worry about everything else in the Parser
    // class, but not Parser.Result.

    public final T production;
    public final Seq<Token<JsonPatterns>> tokens;

    Result(T production, Seq<Token<JsonPatterns>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return String.format(
          "Result(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Result<?>)) {
        return false;
      }

      var otherResult = (Result<?>) o;

      return production.equals(otherResult.production) && tokens.equals(otherResult.tokens);
    }

    @Override
    public int hashCode() {
      return production.hashCode() * 7 + tokens.hashCode() * 31;
    }
  }

  // Engineering notes / Data definition:

  // A JSON value can be:
  //   string
  //   number
  //   object
  //   array
  //   true
  //   false
  //   null
  //
  // Every different method here represents a different JSON
  // production type that we might be able to parse. If it
  // successfully parses the thing it's looking for, it will return an
  // Option<Result<Value>> containing the production (JObject,
  // JString, etc.) as well as a list of the remaining unparsed
  // tokens. If it fails, it returns Option.None.
  //
  // Each of the make-methods returns the wider type
  // Option<Result<Value>> rather than something more specific, like
  // makeString returning Option<Result<JString>>, in order for all of
  // the make-methods to have the *same* type signature, which lets us
  // make a list of them, as below. Later in the semester, we'll learn
  // how to use type wildcards so we can avoid this restriction.
  //
  // Here is a list of the maker-methods as lambdas; note the impressive
  // type signature of the list! Some other programming languages would
  // let us declare "aliases" for these things, to avoid writing them
  // over and over again, but Java doesn't have aliases. We'll discuss
  // how we can work around this issue later in the semester.

  private static final Seq<Function<Seq<Token<JsonPatterns>>, Option<Result<Value>>>> MAKERS =
      List.of(
          Parser::makeString,
          Parser::makeNumber,
          Parser::makeObject,
          Parser::makeArray,
          Parser::makeBoolean,
          Parser::makeNull);

  /**
   * General-purpose maker for all value types; will internally try all the concrete JSON builders
   * and return the result of whichever one succeeds.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeValue(Seq<Token<JsonPatterns>> tokenList) {
    var parserResults = MAKERS.flatMap(x -> x.apply(tokenList));
    switch (parserResults.length()) {
      case 0:
        // none of the builders succeeded, so we'll pass that along
        return none();
      case 1:
        // we got exactly one success, which is exactly what we want
        return some(parserResults.head());
      default:
        // oops, multiple successful builders!
        Log.ethrow(TAG, "Ambiguous parser! Only one production should be successful.");

        // control flow never gets here
        return none();
    }
  }

  /**
   * General-purpose recursive helper function, for lists of things with a separator token that is
   * required to be there but that we want to ignore in our output.
   *
   * @param tokenList List of tokens that we're parsing
   * @param itemParser Recursive parser for the items we want
   * @param separatorToken Token that's expected to separate the items we want
   * @param endToken Token that indicates we've hit the end
   * @param firstTime Call this with 'true' initially, indicating that the separator isn't required
   *     before calling the itemParser
   * @param <T> Any type suitable as a parameter for {@link Result}
   * @return An optional result, which if successful, contains a list of results from calling
   *     itemParser
   */
  static <T> Option<Result<Seq<T>>> separatedList(
      Seq<Token<JsonPatterns>> tokenList,
      Function<Seq<Token<JsonPatterns>>, Option<Result<T>>> itemParser,
      JsonPatterns separatorToken,
      JsonPatterns endToken,
      boolean firstTime) {

    // Engineering notes: This is probably the most subtle code here. We originally
    // had basically the same code repeated twice:
    //
    // -- once for reading in JSON arrays, where the elements (arbitrary JSON values)
    //    are separated by commas
    // -- once for reading in JSON objects, where the elements (key-value tuples) are also
    //    separated by commas

    // To refactor this code, and have one shared function, we have to somehow be able
    // to deal with makeKeyValue, as needed by makeObject, which returns a very different
    // type from makeValue, as needed by makeArray. Option<Result<JKeyValue>> isn't
    // normally compatible with Option<Result<Value>>. Our solution is to make the
    // inner part be a type parameter (thus the <T> as part of our function signature),
    // and then to pass a lambda that implements the desired parser, which you'll notice
    // has Option<Result<T>> as its return type.

    // Note that JsonPatterns could *also* be abstracted away to another type parameter,
    // e.g., "P extends Enum<P> & TokenPatterns", which would make this function useful
    // in a general-way, well beyond our JSON parser. To read more about this, scroll
    // down to the very bottom of the file.

    // Anyway, the logic here is straightforward. If we see the endToken, then we're done,
    // and we'll just return an empty-list of productions. Otherwise, if it's the first
    // time through, we expect to *not* see the separatorToken. After that, we *require*
    // it and will then be sure to skip over it.

    // Each time, we'll apply our itemParser, which will either be makeValue or makeKeyValue
    // -- we don't care! -- and will recursively call ourselves on the remaining tokens,
    // placing the initial result back onto the front of whatever comes back from the
    // recursive call.

    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) -> {
          if (token.type == endToken) {
            return some(new Result<>(List.empty(), remainingTokens));
          }

          // The use of Java's ternary operator, nested like this,
          // isn't particularly attractive, but it does what we need.
          // Future versions of Java will have a "switch expression" which
          // will hopefully allow this sort of thing to be written more naturally.
          // Also of note: if you try replacing the type declaration below
          // with "var", you'll see that Java cannot figure out the type
          // parameter (all it can guess is that the type is Option<?>).
          // You could fix that by adding type parameters to the none() calls,
          // but the result is no better, maybe worse, then what's here now.

          Option<Result<T>> resultOption =
              (token.type == separatorToken)
                  ? (firstTime ? none() : itemParser.apply(remainingTokens))
                  : (!firstTime ? none() : itemParser.apply(tokenList));

          return resultOption.flatMap(
              headResult ->
                  separatedList(headResult.tokens, itemParser, separatorToken, endToken, false)
                      .map(
                          tailResults ->
                              new Result<>(
                                  tailResults.production.prepend(headResult.production),
                                  tailResults.tokens)));
        });
  }

  /**
   * Maker for JSON Objects.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeObject(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            token.type == JsonPatterns.OPENCURLY
                // next, we recursively add together the list of key-value pairs; this will consume
                // the close curly
                ? separatedList(remainingTokens, Parser::makeKeyValue, COMMA, CLOSECURLY, true)
                    .map(result -> new Result<>(JObject.fromSeq(result.production), result.tokens))
                : none());
  }

  /**
   * Attempts to construct a JKeyValue from a list of tokens.
   *
   * @return Option.some of the Result, which includes the JKeyValue and a list of the remaining
   *     tokens; option.none if it failed
   */
  static Option<Result<JKeyValue>> makeKeyValue(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (head, emptyTail) -> none(),
        (string, colon, remainingTokens) -> {
          if (string.type != JsonPatterns.STRING || colon.type != JsonPatterns.COLON) {
            return none();
          }

          // We could alternatively call into JString.build(), but we've already verified
          // the token type, and it's a terminal token, so we'll take a short-cut.
          var jstring = JString.ofEscaped(string.data);

          // and finally grab the value and turn it into a pair
          return makeValue(remainingTokens)
              .map(value -> new Result<>(JKeyValue.of(jstring, value.production), value.tokens));
        });
  }

  /**
   * Attempts to construct a JArray from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeArray(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            token.type == JsonPatterns.OPENSQUARE
                ? separatedList(remainingTokens, Parser::makeValue, COMMA, CLOSESQUARE, true)
                    .map(result -> new Result<>(JArray.fromSeq(result.production), result.tokens))
                : none());
  }

  /**
   * Attempts to construct a JString from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeString(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            Option.when(
                token.type == STRING,
                new Result<>(JString.ofEscaped(token.data), remainingTokens)));
  }

  /**
   * Attempts to construct a JNumber from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeNumber(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            token.type == JsonPatterns.NUMBER
                ? stringToOptionDouble(token.data)
                    .map(number -> new Result<>(JNumber.of(number), remainingTokens))
                : none());
  }

  /**
   * Attempts to construct a JBoolean from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeBoolean(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) -> {
          switch (token.type) {
            case TRUE:
              return some(new Result<>(JBoolean.of(true), remainingTokens));
            case FALSE:
              return some(new Result<>(JBoolean.of(false), remainingTokens));
            default:
              return none();
          }
        });
  }

  /**
   * Attempts to construct a JNull from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens;
   *     option.none if it failed
   */
  static Option<Result<Value>> makeNull(Seq<Token<JsonPatterns>> tokenList) {
    return seqMatch(
        tokenList,
        emptyList -> none(),
        (token, remainingTokens) ->
            Option.when(token.type == NULL, new Result<>(JNull.make(), remainingTokens)));
  }
}

// Engineering note: in the real world, you hardly ever would code up
// a hand-built recursive descent parser like this. Instead, you tend
// to use "parser-generators" where you write down the BNF in a file
// and the parser-generator does all the rest for you. You'll see
// these things in Comp412. Java programmers might tend to use ANTLR
// (http://www.antlr.org/), and there are many comparable tools that
// target other programming languages.  Here, for example, is an ANTLR
// grammar for JSON:
// https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4

// If you want to see something really crazy, check out this JSON
// parser, implemented in Java, using a library ported from Haskell,
// that does basically everything here in 1/4 the lines of code.
// https://github.com/jon-hanson/parsecj/blob/master/src/test/java/org/javafp/parsecj/json/Grammar.java

// This is an example of a "parser combinator", which is a structure
// that lets you "compose" together little parsers and then make
// bigger parsers out of them, such that your composition exactly
// follows the rules of the grammar you're trying to parse. You
// can see a little bit of this happening with our separatedList()
// function, to which we pass a lambda, which is just another parser.
// A full parser-combinator library provides a variety of operators
// like that, which all compose nicely, letting you write parser-code
// that looks very close to the grammar that you're trying to parse.
