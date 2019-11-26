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

import javax.annotation.Nullable;

/** Demo to exercise the null-checking static analysis features of IntelliJ. */
public class NotNullTest {
  static final String TAG = "NotNullTest";

  static void notNull(String input) {
    Log.i(TAG, "notNull: " + input);
  }

  static void nullable(@Nullable String input) {
    Log.i(TAG, "nullable: " + input);
  }

  static void testGoodCases() {
    nullable("should work");
    nullable(null);
    notNull("should also work");
  }

  // Engineering note: This test is actually measuring whether the various null-related annotation
  // do their job. These tests aren't "unit" tests because they don't actually run with JUnit.
  // They run with IntelliJ's "Analyze -> Inspect Code" or when you run Error-Prone via Gradle.

  static void testNotNullWithNull() {
    notNull(null);
  }

  static String getString() {
    return "Hello"; // not null...
  }

  static String getStringNull() {
    return null;
  }

  static void testParametersAreNonnull() {
    notNull("Ahoy!");
    notNull(getString());
    notNull(getStringNull());
    notNull(null);
  }
}
