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
import static edu.rice.prettypictures.Allele.constantRgb;
import static edu.rice.prettypictures.ExternalImageAlleles.EXTERNAL_IMAGE_ALLELE_MAP;
import static edu.rice.prettypictures.ExternalImageAlleles.VALID_EXTERNAL_IMAGE_FILENAMES;
import static edu.rice.prettypictures.GeneTree.constantColorTree;
import static edu.rice.prettypictures.GeneTree.constantNumberTree;
import static edu.rice.prettypictures.GeneTree.externalImageTree;
import static edu.rice.prettypictures.GeneTree.geneLeaf;
import static edu.rice.prettypictures.GeneTree.geneTree;
import static edu.rice.prettypictures.GeneTree.geneTreeOption;
import static io.vavr.control.Option.some;

import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.Random;

/**
 * This class implements all of the necessary support to create a random GeneTree of a specified
 * maximum depth. The various probabilities within have been tuned based on what seems to look
 * visually interesting, with a particularly high bias in factor of Perlin noise alleles.
 */
public class RandomGeneTree {
  private static final String TAG = "RandomGeneTree";
  private static final Random defaultRandom = new Random();

  private static final GeneTree X_LEAF = geneLeaf("x").get();
  private static final GeneTree Y_LEAF = geneLeaf("y").get();

  private static final Seq<GeneTree> EXTERNAL_IMAGE_GENE_LEAVES =
      VALID_EXTERNAL_IMAGE_FILENAMES.flatMap(
          imageName ->
              externalImageTree(imageName, X_LEAF, Y_LEAF)
                  .onEmpty(
                      () ->
                          Log.e(
                              TAG, () -> "couldn't build a leaf for external-image" + imageName)));
  private static final Seq<Allele> EXTERNAL_IMAGE_ALLELES =
      EXTERNAL_IMAGE_ALLELE_MAP.values().toList();
  private static final int NUM_EXTERNAL_IMAGES = EXTERNAL_IMAGE_GENE_LEAVES.length();

  private static final Seq<Allele> ZERO_ARG_ALLELES =
      INTRINSIC_ALLELE_MAP.values().filter(gene -> gene.numChildren() == 0).toList();
  private static final int NUM_ZERO_ARG_ALLELES = ZERO_ARG_ALLELES.length();

  private static final Seq<Allele> NON_ZERO_ARG_ALLELES =
      INTRINSIC_ALLELE_MAP.values().filter(gene -> gene.numChildren() > 0).toList();
  private static final int NUM_NON_ZERO_ARG_ALLELES = NON_ZERO_ARG_ALLELES.length();
  private static final Allele COLOR_PERLIN = INTRINSIC_ALLELE_MAP.get("color-perlin").get();
  private static final Allele GREY_PERLIN = INTRINSIC_ALLELE_MAP.get("grey-perlin").get();

  // never instantiate this class!
  private RandomGeneTree() {}

  /** Create a random GeneTree instance of the requested maximum depth. */
  public static GeneTree randomTree(int maxDepth) {
    return randomTreeOption(maxDepth)
        .getOrElseThrow(
            () ->
                new RuntimeException(
                    "unexpected randomTree generator failure; see logs for details"));
  }

  /**
   * Create a random GeneTree instance of the requested maximum depth. Not expected to fail,
   * although if it does, it will return {@link Option#none()}.
   */
  public static Option<GeneTree> randomTreeOption(int maxDepth) {
    return randomTreeOption(maxDepth, defaultRandom);
  }

  /**
   * Create a random GeneTree instance of the requested maximum depth. Not expected to fail,
   * although if it does, it will return {@link Option#none()}. This variant lets you specify your
   * own {@link Random} source, allowing for reproducible outcomes.
   */
  public static Option<GeneTree> randomTreeOption(int maxDepth, Random random) {
    // Engineering note: we've got Option<GeneTree> as the return type
    // throughout our GeneTree maker-methods, so it's much easier to
    // have our recursive function returning the same type. Since
    // we're working from internal data, rather than JSON or
    // something, we don't ever expect this to fail in practice.  Any
    // failure represents a bug in our program rather than invalid
    // input. This means that we've designed this to log failures, and
    // the public method, exposed above, hides all these
    // Option<GeneTree> parts.

    if (maxDepth <= 0) {
      // 10% probability we pick a 1-D constant grey color
      // 20% probability we pick a 3-D constant color
      // 10% chance we pick an external image (if too big, the results get ugly *and* slow)
      // 60% probability we pick one of our zero-arg leaves
      switch (random.nextInt(10)) {
        case 0:
          return constantNumberTree(random.nextDouble() * 2.0 - 1.0);

        case 1:
        case 2:
          return constantColorTree(
              RgbColor.color(
                  random.nextDouble() * 2.0 - 1.0,
                  random.nextDouble() * 2.0 - 1.0,
                  random.nextDouble() * 2.0 - 1.0));

        case 3:
          return some(EXTERNAL_IMAGE_GENE_LEAVES.get(random.nextInt(NUM_EXTERNAL_IMAGES)));

        default:
          return geneTree(ZERO_ARG_ALLELES.get(random.nextInt(NUM_ZERO_ARG_ALLELES)), List.empty());
      }
    } else {
      // 5% probability we pick an external image
      // 10% chance of grey perlin
      // 10% chance of color perlin (the Perlin functions are cool; we're biasing them up)
      // 75% probability we pick one of the other n-argument genes

      Allele nextNode;

      switch (random.nextInt(20)) {
        case 0:
          nextNode = EXTERNAL_IMAGE_ALLELES.get(random.nextInt(NUM_EXTERNAL_IMAGES));
          break;
        case 1:
        case 2:
          nextNode = COLOR_PERLIN;
          break;
        case 3:
        case 4:
          nextNode = GREY_PERLIN;
          break;
        default:
          nextNode = NON_ZERO_ARG_ALLELES.get(random.nextInt(NUM_NON_ZERO_ARG_ALLELES));
          break;
      }

      return geneTreeOption(
          nextNode,
          Stream.continually(() -> randomTreeOption(maxDepth - 1, random))
              .take(nextNode.numChildren()));
    }
  }

  /**
   * Fetches a random Allele of a desired number of children from the pool of available genes. Handy
   * for use when mutating trees.
   */
  public static Allele randomAllele(int numChildren) {
    return randomAllele(numChildren, defaultRandom);
  }

  /**
   * Fetches a random Allele of a desired number of children from the pool of available genes. Handy
   * for use when mutating trees. This variant lets you specify your own {@link Random} source,
   * allowing for reproducible outcomes.
   */
  public static Allele randomAllele(int numChildren, Random random) {
    if (numChildren == 0) {
      // 1/3 probability we pick a 1-D constant grey color
      // 1/3 probability we pick a 3-D constant color
      // 1/3 probability we pick one of our zero-arg leaves

      switch (random.nextInt(3)) {
        case 0:
          return constantNumber(random.nextDouble() * 2.0 - 1.0);

        case 1:
          return constantRgb(
              RgbColor.color(
                  random.nextDouble() * 2.0 - 1.0,
                  random.nextDouble() * 2.0 - 1.0,
                  random.nextDouble() * 2.0 - 1.0));

        default:
          return ZERO_ARG_ALLELES.get(random.nextInt(NUM_ZERO_ARG_ALLELES));
      }
    } else {
      if (numChildren == 2 && random.nextInt(3) == 0 && NUM_EXTERNAL_IMAGES > 0) {
        // an external image can work here, so we'll introduce one with 1/3 probability
        return EXTERNAL_IMAGE_ALLELES.get(random.nextInt(NUM_EXTERNAL_IMAGES));

      } else {
        var choices =
            INTRINSIC_ALLELE_MAP.values().filter(gene -> gene.numChildren() == numChildren);
        if (choices.isEmpty()) {
          Log.e(TAG, "we don't have any genes that accept " + numChildren + " children!");
          throw new IllegalArgumentException("no genes available");
        }
        return choices.get(random.nextInt(choices.length()));
      }
    }
  }
}
