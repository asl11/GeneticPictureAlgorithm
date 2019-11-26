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

import static edu.rice.prettypictures.Allele.INTRINSIC_ALLELE_MAP;
import static edu.rice.prettypictures.Allele.constantNumber;

import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;

/** This class sets up a series of genes derived from the INTRINSIC_ALLELE_MAP. */
public class TestGenesWeek1 {
  private static final String TAG = "TestGenesWeek1";

  private final Seq<ImageFunction> genes; // initialized in constructor

  /** Given the name of the resource file, loads the requested genes. */
  public TestGenesWeek1() {
    genes =
        List.of(
            INTRINSIC_ALLELE_MAP.apply("black").assemble(), // 0
            INTRINSIC_ALLELE_MAP.apply("red").assemble(), // 1
            INTRINSIC_ALLELE_MAP.apply("green").assemble(), // 2
            INTRINSIC_ALLELE_MAP.apply("blue").assemble(), // 3
            INTRINSIC_ALLELE_MAP.apply("white").assemble(), // 4
            INTRINSIC_ALLELE_MAP.apply("x").assemble(), // 5
            INTRINSIC_ALLELE_MAP.apply("y").assemble(), // 6
            INTRINSIC_ALLELE_MAP
                .apply("negate")
                .assemble(INTRINSIC_ALLELE_MAP.apply("x").assemble()), // 7
            INTRINSIC_ALLELE_MAP
                .apply("negate")
                .assemble(INTRINSIC_ALLELE_MAP.apply("y").assemble()), // 8
            INTRINSIC_ALLELE_MAP.apply("xy0").assemble(), // 9
            INTRINSIC_ALLELE_MAP
                .apply("div")
                .assemble(
                    INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                    INTRINSIC_ALLELE_MAP.apply("y").assemble()), // 10
            INTRINSIC_ALLELE_MAP
                .apply("sine")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                            constantNumber(10).assemble())), // 11
            INTRINSIC_ALLELE_MAP
                .apply("sine")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(5).assemble(),
                            INTRINSIC_ALLELE_MAP
                                .apply("mul")
                                .assemble(
                                    INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                                    INTRINSIC_ALLELE_MAP.apply("y").assemble()))), // 12
            INTRINSIC_ALLELE_MAP
                .apply("sine")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("div")
                        .assemble(
                            INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                            INTRINSIC_ALLELE_MAP.apply("y").assemble())), // 13
            INTRINSIC_ALLELE_MAP
                .apply("sub")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("abs")
                        .assemble(
                            INTRINSIC_ALLELE_MAP
                                .apply("add")
                                .assemble(
                                    INTRINSIC_ALLELE_MAP
                                        .apply("sine")
                                        .assemble(
                                            INTRINSIC_ALLELE_MAP
                                                .apply("mul")
                                                .assemble(
                                                    INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                                                    constantNumber(5).assemble())),
                                    INTRINSIC_ALLELE_MAP
                                        .apply("cosine")
                                        .assemble(
                                            INTRINSIC_ALLELE_MAP
                                                .apply("mul")
                                                .assemble(
                                                    INTRINSIC_ALLELE_MAP.apply("y").assemble(),
                                                    constantNumber(8).assemble())))),
                    constantNumber(1).assemble()), // 14
            INTRINSIC_ALLELE_MAP
                .apply("sine")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("div")
                        .assemble(
                            constantNumber(1).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("x").assemble())), // 15
            INTRINSIC_ALLELE_MAP
                .apply("arctan2")
                .assemble(
                    INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                    INTRINSIC_ALLELE_MAP.apply("y").assemble()), // 16
            INTRINSIC_ALLELE_MAP
                .apply("floor")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("add")
                        .assemble(
                            INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                            INTRINSIC_ALLELE_MAP.apply("y").assemble())), // 17
            INTRINSIC_ALLELE_MAP
                .apply("wrap")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("add")
                        .assemble(
                            INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                            INTRINSIC_ALLELE_MAP.apply("y").assemble())), // 18
            INTRINSIC_ALLELE_MAP
                .apply("clip")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("add")
                        .assemble(
                            INTRINSIC_ALLELE_MAP.apply("x").assemble(),
                            INTRINSIC_ALLELE_MAP.apply("y").assemble())), // 19
            INTRINSIC_ALLELE_MAP
                .apply("grey-perlin")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(2.5).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("xy0").assemble())), // 20
            INTRINSIC_ALLELE_MAP
                .apply("color-perlin")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(2.5).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("xy0").assemble()),
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(2.5).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("yx0").assemble()),
                    constantNumber(3).assemble()), // 21
            INTRINSIC_ALLELE_MAP
                .apply("grey-perlin")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(8).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("xy0").assemble())), // 22
            INTRINSIC_ALLELE_MAP
                .apply("color-perlin")
                .assemble(
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(8).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("xy0").assemble()),
                    INTRINSIC_ALLELE_MAP
                        .apply("mul")
                        .assemble(
                            constantNumber(8).assemble(),
                            INTRINSIC_ALLELE_MAP.apply("yx0").assemble()),
                    constantNumber(3).assemble())); // 23

    Log.i(TAG, "Found " + genes.length() + " test genes");
  }

  /** Gets all of the test genes. */
  public Seq<ImageFunction> getGenes() {
    return genes;
  }

  /** Gets the number of test genes. */
  public int getNumGenes() {
    return genes.length();
  }

  /** Gets the nth test gene. */
  public ImageFunction getNthGene(int n) {
    return genes.get(n);
  }
}
