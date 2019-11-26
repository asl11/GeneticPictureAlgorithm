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
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.FAIL;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.NUMBER;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.PLUS;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.TIMES;
import static edu.rice.rpn.RpnCalculator.add;
import static edu.rice.rpn.RpnCalculator.drop;
import static edu.rice.rpn.RpnCalculator.dup;
import static edu.rice.rpn.RpnCalculator.numberPusher;
import static edu.rice.rpn.RpnCalculator.swap;
import static edu.rice.rpn.RpnGenerators.binaryCalcOps;
import static edu.rice.rpn.RpnGenerators.errorStack;
import static edu.rice.rpn.RpnGenerators.goodStacks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.Generate.oneOf;
import static org.quicktheories.generators.SourceDSL.integers;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.regex.Token;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

@GradeProject(name = "Week10", description = "RPN, Infix, More Treaps", warningPoints = 1.0)
@GradeTopic(project = "Week10", topic = "RPN Main")
@GradeTopic(project = "Week10", topic = "Thursday RPN")
public class RpnCalculatorTest {
  private static final String TAG = "RpnCalculatorTest";

  @Test
  public void testScan() {
    var scan1 = RpnCalculator.scan("1 2 + 3 *");
    var expectedResult1 =
        List.of(
            new Token<>(NUMBER, "1"),
            new Token<>(NUMBER, "2"),
            new Token<>(PLUS, "+"),
            new Token<>(NUMBER, "3"),
            new Token<>(TIMES, "*"));

    assertEquals(expectedResult1, scan1);

    var scan2 = RpnCalculator.scan("1 2 + 3 yuck");
    var expectedResult2 =
        List.of(
            new Token<>(NUMBER, "1"),
            new Token<>(NUMBER, "2"),
            new Token<>(PLUS, "+"),
            new Token<>(NUMBER, "3"),
            new Token<>(FAIL, ""));

    assertEquals(expectedResult2, scan2);
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Main", points = 0.5)
  public void testCalc() {
    var calculator = new RpnCalculator();
    assertEquals("9.0", calculator.calc("1 2 + 3 *"));
    assertEquals("81.0", calculator.calc("1 2 + 3 * dup *"));

    // those last two values should still be on the stack
    assertEquals("81.0", calculator.calc("="));
    assertEquals("9.0", calculator.calc("drop"));
    assertEquals("Empty stack", calculator.calc("drop"));
    assertEquals("Error!", calculator.calc("drop"));

    // let's test the non-commutative operators as well
    assertEquals("1.0", calculator.calc("3 2 -"));
    assertEquals("2.0", calculator.calc("4 2 /"));

    // make sure that division by zero fails correctly
    assertEquals("Error!", calculator.calc("4 0 /"));

    // and make sure swap works
    assertEquals("1.0", calculator.calc("2 3 swap -"));

    // and clear
    assertEquals("Empty stack", calculator.calc("clear"));

    // and errors shouldn't change anything on the stack
    assertEquals("2.0", calculator.calc("1 2"));
    assertEquals("Error!", calculator.calc("oops"));
    assertEquals("2.0", calculator.calc("="));

    // fun property: after an error, subsequent operations won't fix it; it will stay an error
    assertEquals("Error!", calculator.calc("clear 2 / 2 3 +"));

    // another fun property: clear, after an error, yields a clean stack and computation can
    // continue!
    assertEquals("5.0", calculator.calc("clear 2 / clear 2 3 +"));
  }

  @Test
  @Grade(project = "Week10", topic = "RPN Main", points = 0.5)
  public void testOperatorComposition() {
    // f(x) = (x + 10) * 27
    final var push27 = numberPusher(27);
    final var push10 = numberPusher(10);
    final CalcOp add = RpnCalculator::add;
    final CalcOp mult = RpnCalculator::multiply;

    final var f = push10.andThen(add).andThen(push27).andThen(mult);

    final var expectedResult = Option.some(List.of((3.0 + 10.0) * 27.0));

    // first, let's verify that our understanding of RpnCalculator.getOption() is correct
    assertEquals(expectedResult, RpnCalculator.of((3.0 + 10.0) * 27.0).getOption());

    // okay, now let's start applying our functions
    assertEquals(expectedResult, f.apply(RpnCalculator.of(3.0)).getOption());

    // here's the same thing written more concisely!
    final var f2 =
        numberPusher(10)
            .andThen(RpnCalculator::add)
            .andThen(numberPusher(27))
            .andThen(RpnCalculator::multiply);
    assertEquals(expectedResult, f2.apply(RpnCalculator.of(3.0)).getOption());

    // this time, let's show off how cool the logWrap method can be.
    final var f3 =
        push10
            .logWrap(TAG)
            .andThen(add.logWrap(TAG))
            .andThen(push27.logWrap(TAG))
            .andThen(mult.logWrap(TAG));
    assertEquals(expectedResult, f3.apply(RpnCalculator.of(3.0)).getOption());

    // lastly, let's push all the numbers first, then do the
    // operations, proving to ourselves that we understand the order
    // in which things are landing on the stack
    assertEquals(
        expectedResult, mult.apply(add.apply(RpnCalculator.of(3.0, 10.0, 27.0))).getOption());

    // another variant using operator composition
    assertEquals(
        expectedResult, add.andThen(mult).apply(RpnCalculator.of(3.0, 10.0, 27.0)).getOption());
  }

  /** Testing the add operations on stacks. We are using Quick Theories here. */
  @Test
  public void addTest() {
    // After an add, the top of the stack should be the result of
    // adding the top two element of the original stack
    qt().forAll(goodStacks(integers().between(2, 10)))
        .checkAssert(
            ostack ->
                assertEquals(
                    ostack.get().head() + ostack.get().tail().head(),
                    add(ostack).get().head().doubleValue()));

    // After an add, the stack should be identical, other than the top
    // two elements now being replaced by a single element
    qt().forAll(goodStacks(integers().between(2, 10)))
        .checkAssert(ostack -> assertEquals(ostack.get().tail().tail(), add(ostack).get().tail()));

    // If we have a stack of size 0 or 1, or an error stack, applying
    // an add to it should result in an error stack
    qt().forAll(oneOf(errorStack(), goodStacks(integers().between(0, 1))))
        .check(ostack -> add(ostack).isError());
  }

  /**
   * Test that all binary operators consume the top two entries in a valid stack of size >= 2, and
   * return a stack that is identical to the original one, except for the top element.
   */
  @Test
  @Grade(project = "Week10", topic = "Thursday RPN", points = 0.25)
  public void binaryOperatorsConsumeTwoEntriesReturnOneTest() {
    // TODO: "the more things change, the more they stay the same" -- write a test
    //   showing that any attempt to run a binary operator on a stack with at least
    //   two entries will yield a new stack, with the two top entries replaced by a
    //   new one, and everything else the same.

    qt().forAll(binaryCalcOps(), goodStacks(integers().from(2).upTo(10)))
        .check(
            (calcOp, ostack) ->
                calcOp.apply(ostack).map(Seq::tail).equals(ostack.map(x -> x.drop(2))));

    //    throw new RuntimeException("Not implemented yet!");
  }

  /**
   * Test that all binary operators return an error stack when given an error stack or a stack that
   * has less than two elements.
   */
  @Test
  @Grade(project = "Week10", topic = "Thursday RPN", points = 0.25)
  public void binaryOperatorsFailWithoutTwoEntriesTest() {
    // TODO: write a test showing that any attempt to run a binary operator on a stack with fewer
    //   than two entries on it will fail.

    qt().forAll(
            binaryCalcOps(),
            oneOf(goodStacks(integers().from(0).upToAndIncluding(1)), errorStack()))
        .check((calcOp, ostack) -> calcOp.apply(ostack).isError());

    //    throw new RuntimeException("Not implemented yet!");
  }

  /**
   * Test that doing dup on a valid stack of size 1 or more followed by a drop results in a stack
   * that is identical to the original.
   */
  @Test
  @Grade(project = "Week10", topic = "Thursday RPN", points = 0.25)
  public void dropUndoesDupTest() {
    // TODO: write a "there and back again" test that shows that dupping and
    //   then dropping yields the same stack.

    qt().forAll(goodStacks(integers().from(1).upTo(10)))
        .check(ostack -> drop(dup(ostack)).equals(ostack));

    //    throw new RuntimeException("Not implemented yet!");
  }

  /**
   * Test that doing swap twice on a valid stack of size 2 or more results in a stack that is
   * identical to the original stack.
   */
  @Test
  @Grade(project = "Week10", topic = "Thursday RPN", points = 0.25)
  public void swapUndoesSwapTest() {
    // TODO: write a "there and back again" test that shows that swapping twice
    //   yields the same stack.

    qt().forAll(goodStacks(integers().from(2).upTo(10)))
        .check(ostack -> swap(swap(ostack)).equals(ostack));

    //    throw new RuntimeException("Not implemented yet!");
  }
}
