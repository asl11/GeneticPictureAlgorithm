package edu.rice.prettypictures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.vavr.collection.Seq;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class TestGenesWeek3Test {

  private static final String TAG = "TestGenesWeek2Test";

  @TestFactory
  Seq<DynamicTest> testMutatedAndBreededTrees() {
    var img1 = RandomGeneTree.randomTree(5);
    var img2 = RandomGeneTree.randomTree(5);
    Seq<GeneTree> mutatedTrees = new TestGenesWeek3(img1, img2, 30).getGenes();
    return mutatedTrees.map(
        gene ->
            dynamicTest(
                gene.toString(),
                // test that new mutated and breeded trees are rendered
                () -> {
                  BufferedImage result = gene.toImageFunction().toImage(150, 100);
                  assertEquals(150, result.getWidth());
                  assertEquals(100, result.getHeight());
                }));
  }

  @TestFactory
  Seq<DynamicTest> testZeroGenerationTrees() {
    Seq<GeneTree> newTrees = new TestGenesWeek3(30).getGenes();
    return newTrees.map(
        gene ->
            dynamicTest(
                gene.toString(),
                // test that new mutated and breeded trees are rendered
                () -> {
                  BufferedImage result = gene.toImageFunction().toImage(150, 100);
                  assertEquals(150, result.getWidth());
                  assertEquals(100, result.getHeight());
                }));
  }

  @TestFactory
  Seq<DynamicTest> testTest3TreesAreWorking() {
    var img1 = RandomGeneTree.randomTree(5);
    var img2 = RandomGeneTree.randomTree(5);
    Seq<GeneTree> newTrees = new TestGenesWeek3(img1, img2, true).getGenes();
    return newTrees.map(
        gene ->
            dynamicTest(
                gene.toString(),
                // test that new mutated and breeded trees are rendered
                () -> {
                  BufferedImage result = gene.toImageFunction().toImage(150, 100);
                  assertEquals(150, result.getWidth());
                  assertEquals(100, result.getHeight());
                }));
  }

  @TestFactory
  Seq<DynamicTest> testTest4TreesAreWorking() {
    var img1 = RandomGeneTree.randomTree(5);
    var img2 = RandomGeneTree.randomTree(5);
    Seq<GeneTree> newTrees = new TestGenesWeek3(img1, img2, false).getGenes();
    return newTrees.map(
        gene ->
            dynamicTest(
                gene.toString(),
                // test that new mutated and breeded trees are rendered
                () -> {
                  BufferedImage result = gene.toImageFunction().toImage(150, 100);
                  assertEquals(150, result.getWidth());
                  assertEquals(100, result.getHeight());
                }));
  }

}
