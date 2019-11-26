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

package edu.rice.util;

import static edu.rice.vavr.Tries.tryOfNullable;
import static org.unbescape.java.JavaEscape.escapeJava;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.RegEx;
import org.intellij.lang.annotations.Language;
import org.unbescape.java.JavaEscapeLevel;

/** These static utility functions are helpful when converting arbitrary Java objects to strings. */
public interface Strings {
  /**
   * This helper function converts any object to a String, with special handling for null and for
   * String itself. Strings will be "escaped" and surrounded by quotation marks. All the rest just
   * get Object.toString() called on them.
   */
  static String objectToEscapedString(@Nullable Object o) {
    if (o instanceof String) {
      // escapeJava comes to us from the "Unbescape" escaping library.
      return "\"" + escapeJava((String) o, JavaEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET) + "\"";
    } else {
      return objectToString(o);
    }
  }

  /**
   * This helper function converts any object to a String, with special handling for null. All the
   * rest just get Object.toString() called on them.
   */
  static String objectToString(@Nullable Object o) {
    if (o == null) {
      return "null";
    } else {
      return o.toString();
    }
  }

  /**
   * This helper function converts a String to its UTF-8 representation. If there's an error in the
   * internal conversion, an array of length 0 is returned.
   */
  static byte[] stringToUTF8(String input) {
    return input.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * This helper function takes a string and a "default value". If the given string is non-null,
   * then that's what's returned. If the given string is null, then the default value is returned
   * instead. This helper is useful when dealing with libraries that want to return null as an error
   * condition, but you would prefer something else.
   */
  static String stringOrDefault(@Nullable String input, String defaultIfNull) {
    return input == null ? defaultIfNull : input;
  }

  /**
   * This helper function converts from a string to a long, or returns an Option.none if there's a
   * failure.
   */
  static Option<Long> stringToOptionLong(String s) {
    return tryOfNullable(() -> Long.decode(s)).toOption();
  }

  private static Try<Integer> stringToTryInteger(String s, int base) {
    return tryOfNullable(() -> Integer.valueOf(s, base));
  }

  /**
   * This helper function converts from a string to an integer, or returns an Option.none if there's
   * a failure.
   */
  static Option<Integer> stringToOptionInteger(String s) {
    return stringToTryInteger(s, 10).toOption();
  }

  /**
   * This helper function converts from a string to an integer, or returns an Option.none if there's
   * a failure. You can also specify the base (e.g., base 16, base 10, etc.).
   */
  static Option<Integer> stringToOptionInteger(String s, int base) {
    return stringToTryInteger(s, base).toOption();
  }

  /**
   * This helper function converts from a string to a double, or returns an Option.none if there's a
   * failure.
   */
  static Option<Double> stringToOptionDouble(String s) {
    return tryOfNullable(() -> Double.valueOf(s)).toOption();
  }

  /**
   * This helper function converts from a date, in <a
   * href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 format</a>, to a {@link Date} object,
   * while using VAVR error handling. An example of a valid ISO date is <code>
   * 2017-10-23T22:36:39Z</code>. This methods require its input to have hours, minutes, and
   * seconds. Subsecond accuracy is optional.
   */
  static Try<Date> stringToTryDate(String s) {
    return tryOfNullable(
        () -> Date.from(Instant.parse(s))); // throws an exception if the string isn't a valid date
  }

  /**
   * This helper function converts from a date, in <a
   * href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 format</a>, to a {@link Date} object,
   * while using VAVR error handling. An example of a valid ISO date is <code>
   * 2017-10-23T22:36:39Z</code> This method requires its input to have hours, minutes, and seconds.
   * Subsecond accuracy is optional.
   */
  static Option<Date> stringToOptionDate(String s) {
    return stringToTryDate(s).toOption();
  }

  /** This helper function converts a regular expression into a predicate on a string. */
  static Predicate<String> regexToPredicate(@RegEx @Language("RegExp") String regex) {
    // Engineering note: we're using two separate annotations here to
    // indicate that the argument is a regular expression. Why?
    // The @Language annotation causes IntelliJ to do full syntax
    // highlighting on the arguments.  Very convenient! The former is
    // understood by FindBugs and other common tools but not by
    // IntelliJ.  See the bug report below.
    // https://youtrack.jetbrains.com/issue/IDEA-172271

    return Pattern.compile(regex).asPredicate();
  }

  /**
   * Normalizes line endings in strings from CR-LF and CR to be only LF. (That is, given Windows or
   * older Mac line endings, convert everything to Unix-style.) Useful for unit tests.
   */
  static String stringToUnixLinebreaks(String input) {
    return input
        .replaceAll("\r\n", "\n") // Windows CR-LF -> Unix LF
        .replaceAll("\r", "\n"); // Mac CR -> Unix LF
  }

  /**
   * The real {@link String#substring(int, int)} will throw an exception if you ask for anything
   * beyond the end of the string. This method will truncate at the end. No exceptions.
   */
  static String safeSubstring(String input, int offset, int length) {
    if (offset + length > input.length()) {
      return input.substring(offset);
    } else {
      return input.substring(offset, offset + length);
    }
  }

  /**
   * This helper function returns its input unchanged, but will cause IntelliJ to highlight it as a
   * regular expression and will cause other static checking tools to validate the string as a
   * well-formed regular expression. Presumably, the compiler will optimize it away, so there should
   * be no runtime performance cost. This method is also useful for methods that require their input
   * to be a well-formed regular expression. Wrapping a String with a call to <code>regex()</code>
   * will also apply the appropriate label to the String so other static checks will be satisfied.
   */
  static @Language("RegExp") String regex(@RegEx @Language("RegExp") String s) {
    return s;
  }

  /**
   * This helper function returns its input unchanged, but will cause IntelliJ to highlight it as a
   * JSON (JavaScript Object Notation) string and will cause other static checking tools to validate
   * the string as well-formed JSON. Presumably, the compiler will optimize it away, so there should
   * be no runtime performance cost. This method is also useful for methods that require their input
   * to be well-formed JSON. Wrapping a String with a call to <code>json()</code> will also apply
   * the appropriate label to the String so other static checks will be satisfied.
   */
  static @Language("JSON") String json(@Language("JSON") String input) {
    return input;
  }
}
