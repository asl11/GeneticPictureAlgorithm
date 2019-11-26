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

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class OptionTest {
  @Test
  public void someTest() {
    var stringOption = some("Hello");
    assertTrue(stringOption.isDefined());
    assertEquals("Hello", stringOption.get());
  }

  @Test
  public void ofNullableTest() {
    var stringOption = Option.of("Hello");
    Option<String> stringOption2 = Option.of(null);
    assertTrue(stringOption.isDefined());
    assertEquals("Hello", stringOption.get());
    assertFalse(stringOption2.isDefined());

    assertThrows(NoSuchElementException.class, stringOption2::get);
  }

  @Test
  public void noneTest() {
    Option<String> stringOption = none();
    assertFalse(stringOption.isDefined());

    assertThrows(NoSuchElementException.class, stringOption::get);
  }

  @Test
  public void fromOptionalTest() {
    // Note: java.util.Optional vs. io.vavr.control.Option
    var stringOption = Optional.of("Hello");
    var stringOption2 = Option.ofOptional(stringOption);

    //noinspection ConstantConditions
    assertTrue(stringOption.isPresent());
    assertTrue(stringOption2.isDefined());
    assertEquals("Hello", stringOption.get());
    assertEquals("Hello", stringOption2.get());
  }

  @Test
  public void toListTest() {
    assertEquals(List.of("Hello"), some("Hello").toList());
    assertEquals(List.empty(), none().toList());
  }

  @Test
  public void matchTest() {
    assertEquals("HelloWorld", some("Hello").fold(() -> "Nope", str -> str + "World"));
    assertEquals("Empty", none().fold(() -> "Empty", str -> "Fail"));
  }

  @Test
  public void getOrElseTest() {
    assertEquals("Correct", none().getOrElse("Correct"));
    assertEquals("Correct", some("Correct").getOrElse("Wrong"));
  }

  @Test
  public void orElseTest() {
    assertEquals(some("A"), some("A").orElse(some("B")));
    assertEquals(some("A"), some("A").orElse(none()));
    assertEquals(some("B"), Option.<String>none().orElse(some("B")));
    assertEquals(Option.<String>none(), Option.<String>none().orElse(none()));

    assertEquals(some("A"), some("A").orElse(() -> some("B")));
    assertEquals(
        some("A"),
        some("A")
            .orElse(
                () -> {
                  fail("This lambda should never be executed!");
                  return some("B");
                }));
  }

  @Test
  @SuppressWarnings("unused")
  public void orElseThrowTest() {
    RuntimeException re = new RuntimeException("test exception");

    assertEquals("Correct", some("Correct").getOrElseThrow(() -> re));

    RuntimeException expected =
        assertThrows(
            RuntimeException.class,
            () -> {
              String notUsed = Option.<String>none().getOrElseThrow(() -> re); // should throw re
            });
    assertEquals(re, expected);
  }

  @Test
  public void filterTest() {
    assertEquals(some("Hello"), some("Hello").filter(str -> str.contains("H")));
    assertEquals(none(), some("World").filter(str -> str.contains("H")));
  }

  @Test
  public void mapTest() {
    assertEquals(some("hello"), some("Hello").map(String::toLowerCase));
    assertEquals(Option.<String>none(), Option.<String>none().map(String::toLowerCase));
  }

  @Test
  public void flatMapTest() {
    Function<String, Option<String>> mapFunc = str -> some(str.toLowerCase());

    assertEquals(some("hello"), some("Hello").flatMap(mapFunc));
    assertEquals(Option.<String>none(), Option.<String>none().flatMap(mapFunc));
  }

  @Test
  public void testEquals() {
    assertEquals(some("Hello"), some("He" + "llo"));
  }

  @Test
  public void testHashCode() {
    assertEquals(some("Hello").hashCode(), some("He" + "llo").hashCode());
  }

  @Test
  public void testToString() {
    assertEquals("Some(Hello)", some("Hello").toString());
    assertEquals("None", none().toString());
  }
}
