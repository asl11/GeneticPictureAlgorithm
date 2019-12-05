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

import static edu.rice.image.Images.imageToPng;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static edu.rice.util.Strings.stringToOptionInteger;
import static edu.rice.util.Strings.stringToUTF8;
import static edu.rice.web.Utils.jsonSparkExceptionHandler;
import static edu.rice.web.Utils.launchBrowser;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.redirect;
import static spark.Spark.staticFileLocation;

import edu.rice.autograder.annotations.GradeCoverage;
import edu.rice.util.Log;
import edu.rice.vavr.Sequences;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import javax.sound.midi.Sequence;

/**
 * Web server for Pretty Pictures. "Run" this and it will launch your browser with our
 * prettyPicturesBreeder JavaScript client. Other common URLs will be redirected there as well if
 * you want to close and restart your browser.
 *
 * <p>Our reference implementation and the JavaScript client you see were written by Tim Van Baak,
 * based in part on a previous version by Clayton Drazner and Matthew Kindy II.
 *
 * <p>Week3: This time around, you'll be supporting four different "tests" (two from last week and
 * two new ones) as well as the real deal database where you track each gene from each generation.
 * <i>As in prior weeks, all of your management of the gene database should happen in a separate
 * Java file where you can test it.</i>
 */
@GradeCoverage(project = "PP1", exclude = true)
@GradeCoverage(project = "PP2", exclude = true)
@GradeCoverage(project = "PP3", exclude = true)
public class PrettyPicturesServerWeek3 {
  private static final String TAG = "PrettyPicturesServerWeek3";
  private static int testNumber = 1; // mutated by the /test route
  private static Seq<GeneTree> testGenes = List.empty(); // mutated by the /test route
  private static int testGenesLength = 1; // mutated by the /test route

  private static int totalGenerationNumber = 0; //mutated by the /test route
  private static int currentGenerationNumber = 0; //mutated by the /test route
  private static Map<Integer, Seq<GeneTree>> mutationStateRecorder = HashMap.empty();
  private static Map<Integer, Seq<GeneTree>> breedingStateRecorder = HashMap.empty();



  /** Main entry point for the PrettyPictures web server. Args are ignored. */
  public static void main(String[] args) {
    staticFileLocation("/WebPublic/");
    jsonSparkExceptionHandler(TAG);
    setupDefaultHandlers();

    Log.i(TAG, "PrettyPictures: starting up!");
    Log.i(TAG, () -> "Available processors: " + Runtime.getRuntime().availableProcessors());

    // Perform setup here
    final var week2db = new TestGenesWeek2("prettypictures-week2.json");
    // TODO: implement this handler
    /*
     * GET /image/gen/:gen/img/:img/height/:height/width/:width/
     * This handler is used to request a specific image from a specific generation.
     * Return image number :img from generation number :gen as a :width by :height png.
     * Hint: Remember that Images.imageToPng returns a Try<byte[]>, and so you will
     *   need to get() the byte[] out of the Try<>. Remember also to set the response
     *   type to "image/png".
     */
    get(
        "/image/gen/:gen/img/:img/height/:height/width/:width/",
        (request, response) -> {
          final var params = request.params();
          final var genNum =
              stringToOptionInteger(params.get(":gen"))
                  .onEmpty(() -> Log.e(TAG, "failed to decode generation number: " + request.url()))
                  .getOrElse(0);
          final var imageNum =
              stringToOptionInteger(params.get(":img"))
                  .onEmpty(
                      () -> Log.e(TAG, () -> "failed to decode image number: " + request.url()))
                  .getOrElse(0);
          final var width =
              stringToOptionInteger(params.get(":width"))
                  .onEmpty(() -> Log.e(TAG, () -> "failed to decode image width: " + request.url()))
                  .getOrElse(1);
          final var height =
              stringToOptionInteger(params.get(":height"))
                  .onEmpty(
                      () -> Log.e(TAG, () -> "failed to decode image height: " + request.url()))
                  .getOrElse(1);
          //TODO: - Bad Requests, check this again
//          if (genNum < 0 || genNum >= stateRecorder.length() || imageNum < 0 || imageNum >= stateRecorder.get(genNum).length()) {
//            Log.e(TAG, () -> "bogus generation/image (" + genNum + "/" + imageNum + ")");
//            response.status(300); // error!
//            return stringToUTF8("Bad arguments");
//          }

          switch (testNumber) {
            case 4:
              //INSERT conditions
              //INSERT lambda to get rid of get
              testGenes = breedingStateRecorder.get(genNum).get();
              break;
            case 3:
              //INSERT conditions
              //Insert lambda to get rid of get
              testGenes = mutationStateRecorder.get(genNum).get();
          }
          var results =
              nanoBenchmarkVal(
                  () -> testGenes.get(imageNum).toImageFunction().toImage(width, height));
          Log.iformat(
              TAG,
              "rendered gen: %d, image: %02d (%dx%d), time: %.3f ms (%.3f μs/pixel)",
              genNum,
              imageNum,
              width,
              height,
              results._1 / 1_000_000.0,
              results._1 / (1_000.0 * width * height));

          return imageToPng(results._2)
              .map(
                  imageBytes -> {
                    response.type("image/png");
                    return imageBytes;
                  })
              .getOrElse(
                  () -> {
                    response.status(300); // error!
                    return stringToUTF8("Internal failure");
                  });

//          //Check if it is the first generation
//          if (genNum.equals(0)) {
//            //Construct a random tree up to a certain depth
//            //update functionally?
//            stateRecorder = stateRecorder.append(List.of(RandomGeneTree.randomTree(5)));
//          }
//            var results =
//                nanoBenchmarkVal(
//                    () -> stateRecorder.get(genNum).get(imageNum).toImageFunction().toImage(width, height));
//            //() -> testGenes.get(imageNum).toImageFunction().toImage(width, height));
//            Log.iformat(
//                TAG,
//                "rendered gen: %d, image: %02d (%dx%d), time: %.3f ms (%.3f μs/pixel)",
//                genNum,
//                imageNum,
//                width,
//                height,
//                results._1 / 1_000_000.0,
//                results._1 / (1_000.0 * width * height));
//
//            return imageToPng(results._2)
//                .map(
//                    imageBytes -> {
//                      response.type("image/png");
//                      return imageBytes;
//                    })
//                .getOrElse(
//                    () -> {
//                      response.status(300); // error!
//                      return stringToUTF8("Internal failure");
//                    });

        });

    // TODO: implement this handler
    /*
     * POST /test/:number
     * This handler is used to load the standard test generation.
     * Return a JSON response where "response" is keyed to a JObject with three key/values:
     * - numGenerations, the total number of generations
     * - currentGeneration, the number of the generation to display
     * - numImages, the number of images per generation
     */
    post(
        "/test/:number",
        (request, response) -> {
          testNumber =
              stringToOptionInteger(request.params().get(":number"))
                  .onEmpty(() -> Log.e(TAG, () -> "failed to decode test number: " + request.url()))
                  .getOrElse(1);

          switch (testNumber) {
            case 4:
              testGenes = new TestGenesWeek3(4).getGenes();
              break;
            case 3:
              testGenes = new TestGenesWeek3(3).getGenes();//states of test 3
              break;
            case 2:
              // regenerate every time, makes testing a little easier
              testGenes = TestGenesWeek2.randomTrees(50);
              break;
            case 1:
            default:
              testGenes = week2db.getGenes();
              break;
          }
          testGenesLength = testGenes.length();

          return customJsonResponse(1, 0, testGenesLength);
        });

    // TODO: implement this handler

    /*
     * GET /string/gen/:gen/img/:img/
     * This handler is used to print the internal structure of your image functions.
     * Return a string representation of image number :image from generation number :gen.
     */
    get(
        "/string/gen/:gen/img/:img/",
        (request, response) -> {
          final var params = request.params();
          final var genNum =
              stringToOptionInteger(params.get(":gen"))
                  .onEmpty(
                      () ->
                          Log.e(TAG, () -> "failed to decode generation number: " + request.url()))
                  .getOrElse(0);
          final var imageNum =
              stringToOptionInteger(params.get(":img"))
                  .onEmpty(
                      () -> Log.e(TAG, () -> "failed to decode image number: " + request.url()))
                  .getOrElse(0);



          if (genNum < 0 || genNum >= stateRecorder.length() || imageNum < 0 || imageNum >= stateRecorder.get(genNum).length()) {
            Log.e(TAG, () -> "bogus generation/image (" + genNum + "/" + imageNum + ")");
            response.status(300); // error!
            return stringToUTF8("Bad arguments");
          }

          response.type("application/json");
          return stateRecorder.get(genNum).get(imageNum).toJson().toIndentedString();
        });

    // TODO: implement this handler

    /*
     * GET /client-init/
     * This handler is used to initialize the breeder client with information about the server.
     * Return a JSON response as in POST /test/.
     */

    //Good Case, but what about bad case where either no generation or no images to generation?
    get("/client-init/", (request, response) -> {
      //if (bad case) {call post reset} else {}
    return customJsonResponse( totalGenerationNumber, currentGenerationNumber,testGenesLength); });

    // TODO: implement this handler
    /*
     * POST /reset/:count/
     * This handler is used to reset the server to a random first generation.
     * Reset the stored generations to a new, randomly generated first generation with :count images.
     * Return a JSON response as in POST /test/.
     */
    post("/reset/:count/", (request, response) -> {
          var count =
              stringToOptionInteger(request.params().get(":countr"))
                  .onEmpty(() -> Log.e(TAG, () -> "failed to decode count: " + request.url()))
                  .getOrElse(1);
          stateRecorder = List.of() ;//Construct random tree of count images
          // reset totalGenerationNumber, currentGenerationNumber, testGenesLength
          totalGenerationNumber = 1;
          currentGenerationNumber = 0;
          testGenesLength = count;
          return customJsonResponse( totalGenerationNumber, currentGenerationNumber,testGenesLength);
        }
        );


    // TODO: implement this handler

    /*
     * POST /breed/oldgen/:oldgen/img/*
     * This handler is used to breed a new generation from a previous one.
     * Create a new generation bred from generation :olggen using the images in *.
     * Return a JSON response as in POST /test/.
     */

    // You should not launch the client until all setup has been performed.
    launchBrowser("http://localhost:4567/prettyPicturesBreeder.html");
  }

  private static String customJsonResponse(
      int numGenerations, int currentGeneration, int numImages) {
    return jobject(
            jpair(
                "response",
                jobject(
                    jpair("numGenerations", numGenerations),
                    jpair("currentGeneration", currentGeneration),
                    jpair("numImages", numImages))))
        .toString();
  }

  /**
   * You shouldn't need to worry about these handlers. When our JavaScript, running in the browser,
   * wants to log something, it will call these handlers, so the log data shows up in your Java log.
   */
  private static void setupDefaultHandlers() {
    // If our JavaScript client wants to log anything, it will send those logs back to the server,
    // so they appear alongside any server logs.
    post(
        "/log/i/",
        (request, response) -> {
          var msg = request.queryParams("msg");
          Log.i("Client", msg);
          return "Information logged.";
        });

    post(
        "/log/e/",
        (request, response) -> {
          var msg = request.queryParams("msg");
          Log.e("Client", msg);
          return "Error logged.";
        });

    // Send any stray requests back to the PrettyPictures server
    redirect.get("/", "/prettyPicturesBreeder.html");
    redirect.get("/prettyPictures", "/prettyPicturesBreeder.html");
  }
}
