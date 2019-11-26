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

package edu.rice.web;

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.vavr.Tries.tryOfNullable;
import static edu.rice.web.Utils.bigRandom;
import static edu.rice.web.Utils.jsonSparkExceptionHandler;
import static edu.rice.web.Utils.launchBrowser;
import static edu.rice.web.Utils.logSparkRequest;
import static edu.rice.web.Utils.muicssCommandLineBody;
import static edu.rice.web.Utils.muicssHeader;
import static j2html.TagCreator.b;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.html;
import static j2html.TagCreator.p;
import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

import edu.rice.util.Log;
import io.vavr.control.Option;
import javax.script.ScriptEngineManager;

/**
 * A read-eval-print loop for Nashorn (the JavaScript environment built into Java8) which runs in a
 * web page.
 *
 * <p>This is cool because you can see all of the Java classes that you've created and use them
 * interactively. Java9 will support a REPL for the Java language, itself, but for our purposes
 * JavaScript is still quite useful.
 *
 * <p>Note that there are important security issues here, since JavaScript exposes commands that
 * would be particularly undesirable if an intruder was able to send commands, over the Internet,
 * directly into this running web server. As such, we use a random number as part of our
 * initialization and launch the browser with that number as part of the URL. This keeps anybody
 * else from being able to get in.
 */
public class JavaScriptRepl {
  private static final String TAG = "JavaScriptRepl";

  /**
   * The accessKey represents a session key. This key is stored in the browser in a hidden form
   * field and returned to the server as part of the JSON structure. That's how we reject requests
   * that might arrive from elsewhere than the browser that we want.
   */
  private static final String accessKey = bigRandom(80);

  /**
   * The launchCode represents a single-use key, which we feed to the browser as part of the initial
   * URL when we launch the REPL. If the launchCode matches up, then the browser is sent an HTML
   * page that includes the accessKey, which will then be used for subsequent transactions.
   *
   * <p>One of the weird limitations of how Java launches a browser is that all we get is a URL.
   * There's no way to set any other browser state. No cookies. No nothing. So that's why we're
   * doing this whole launchCode thing and making it single-use. Unfortunately, it means that
   * bookmarking the URL will totally fail.
   */
  private static final String launchCode = bigRandom(20);

  /** This boolean tracks whether we've consumed the launchCode. */
  private static boolean launchCodeUsed = false;

  /** When we want to log an error and return an error JSON response. */
  private static String logAndJsonError(String errorMessage) {
    Log.e(TAG, errorMessage);
    return jobject(jpair("response", errorMessage)).toString();
  }

  /** When we want to log an error and return an error HTML response. */
  private static String logAndHtmlError(String errorMessage) {
    Log.e(TAG, errorMessage);
    return html().withTitle(errorMessage).with(body(h1(errorMessage))).render();
  }

  /**
   * Launch a JavaScript REPL, living in the same Java virtual machine as whatever else you're
   * doing, so it can call any public static method, perhaps useful for inspecting state.
   */
  public static void launch() {
    Log.i(TAG, "Starting!");

    // Create a Nashorn JavaScript instance
    final var engine = new ScriptEngineManager().getEngineByName("nashorn");

    // Initialize the SparkJava web server
    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG);
    launchBrowser("http://localhost:4567/jsrepl/" + launchCode);

    // This handles the requests that we field from the web page,
    // evaluating JavaScript strings and returning the results to be
    // displayed.
    get(
        "/jseval/",
        (request, response) -> {
          logSparkRequest(TAG, request);

          // Engineering notes: SparkJava returns null rather than
          // using Options to indicate missing parameters.  We could
          // have used a series of if (whatever == null) statements,
          // but maybe we'd end up forgetting a case and we'd have a
          // weird bug, and weird bugs here could translate to
          // security vulnerabilities.  By converting these maybe-null
          // values into Option-values, we can use our pattern
          // matching machinery to ensure that we don't forget a case.

          var oCommandLine = Option.of(request.queryParams("input"));
          var oFoundKey = Option.of(request.queryParams("key"));
          Log.i(TAG, "commandLine: " + oCommandLine);

          // no-cache because we're regenerating it every time
          response.header("cache-control", "no-cache");

          return oCommandLine.fold(
              () -> logAndJsonError("empty command line"),
              commandLine ->
                  oFoundKey.fold(
                      () -> logAndJsonError("absent access key, permission denied"),
                      foundKey -> {
                        // We're only going to evaluate the statement
                        // if we got the access key, because security
                        // matters.

                        if (accessKey.equals(foundKey)) {
                          // Engineering notes: we're constructing the
                          // JSON response, which includes evaluating
                          // the commandLine and dealing with any
                          // errors that might have occurred, which
                          // will manifest as a ScriptException (on
                          // syntax errors and the like). Annoyingly,
                          // if a JavaScript expression evaluates to
                          // null, then the eval() statement below
                          // returns Java's null, which our Try class
                          // will reject as an invalid input, leading
                          // us to the failure case. So fine, we deal
                          // with that explicitly.

                          final var jsEvalTxt =
                              tryOfNullable(() -> engine.eval(commandLine))
                                  .map(Object::toString)
                                  .recover(
                                      exception ->
                                          (exception instanceof NullPointerException)
                                              ? "null"
                                              : "Error: " + exception.getMessage())
                                  .get();

                          // Notice how we're building the response
                          // with j2html rather than just
                          // concatenating a bunch of raw HTML text
                          // together? This guarantees that weird
                          // output won't accidentally end up running
                          // in the client-side JavaScript
                          // interpreter. This is a good security
                          // practice.

                          final var responseTxt =
                              p().with(b("> " + commandLine))
                                  .with(br())
                                  .withText(jsEvalTxt)
                                  .render();

                          // We could have avoided these intermediate
                          // strings and just mashed everything
                          // together into a single expression, but
                          // it's actually nice to deal with each
                          // stage separately. First we deal with
                          // calling the JavaScript interpreter. Next
                          // we deal with constructing the HTML
                          // response. Lastly, we package that into a
                          // JSON message. Three stages, three values.

                          return jobject(jpair("response", responseTxt)).toString();
                        } else {
                          return logAndJsonError("incorrect access key, permission denied");
                        }
                      }));
        });

    // This generates the web page front-end for our JavaScript REPL.
    get(
        "/jsrepl/:launchcode",
        (request, response) -> {
          var oLaunchCode = Option.of(request.params("launchcode"));

          return oLaunchCode.fold(
              () -> logAndHtmlError("absent launch code, permission denied"),
              foundLaunchCode -> {
                if (launchCode.equals(foundLaunchCode) && !launchCodeUsed) {
                  launchCodeUsed = true;
                  return html()
                      .with(
                          muicssHeader("Nashorn JavaScript REPL", "/jsrepl.js", "/commandline.css"),
                          muicssCommandLineBody("Nashorn JavaScript REPL!", "accessKey", accessKey))
                      .renderFormatted();
                } else {
                  return logAndHtmlError("incorrect launch code, permission denied");
                }
              });
        });
  }

  /** Main routine that just launches the JavaScript REPL. */
  public static void main(String[] args) {
    launch();
  }
}
