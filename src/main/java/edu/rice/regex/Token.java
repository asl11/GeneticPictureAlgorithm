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
import static edu.rice.util.Strings.objectToEscapedString;

import edu.rice.json.Value;

/** General-purpose parsing tokens, produced by {@link RegexScanner}, among other places. */
public class Token<T extends Enum<T> & TokenPatterns> {
  public final T type;
  public final String data;

  public Token(T type, String data) {
    this.type = type;
    this.data = data;
  }

  @Override
  public int hashCode() {
    return toString().hashCode(); // a kludge, but hopefully useful
  }

  @Override
  public boolean equals(Object t) {
    if (!(t instanceof Token<?>)) {
      return false;
    }

    // we're doing an unchecked type cast here, but it's okay because if the TokenPatterns differ,
    // the equals() test will sort it out
    var tt = (Token<? extends Enum<T>>) t;
    return this.data.equals(tt.data) && this.type.equals(tt.type);
  }

  @Override
  public String toString() {
    return String.format("(%s: %s)", type.name(), objectToEscapedString(data));
  }

  /** Converts a token into a JSON object with a one key (the name) and value (the data string). */
  public Value toJson() {
    return jobject(jpair(type.name(), data));
  }
}
