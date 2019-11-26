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

import static edu.rice.image.Images.readImageResource;
import static edu.rice.io.Files.readResourceDir;
import static edu.rice.prettypictures.Allele.twoChildRgb;
import static edu.rice.prettypictures.RgbColor.color;
import static java.lang.Math.floor;

import edu.rice.util.Log;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import java.awt.image.BufferedImage;

/**
 * This class includes all the necessary support to load external images from disk and create
 * Alleles from them. All of the image loading happens statically, the first time anything in this
 * class is referenced, and all the successes and failure of the image loading process will be
 * logged.
 *
 * <p>Users of this class will mostly be interested in {@link #EXTERNAL_IMAGE_ALLELE_MAP}, which
 * maps from filenames to {@link Allele} instances.
 */
public class ExternalImageAlleles {
  private static final String TAG = "ExternalImageAlleles";

  // never instantiate this class
  private ExternalImageAlleles() {}

  public static final List<String> POSSIBLE_EXTERNAL_IMAGE_FILENAMES =
      readResourceDir("cool-images")
          .map(
              list ->
                  list.filter(
                          string ->
                              string.endsWith(".jpeg")
                                  || string.endsWith(".jpg")
                                  || string.endsWith(".png")
                                  || string.endsWith(".gif"))
                      .toList())
          .getOrElse(List.of("ricelogo.gif")); // if all else fails, we'll at least get one image

  // We load every image, all at once, skipping over any with errors
  public static final Map<String, Allele> EXTERNAL_IMAGE_ALLELE_MAP =
      POSSIBLE_EXTERNAL_IMAGE_FILENAMES
          .map(
              name ->
                  Tuple.of(
                      name,
                      readImageResource(name)
                          .onFailure(err -> Log.e(TAG, "Failed to load " + name, err))
                          .toOption()))

          // here, we have a list of Tuple2<String, Option<byte[]>>; we want to remove the
          // failures
          .filter(kv -> kv._2.isDefined())
          .map(kv -> kv.apply((k, v) -> externalImage(k, v.get())))

          // here, we have a list of Allele, which we want to convert to a map from name to gene
          .toMap(Allele::getParam, imageGene -> imageGene);

  public static final List<String> VALID_EXTERNAL_IMAGE_FILENAMES =
      EXTERNAL_IMAGE_ALLELE_MAP.keySet().toList();

  static {
    // helpful to log this at startup so we know which images succeeded and failed
    POSSIBLE_EXTERNAL_IMAGE_FILENAMES.forEach(
        image ->
            Log.i(
                TAG,
                "found image: "
                    + image
                    + " valid("
                    + EXTERNAL_IMAGE_ALLELE_MAP.get(image).isDefined()
                    + ")"));
  }

  private static Allele externalImage(String name, BufferedImage img) {
    return twoChildRgb(
        "external-image",
        name,
        (c1, c2) ->
            color(
                fetchColor(img, c1.r, c2.r).r,
                fetchColor(img, c1.g, c2.g).g,
                fetchColor(img, c1.b, c2.b).b));
  }

  private static int clip(int x, int xmax) {
    return x < 0 ? 0 : Math.min(x, xmax);
  }

  private static double wrap(double x, double xmax) {
    var scaleDown = x / xmax;
    return (scaleDown - floor(scaleDown)) * xmax;
  }

  private static final boolean USE_INTERPOLATION = true;
  private static final boolean WRAP_IMAGE = true;
  private static final double EPSILON = 0.000001f;

  /**
   * Fetches the four pixels adjacent to the query pixel and linearly interpolates their colors.
   * Note that xp and yp are in our usual unit square coordinates ([-1,1], [-1,1]) and will be
   * linearly scaled to fit the original pixels of the {@link BufferedImage}.
   *
   * @return an {@link RgbColor} in the usual [-1,1] space.
   */
  private static RgbColor fetchColor(BufferedImage image, double xp, double yp) {
    var imageWidth = image.getWidth();
    var imageHeight = image.getHeight();

    var xscale = imageWidth * (xp + 1) / 2.0;
    var yscale =
        imageHeight * (1 - (yp + 1) / 2.0); // flip the y to match BufferedImage coordinates

    var xwrap = WRAP_IMAGE ? wrap(xscale, imageWidth) : xscale;
    var ywrap = WRAP_IMAGE ? wrap(yscale, imageHeight) : yscale;

    var xmin = (int) xwrap;
    var xmax = xmin + 1;
    var ymin = (int) ywrap;
    var ymax = ymin + 1;

    var xt = xwrap - xmin; // "time" from zero to one for linear interpolation
    var yt = ywrap - ymin;

    // if we've disabled interpolation, or if we're so close to an
    // integer pixel, then we'll just return the nearest integer pixel
    // and call it a day. Likewise, if we're somehow hanging off the
    // edge, we'll give up and fall back to the simpler solution.
    if (!USE_INTERPOLATION
        || (xt < EPSILON && yt < EPSILON)
        || xmin <= 0
        || ymin <= 0
        || xmax >= imageWidth
        || ymax >= imageHeight) {
      return color(image.getRGB(clip(xmin, imageWidth), clip(ymin, imageHeight)));
    }

    // We're going to split the color up into its constituent 8-bit
    // parts and operate on each channel separately.  This seems like
    // it might be inefficient, but it's also more likely to be
    // correct. Plus, it's highly likely that the Java optimizer will
    // be able to look at this and rearrange it to run faster.

    var rgbXMinYMin = image.getRGB(xmin, ymin);
    var rXMinYMin = (rgbXMinYMin & 0xff0000) >> 16;
    var gXMinYMin = (rgbXMinYMin & 0xff00) >> 8;
    var bXMinYMin = (rgbXMinYMin & 0xff);

    var rgbXMaxYMin = image.getRGB(xmax, ymin);
    var rXMaxYMin = (rgbXMaxYMin & 0xff0000) >> 16;
    var gXMaxYMin = (rgbXMaxYMin & 0xff00) >> 8;
    var bXMaxYMin = (rgbXMaxYMin & 0xff);

    var rgbXMinYMax = image.getRGB(xmin, ymax);
    var rXMinYMax = (rgbXMinYMax & 0xff0000) >> 16;
    var gXMinYMax = (rgbXMinYMax & 0xff00) >> 8;
    var bXMinYMax = (rgbXMinYMax & 0xff);

    var rgbXMaxYMax = image.getRGB(xmax, ymax);
    var rXMaxYMax = (rgbXMaxYMax & 0xff0000) >> 16;
    var gXMaxYMax = (rgbXMaxYMax & 0xff00) >> 8;
    var bXMaxYMax = (rgbXMaxYMax & 0xff);

    // Cool numerical wizardry: rather than using integer division or
    // floating point division, we're instead computing fractions
    // where the denominator is a power of two. This means we can do
    // an integer multiply for the numerator and a right-shift for the
    // denominator.

    var xfracMax = (int) (xt * 65536);
    var yfracMax = (int) (yt * 65536);

    var xfracMin = 65536 - xfracMax;
    var yfracMin = 65536 - yfracMax;

    // Cool performance wizardry: notice how each of these expressions
    // are pretty similar? Modern CPUs will do something called "out
    // of order execution", which means that the code below is likely
    // to end up running all at the same time. The only thing that's
    // really going to slow down this method is the part where we make
    // four queries to image.getRGB(). Everything else should be
    // screaming fast.

    var rVal =
        ((((rXMinYMin * xfracMin + rXMaxYMin * xfracMax) >> 16) * yfracMin
                    + (((rXMinYMax * xfracMin + rXMaxYMax * xfracMax) >> 16) * yfracMax))
                >> 16)
            & 0xff;
    var gVal =
        ((((gXMinYMin * xfracMin + gXMaxYMin * xfracMax) >> 16) * yfracMin
                    + (((gXMinYMax * xfracMin + gXMaxYMax * xfracMax) >> 16) * yfracMax))
                >> 16)
            & 0xff;
    var bVal =
        ((((bXMinYMin * xfracMin + bXMaxYMin * xfracMax) >> 16) * yfracMin
                    + (((bXMinYMax * xfracMin + bXMaxYMax * xfracMax) >> 16) * yfracMax))
                >> 16)
            & 0xff;

    return color((rVal << 16) | (gVal << 8) | bVal);
  }
}
