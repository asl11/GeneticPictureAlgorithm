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

package edu.rice.week1letters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

@GradeProject(
    name = "Week01",
    description = "Letters / Introduction to Comp215",
    warningPoints = 1.0)
@GradeTopic(project = "Week01", topic = "Project")
public class LettersTest {
  @Grade(project = "Week01", topic = "Project", points = 3.0)
  @Test
  public void testGuessLetter() {
    var letters = new Letters("mutation");
    assertEquals("mutation", letters.getWordToGuess());
    assertEquals(1, letters.guessLetter('a'));
    assertEquals(2, letters.guessLetter('t'));
    assertEquals(10, letters.guessesLeft());
    assertEquals(0, letters.guessLetter('s'));
    assertEquals(9, letters.guessesLeft());
  }

  @Grade(project = "Week01", topic = "Project", points = 3.0)
  @Test
  public void testAddLetter() {
    var letters = new Letters("mutation");
    assertEquals("########", letters.getCurrentGuess());
    letters.addLetter('a');
    assertEquals("###a####", letters.getCurrentGuess());
    letters.addLetter('t');
    assertEquals("##tat###", letters.getCurrentGuess());
  }

  @Grade(project = "Week01", topic = "Project", points = 3.0)
  @Test
  public void testLetterPresent() {
    var letters = new Letters("mutation");
    assertEquals(1, letters.guessLetter('a'));
    assertEquals(2, letters.guessLetter('t'));

    // Correctly guessing the letters should not by itself change the current guess
    assertEquals(1, letters.guessLetter('a'));
    assertEquals(2, letters.guessLetter('t'));

    // Adding the letters to the solution should change the current guess
    letters.addLetter('a');
    assertEquals("###a####", letters.getCurrentGuess());
    letters.addLetter('t');
    assertEquals("##tat###", letters.getCurrentGuess());

    // Testing the 'already there' logic
    assertEquals(-1, letters.guessLetter('a'));
    assertEquals(-1, letters.guessLetter('t'));
  }
}
