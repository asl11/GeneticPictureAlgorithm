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

package edu.rice.dynamic;

import static edu.rice.dynamic.MemoizedFunction.yCached;
import static edu.rice.dynamic.MemoizedFunction.yUncached;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;

public class MemoizedFunctionTest {
  private final Function<Integer, Integer> incrementer = x -> x + 1;

  @Test
  public void memoizedIncrementerWorks() {
    final var memoizedIncrementer = MemoizedFunction.of(incrementer);
    assertEquals((Integer) 2, memoizedIncrementer.apply(1));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
  }

  @Test
  public void memoizedLambdaCalledAtMostOnce() {
    // Engineering note: since we're testing a memoization system, we
    // want to verify that the function we're memoizing is only ever
    // called once with any given argument. We're using a library
    // called Mockito that can *wrap* our existing objects and
    // functions and then let us make assertions about their
    // behavior. We'll explain Mockito more in week12 of the semester.
    //
    // For now, the things to notice are that we're making a "mock
    // function" which delegates its requests to a "real" function,
    // but then gives us a bunch of new assertions we can make about
    // how it's been used, how many times it's been called with any
    // given arguments, etc. We're "spying" on it, which as you might
    // imagine, runs slowly for all the bookkeeping, so you'd never
    // want to do this "in production", but it's perfect for testing.

    @SuppressWarnings("unchecked")
    final Function<Integer, Integer> spyIncrementer =
        mock(Function.class, AdditionalAnswers.delegatesTo(incrementer));
    final var memoizedIncrementer = MemoizedFunction.of(spyIncrementer);

    verify(spyIncrementer, never()).apply(1);
    verify(spyIncrementer, never()).apply(2);
    verify(spyIncrementer, never()).apply(3);
    verify(spyIncrementer, never()).apply(4);

    assertEquals((Integer) 2, memoizedIncrementer.apply(1));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));

    verify(spyIncrementer, atMost(1)).apply(1);
    verify(spyIncrementer, never()).apply(2);
    verify(spyIncrementer, never()).apply(3);
    verify(spyIncrementer, atMost(1)).apply(4);
  }

  private final BiFunction<Function<Long, Long>, Long, Long> fibonacci =
      (self, n) -> {
        // 1 1 2 3 5 8 13 ...
        if (n < 2) {
          return 1L;
        } else {
          return self.apply(n - 1) + self.apply(n - 2);
        }
      };

  @Test
  public void memoFibonacciReturnsExpectedResults() {
    final var memoFibonacci = MemoizedFunction.ofRecursive(fibonacci);

    assertEquals((Long) 13L, memoFibonacci.apply(6L));
  }

  @Test
  public void internalFibonacciOnlyCalledOnce() {
    // See engineering note above for discussion on the weird mock call here.

    @SuppressWarnings("unchecked")
    final BiFunction<Function<Long, Long>, Long, Long> spyFibonacci =
        mock(BiFunction.class, AdditionalAnswers.delegatesTo(fibonacci));

    final var memoFibonacci = MemoizedFunction.ofRecursive(spyFibonacci);

    verify(spyFibonacci, never()).apply(any(), any());

    assertEquals((Long) 13L, memoFibonacci.apply(6L));

    verify(spyFibonacci, atMost(1)).apply(any(), eq(0L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(1L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(2L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(3L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(4L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(5L));
  }

  // See "advanced" engineering notes at the bottom of MemoizedFunction to
  // understand the difference between MemoizedFunction's yCached(),
  // yUncached(), and ofRecursive() methods.

  @Test
  public void yCombinatorFibonacciCaching() {
    @SuppressWarnings("unchecked")
    final BiFunction<Function<Long, Long>, Long, Long> spyFibonacci =
        mock(BiFunction.class, AdditionalAnswers.delegatesTo(fibonacci));

    final var memoFibonacci = yCached(spyFibonacci);

    verify(spyFibonacci, never()).apply(any(), any());

    assertEquals((Long) 13L, memoFibonacci.apply(6L));

    verify(spyFibonacci, atMost(1)).apply(any(), eq(0L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(1L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(2L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(3L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(4L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq(5L));
  }

  @Test
  public void yCombinatorFibonacciUncached() {
    final var yFib = yUncached(fibonacci);
    assertEquals((Long) 13L, yFib.apply(6L));
  }
}
