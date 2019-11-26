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

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jnull;
import static edu.rice.json.Builders.jobject;
import static edu.rice.qt.SequenceGenerators.sequences;
import static org.quicktheories.generators.Generate.constant;
import static org.quicktheories.generators.Generate.oneOf;
import static org.quicktheories.generators.SourceDSL.booleans;
import static org.quicktheories.generators.SourceDSL.doubles;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;

import org.quicktheories.core.Gen;

/**
 * This class implements generators to make random JSON trees for use in {@link
 * org.quicktheories.QuickTheory} theories.
 */
public interface JsonGenerators {
  // for each level of recursion, we'll generate JSON arrays or objects with this many entries
  int MAX_RECURSION_FANOUT = 5;

  // when we generate strings, they will be at most this long
  int MAX_STRING_LENGTH = 4;

  /** Internal function, generates JSON tree depths from 0 to max. */
  private static Gen<Integer> depthMaxGen(int max) {
    return integers().between(0, max);
  }

  /** Internal function, generates short but arbitrary strings, suitable for other tests. */
  private static Gen<String> stringGen() {
    // Engineering note: this generator generates short but nasty
    // strings, which will definitely help stress test our JSON
    // library. This, as it turns out, helped us find a bug in the
    // JSON escaping library (Apache Commons Text) we'd originally
    // been using, because it didn't correctly handle some control
    // characters.

    return strings().allPossible().ofLengthBetween(0, MAX_STRING_LENGTH);
  }

  /** Generates random JSON values, recursively, of a desired maximum depth. */
  static Gen<Value> jsons(int maxDepth) {
    return jsons(depthMaxGen(maxDepth));
  }

  /**
   * Generates random JSON values, recursively, drawn from a {@link Gen} that supplies the maximum
   * depth.
   */
  static Gen<Value> jsons(Gen<Integer> depthGenerator) {
    return depthGenerator.flatMap(
        depth ->
            depth <= 0
                ? jsonTerminals()
                : oneOf(jsonObjects(depthMaxGen(depth - 1)), jsonArrays(depthMaxGen(depth - 1))));
  }

  /** Generates random JSON objects, recursively, of a desired maximum depth. */
  static Gen<Value> jsonObjects(Gen<Integer> depthGenerator) {
    return depthGenerator
        .flatMap(
            depth ->
                sequences()
                    .of(jsonKeyValues(depthGenerator))
                    .ofSizeBetween(0, MAX_RECURSION_FANOUT))
        .map(list -> jobject(list));
  }

  /** Generates random JSON arrays, recursively, of a desired maximum depth. */
  static Gen<Value> jsonArrays(Gen<Integer> depthGenerator) {
    return depthGenerator.flatMap(
        depth ->
            sequences()
                .of(jsons(depthGenerator))
                .ofSizeBetween(0, MAX_RECURSION_FANOUT)
                .map(list -> jarray(list)));
  }

  /**
   * Generates random JSON key-value tuples where the value will be derived recursively with {@link
   * #jsons(Gen)}.
   */
  static Gen<Value.JKeyValue> jsonKeyValues(Gen<Integer> depthGenerator) {
    return depthGenerator.flatMap(depth -> stringGen().zip(jsons(depthGenerator), Builders::jpair));
  }

  /** Generates random JSON terminals (strings, numbers, booleans, and nulls). */
  static Gen<Value> jsonTerminals() {
    // Engineering note: this generator uses a composition operator
    // (Generate.oneOf()), one of several composition operators
    // supported by QuickTheories to draw values from multiple
    // generators.

    // Exactly which of the generators that oneOf() selects will
    // happen at random, different for each trial, like everything
    // else with QuickTheories, but all the random-handling happens
    // inside oneOf().  Ask IntelliJ to follow this to its source code
    // (e.g., Command-B on a Mac) and check out what's going on under
    // the hood.

    return oneOf(jsonStrings(), jsonNumbers(), jsonBools(), jsonNulls());
  }

  /** Generates random JSON strings. */
  static Gen<Value> jsonStrings() {
    return stringGen().map(Builders::jstring);
  }

  /** Generates random JSON numbers. */
  static Gen<Value> jsonNumbers() {
    // Engineering note: we need to filter out NaN, Infinity, etc.,
    // since those aren't allowed as JSON numbers Initially, we didn't
    // have this constraint, and QT dutifully identified these
    // "non-finite" doubles as something that our parser couldn't
    // handle.

    return doubles().any().assuming(Double::isFinite).map(Builders::jnumber);
  }

  /** Generates random JSON booleans. */
  static Gen<Value> jsonBools() {
    // Engineering note: this generator uses the map() functionality,
    // available with all QT generators, which works similarly to
    // map() on VAVR's Seq classes. We're using QT's built-in boolean
    // generator and then mapping its output to our JBoolean.

    // Exercise for the reader: Rebuild this using oneOf(), as used
    // above in jsonTerminals(), and constant(), as used below with
    // jsonNulls(). The results should be exactly the same. See also
    // Generate.pick(), which selects from a list of
    // inputs. (Unfortunately, it uses java.util.List, as opposed to
    // VAVR lists/seqs.)

    return booleans().all().map(Builders::jboolean);
  }

  /**
   * Generates random JSON nulls. (There's really only one of them, but we can still generate it!)
   */
  static Gen<Value> jsonNulls() {
    return constant(jnull());
  }
}
