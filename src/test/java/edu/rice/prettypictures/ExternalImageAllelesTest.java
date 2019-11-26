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
import static edu.rice.prettypictures.ExternalImageAlleles.EXTERNAL_IMAGE_ALLELE_MAP;
import static edu.rice.prettypictures.ExternalImageAlleles.POSSIBLE_EXTERNAL_IMAGE_FILENAMES;
import static edu.rice.prettypictures.ExternalImageAlleles.VALID_EXTERNAL_IMAGE_FILENAMES;
import static edu.rice.prettypictures.RgbColor.color;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.doubles;

import org.junit.jupiter.api.Test;

class ExternalImageAllelesTest {
  private static ImageFunction mexicanCoatOfArms() {
    var imageGene = EXTERNAL_IMAGE_ALLELE_MAP.apply("cool-images/mexican-coat-of-arms.png");
    var xgene = INTRINSIC_ALLELE_MAP.apply("x");
    var ygene = INTRINSIC_ALLELE_MAP.apply("y");
    return imageGene.assemble(xgene.assemble(), ygene.assemble());
  }

  @Test
  void testMexicanCoatOfArms() {
    assertFalse(VALID_EXTERNAL_IMAGE_FILENAMES.isEmpty());

    var image = mexicanCoatOfArms();

    // upper left corner is white
    assertEquals(color(1, 1, 1), image.render(-1, 1));

    // brown pixel near the head (via poking around the pixels manually)
    assertEquals(
        color((161 << 16) | (66 << 8) | 11),
        image.render(-1.0 + 2.0 * 414.0 / 1129.0, 1.0 - 2.0 * 154.0 / 1024.0));

    var outputImage = image.toImage(200, 200);

    // upper left corner is white
    assertEquals(0xffffff, 0xffffff & outputImage.getRGB(1, 1));

    // brown pixel near the head (via poking around the pixels manually)
    assertEquals(
        (161 << 16) | (66 << 8) | 11,
        0xffffff
            & outputImage.getRGB(
                (int) ((200.0 * 414.0) / 1129.0), (int) ((200.0 * 154.0) / 1024.0)));
  }

  @Test
  void allImageCoordinatesDefined() {
    var image = mexicanCoatOfArms();

    // assertNotNull() also implies the absence of any thrown exceptions
    qt().forAll(doubles().any(), doubles().any())
        .checkAssert((x, y) -> assertNotNull(image.render(x, y)));
  }

  @Test
  void allImagesLoadedSuccessfully() {
    // if any image fails to load, POSSIBLE_EXTERNAL_IMAGE_FILENAMES will be longer
    assertEquals(
        POSSIBLE_EXTERNAL_IMAGE_FILENAMES.length(), VALID_EXTERNAL_IMAGE_FILENAMES.length());
  }
}
