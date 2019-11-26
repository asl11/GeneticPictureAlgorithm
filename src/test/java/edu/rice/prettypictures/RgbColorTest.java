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

package edu.rice.prettypictures;

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jnumber;
import static edu.rice.json.Builders.jstring;
import static edu.rice.json.Parser.parseJsonArray;
import static edu.rice.json.Parser.parseJsonValue;
import static edu.rice.prettypictures.RgbColor.color;
import static edu.rice.util.Strings.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quicktheories.core.Gen;

class RgbColorTest {
  @Test
  void testMakers() {
    assertEquals(color(0, 0, 0), color(0x808080));
    assertEquals(color(1, 1, 1), color(0xffffff));
    assertEquals(color(-1, -1, -1), color(0x000000));
    assertEquals(color(1, 0, -1), color(0xff8000));

    assertEquals(color(0, 0, 0).hashCode(), color(0x808080).hashCode());
    assertEquals(color(1, 1, 1).hashCode(), color(0xffffff).hashCode());
    assertEquals(color(-1, -1, -1).hashCode(), color(0x000000).hashCode());
    assertEquals(color(1, 0, -1).hashCode(), color(0xff8000).hashCode());
  }

  @Test
  void testFromJson() {
    assertEquals(color(-1, 0, 1), color(parseJsonArray("[-1, 0, 1]").get()).get());
  }

  @Test
  void testToString() {
    assertEquals("RgbColor(-1.000, 0.000, 1.000)", color(-1, 0, 1).toString());
  }

  @Test
  void testToRgb() {
    assertEquals(0xffff8000, color(1, 0, -1).toRgb());
  }

  Gen<RgbColor> standardColors() {
    return integers().between(0, 0xffffff).map(x -> RgbColor.color(x));
  }

  Gen<String> hexColorStrings() {
    return integers().between(0, 0xffffff).map(x -> String.format("%06x", x));
  }

  @Test
  void consistentColorHexDigits() {
    qt().forAll(standardColors())
        .checkAssert(color -> assertEquals(color, RgbColor.color(color.toHexColor()).get()));

    qt().forAll(hexColorStrings())
        .checkAssert(hex -> assertEquals(hex, RgbColor.color(hex).get().toHexColor()));
  }

  @Test
  void consistentColorPackedInt() {
    qt().forAll(standardColors())
        .checkAssert(color -> assertEquals(color, RgbColor.color(color.toRgb())));
  }

  @Test
  void consistentColorListDouble() {
    qt().forAll(standardColors())
        .checkAssert(color -> assertEquals(color, RgbColor.color(color.toList()).get()));
  }

  @Test
  void consistentColorListJson() {
    // RgbColor has two different ways of parsing JSON colors, so
    // we'll test both. It doesn't actually have JSON output, although
    // that would be straightforward enough to add if you needed it.

    qt().forAll(standardColors())
        .checkAssert(
            color ->
                assertEquals(
                    color,
                    RgbColor.color(jarray(jnumber(color.r), jnumber(color.g), jnumber(color.b)))
                        .get()));

    qt().forAll(standardColors())
        .checkAssert(
            color -> assertEquals(color, RgbColor.color(jstring(color.toHexColor())).get()));
  }

  @TestFactory
  Seq<DynamicTest> testJsonFailures() {
    // Lots of ways that the color builders should fail on bad input
    return List.of(
            json("[]"),
            json("[1, 2]"),
            json("[1, 2, 3, 4]"),
            json("[1, 2, true]"),
            json("{}"),
            json("{\"r\": 0, \"g\": 0, \"b\": 0}"),
            json("123456"),
            json("true"),
            json("null"))
        .map(
            x ->
                DynamicTest.dynamicTest(
                    x, () -> assertTrue(RgbColor.color(parseJsonValue(x).get()).isEmpty())));
  }

  @Test
  void testOtherFailures() {
    assertTrue(RgbColor.color(List.of(1.0, 2.0)).isEmpty()); // must be three
    assertTrue(RgbColor.color(List.of(1.0, 2.0, 3.0, 4.0)).isEmpty()); // must be three
  }
}
