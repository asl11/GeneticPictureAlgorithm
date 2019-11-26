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

import static edu.rice.week3lists.GList.average;
import static edu.rice.week3lists.GList.maximum;
import static edu.rice.week3lists.GList.minimum;
import static edu.rice.week3lists.GList.naiveSum;
import static edu.rice.week3lists.GList.sum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

/**
 * These unit tests are meant to exercise your project for week3. If you pass these tests, there's a
 * good chance your code works. However, we encourage you to add additional tests.
 */
@GradeProject(name = "Week03", description = "Functional Lists / Subsets", warningPoints = 1.0)
@GradeTopic(project = "Week03", topic = "AppendAll")
@GradeTopic(project = "Week03", topic = "Ranges")
@GradeTopic(project = "Week03", topic = "Folds", maxPoints = 2.5)
public class Week3Project {
  @Test
  @Grade(project = "Week03", topic = "AppendAll", points = 1.0)
  public void testAppendAll() {
    var empty = GList.<Integer>empty();
    var list1 = GList.of(5, 4, 3, 2, 1);
    var list2 = GList.of(8, 7, 6);
    var list3 = list1.appendAll(list2);
    assertEquals(GList.of(5, 4, 3, 2, 1, 8, 7, 6), list3);

    // now for a bunch of tests involving empty lists
    var list4 = empty.appendAll(empty);
    assertTrue(list4.isEmpty());

    var list5 = list1.appendAll(empty);
    var list6 = empty.appendAll(list1);
    assertEquals(list1, list5);
    assertEquals(list1, list6);

    // lastly, let's double check that we're getting ordering correct
    var list7 = list1.prepend(6).prepend(7);
    assertEquals(GList.of(7, 6, 5, 4, 3, 2, 1), list7);
  }

  @Test
  @Grade(project = "Week03", topic = "Ranges", points = 2.0)
  public void testRanges() {
    var firstTen = GList.rangeClosed(1, 10);
    assertEquals(GList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), firstTen);

    var odds = GList.rangeClosedBy(1, 10, 2);
    assertEquals(GList.of(1, 3, 5, 7, 9), odds);

    var backwards = GList.rangeClosedBy(10, 1, -2);
    assertEquals(GList.of(10, 8, 6, 4, 2), backwards);

    var shouldBeEmpty = GList.rangeClosed(10, 5);
    assertTrue(shouldBeEmpty.isEmpty());

    var shouldAlsoBeEmpty = GList.rangeClosedBy(5, 10, -1);
    assertTrue(shouldAlsoBeEmpty.isEmpty());
  }

  @Test
  @Grade(project = "Week03", topic = "Folds", points = 0.5)
  public void testMinimum() {
    var emptyList = GList.<String>empty();
    var stringList = GList.of("Charlie", "Dorothy", "Alice", "Bob");
    assertEquals("Alice", minimum("Nobody", stringList));
    assertEquals("Nobody", minimum("Nobody", emptyList));
  }

  @Test
  @Grade(project = "Week03", topic = "Folds", points = 0.5)
  public void testMaximum() {
    var emptyList = GList.<String>empty();
    var stringList = GList.of("Charlie", "Dorothy", "Alice", "Bob");
    assertEquals("Dorothy", maximum("Nobody", stringList));
    assertEquals("Nobody", maximum("Nobody", emptyList));
  }

  @Test
  @Grade(project = "Week03", topic = "Folds", points = 0.5)
  public void testAverage() {
    var emptyList = GList.<Double>empty();
    var list = GList.of(8.0, 2.0, 0.0, 10.0);

    // we have to cast from double to Double because assertEquals() doesn't want primitive types
    assertEquals(5.0, average(0.0, list));
    assertEquals(0.0, average(0.0, emptyList));
  }

  @Test
  @Grade(project = "Week03", topic = "Folds", points = 0.5)
  public void testSum() {
    var list = GList.of(8.0, 2.0, 0.0, 10.0);
    var emptyList = GList.<Double>empty();

    assertEquals(0.0, naiveSum(emptyList));
    assertEquals(0.0, sum(emptyList));
    assertEquals(20.0, naiveSum(list));
    assertEquals(20.0, sum(list));
  }

  @Test
  @Grade(project = "Week03", topic = "Folds", points = 0.5)
  public void testSumOnCrazyNumbers() {
    // Correct sum is 2.0, but lack of precision in a naive sum gives us zero.
    var list = GList.of(1.0, 1e100, 1.0, -1e100);
    assertEquals(0.0, naiveSum(list));
    assertEquals(2.0, sum(list));
  }

  @Test
  @Grade(project = "Week03", topic = "Folds", points = 0.5)
  public void testAverageOnCrazyNumbers() {
    // Correct average is 0.5, but lack of precision in a naive sum gives us zero.
    var list = GList.of(1.0, 1e100, 1.0, -1e100);
    assertEquals(0.5, average(0.0, list));
  }
}
