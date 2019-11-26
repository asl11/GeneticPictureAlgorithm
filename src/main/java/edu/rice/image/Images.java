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

import edu.rice.io.Files;
import edu.rice.util.Log;
import io.vavr.control.Try;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/** A front-end to make it easy to read and write image files. */
public class Images {
  private static final String TAG = "Images";

  private Images() {} // this class should never be instantiated

  /**
   * Given the name of a resource, typically a file in the "resources" directory, containing some
   * sort of image in any format that Java knows how to load (JPEG, PNG, whatever), this function
   * returns a standard BufferedImage object, suitable for further processing.
   *
   * @param resourceName path to the file
   * @return a Try.success of the image, or a Try.failure indicating what went wrong
   */
  public static Try<BufferedImage> readImageResource(String resourceName) {
    return Files.readResourceBytes(resourceName).flatMap(Images::readBytes);
  }

  /**
   * Given the location of a file on disk, containing some sort of image in any format that Java
   * knows how to load (JPEG, PNG, whatever), this function returns a standard BufferedImage object,
   * suitable for further processing.
   *
   * @param filePath path to the file
   * @return a Try.success of the image, or a Try.failure indicating what went wrong
   */
  public static Try<BufferedImage> readFile(String filePath) {
    return Files.readBytes(filePath).flatMap(Images::readBytes);
  }

  /**
   * Given an array of raw byte data, containing any sort of image that Java knows how to load
   * (JPEG, PNG, whatever), this function returns a standard BufferedImage object, suitable for
   * further processing.
   *
   * @param data raw byte array of input
   * @return a Try.success of the image, or a Try.failure indicating what went wrong
   */
  public static Try<BufferedImage> readBytes(byte[] data) {
    // Engineering notes: There are two possible failure modes
    // here. First, the data might not be in an understood image
    // format. ImageIO.read will simply return null. It's also
    // hypothetically possible that an IOException could happen,
    // because ImageIO.read wants an InputStream, which could be a
    // file or network. We've already dealt with IO errors before we
    // got here, but it's a "checked exception" so we're required to
    // have logic for it here, even though there's exactly zero chance
    // of it happening.

    // We don't want to use the internal null-handling supported by
    // tryOfNullable(), since the logged error would be
    // "NullPointerException", which gives zero useful feedback. Thus,
    // we're throwing our own exception, which will then be caught
    // along with the hypothetical IOException and handled.

    // Also, because ImageIO.read() really wants an "InputStream",
    // we're going to wrap our array of bytes with a
    // ByteArrayInputStream object to make it happy. You'll see we
    // similarly have to use a ByteArrayOutputStream to collect the
    // output of ImageIO.write(), below, and we simply extract the
    // array of bytes when it's done.

    // ImageIO has been around since Java4 (before they even added
    // generics!) and hasn't been updated. Unsurprisingly, it has
    // adhoc error handling and uses ancient IO primitives. Are there
    // alternatives? There's something called the Apache Commons
    // Imaging Library, which we used in prior years of
    // Comp215. Unfortunately, it doesn't support the full JPEG
    // standard, so a number of cool images we found online failed to
    // load correctly.  If we really wanted to add more file formats,
    // the best free library out there for Java seems to be
    // TwelveMonkeys: https://github.com/haraldk/TwelveMonkeys

    // The APIs used here wouldn't change. TwelveMonkeys plugs into
    // ImageIO and internally supports everything.

    return Try.of(
            () -> {
              var result = ImageIO.read(new ByteArrayInputStream(data));
              return (result == null)
                  ? Log.ethrow(
                      TAG, "image data isn't a recognized image format or is internally corrupted")
                  : result;
            })
        .onFailure(
            err ->
                Log.e(
                    TAG,
                    String.format(
                        "failed to read image from data (%d bytes): %s",
                        data.length, err.getMessage())));
  }

  /**
   * Given the path to write a file and a BufferedImage, generate a PNG representation of that image
   * and write it to the given file.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went
   *     wrong
   */
  public static Try<Void> writePngFile(String filePath, BufferedImage image) {
    return imageToPng(image).flatMap(imageBytes -> Files.writeBytes(filePath, imageBytes));
  }

  /**
   * Given a BufferedImage, convert it to PNG format.
   *
   * @return a Try.success of the raw PNG bytes, or a Try.failure indicating what went wrong
   */
  public static Try<byte[]> imageToPng(BufferedImage image) {
    return Try.of(
            () -> {
              var os = new ByteArrayOutputStream();
              var success = ImageIO.write(image, "png", os);
              if (!success) {
                throw new RuntimeException(
                    "ImageIO internal failure"); // useless feedback, but it's all we have
              } else {
                return os.toByteArray();
              }
            })
        .onFailure(err -> Log.e(TAG, "failed to convert image to bytes: " + err.getMessage()));
  }
}
