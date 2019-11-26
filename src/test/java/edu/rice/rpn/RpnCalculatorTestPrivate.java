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

import static edu.rice.rpn.RpnCalculator.CalcOp;
import static edu.rice.rpn.RpnCalculator.clear;
import static edu.rice.rpn.RpnCalculator.dup;
import static edu.rice.rpn.RpnCalculator.noop;
import static edu.rice.rpn.RpnCalculator.numberPusher;
import static edu.rice.rpn.RpnCalculator.swap;
import static edu.rice.rpn.RpnGenerators.allStacks;
import static edu.rice.rpn.RpnGenerators.binaryCalcOps;
import static edu.rice.rpn.RpnGenerators.errorStack;
import static edu.rice.rpn.RpnGenerators.goodStacks;
import static edu.rice.rpn.RpnGenerators.goodStacksWithoutZeros;
import static edu.rice.rpn.RpnGenerators.nonSpecialCalcOps;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.Generate.constant;
import static org.quicktheories.generators.Generate.oneOf;
import static org.quicktheories.generators.SourceDSL.doubles;
import static org.quicktheories.generators.SourceDSL.integers;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week10", topic = "RPN Ops", maxPoints = 2.0)
public class RpnCalculatorTestPrivate {
  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void binaryOperatorsConsumeTwoEntriesReturnOne() {
    qt().forAll(binaryCalcOps(), goodStacksWithoutZeros(integers().between(2, 10)))
        .checkAssert(
            (calcOp, ostack) ->
                assertEquals(ostack.get().tail().tail(), calcOp.apply(ostack).get().tail()));
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void binaryOperatorsFailWithoutTwoEntries() {
    qt().forAll(
            binaryCalcOps(),
            oneOf(RpnGenerators.errorStack(), goodStacks(integers().between(0, 1))))
        .check((calcOp, ostack) -> calcOp.apply(ostack).isError());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void swapOnlyTouchesTopTwoEntries() {
    qt().forAll(goodStacks(integers().between(2, 10)))
        .checkAssert(
            ostack -> {
              assertEquals(ostack.get().tail().tail(), swap(ostack).get().tail().tail());
              assertNotEquals(ostack, swap(ostack)); // and it's not a no-op!
            });
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void swapFailsWithZeroOrOne() {
    qt().forAll(goodStacks(integers().between(0, 1))).check(ostack -> swap(ostack).isError());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void noOpDoesNothing() {
    qt().forAll(allStacks()).checkAssert(ostack -> assertEquals(ostack, noop(ostack)));
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void dupOnlyTouchesTopEntry() {
    qt().forAll(goodStacks(integers().between(1, 10)))
        .checkAssert(ostack -> assertEquals(ostack.get().tail(), dup(ostack).get().tail().tail()));
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void dupFailsWithZero() {
    qt().forAll(goodStacks(constant(0))).check(ostack -> dup(ostack).isError());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void dropUndoesDup() {
    var dupThenDrop = CalcOp.of(RpnCalculator::dup).andThen(RpnCalculator::drop);
    qt().forAll(goodStacks(integers().between(1, 10)))
        .checkAssert(ostack -> assertEquals(ostack, dupThenDrop.apply(ostack)));
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void swapUndoesSwap() {
    var swapTwice = CalcOp.of(RpnCalculator::swap).andThen(RpnCalculator::swap);
    qt().forAll(goodStacks(integers().between(2, 10)))
        .checkAssert(ostack -> assertEquals(ostack, swapTwice.apply(ostack)));
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void clearOnAnyStackReturnsEmptyValid() {
    qt().forAll(allStacks())
        // the .get will fail and thus the assertion will fail if the stack is in the error state
        .check(ostack -> clear(ostack).get().isEmpty());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void simpleOperationsShouldResultInGoodStack() {
    qt().forAll(nonSpecialCalcOps(), goodStacks(integers().between(2, 10)))
        .check((calcOp, ostack) -> calcOp.apply(ostack).isSuccess());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void anythingButClearOnErrorYieldsError() {
    qt().forAll(
            oneOf(
                nonSpecialCalcOps(),
                constant(RpnCalculator::fail),
                constant(RpnCalculator::divide)),
            errorStack())
        .check((calcOp, ostack) -> calcOp.apply(ostack).isError());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void divisionByZeroYieldsError() {
    var divByZero = CalcOp.of(numberPusher(0)).andThen(RpnCalculator::divide);
    qt().forAll(allStacks()).check(ostack -> divByZero.apply(ostack).isError());
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Ops", points = 0.25)
  public void divisionByNonZeroYieldsSuccess() {
    qt().forAll(goodStacks(integers().between(1, 10)), doubles().any().assuming(x -> x != 0))
        .check(
            (ostack, divisor) ->
                CalcOp.of(numberPusher(divisor))
                    .andThen(RpnCalculator::divide)
                    .apply(ostack)
                    .isSuccess());
  }
}
