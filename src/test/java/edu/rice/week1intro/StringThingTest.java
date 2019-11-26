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

package edu.rice.week1intro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

/**
 * This verifies that you've got a working JUnit4 and Mockito library installed and you've fixed the
 * small bug we left in StringThing.
 */
@GradeTopic(project = "Week01", topic = "Lab")
public class StringThingTest {

  @Test
  @Grade(project = "Week01", topic = "Lab", points = 1.0)
  public void simpleTests() {
    StringThing.YOUR_STUDENT_RECORD.logMe();

    // This is a JUnit test; we'll explain this soon enough, but for now we just
    // want to make sure that you've got everything installed.

    var hollywoodStar =
        new StringThing("Harrison Ford", "1970", "hf7", "hanshotfirst@starwars.com");
    assertEquals("Harrison Ford", hollywoodStar.getName());
    assertEquals("1970", hollywoodStar.getClassYear());
    assertEquals("hf7", hollywoodStar.getNetID());
    assertEquals("hanshotfirst@starwars.com", hollywoodStar.getPreferredEmail());

    // The stuff below is building a "mock" version of StringThing and is defining
    // some behaviors for it. We'll eventually explain how mocks are super awesome
    // for testing. This shows how you might use it to test an "equals" method.

    // Mostly, this is just testing that your Java environment has the Mockito
    // library loaded. If it doesn't compile correctly, then we need to fix it.

    var mockThing = mock(StringThing.class);
    when(mockThing.getName()).thenReturn("Harrison Ford");
    when(mockThing.getNetID()).thenReturn("hf7");
    when(mockThing.getClassYear()).thenReturn("1970");
    when(mockThing.getPreferredEmail()).thenReturn("hanshotfirst@starwars.com");

    assertEquals(hollywoodStar, mockThing);
  }
}
