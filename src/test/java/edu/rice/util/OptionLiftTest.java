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

package edu.rice.util;

import static edu.rice.vavr.Options.optionLift;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vavr.control.Option;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class OptionLiftTest {
  static int negate(int x) {
    return -x;
  }

  static int add(int x, int y) {
    return x + y;
  }

  static int add3(int x, int y, int z) {
    return x + y + z;
  }

  static final Function<Option<Integer>, Option<Integer>> onegate =
      optionLift(OptionLiftTest::negate);
  static final BiFunction<Option<Integer>, Option<Integer>, Option<Integer>> oadd =
      optionLift(OptionLiftTest::add);
  static final TriFunction<Option<Integer>, Option<Integer>, Option<Integer>, Option<Integer>>
      oadd3 = optionLift(OptionLiftTest::add3);

  static final Option<Integer> some0 = some(0);
  static final Option<Integer> some1 = some(1);
  static final Option<Integer> some2 = some(2);
  static final Option<Integer> some3 = some(3);
  static final Option<Integer> someNeg1 = some(-1);

  @Test
  public void liftWithSomeReturnsSome() {
    assertEquals(someNeg1, onegate.apply(some1));
    assertEquals(some3, oadd.apply(some1, some2));
    assertEquals(some3, oadd3.apply(some0, some1, some2));
  }

  @Test
  public void liftWithNoneReturnsNone() {
    assertEquals(none(), onegate.apply(none()));
    assertEquals(none(), oadd.apply(some1, none()));
    assertEquals(none(), oadd3.apply(some0, none(), some2));
  }
}
