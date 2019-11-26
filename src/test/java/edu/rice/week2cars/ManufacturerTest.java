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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.util.Log;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week02", topic = "Counting", maxPoints = 2.0)
class ManufacturerTest {
  private static final String TAG = "ManufacturerTest";

  // We're including a bunch of different tests here in the "Counting" topic,
  // but all the ones on top should pass from the very beginning. The tests
  // count to make sure that nothing gets accidentally broken.

  @Test
  @Grade(project = "Week02", topic = "Counting", points = 1.0)
  public void registryBasics() {
    assertTrue(Manufacturer.exists("Chevrolet"));
    assertFalse(Manufacturer.exists("Saab"));

    // This is how you test that something fails and throws a particular type of exception.
    assertThrows(
        NoSuchElementException.class,
        () -> {
          var saab = Manufacturer.lookup("Saab");
        });

    var chevy = Manufacturer.lookup("Chevrolet");

    assertEquals("Chevrolet", chevy.getName());
    assertEquals("http://www.chevrolet.com", chevy.getHomepageUrl());

    // Some example code we use in class
    try {
      var bmw = Manufacturer.lookup("BMW");
      Log.i(TAG, "BMW exists!");
      Log.i(TAG, bmw);
    } catch (NoSuchElementException ex) {
      Log.e(TAG, "BMW is missing!");
    }

    try {
      var danCorp = Manufacturer.lookup("DanCorp");
      Log.i(TAG, "DanCorp exists!");
      Log.i(TAG, danCorp);
    } catch (NoSuchElementException ex) {
      Log.e(TAG, "DanCorp is missing!");
    }
  }

  @Test
  @Grade(project = "Week02", topic = "Counting", points = 1.0)
  public void allNamesAndAllManufacturersAreCorrectSize() {
    var allNames = Manufacturer.allNames();
    assertTrue(allNames.contains("Chevrolet"));
    assertFalse(allNames.contains("Saab"));
    assertEquals(48, allNames.size());

    var allManufacturers = Manufacturer.allManufacturers();
    assertEquals(48, allManufacturers.size());
  }

  @Test
  @Grade(project = "Week02", topic = "Counting", points = 1.0)
  public void differentManufacturersShouldNotBeEquals() {
    var bmw = Manufacturer.lookup("BMW");
    var chevy = Manufacturer.lookup("Chevrolet");
    var chevy2 = Manufacturer.lookup("Chev" + "rolet");

    assertEquals(chevy, chevy2);

    // not only are they dot-equals, they point to the exact same object instance
    assertSame(chevy, chevy2);

    assertNotEquals(bmw, chevy);
  }

  @Test
  @Grade(project = "Week02", topic = "Counting", points = 1.0)
  public void countManufacturersWithHttpUrls() {
    // Write code that counts the number of manufacturers that have
    // URLs beginning with "http" but not "https" and assert that
    // there are exactly NINE (9) of them.

    // You may want to look at all the different methods that are
    // available on Java Strings.
    // https://docs.oracle.com/javase/8/docs/api/java/lang/String.html

    var counter = 0;
    for (var m : Manufacturer.allManufacturers()) {
      if (m.getHomepageUrl().startsWith("http:")) {
        counter++;
      }
    }

    assertEquals(9, counter);
  }

  @Test
  @Grade(project = "Week02", topic = "Counting", points = 1.0)
  public void countManufacturersShortNames() {
    // Write code that counts the number of manufacturers that have
    // names of five characters or less and assert that there are
    // exactly NINETEEN (19) of them.

    var counter = 0;
    for (var name : Manufacturer.allNames()) {
      if (name.length() <= 5) {
        counter++;
      }
    }

    assertEquals(19, counter);
  }
}
