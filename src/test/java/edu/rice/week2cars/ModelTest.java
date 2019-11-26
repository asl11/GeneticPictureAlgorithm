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

package edu.rice.week2cars;

import static edu.rice.week2cars.Model.averageAge;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week02", topic = "Average Age")
public class ModelTest {
  @Test
  @Grade(project = "Week02", topic = "Average Age", points = 1.0)
  public void correctAverageForEmptyGarage() {
    assertThrows(
        NoSuchElementException.class,
        () -> {
          var ignored = averageAge(emptySet(), 2018);
        });
  }

  @Test
  @Grade(project = "Week02", topic = "Average Age", points = 1.0)
  public void correctAverageAge() {
    final var actual = averageAge(DreamGarage.getCars(), 2018);
    assertEquals(48, actual);
  }
}
