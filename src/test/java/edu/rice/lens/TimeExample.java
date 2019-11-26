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

package edu.rice.lens;

import static edu.rice.lens.Lens.lens;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Shows how lenses can do something more than just simple getters and setters. We want to have a
 * "minutes" and "seconds" field where we keep them normalized. So you'll never have 5 minute and 63
 * seconds, but rather that will magically become 6 minutes and 3 seconds.
 */
public class TimeExample {
  public static class Time {
    public final int minutes;
    public final int seconds;

    // We're hiding the constructor, forcing Time instances to be created through
    // the make() method, which normalizes the time.
    private Time(int minutes, int seconds) {
      this.minutes = minutes;
      this.seconds = seconds;
    }

    /**
     * Builds an instance of the Time class, guaranteeing that the minutes and seconds are
     * "normalized" (i.e., the resulting seconds is always in [0,59] and the resulting minutes will
     * be adjusted appropriately).
     */
    public static Time make(int minutes, int seconds) {
      if (seconds > 0) {
        return new Time(minutes + seconds / 60, seconds % 60);
      } else {
        var minutesDelta = 1 - seconds / 60;
        return new Time(minutes - minutesDelta, seconds + minutesDelta * 60);
      }
    }

    // These lenses demonstrate setters that do more than just make a new instance with
    // updated values, but rather can call any method or do any other necessary computation,
    // in this case to ensure a "normalized" result.
    public static final Lens<Time, Integer> MinutesLens =
        lens(time -> time.minutes, (time, minutes) -> Time.make(minutes, time.seconds));

    public static final Lens<Time, Integer> SecondsLens =
        lens(time -> time.seconds, (time, seconds) -> Time.make(time.minutes, seconds));

    @Override
    public String toString() {
      return String.format("%d:%02d", minutes, seconds);
    }
  }

  @Test
  public void timeTest() {
    var t1 = new Time(5, 23);
    assertEquals("5:23", t1.toString());
    assertEquals("5:40", Time.SecondsLens.set(t1, 40).toString());
    assertEquals("6:10", Time.SecondsLens.set(t1, 70).toString());
    assertEquals("4:50", Time.SecondsLens.set(t1, -10).toString());
    assertEquals("2:50", Time.SecondsLens.set(t1, -130).toString());
    assertEquals("6:23", Time.MinutesLens.set(t1, 6).toString());
  }
}
