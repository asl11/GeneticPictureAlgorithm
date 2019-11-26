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

import static edu.rice.qt.SequenceGenerators.sequences;
import static org.quicktheories.generators.Generate.constant;
import static org.quicktheories.generators.Generate.oneOf;
import static org.quicktheories.generators.SourceDSL.doubles;
import static org.quicktheories.generators.SourceDSL.integers;

import edu.rice.rpn.RpnCalculator.CalcOp;
import edu.rice.rpn.RpnCalculator.OStack;
import io.vavr.collection.Seq;
import org.quicktheories.core.Gen;

interface RpnGenerators {
  int DEFAULT_MAX_STACK_SIZE = 10;

  /**
   * Generates stacks which might be in the error state (i.e., {@link OStack#error()} or might be of
   * sizes ranging from 0 to {@link #DEFAULT_MAX_STACK_SIZE}.
   */
  static Gen<OStack> allStacks() {
    return oneOf(errorStack(), goodStacks());
  }

  /** Generates stacks of sizes ranging from 0 to {@link #DEFAULT_MAX_STACK_SIZE}. */
  static Gen<OStack> goodStacks() {
    return goodStacks(integers().between(0, DEFAULT_MAX_STACK_SIZE));
  }

  /**
   * Generates stacks of the given size of entries. Always guaranteed to be in a non-error state
   * (i.e., {@link OStack#success(Seq)}).
   */
  static Gen<OStack> goodStacks(Gen<Integer> sizes) {
    return sequences()
        .of(doubles().any().assuming(Double::isFinite))
        .ofSizes(sizes)
        .map(OStack::success);
  }

  /**
   * Generates stacks of the given size of entries. Always guaranteed to be in a non-error state
   * (i.e., {@link OStack#success(Seq)}).
   */
  static Gen<OStack> goodStacksWithoutZeros(Gen<Integer> sizes) {
    return sequences()
        .of(doubles().any().assuming(x -> x != 0.0 && Double.isFinite(x)))
        .ofSizes(sizes)
        .map(OStack::success);
  }

  /** Generates stacks in the error state (i.e., {@link OStack#error()}. */
  static Gen<OStack> errorStack() {
    return constant(OStack.error());
  }

  static Gen<CalcOp> calcOps() {
    return oneOf(nonSpecialCalcOps(), specialCalcOps());
  }

  /**
   * CalcOps that won't change the resulting good/error state, at least unless there are an
   * inadequate number of entries on the stack.
   */
  static Gen<CalcOp> nonSpecialCalcOps() {
    return oneOf(
        numberPushers(),
        constant(RpnCalculator::add),
        constant(RpnCalculator::subtract),
        constant(RpnCalculator::multiply),
        constant(RpnCalculator::drop),
        constant(RpnCalculator::dup),
        constant(RpnCalculator::swap),
        constant(RpnCalculator::noop));
  }

  /** CalcOps that can change the resulting good/error state. */
  static Gen<CalcOp> specialCalcOps() {
    return oneOf(
        constant(RpnCalculator::fail),
        constant(RpnCalculator::clear),
        constant(RpnCalculator::divide));
  }

  /** CalcOps that consume two entries on the stack and push one new result. */
  static Gen<CalcOp> binaryCalcOps() {
    return oneOf(
        constant(RpnCalculator::add),
        constant(RpnCalculator::subtract),
        constant(RpnCalculator::multiply),
        constant(RpnCalculator::divide));
  }

  /** CalcOps that push a number on the stack. */
  static Gen<CalcOp> numberPushers() {
    return doubles().any().map(RpnCalculator::numberPusher);
  }
}
