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

import static edu.rice.week2cars.Manufacturer.manufacturerFrequencyCount;
import static edu.rice.week2cars.Manufacturer.mostPopularManufacturer;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@GradeProject(name = "Week02", description = "Dream Garage / Subsets", warningPoints = 1.0)
@GradeTopic(project = "Week02", topic = "Frequency")
public class DreamGarageTest {
  @Test
  @Grade(project = "Week02", topic = "Frequency", points = 1.0)
  public void correctFrequencyCountsForEmptyGarage() {
    var emptyCount = manufacturerFrequencyCount(emptySet());
    Map<Manufacturer, Integer> emptyExpected = emptyMap();
    assertEquals(emptyExpected, emptyCount); // Java helpfully defines "equality" on its Collections

    assertThrows(
        NoSuchElementException.class,
        () -> {
          var ignored = mostPopularManufacturer(emptySet());
        });
  }

  @Test
  @Grade(project = "Week02", topic = "Frequency", points = 1.0)
  public void correctFrequencyCountsForDreamGarage() {
    final var counts = manufacturerFrequencyCount(DreamGarage.getCars());
    final var expected =
        Map.of(
            Manufacturer.lookup("Ferrari"), 3,
            Manufacturer.lookup("Cadillac"), 1,
            Manufacturer.lookup("Chevrolet"), 1,
            Manufacturer.lookup("DeLorean"), 1,
            Manufacturer.lookup("Ford"), 1,
            Manufacturer.lookup("Lamborghini"), 1,
            Manufacturer.lookup("Nissan"), 1,
            Manufacturer.lookup("Porsche"), 2,
            Manufacturer.lookup("Toyota"), 1);

    assertEquals(expected, counts);
  }

  @Test
  @Grade(project = "Week02", topic = "Frequency", points = 2.0)
  public void ferrariMostPopularForDreamGarage() {
    assertEquals("Ferrari", mostPopularManufacturer(DreamGarage.getCars()).getName());
  }
}
