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

import static edu.rice.util.Strings.stringOrDefault;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.Language;

/**
 * A relatively pleasant wrapper around the entirely unpleasant java.util.regex.* library for a
 * variety of common use-case scenarios.
 */
public class Matcher {
  @SuppressWarnings("unused")
  private static final String TAG = "Matcher";

  private final Pattern pattern;

  /** Builds a regular expression matcher using the supplied regular expression. */
  public Matcher(@Language("RegExp") String regex) {
    this.pattern = Pattern.compile(regex);
  }

  /**
   * If you just want to find all the places in your input that match the regex, then this is the
   * method for you.
   */
  public Seq<String> getMatches(String input) {
    var jmatcher = pattern.matcher(input);
    return Stream.iterate(() -> Option.when(jmatcher.find(), jmatcher::group));
  }

  /**
   * If your regex has groups in it, this will find the first instance in your regex where it
   * matches and return a list of all the strings matching the corresponding groups.
   *
   * <p>Warning, per {@link java.util.regex.Matcher#group(int)}: "If the match was successful but
   * the group specified failed to match any part of the input sequence, then null is returned. Note
   * that some groups, for example (a*), match the empty string. This method will return the empty
   * string when such a group successfully matches the empty string in the input."
   *
   * <p>If we get "null" back from java.util.regex.Matcher, we'll replace it with the empty string.
   *
   * @see java.util.regex.Matcher#group(int)
   */
  public Seq<String> getGroupMatches(String input) {
    var jmatcher = pattern.matcher(input);
    if (!jmatcher.find()) {
      return List.empty();
    }
    var numGroups = jmatcher.groupCount();
    if (numGroups == 0) {
      return List.empty();
    }
    return Stream.rangeClosed(1, numGroups).map(i -> stringOrDefault(jmatcher.group(i), ""));
  }
}
