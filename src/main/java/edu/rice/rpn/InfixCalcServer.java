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

package edu.rice.rpn;

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.web.Utils.commandLineTemplate;
import static edu.rice.web.Utils.jsonSparkExceptionHandler;
import static edu.rice.web.Utils.launchBrowser;
import static edu.rice.web.Utils.logSparkRequest;
import static spark.Spark.get;
import static spark.Spark.redirect;
import static spark.Spark.staticFileLocation;

import edu.rice.util.Log;

/**
 * Web server for your Infix Calculator. "Run" this, then point your browser at
 * localhost:4567/infixcalc/
 */
public class InfixCalcServer {
  private static final String TAG = "InfixCalcServer";

  /** Main method to initialize the web server; args are ignored. */
  public static void main(String[] args) {
    Log.i(TAG, "Starting!");
    final var infixCalculator = new InfixCalculator();

    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG);
    launchBrowser("http://localhost:4567/infixcalc/");

    // This route handles the requests from our JavaScript client, running inside the web browser.
    get(
        "/infixserver/",
        (request, response) -> {
          logSparkRequest(TAG, request);

          String commandLine = request.queryParams("input");

          if (commandLine != null) {
            response.status(200); // okay!

            // no-cache because we're regenerating it every time
            response.header("cache-control", "no-cache");
            return jobject(
                    jpair(
                        "response",
                        "<b>&gt; " + commandLine + "</b><br/>" + infixCalculator.calc(commandLine)))
                .toString();
          } else {
            Log.i(TAG, "empty command line");
            response.status(400); // bad request
            return jobject().toString(); // empty JSON object
          }
        });

    // This route handles the request for the web page that will run
    // on the client, which will in turn make requests to us using the
    // /infixserver route, above.
    get(
        "/infixcalc/",
        (request, response) ->
            commandLineTemplate("Infix Calculator", "/infixserver/").renderFormatted());

    // Send any stray requests back to the top-level calculator
    redirect.get("/", "/infixcalc/");
  }
}
