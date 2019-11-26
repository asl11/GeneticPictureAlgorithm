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

import static edu.rice.prettypictures.RgbColor.color;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.doubles;

import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

class TestGenesWeek1Test {
  TestGenesWeek1 tg1 = new TestGenesWeek1();

  @Test
  public void testGeneLoading() {
    assertEquals(tg1.getGenes().length(), tg1.getNumGenes());
    assertTrue(tg1.getNumGenes() > 10);
  }

  @Test
  Seq<DynamicTest> testRendering() {
    // This test might seem useless, but it verifies that we can render each test gene.
    // If an exception is thrown anywhere in there, the test will fail.

    return Stream.range(0, tg1.getNumGenes())
        .map(
            i ->
                dynamicTest(
                    String.format("Test gene %d", i),
                    () -> {
                      BufferedImage result = tg1.getNthGene(i).toImage(150, 100);
                      assertEquals(150, result.getWidth());
                      assertEquals(100, result.getHeight());
                    }));
  }

  @Test
  public void testSimpleGenes() {
    var black = tg1.getNthGene(0);
    var red = tg1.getNthGene(1);
    var green = tg1.getNthGene(2);
    var blue = tg1.getNthGene(3);
    var white = tg1.getNthGene(4);
    var posx = tg1.getNthGene(5);
    var posy = tg1.getNthGene(6);
    var negx = tg1.getNthGene(7);
    var negy = tg1.getNthGene(8);

    qt().forAll(doubles().between(-100.0, 100.0), doubles().between(-100.0, 100.0))
        .checkAssert(
            (x, y) -> {
              assertEquals(color(-1, -1, -1), black.render(x, y));
              assertEquals(color(1, -1, -1), red.render(x, y));
              assertEquals(color(-1, 1, -1), green.render(x, y));
              assertEquals(color(-1, -1, 1), blue.render(x, y));
              assertEquals(color(1, 1, 1), white.render(x, y));
              assertEquals(color(x, x, x), posx.render(x, y));
              assertEquals(color(y, y, y), posy.render(x, y));
              assertEquals(color(-x, -x, -x), negx.render(x, y));
              assertEquals(color(-y, -y, -y), negy.render(x, y));
            });
  }
}
