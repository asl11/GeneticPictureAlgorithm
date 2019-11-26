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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

public class MatcherTest {
  @Test
  public void testGetMatches() {
    var test1 = "Hello, world, how are you doing? This is a beautiful day.";
    var matcher1 = new Matcher("[hH]\\S*"); // starting with 'h' or 'H' then non-space chars

    var results1 = matcher1.getMatches(test1);
    assertEquals(List.of("Hello,", "how", "his"), results1);
  }

  @Test
  public void testGetGroupMatches() {
    // A test like this exercises our code, of course, but if you're
    // learning a non-trivial concept like regular expressions, it's
    // helpful to write unit tests to test your *understanding* of the
    // concept itself.

    // Also, notice how the @Language("RegExp") annotation causes
    // fancy highlighting of your regular expression?  That's a
    // convenient feature built into IntelliJ.

    var test2 = "dwallach:odfkjewhglx:24:97:/bin/tcsh:/Users/dwallach";
    var matcher2 = new Matcher("([^:]*):([^:]*):(\\d+):(\\d+):([^:]*):([^:]*)");

    var results2 = matcher2.getGroupMatches(test2);
    assertEquals(
        List.of("dwallach", "odfkjewhglx", "24", "97", "/bin/tcsh", "/Users/dwallach"), results2);
  }
}
