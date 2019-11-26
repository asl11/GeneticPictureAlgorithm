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

import static edu.rice.io.Files.readResource;
import static edu.rice.json.Parser.parseJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.doubles;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.longs;

import edu.rice.util.Log;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.awt.image.BufferedImage;
import java.util.Random;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class TestGenesWeek2Test {
  private static final String TAG = "TestGenesWeek2Test";
  Seq<ImageFunction> tg1 = new TestGenesWeek1().getGenes();
  TestGenesWeek2 tg2 = new TestGenesWeek2("prettypictures-week2.json");

  @TestFactory
  Seq<DynamicTest> testRendering() {
    // This test might seem useless, but it verifies that we can
    // render each test gene.  If an exception is thrown anywhere in
    // there, the test will fail.

    return Stream.range(0, tg2.getNumGenes())
        .map(i -> tg2.getNthGene(i))
        .map(
            gene ->
                dynamicTest(
                    gene.toString(),
                    () -> {
                      BufferedImage result = gene.toImageFunction().toImage(150, 100);
                      assertEquals(150, result.getWidth());
                      assertEquals(100, result.getHeight());
                    }));
  }

  @TestFactory
  Seq<DynamicTest> testEquivalenceToWeek1Rendering() {
    // This test is a bit more vigorous than the one above. It does
    // pixel-by-pixel comparisons of what the parsed image does and
    // what the hand-build version in week1 does.

    return Stream.range(0, 18)
        .map(
            imgNum ->
                dynamicTest(
                    String.format("image %d", imgNum),
                    () -> {
                      var week1image = tg1.get(imgNum); // after that, they're no longer the same
                      var week2image = tg2.getNthGene(imgNum).toImageFunction();
                      qt().forAll(doubles().between(-1, 1), doubles().between(-1, 1))
                          .checkAssert(
                              (x, y) ->
                                  assertEquals(week1image.render(x, y), week2image.render(x, y)));
                    }));
  }

  @TestFactory
  Seq<DynamicTest> testJsonFileSuccesses() {
    // every entry in prettypictures-week2.json should succeed!

    final var possibleGenes =
        // Unlike the code in we use in production, where we want to keep going if we have
        // an error, here if there's an error this will simply throw an exception and the test
        // will fail. That's fine.
        parseJsonObject(readResource("prettypictures-week2.json").get())
            .get()
            .apply("testGenes")
            .asJArray()
            .getSeq();

    Log.i(TAG, "testJsonFileSuccesses: examining " + possibleGenes.length() + " good genes");

    return possibleGenes.map(
        jsonGene ->
            dynamicTest(
                jsonGene.toString(),
                // convert the gene-tree back to JSON, should be the same
                () -> assertEquals(jsonGene, GeneTree.of(jsonGene).get().toJson())));
  }

  @TestFactory
  Seq<DynamicTest> testJsonFileFailures() {
    // every entry in prettypictures-week2-fail.json should succeed!

    final var possibleGenes =
        parseJsonObject(readResource("prettypictures-week2-fail.json").get())
            .get()
            .apply("testGenes")
            .asJArray()
            .getSeq();

    Log.i(TAG, "testJsonFileFailures: examining " + possibleGenes.length() + " bad genes");

    return possibleGenes.map(
        jsonGene ->
            dynamicTest(jsonGene.toString(), () -> assertTrue(GeneTree.of(jsonGene).isEmpty())));
  }

  @TestFactory
  Seq<DynamicTest> testRandomTrees() {
    var geneTrees = TestGenesWeek2.randomTrees(10);
    return geneTrees.map(
        gene ->
            dynamicTest(
                gene.toString(),
                // rather than testing the rendering, which we're
                // confident of, we'll test that the maxDepth is
                // reasonable
                () -> assertTrue(gene.maxDepth() > 4)));
  }

  @Test
  public void jsonConsistencyTest() {
    // Engineering note: this exercises the JSON parser and the JSON
    // output using randomly generated JSON trees, using the random
    // tree generation that we already require for Pretty Pictures.
    // What's good is that it will generate a lot of trees. What's
    // unfortunate is that it doesn't use any of the "generator"
    // facilities built into QuickTheories. That would require
    // effectively reimplementing the RandomGeneTree builder, which we
    // don't really want to do. The hacky solution was to pass in a
    // random number generator, causing us to at least produce
    // deterministic tests.

    qt().forAll(longs().all(), integers().between(1, 5))
        .checkAssert(
            (seed, depth) -> {
              // shouldn't fail
              var tree = RandomGeneTree.randomTreeOption(depth, new Random(seed)).get();

              assertEquals(tree, GeneTree.of(tree.toJson()).get());
            });
  }
}
