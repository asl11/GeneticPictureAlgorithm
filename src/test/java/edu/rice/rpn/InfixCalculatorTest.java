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

package edu.rice.rpn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@GradeTopic(project = "Week10", topic = "Infix", maxPoints = 3.0)
public class InfixCalculatorTest {
  @Test
  @Grade(project = "Week10", topic = "Infix", points = 1.0)
  public void operatorPrecedence() {
    InfixCalculator x = new InfixCalculator();
    assertEquals("23.0", x.calc("3 + 4 * 5"));
    assertEquals("17.0", x.calc("3 * 4 + 5"));
    assertEquals("27.0", x.calc("3 * (4 + 5)"));
    assertEquals("35.0", x.calc("(3 + 4) * 5"));
  }

  @Test
  @Grade(project = "Week10", topic = "Infix", points = 1.0)
  public void leftAssociativity() {
    InfixCalculator x = new InfixCalculator();
    assertEquals("-5.0", x.calc("2 - 3 - 4"));
    assertEquals("-5.0", x.calc("(2 - 3) - 4"));
    assertEquals("0.25", x.calc("2 / 4 / 2"));
    assertEquals("0.25", x.calc("(2 / 4) / 2"));
  }

  @Test
  @Grade(project = "Week10", topic = "Infix", points = 1.0)
  public void noWhitespaceHandledProperly() {
    InfixCalculator x = new InfixCalculator();
    assertEquals("-5.0", x.calc("2-3-4"));
    assertEquals("9.0", x.calc("2+3+4"));
    assertEquals("-5.0", x.calc("2+-3-4"));
  }

  @Test
  @Grade(project = "Week10", topic = "Infix", points = 1.0)
  public void equivalentExpressionsAreEqual() {
    InfixCalculator x = new InfixCalculator();
    assertEquals(x.parseTree("3"), x.parseTree("(3)"));
    assertEquals(x.parseTree("3 + (4 * 5)"), x.parseTree("3 + 4 * 5"));
    assertEquals(x.parseTree("(2 - 3) - 4"), x.parseTree("2 - 3 - 4"));
    assertEquals(x.parseTree("3").toString(), x.parseTree("(3)").toString());
    assertEquals(x.parseTree("3 + (4 * 5)").toString(), x.parseTree("3 + 4 * 5").toString());
    assertEquals(x.parseTree("(2 - 3) - 4").toString(), x.parseTree("2 - 3 - 4").toString());
    assertEquals(x.parseTree("3").hashCode(), x.parseTree("(3)").hashCode());
    assertEquals(x.parseTree("3 + (4 * 5)").hashCode(), x.parseTree("3 + 4 * 5").hashCode());
    assertEquals(x.parseTree("(2 - 3) - 4").hashCode(), x.parseTree("2 - 3 - 4").hashCode());
  }

  @Test
  @Grade(project = "Week10", topic = "Infix", points = 2.0)
  public void variableAssignment() {
    InfixCalculator x = new InfixCalculator();
    assertEquals("7.0", x.calc("3 + 4"));
    assertEquals("3.0", x.calc("x = 3"));
    assertEquals("3.0", x.calc("x"));
    assertEquals("6.0", x.calc("3.0 + x"));
    assertEquals("6.0", x.calc("x = 3.0 + x"));
    assertEquals("6.0", x.calc("x"));
    assertEquals("Undefined", x.calc("y"));
    assertEquals("Undefined", x.calc("3 + y"));
    assertEquals("Undefined", x.calc("z = 3 + y"));
    assertEquals("Undefined", x.calc("z"));
    assertEquals("6.0", x.calc("x"));
    assertEquals("Undefined", x.calc("y"));
    assertEquals("Undefined", x.calc("x = y"));
    assertEquals("Undefined", x.calc("x"));
  }

  @Test
  @Grade(project = "Week10", topic = "Infix", points = 1.0)
  public void handlingNaN() {
    InfixCalculator x = new InfixCalculator();
    assertEquals("3.0", x.calc("x = 3"));
    assertEquals("3.0", x.calc("x"));
    assertEquals("Undefined", x.calc("x = 3 / 0"));
    assertEquals("Undefined", x.calc("x"));
  }

  /** Test several malformed inputs, all at once. */
  @TestFactory
  public Seq<DynamicTest> badExpressionsFail() {
    InfixCalculator x = new InfixCalculator();
    Log.i("InfixCalculatorTest", "Testing bad inputs, expect logs of parse failures!");

    return List.of(
            "3 +",
            "3 + 4 *",
            "3 *",
            "3 * 4 + ( 2",
            "(2 - 3) - 4)",
            "- 3",
            "3 4 5",
            "3-4 5",
            "-3 4-5",
            "-3 4+-5",
            "2+3-+4",
            "2+-3-+4",
            "x =",
            "= x =",
            "= x 3",
            "3 = x",
            "x = 3 = x",
            "=")
        .map(badStr -> dynamicTest(badStr, () -> assertEquals("Parse failure!", x.calc(badStr))))
        .prepend(dynamicTest("empty string", () -> assertEquals("Parse failure!", x.calc(""))))
        .prepend(dynamicTest("whitespace", () -> assertEquals("Parse failure!", x.calc("  "))));
  }
}
