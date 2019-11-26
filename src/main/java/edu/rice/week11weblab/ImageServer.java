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

package edu.rice.week11weblab;

import static edu.rice.image.Images.imageToPng;
import static edu.rice.image.Images.readImageResource;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static edu.rice.util.Strings.stringToOptionInteger;
import static edu.rice.util.Strings.stringToUTF8;
import static edu.rice.web.Utils.jsonSparkExceptionHandler;
import static edu.rice.web.Utils.launchBrowser;
import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

import edu.rice.util.Log;
import java.awt.image.BufferedImage;

/**
 * Week 11 lab, to make you learn a bit about image processing and Spark's version of structured
 * pattern matching.
 */
public class ImageServer {
  private static final String TAG = "ImageServer";

  /** Main entry function; arguments are ignored. */
  public static void main(String[] args) {
    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/week11images.html");

    get(
        "/",
        (request, response) -> {
          response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
          return "Next page!";
        });

    get(
        "/color-rectangle/imgsize/:width/:height/color/:rgb",
        (request, response) -> {
          //      logSparkRequest(TAG, request);

          // You may optionally want to turn on the logSparkRequest
          // call, commented out above, to see the full input we're
          // getting from the client web page.

          // Engineering note: the Spark web server guarantees us that
          // we'll have width, height, and color parameters in the
          // URL, otherwise we would never get here. However, it makes
          // no guarantees that those numbers are well-formed
          // integers, greater than zero, etc. If a negative number
          // shows up, Spark won't care, but we need to properly
          // handle the error. We're using stringToOptionInteger to do
          // make the attempt, but it might have some() or
          // none() within. We don't especially care *why* it might
          // have failed, so in those cases we'll just reach for
          // tolerable defaults via getOrElse().

          final var widthParam = stringToOptionInteger(request.params().get(":width")).getOrElse(1);
          final var heightParam =
              stringToOptionInteger(request.params().get(":height")).getOrElse(1);
          final var rgb = stringToOptionInteger(request.params().get(":rgb"), 16).getOrElse(0);

          final var width = (widthParam < 1) ? 1 : widthParam;
          final var height = (heightParam < 1) ? 1 : heightParam;

          return nanoBenchmarkVal(
                  () -> {
                    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    for (var y = 0; y < height; y++) {
                      for (var x = 0; x < width; x++) {
                        image.setRGB(x, y, rgb);
                      }
                    }
                    return imageToPng(image).getOrElse(stringToUTF8("Internal failure"));
                  })
              .apply(
                  (time, result) -> {
                    Log.iformat(
                        TAG,
                        "color-rectangle: made %dx%d image in %.3fms",
                        width,
                        height,
                        1e-6 * time);
                    response.type("image/png");
                    return result;
                  });
        });

    get(
        "/checkers/imgsize/:width/:height/boxsize/:boxsize/color1/:rgb1/color2/:rgb2",
        (request, response) -> {
          //      logSparkRequest(TAG, request);

          // You may optionally want to turn on the logSparkRequest
          // call, commented out above, to see the full input we're
          // getting from the client web page.

          final var widthParam = stringToOptionInteger(request.params().get(":width")).getOrElse(1);
          final var heightParam =
              stringToOptionInteger(request.params().get(":height")).getOrElse(1);
          final var boxsizeParam =
              stringToOptionInteger(request.params().get(":boxsize")).getOrElse(1);
          final var rgb1 = stringToOptionInteger(request.params().get(":rgb1"), 16).getOrElse(0);
          final var rgb2 = stringToOptionInteger(request.params().get(":rgb2"), 16).getOrElse(0);

          final var width = (widthParam < 1) ? 1 : widthParam;
          final var height = (heightParam < 1) ? 1 : heightParam;
          final var boxsize = (boxsizeParam < 1) ? 1 : boxsizeParam;

          return nanoBenchmarkVal(
                  () -> {
                    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    for (var y = 0; y < height; y++) {
                      for (var x = 0; x < width; x++) {
                        var checkCounter = x / boxsize + y / boxsize;
                        image.setRGB(x, y, (checkCounter % 2 == 0) ? rgb1 : rgb2);
                      }
                    }
                    return imageToPng(image).getOrElse(stringToUTF8("Internal failure"));
                  })
              .apply(
                  (time, result) -> {
                    Log.iformat(
                        TAG, "checkers: made %dx%d image in %.3fms", width, height, 1e-6 * time);
                    response.type("image/png");
                    return result;
                  });
        });

    get(
        "/rice-logo/imgsize/:width/:height/",
        (request, response) -> {
          //      logSparkRequest(TAG, request);

          // You may optionally want to turn on the logSparkRequest
          // call, commented out above, to see the full input we're
          // getting from the client web page.

          final var widthParam = stringToOptionInteger(request.params().get(":width")).getOrElse(1);
          final var heightParam =
              stringToOptionInteger(request.params().get(":height")).getOrElse(1);

          final var width = (widthParam < 1) ? 1 : widthParam;
          final var height = (heightParam < 1) ? 1 : heightParam;

          var tryRiceLogo = readImageResource("rice-logo.png");
          if (tryRiceLogo.isFailure()) {
            response.status(404);
            return "rice-logo.png not found!";
          }

          var riceLogo = tryRiceLogo.get();
          var riceLogoWidth = riceLogo.getWidth();
          var riceLogoHeight = riceLogo.getHeight();

          return nanoBenchmarkVal(
                  () -> {
                    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    for (var y = 0; y < height; y++) {
                      for (var x = 0; x < width; x++) {
                        var riceLogoPixel = riceLogo.getRGB(x % riceLogoWidth, y % riceLogoHeight);
                        image.setRGB(x, y, riceLogoPixel);
                      }
                    }
                    return imageToPng(image).getOrElse(stringToUTF8("Internal failure"));
                  })
              .apply(
                  (time, result) -> {
                    Log.iformat(
                        TAG, "rice-logo: made %dx%d image in %.3fms", width, height, 1e-6 * time);
                    response.type("image/png");
                    return result;
                  });
        });
  }
}
