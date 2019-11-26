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

import static edu.rice.util.Helpers.also;
import static edu.rice.util.Helpers.curry;
import static edu.rice.util.Helpers.uncurry2;
import static edu.rice.util.Helpers.with;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class HelpersTest {
  @Test
  void curryBasics() {
    // Note that we could use var here to simplify many of the type
    // declarations, but it's useful to see them, to help understand
    // how curry() actually works.

    BiFunction<Integer, Integer, Integer> add = Integer::sum;
    Function<Integer, Function<Integer, Integer>> cadd = curry(add);
    Function<Integer, Integer> threePlus = cadd.apply(3);
    assertEquals(5, (int) threePlus.apply(2));
  }

  @Test
  void uncurryInvertsCurry() {
    BiFunction<Integer, Integer, Integer> add = Integer::sum;
    Function<Integer, Function<Integer, Integer>> cadd = curry(add);
    BiFunction<Integer, Integer, Integer> backToAdd = uncurry2(cadd);

    qt().forAll(integers().all(), integers().all())
        .checkAssert((a, b) -> assertEquals(add.apply(a, b), backToAdd.apply(a, b)));
  }

  @Test
  void withBasics() {
    assertEquals("HelloRice", with("Hello", hello -> hello + "Rice"));
  }

  @Test
  public void withTest() {
    String ho = "Ho";
    String result = with(ho + ho, hoho -> hoho + hoho);
    assertEquals("HoHoHoHo", result);
  }

  @Test
  public void alsoTest() {
    String result = also("Ho", ho -> ho = ho + ho, hoho -> hoho = hoho + hoho);
    assertEquals("Ho", result);
  }
}
