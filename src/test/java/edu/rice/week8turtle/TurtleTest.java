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

package edu.rice.week8turtle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Unit tests to exercise {@link Turtle}. */
public class TurtleTest {
  @Test
  public void testBasics() {
    var origin = new Turtle().penDown().setColor(0xffffff).moveTo(0, 0).setHeadingDegrees(0);

    // Engineering note: When you really don't want to have to force
    // your user to remember all the different possible arguments and
    // the order in which they go, this coding style is the
    // alternative. In some cases, developers create a whole separate
    // class, called a "builder" (e.g., "TurtleBuilder"). Android
    // programmers have to use builders for all kinds of services,
    // except their code looks a bit more like so:
    //
    //    Turtle origin = new TurtleBuilder()
    //        .penDown()
    //        .setColor(0xffffff)
    //        .moveTo(0,0)
    //        .setHeadingDegrees(0)
    //        .build();
    //
    // That's not really necessary for our Turtle example, but it's
    // very handy when you're building graphical user interfaces and
    // such, where you want the builders to be cheap, but once you
    // instantiate the button or slider or whatever, a lot of work
    // starts happening, things get drawn on the screen, and so forth.

    // let's verify that the above turtle parameters are, in fact, the
    // default parameters
    assertEquals(origin, new Turtle());

    // now, let's verify that we're not mutating the original when we
    // do various "setters"
    var originStr = origin.toString();
    var tweak = origin.penUp().setColor(0);
    assertEquals(originStr, origin.toString());

    // and let's verify that the parts of "tweak" that we didn't
    // change are the same
    assertEquals(origin.heading, tweak.heading, 0.0000001);
    assertEquals(origin.location, tweak.location);
    assertNotEquals(origin.penDown, tweak.penDown);
    assertNotEquals(origin.color, tweak.color);

    // okay, let's start doing turtle math!
    var right1 = origin.forward(1).leftDegrees(90);

    assertEquals(origin.location.x + 1.0, right1.location.x, 0.01);
    assertEquals(origin.location.y, right1.location.y, 0.01);
    assertEquals(Math.PI / 2, right1.heading, 0.01);

    var backHomeLeft =
        origin
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90)
            .forward(2)
            .leftDegrees(90);

    assertEquals(origin.location.x, backHomeLeft.location.x, 0.01);
    assertEquals(origin.location.y, backHomeLeft.location.y, 0.01);
    assertEquals(origin.heading, backHomeLeft.heading, 0.01);

    var backHomeRight =
        origin
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90)
            .forward(3)
            .rightDegrees(90);

    assertEquals(origin.location.x, backHomeRight.location.x, 0.01);
    assertEquals(origin.location.y, backHomeRight.location.y, 0.01);
    assertEquals(origin.heading, backHomeRight.heading, 0.01);

    // double check that our normalization really works
    assertEquals(origin.leftDegrees(30).heading, origin.rightDegrees(330).heading, 0.01);
  }
}
