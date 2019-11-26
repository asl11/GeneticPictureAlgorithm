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

package edu.rice.image;

import static edu.rice.image.Images.imageToPng;
import static edu.rice.image.Images.readImageResource;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

public class ImagesTest {

  @Test
  public void riceLogoPixelsAreTheRightColors() {
    var tImage = readImageResource("rice-logo.png");
    assertTrue(tImage.isSuccess());
    var image = tImage.get();
    assertEquals(449, image.getWidth());
    assertEquals(276, image.getHeight());
    assertEquals(
        0xffffff,
        image.getRGB(0, 0) & 0xffffff); // masking out alpha; we just want to compare R,G,B
  }

  @Test
  public void readingAndWritingOrangeSquares() {
    var tImage = readImageResource("orange-square.png");
    assertTrue(tImage.isSuccess());
    var referenceOrangeSquare = tImage.get();
    assertEquals(200, referenceOrangeSquare.getWidth());
    assertEquals(200, referenceOrangeSquare.getHeight());

    // red=255, green=80,blue=0, also important to mask out the alpha
    assertEquals(0xff8000, referenceOrangeSquare.getRGB(0, 0) & 0xffffff);

    var orangeSquare = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    for (var y = 0; y < 200; y++) {
      for (var x = 0; x < 200; x++) {
        orangeSquare.setRGB(x, y, 0xff8000);
      }
    }

    var pngOrangeTest = imageToPng(orangeSquare);
    assertTrue(pngOrangeTest.isSuccess());
    var pngReferenceOrangeTest = imageToPng(referenceOrangeSquare);
    assertTrue(pngReferenceOrangeTest.isSuccess());

    assertArrayEquals(pngReferenceOrangeTest.get(), pngOrangeTest.get());
  }
}
