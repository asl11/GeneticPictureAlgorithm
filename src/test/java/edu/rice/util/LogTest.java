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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

public class LogTest {
  private static final String TAG = "LogTest";

  @Test
  public void logbackTest() {
    // this is to make sure we can find the logback config files
    var logbackFile = ClassLoader.getSystemResource("logback.xml");

    if (logbackFile != null) {
      Log.i(TAG, "Logback: " + logbackFile.toString());
    } else {
      fail("Logback: config file not found!");
    }
  }

  @Test
  public void basicTest() {
    Log.i(TAG, "starting basicTest");
    Log.e(TAG, "some error");

    Log.i(TAG, "trying out lambdas");
    Log.i(TAG, () -> String.format("Lambda %d %d", 100, 1000));
    Log.iformat(TAG, "Formatted %d %d", 100, 1000);

    Log.i(TAG, "messing with log levels");

    Log.setLogLevel(Log.ERROR);
    Log.i(TAG, "shouldn't see this");
    Log.e(TAG, "should see this (1/1)");

    Log.setLogLevel(Log.NOTHING);
    Log.i(TAG, "shouldn't see this");
    Log.e(TAG, "shouldn't see this");

    Log.setLogLevel(Log.ALL);

    Log.i(TAG, "finishing basicTest");
  }

  private static void foo() {
    bar();
  }

  private static void bar() {
    throw new RuntimeException("foo/bar");
  }

  @Test
  public void wrapTest() {
    var list = List.of("A", "New", "World", "To", "Play", "With", "Lambda");
    assertEquals("A", list.head()); // sanity check
    var lengths = list.map(String::length);
    var lengths2 = list.map(Log.iwrap(TAG, String::length));
    assertEquals(lengths, lengths2);

    int totalLength = list.foldLeft(0, (len, elem) -> len + elem.length());
    int totalLength2 = list.foldLeft(0, Log.iwrap(TAG, (len, elem) -> len + elem.length()));
    assertEquals(totalLength, totalLength2);
  }

  @Test
  public void otherObjectsTest() {
    // We should also be able to Log any object at all, and its
    // internal toString method will be called, along with special
    // handling of Optionals. (If you're reading this in the first
    // weeks of the semester, don't panic. We'll explain them later
    // on.)
    Log.i(TAG, "And now, we log some other things besides strings");
    Log.i(TAG, Option.some("Hello Rice Owls!"));
    Log.i(TAG, Option.<String>none());
    Log.i(TAG, List.of("Hello", "Rice", "Owls"));
  }
}
