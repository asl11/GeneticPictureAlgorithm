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

package edu.rice.qt;

import static edu.rice.qt.MapGenerators.maps;
import static edu.rice.qt.SequenceGenerators.sequences;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;

import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import java.util.ArrayList;
import org.quicktheories.core.Gen;

public interface QtHelpers {
  /** Generates sequences of integers of size between 0 and 20. */
  static Gen<Seq<Integer>> integerSequences() {
    return sequences().of(integers().all()).ofSizeBetween(0, 20);
  }

  /** Generates simple, short strings. */
  static Gen<String> reasonableStrings() {
    return strings().basicLatinAlphabet().ofLengthBetween(1, 5);
  }

  /** Generates a sequence of the requested length of strings. */
  static Gen<Seq<String>> stringSequences(int length) {
    return sequences().of(reasonableStrings()).ofSize(length);
  }

  /** Generates a sequence of strings of length between 0 and 10. */
  static Gen<Seq<String>> stringSequences() {
    return sequences().of(reasonableStrings()).ofSizeBetween(0, 10);
  }

  /** Generates maps of the requested size. */
  static Gen<Map<String, String>> stringMaps(int size) {
    return maps().of(reasonableStrings(), reasonableStrings()).ofSize(size);
  }

  /** Generates maps of size ranging from 0 to 100. */
  static Gen<Map<String, String>> stringMaps() {
    return maps().of(reasonableStrings(), reasonableStrings()).ofSizeBetween(0, 100);
  }

  /** Generates sets of size ranging from 0 to 100. */
  static Gen<Set<String>> reasonableStringSets() {
    return integers()
        .between(0, 100) // Gen<Integer>
        .flatMap(QtHelpers::stringSequences) // Gen<Seq<String>>
        .map(seq -> HashSet.ofAll(seq));
  }

  /**
   * Runs a QuickTheories {@link Gen} generator once and returns the results. Useful when you need a
   * single value for repeated testing, as opposed to the regular use of QuickTheories where you
   * wish to run many tests with many different values.
   */
  static <T> T qtGenOnce(Gen<T> generator) {
    // Engineering note: We're doing this with a side-effect (dumping the
    // generated value into a ArrayList and later retrieving it). It would
    // be better if we could directly invoke the Gen, but QuickTheories
    // makes this remarkably difficult to do. This uses mutation, but
    // it's at least simple.
    var store = new ArrayList<T>();
    qt().withExamples(1).forAll(generator).check(store::add);
    return store.get(0);
  }
}
