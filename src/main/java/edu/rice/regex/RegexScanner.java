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

package edu.rice.regex;

import static edu.rice.util.Strings.safeSubstring;
import static edu.rice.vavr.Tries.tryOfNullable;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.regex.Pattern;

/**
 * Given a Java enum which defines regexes for scanning tokens, this class builds a regex with
 * "named-capturing groups" and then uses it to tokenize your input. The names of the tokens will
 * come from the Enum, and the regular expressions for the tokens will come by fetching the pattern
 * associated with each token in the enum. If you have a token type that doesn't ever occur in your
 * input, such as "FAIL" in the example below, make sure its pattern is the empty string, and it
 * will be ignored while constructing the regular expressions.
 *
 * <p>To make this work, make sure your enum implements the {@link TokenPatterns} interface, which
 * means it will have an extra method, {@link TokenPatterns#pattern()} which returns the regex
 * pattern.
 *
 * <p>Example:
 *
 * <pre>
 * enum CurlyLanguagePatterns implements TokenPatterns {
 *     OPENCURLY("\\{"),
 *     CLOSECURLY("\\}"),
 *     WHITESPACE("\\s+"),
 *     FAIL("");/\
 *
 *     public final String pattern;
 *
 *     CurlyLanguagePatterns(String pattern) {
 *         this.pattern = pattern;
 *     }
 *
 *     public String pattern() { return pattern; }
 * }
 * </pre>
 *
 * <p>Typical usage:
 *
 * <pre>
 * Seq&lt;Token&lt;CurlyLanguagePatterns&gt;&gt; results =
 *     scanPatterns(inputString, CurlyLanguagePatterns.class, new Token&lt;&gt;(CurlyLanguagePatterns.FAIL, ""));
 * </pre>
 */
public interface RegexScanner {
  /**
   * This method runs the scanner on the input, given the set of possible token patterns, and
   * returns a list of tokens.
   *
   * @param enumPatternsClazz a class reference to an enum which implements {@link TokenPatterns}
   * @param <T> a type parameter corresponding to the enum's class reference
   * @param input the string being tokenized
   * @param failToken the token to return if the scanner fails to recognize a token
   * @return a list of {@link Token}'s, each of which will have the type (from the enum) and the
   *     string value; or the <code>failToken</code> if something went wrong.
   */
  static <T extends Enum<T> & TokenPatterns> Stream<Token<T>> scanPatterns(
      String input, Class<T> enumPatternsClazz, Token<T> failToken) {

    // Engineering note: the type constraints on T have seemingly
    // magical properties. If you poke around on StackOverflow for
    // code doing similar things, you'll see lots of wildcard types
    // (i.e., Class<?>) and typecasts. By constraining the argument to
    // the constructor, enumPatternClazz, to be Class<T>, and with all
    // the constraints on T, the Java compiler will only allow you to
    // pass in the Class for an enum that implements our desired
    // String getPattern() method.

    // With these type constraints, there's no way to have the type
    // parameter T and the class object enumPatternsClazz be anything
    // other than one and the same. This leads to the uncomfortable
    // question of why you have to pass both a type parameter and a
    // class parameter. Why not pass just one? Sigh. That would be yet
    // another weakness of the Java language.

    // The "real" solution would be to use a programming language that
    // has "reified generics", wherein the type parameter T is a thing
    // that you can directly interact with, doing all the things that
    // Java forces you to do with these "Class" objects
    // instead. Microsoft's C# does this properly.  Likewise, some of
    // the other languages that run on the JVM, like Kotlin, have
    // reified generics.

    // Further reading:
    // http://stackoverflow.com/questions/31876372/what-is-reification

    return new CrunchedPatterns<>(enumPatternsClazz).tokenize(input, failToken);
  }

  /**
   * This class isn't meant to be visible to the outside world. It's purpose is to parse the enum
   * and build up everything we're going to need to run a lexical scanner based on it. Technically,
   * we could squeeze this constructor and its {@link #tokenize(String, Token)} method all into the
   * static method above, yielding one big monster of a method.
   *
   * <p>That would be a bit ugly. Instead, it's helpful to break things into pieces like this, both
   * in terms of testability and in terms of keeping the complexity of our code under control. Also,
   * at some point we might want to reuse the state here for more than one run of the token scanner;
   * this structure gives us some future extensibility.
   */
  class CrunchedPatterns<T extends Enum<T> & TokenPatterns> {
    private static final String TAG = "RegexScanner";

    private final Pattern pattern;
    private final Map<String, T> nameToTokenMap;
    private final Set<String> groupNames;

    /**
     * Given an enum type that includes String values (and implements the {@link TokenPatterns}
     * interface), this builds a regular expression using "named-capturing groups" and uses that to
     * help tokenize input strings.
     *
     * @param enumPatternsClazz The enum's "Class", corresponding to the type parameter T
     */
    private CrunchedPatterns(Class<T> enumPatternsClazz) {
      if (!enumPatternsClazz.isEnum()) {
        // This particular failure should never actually happen,
        // because of the type constraint T extends Enum<T>.
        // Nonetheless, a bit of paranoia seems reasonable.
        Log.ethrow(TAG, "RegexScanner requires an enum class");
      }

      // this gets us an array of all the enum values in the type.
      final var enumConstants = List.of(enumPatternsClazz.getEnumConstants());

      final var nameToRegexMap =
          enumConstants
              .toMap(Enum::name, TokenPatterns::pattern)
              // get rid of non-parsing tokens, error/metadata tokens, etc.
              .filter(kv -> !kv._2.equals(""));

      final var numPatterns = nameToRegexMap.length();
      nameToTokenMap = enumConstants.toMap(Enum::name, e -> e);

      groupNames = nameToRegexMap.keySet();

      // Before we build the "real" regular expression that combines
      // all the individual ones, we're first going to try compiling
      // the individual expressions to make sure that they're
      // individually well-formed. This will result in better
      // error-feedback to developers.

      final var numSuccess =
          nameToRegexMap
              .map(
                  kv ->
                      tryOfNullable(() -> Pattern.compile(kv._2))
                          .onFailure(
                              x ->
                                  Log.eformat(
                                      TAG,
                                      "regular expression (%s) for (%s) is not valid: %s",
                                      kv._2,
                                      kv._1,
                                      x.getMessage())))
              .filter(Try::isSuccess)
              .length();

      if (numSuccess != numPatterns) {
        Log.eformat(TAG, "found only %d of %d valid regular expressions", numSuccess, numPatterns);
        throw new IllegalArgumentException("invalid regular expression");
      }

      // This is the final "group matching" regex pattern that we'll
      // use in the tokenizer. Here's a short tutorial that shows
      // what's going on here.
      // http://www.logicbig.com/tutorials/core-java-tutorial/java-regular-expressions/named-captruing-groups/

      pattern =
          Pattern.compile(
              nameToRegexMap
                  // build the named-capturing groups regular expression
                  .map(kv -> String.format("(?<%s>%s)", kv._1, kv._2))
                  .mkString("|"));

      // TODO: warn or error out if the key or value contain funny characters that break the regex
      //   (i.e., prevent anything like a SQL injection attack)
    }

    /**
     * This returns a list of pairs corresponding to the output of the tokenizer, where first
     * element of the pair is the enum value (you can later cast this to the type you used when
     * making the RegexScanner) and the second element is the string that the regex matched.
     *
     * <p>If the tokenizer hits something for which there isn't a matching regex, the next element
     * of the resulting list of tokens will be the failToken.
     */
    Stream<Token<T>> tokenize(String input, Token<T> failToken) {
      var extractor = new NamedGroupExtractor(input, failToken);

      return Stream.iterate(extractor::getNext);
    }

    private class NamedGroupExtractor {
      private final java.util.regex.Matcher jmatcher;
      private final String input;
      private final Token<T> failToken;

      // Engineering note: We need a counter for how far into the
      // string we are; we use this to detect skipped characters. We
      // also need to remember if we had a scanner error. We store
      // this state here, and mutate it within every call to
      // getNext().  We have to maintain and mutate our own state
      // because the state inside the regex engine is also
      // mutating. Unsurprisingly, this was difficult to get right.

      // Also, note that this is an *inner class of an inner class*
      // and that there's no "static" keyword in front. This means
      // that every instance of NamedGroupExtractor has a
      // CrunchedPatterns instance *outside* of it. Among other
      // benefits, you'll notice that we don't have to give a
      // definition for the type parameter again, since we pick that
      // up from our outer class. We can also see all the member
      // variables of CrunchedPatterns.

      // You don't really have to understand all this inner class /
      // outer class business for Comp215, but we're using it here for
      // convenience. The important part is that we're *encapsulating
      // mutating state inside a class instance*. Every time we call
      // getNext(), we'll get another token, and all of the state
      // necessary to track where we are is hidden inside.

      // The seeming obvious alternative would be to run
      // Stream.iterate on a lambda. But lambdas can't have persistent
      // state on the inside, so you'd get it from the lexical
      // scope. Unfortunately, you then run into Java's "effectively
      // final" requirement for lambdas. You end up having to create a
      // separate class to hold your state (the two variables below),
      // no matter what.

      private int matchOffset = 0;
      private boolean failure = false;

      private NamedGroupExtractor(String input, Token<T> failToken) {
        jmatcher = pattern.matcher(input);
        this.input = input;
        this.failToken = failToken;
      }

      boolean isFailure() {
        return failure;
      }

      Option<Token<T>> getNext() {
        if (failure) {
          return none();
        }

        if (jmatcher.find(matchOffset)) {
          var mresult = jmatcher.toMatchResult();
          var matchStart = mresult.start();

          var namesFound = groupNames.filter(name -> jmatcher.group(name) != null);
          var numNamesFound = namesFound.length();

          if (numNamesFound == 0) {
            // this case (hopefully) won't happen because, if there
            // are no matches, then jmatcher.find() should return
            // false. But in the interests of paranoia...
            Log.eformat(
                TAG,
                "no matching token found, scanner failed (context: %s)",
                safeSubstring(input, matchOffset, 30));
            failure = true;
            return some(failToken);
          }

          if (numNamesFound > 1) {
            Log.eformat(
                TAG,
                "multiple matches (token types: [%s]), input patterns are ambiguous (error!), scanner failed (context: %s)",
                namesFound.mkString(","),
                safeSubstring(input, matchOffset, 30));
            failure = true;
            return some(failToken);
          }

          if (matchStart > matchOffset) {
            // In this case, the matcher did find something, but it
            // didn't find something starting at the first
            // character. Since we're trying to tokenize, we can't
            // skip characters, therefore it's an error.
            Log.eformat(
                TAG,
                "matcher skipped %d character(s), scanner failed (context: %s) (match start: %s)",
                matchStart - matchOffset,
                safeSubstring(input, matchOffset, 30),
                safeSubstring(input, matchStart, 30));
            //            Log.e(TAG, "Original input: " + input);
            failure = true;
            return some(failToken);
          }

          var matchName = namesFound.head(); // the token, we found it, hurrah!
          var matchString = mresult.group();
          matchOffset += matchString.length(); // advance the state for next time: mutation!

          if (matchString.isEmpty()) {
            Log.eformat(
                TAG,
                "matcher found a zero-length string! bug in regex for token rule (%s)",
                matchName);
            failure = true;
            return some(failToken);
          }

          return nameToTokenMap
              .get(matchName) // go from the token string to the actual TokenPatterns enum
              .map(type -> new Token<>(type, matchString)) // then build a token around it
              .orElse(some(failToken));

        } else {
          // two possibilities: either we hit the end of the input, or
          // we failed to match any of the patterns
          if (matchOffset >= input.length()) {
            return none(); // empty-list; we're done!
          }

          // otherwise, there are some characters remaining that we
          // don't know what to do with
          Log.eformat(
              TAG,
              "no matching token found, scanner failed (context: %s)",
              safeSubstring(input, matchOffset, 10));
          failure = true;
          return some(failToken);
        }
      }
    }
  }
}

// Engineering note: while this scanner uses the regular expression
// system built into the Java standard libraries, it's not at all the
// most efficient way of accomplishing the job. Instead, there are
// tools that are tailor-made for precisely this purpose. See, for
// example, JFlex (http://www.jflex.de/). You'll see a lot more about
// this if you take Comp412.
