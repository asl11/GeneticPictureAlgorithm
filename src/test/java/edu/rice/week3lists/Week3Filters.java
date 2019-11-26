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

package edu.rice.week3lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week03", topic = "Filters", maxPoints = 1.5)
public class Week3Filters {
  @Test
  @Grade(project = "Week03", topic = "Filters", points = 0.5)
  public void testNonNegativeNumbers() {
    // Filters, part 1: rewrite this predicate so the unit tests below succeed.
    Predicate<Integer> pred = x -> x >= 0;

    var list1 = GList.of(-1, -3); // test when list contains only negative numbers
    var list2 = GList.of(0); // test when list contains 0
    var list3 = GList.of(50, -19, 0, -1, 5); // test when list contains positive numbers

    assertTrue(list1.filter(pred).isEmpty());
    assertEquals(GList.of(0), list2.filter(pred));
    assertEquals(GList.of(50, 0, 5), list3.filter(pred));
  }

  @Test
  @Grade(project = "Week03", topic = "Filters", points = 0.5)
  public void testStringLengthBetweenFiveAndTen() {
    // Filters, part 2: rewrite this predicate so the unit
    // tests below succeed. You may wish to consult the Javadoc for
    // java.lang.String:
    // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html
    Predicate<String> pred = x -> (x.length() >= 5) && (x.length() <= 10);

    // test when some string lengths are less than 5
    var list1 = GList.of("apples", "hat", "");

    // test when some strings lengths are greater than 10
    var list2 = GList.of("oranges", "caterpillar");

    assertEquals(GList.of("apples"), list1.filter(pred));
    assertEquals(GList.of("oranges"), list2.filter(pred));

    // test string length of 5 and 10
    var list3 = GList.of("pear", "fruit", "helicopter");

    assertEquals(GList.of("fruit", "helicopter"), list3.filter(pred));
  }

  @Test
  @Grade(project = "Week03", topic = "Filters", points = 0.5)
  public void testLetterCAppearsAnywhereBeforeLetterA() {
    // Filters, part 3: rewrite this predicate so the unit
    // tests below succeed. You may wish to consult the Javadoc for
    // java.lang.String:
    // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html
    Predicate<String> pred =
        x -> {
          var lx = x.toLowerCase();
          return lx.contains("c") && lx.contains("a") && (lx.indexOf("c") < lx.lastIndexOf("a"));
        };

    // test when c is immediately before a
    var list1 = GList.of("cat", "horse", "dog");

    // test capitalization
    var list2 = GList.of("cAt", "CAT", "Cat", "Horse", "Dog");

    assertEquals(GList.of("cat"), list1.filter(pred));
    assertEquals(GList.of("cAt", "CAT", "Cat"), list2.filter(pred));

    // test when c is both after one a and before another a
    var list3 = GList.of("a plant", "a tree", "a cactus");
    assertEquals(GList.of("a cactus"), list3.filter(pred));

    // test when c is not immediately before a
    var list4 = GList.of("c a t", "h o r s e", "d o g");
    var list5 = GList.of("ACCUMULATOR", "ADDER");
    var list6 = GList.of("calCium", "accountable", "accounting");

    assertEquals(GList.of("c a t"), list4.filter(pred));
    assertEquals(GList.of("ACCUMULATOR"), list5.filter(pred));
    assertEquals(GList.of("calCium", "accountable"), list6.filter(pred));
  }
}
