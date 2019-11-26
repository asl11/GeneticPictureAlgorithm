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

package edu.rice.week6counting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.rice.io.Files;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

public class FreqCountTest {
  // Courtesy of: https://baconipsum.com/
  private final String baconIpsum = Files.readResource("baconIpsum.txt").getOrElse("missing file!");

  // Courtesy of: http://www.archives.gov/exhibits/charters/constitution_transcript.html
  private final String usConstitution =
      Files.readResource("USConstitution.txt").getOrElse("missing file!");

  @Test
  public void testFrequencyCount() {
    var baconCount = FreqCount.count(baconIpsum);
    var constitutionCount = FreqCount.count(usConstitution);

    var topBacon = FreqCount.mostFrequent(baconCount);
    var topConstitution = FreqCount.mostFrequent(constitutionCount);

    // A common issue with broken regular expressions is that they'll match the empty string, so
    // first we'll have an assertion that flags this common error.

    assertFalse(
        topBacon.map(Tuple2::_1).contains(""),
        "Empty strings should not appear in frequency counts!");
    assertFalse(
        topConstitution.map(Tuple2::_1).contains(""),
        "Empty strings should not appear in frequency counts!");

    assertEquals(
        List.of(
            Tuple.of("pork", 18),
            Tuple.of("ribs", 12),
            Tuple.of("beef", 11),
            Tuple.of("short", 9),
            Tuple.of("loin", 8),
            Tuple.of("tip", 8),
            Tuple.of("alcatra", 7),
            Tuple.of("corned", 7),
            Tuple.of("bacon", 6),
            Tuple.of("belly", 6)),
        topBacon.take(10));

    assertEquals(
        List.of(
            Tuple.of("the", 423),
            Tuple.of("of", 289),
            Tuple.of("and", 192),
            Tuple.of("shall", 191),
            Tuple.of("be", 125),
            Tuple.of("to", 110),
            Tuple.of("in", 89),
            Tuple.of("states", 80),
            Tuple.of("or", 79),
            Tuple.of("united", 54)),
        topConstitution.take(10));

    System.out.println("Top ten bacon words: " + topBacon.take(10));
    System.out.println("Top ten Constitution words: " + topConstitution.take(10));
  }
}
