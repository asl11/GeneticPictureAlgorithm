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

import static edu.rice.util.Strings.stringToOptionDouble;
import static edu.rice.util.Strings.stringToOptionInteger;
import static edu.rice.util.Strings.stringToTryDate;
import static edu.rice.util.Strings.stringToUnixLinebreaks;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

public class StringsTest {
  @Test
  public void testStringToUnixLinebreaks() {
    assertEquals("hello world", stringToUnixLinebreaks("hello world"));
    assertEquals("hello\nworld\n", stringToUnixLinebreaks("hello\nworld\n"));
    assertEquals("hello\nworld\n", stringToUnixLinebreaks("hello\r\nworld\r"));
    assertEquals("hello\nworld\n", stringToUnixLinebreaks("hello\r\nworld\r\n"));
    assertEquals("hello\\r\\nworld\n", stringToUnixLinebreaks("hello\\r\\nworld\r\n"));
  }

  @Test
  public void testStringToNumber() {
    assertEquals(some(5), stringToOptionInteger("5"));
    assertEquals(some(15), stringToOptionInteger("F", 16));
    assertEquals(none(), stringToOptionInteger("5.5"));
    assertEquals(none(), stringToOptionInteger("hello"));
    assertEquals(some(5.0), stringToOptionDouble("5"));
    assertEquals(some(5.5), stringToOptionDouble("5.5"));
    assertEquals(none(), stringToOptionDouble("hello"));
  }

  @Test
  public void testDate() {
    var tdate1 = stringToTryDate("2017-10-23T22:36:39Z");
    assertTrue(tdate1.isSuccess());
    var time1 = LocalDateTime.ofInstant(tdate1.get().toInstant(), ZoneId.systemDefault());
    assertEquals(2017, time1.getYear());
    assertEquals(10, time1.getMonthValue());
    // we can't check the exact hour since it will vary depending on
    // the timezone where we're running the test!

    var tdate2 = stringToTryDate("2017-10-23"); // doesn't include hh:mm:ss, so should fail
    assertFalse(tdate2.isSuccess());
    assertTrue(tdate2.getCause() instanceof DateTimeParseException);
  }
}
