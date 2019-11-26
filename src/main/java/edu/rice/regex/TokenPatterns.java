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

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;

import edu.rice.json.Value;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.function.BiFunction;

/**
 * When we're implementing tokenizers, we're going to use enums to specify the token names and
 * regular expressions. By default, every enum has a <code>String code()</code> method which can get
 * the token name, but there isn't a method to get the regex pattern you associate with that token.
 * For that, you'll use the TokenPatterns interface.
 */
public interface TokenPatterns {
  String name(); // this comes from the enum

  String pattern(); // you have to implement this one

  /**
   * General-purpose "deconstructing" match on a token. Takes a lambda with two arguments, which
   * will be the name and regex pattern associated with this particular token rule and returns
   * whatever that function returns.
   */
  default <R> R match(BiFunction<? super String, ? super String, ? extends R> func) {
    return func.apply(name(), pattern());
  }

  /** Converts a token's {@link #name()} and {@link #pattern()} into a {@link Tuple2}. */
  default Tuple2<String, String> toTuple() {
    return Tuple.of(name(), pattern());
  }

  /**
   * Useful when trying to construct a larger JSON structure, this creates a simple JSON object with
   * one key and one value, corresponding to the {@link #name()} and {@link #pattern()}.
   */
  default Value toJson() {
    return jobject(jpair(name(), pattern()));
  }
}
