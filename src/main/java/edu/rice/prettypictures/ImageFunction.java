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

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

@FunctionalInterface
public interface ImageFunction {
  /**
   * We're defining an ImageFunction as something where we can loop over (x,y) in the range of
   * [-1,1] and render to {@link RgbColor}.
   */
  RgbColor render(double x, double y);

  /** Renders the given ImageFunction to a BufferedImage at the given integer pixel resolution. */
  default BufferedImage toImage(int xsize, int ysize) {
    // For increased parallelism, our code is going to fill up the following array of ints,
    // which we'll later copy to a BufferedImage. Detailed performance notes below.
    var output = new int[ysize][xsize];

    IntStream.rangeClosed(0, ysize - 1)
        .parallel()
        .forEach(
            y -> {
              var dy = 1.0 - 2.0 * y / (double) ysize;

              for (int x = 0; x < xsize; x++) {
                var dx = -1.0 + 2.0 * x / (double) xsize;

                output[y][x] = render(dx, dy).toRgb();
              }
            });

    // There's no point trying to do this loop in parallel; see the note below.
    var image = new BufferedImage(xsize, ysize, BufferedImage.TYPE_INT_ARGB);
    for (var y = 0; y < ysize; y++) {
      for (var x = 0; x < xsize; x++) {
        image.setRGB(x, y, output[y][x]);
      }
    }

    return image;

    // Engineering note: Check out these performance numbers! First
    // up, a dumb scalar version with nested for-loops and a call to
    // image.setRGB() on the inside. No streams. No parallelism. No
    // separate int-arrays. These were rendered on their own, without
    // anything else using CPU. (2013 Apple MacPro with 6 cores.)

    // (800x800), time: 230.989 ms (0.361 μs/pixel)
    // (2000x2000), time: 1720.302 ms (0.430 μs/pixel)

    // Next up, replacing that outer y-loop with
    // IntStream(...).parallel(), but still calling image.setRGB() on
    // the inside of the x-loop:

    // (800x800), time: 185.455 ms (0.290 μs/pixel)
    // (2000x2000), time: 985.557 ms (0.246 μs/pixel)

    // And lastly, the code as it appears above, using a completely
    // separate int[][] array for storage:

    // (800x800), time: 167.379 ms (0.262 μs/pixel)
    // (800x800), time: 60.064 ms (0.094 μs/pixel)
    // (2000x2000), time: 528.225 ms (0.132 μs/pixel)
    // (2000x2000), time: 279.710 ms (0.070 μs/pixel)

    // (For reference, this particular image is a variation of the
    // "color-perlin" image-function from the first week.)

    // What's going on here? We're running exactly one request at a
    // time, so we're not getting any parallelism from SparkJava. Just
    // one web request at a time. We see a speedup when we just look a
    // the 800x800 zooms, and we see a serious speedup when we look at
    // the 2000x2000 zooms (URL entered by hand in the browser). The
    // difference is the *size of each parallel task*, which is
    // proportional to the horizontal resolution of the image.

    // From the vanilla loop to the cheesy parallel version is
    // 1.7x. From the vanilla loop to the slightly smarter parallel
    // version is 3.3x. Also notice that the *second* 800x800 run is
    // even faster than the first.

    // Needless to say, there's a lot going on here. One source of
    // possible slowdown is BufferedImage.setRGB() is
    // "synchronized". That ensures that if multiple threads are
    // trying to call it at the same time, each one has to wait until
    // the next one is finished. That will limit concurrency. Fixing
    // that helped immensely. But why such a huge performance jump in
    // two successive runs of the same 800x800 image or two successive
    // runs of the same 2000x2000 image? This could be the result of
    // the just-in-time code optimizer. Or it could be something else!
    // But if you start from the slowest scalar 2000x2000 number and
    // look at the ratio of that to the fastest parallel 2000x2000
    // version, the difference is 6.15x. On a 6-core processor, that
    // suggests that squeezing more parallelism out won't be very
    // easy. We might yet squeeze more *performance*, but it would
    // come from improving something else, such as the time spent
    // inside rendering a single pixel.

    // If we *really* wanted to make this go faster, we'd find a way
    // to run it on a GPU, since computing pixels quickly is pretty
    // much the whole point of having a GPU, and high-end GPUs will
    // have thousands of cores which would do this kind parallel job
    // very well.
  }
}
