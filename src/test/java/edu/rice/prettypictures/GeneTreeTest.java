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
import static edu.rice.prettypictures.Allele.INTRINSIC_ALLELE_MAP;
import static edu.rice.prettypictures.GeneTree.constantColorTree;
import static edu.rice.prettypictures.GeneTree.constantNumberTree;
import static edu.rice.prettypictures.GeneTree.externalImageTree;
import static edu.rice.prettypictures.GeneTree.geneLeaf;
import static edu.rice.prettypictures.GeneTree.geneTree;
import static edu.rice.prettypictures.RgbColor.color;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class GeneTreeTest {
  @Test
  void testBasics() {
    final var negate = INTRINSIC_ALLELE_MAP.apply("negate");
    final var x = INTRINSIC_ALLELE_MAP.apply("x");
    final var y = INTRINSIC_ALLELE_MAP.apply("y");
    final var div = INTRINSIC_ALLELE_MAP.apply("div");

    // -(x div y) (here, using the cleaner varargs variant)
    var assembledDirectly = negate.assemble(div.assemble(x.assemble(), y.assemble()));

    var xDivY = geneTree("negate", geneTree("div", geneLeaf("x"), geneLeaf("y"))).get();

    var viaTree = xDivY.toImageFunction();

    List.of(Tuple.of(1, 2), Tuple.of(-2, 1), Tuple.of(-2, 0), Tuple.of(5, 5))
        .forEach(
            pair ->
                assertEquals(
                    assembledDirectly.render(pair._1, pair._2),
                    viaTree.render(pair._1, pair._2),
                    pair.toString()));
  }

  @Test
  void testMaxDepth() {
    var xDivY = geneTree("negate", geneTree("div", geneLeaf("x"), geneLeaf("y"))).get();

    assertEquals(3, xDivY.maxDepth());
  }

  @Test
  void testNumNodes() {
    var xDivY = geneTree("negate", geneTree("div", geneLeaf("x"), geneLeaf("y"))).get();

    assertEquals(4, xDivY.numNodes());
  }

  @TestFactory
  Seq<DynamicTest> testExpectedTreeConstructors() {
    return List.of(
            Tuple.of("geneLeaf(x)", geneLeaf("x")),
            Tuple.of("geneLeaf(y)", geneLeaf("y")),
            Tuple.of("constantColorTree(1,1,1)", constantColorTree(color(1, 1, 1))),
            Tuple.of("div(x, y)", geneTree("div", geneLeaf("x"), geneLeaf("y"))),
            Tuple.of(
                "add(div(x, y), image(...))",
                geneTree(
                    "add",
                    geneTree("div", geneLeaf("x"), geneLeaf("y")),
                    externalImageTree(
                        "cool-images/mexican-coat-of-arms.png",
                        constantColorTree(color("000000").get()),
                        constantNumberTree(1.0)))))
        .map(ot -> dynamicTest(ot._1, ot._2::isDefined));
  }

  @TestFactory
  Seq<DynamicTest> testJson() {
    var testTrees =
        List.of(
                geneLeaf("x"),
                geneLeaf("y"),
                constantColorTree(color(1, 1, 1)),
                geneTree("div", geneLeaf("x"), geneLeaf("y")),
                geneTree(
                    "add",
                    geneTree("div", geneLeaf("x"), geneLeaf("y")),
                    externalImageTree(
                        "cool-images/mexican-coat-of-arms.png",
                        constantColorTree(color("000000").get()),
                        constantNumberTree(1.0))))
            .map(Option::get);

    return testTrees.map(
        tree ->
            dynamicTest(
                tree.toString(),
                () -> {
                  var json = tree.toJson();
                  var reconstructed = GeneTree.of(json).get();

                  // JSON should be the same
                  assertEquals(json, reconstructed.toJson());

                  // trees should be equal as well
                  assertEquals(tree, reconstructed);
                }));
  }

  @TestFactory
  Seq<DynamicTest> testEqualsAndHash() {
    // same input as testJsonFileSuccesses, except here we *assume* it's working and then use
    // it to beat up on equals and hashCode

    final var possibleGenes =
        // Unlike the code in we use in production, where we want to keep going if we have
        // an error, here if there's an error this will simply throw an exception and the test
        // will fail. That's fine.
        parseJsonObject(readResource("prettypictures-week2.json").get())
            .get()
            .apply("testGenes")
            .asJArray()
            .getSeq()
            .map(jsonGene -> GeneTree.of(jsonGene).get());

    return possibleGenes.map(
        gene ->
            dynamicTest(
                gene.toString(),
                () -> {
                  assertEquals(gene, GeneTree.of(gene.toJson()).get());
                  assertEquals(gene.hashCode(), GeneTree.of(gene.toJson()).get().hashCode());
                }));
  }
}
