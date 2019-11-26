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

import static edu.rice.qt.SequenceGenerators.sequences;
import static org.quicktheories.generators.Generate.oneOf;
import static org.quicktheories.generators.SourceDSL.strings;

import org.quicktheories.core.Gen;

/**
 * This class implements generators to make random s-expression for use in {@link
 * org.quicktheories.QuickTheory} theories.
 */
public interface SexprGenerators {
  /** Generates random s-expression values, recursively, of a desired maximum depth. */
  static Gen<Value> sexprs(int maxDepth) {
    return sequences()
        .of((maxDepth <= 0) ? words() : oneOf(words(), sexprs(maxDepth - 1)))
        .ofSizeBetween(0, 5)
        .map(list -> Value.sexpr(list));
  }

  /** Generates random s-expression words (terminals). */
  static Gen<Value> words() {
    // QuickTheories' string generator doesn't let us say what we really want, which
    // is that we're looking for any "word" characters, based on the \w regular expression,
    // so instead we'll just use the range of lower-case letters.

    // We *could* have instead used the .assuming() and search for words that match
    // the regular expression, but that seems unnecessarily painful.

    return strings().betweenCodePoints('a', 'z').ofLengthBetween(1, 5).map(Value::word);
  }
}
