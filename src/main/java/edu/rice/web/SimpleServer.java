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
import static edu.rice.web.Utils.commandLineTemplate;
import static edu.rice.web.Utils.jsonSparkExceptionHandler;
import static edu.rice.web.Utils.launchBrowser;
import static edu.rice.web.Utils.logSparkRequest;
import static j2html.TagCreator.body;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.p;
import static j2html.TagCreator.title;
import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

import edu.rice.util.Log;
import org.intellij.lang.annotations.Language;

public class SimpleServer {
  private static final String TAG = "SimpleServer";

  /** Main entry point; args are ignored. */
  public static void main(String[] args) {
    Log.i(TAG, "Starting!");

    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/lcserver/");

    get(
        "/hello",
        (request, response) -> {
          // This shows how we can return any string of HTML we want,
          // but we can at least take advantage of IntelliJ's syntax
          // highlighting to make sure we don't have any syntax
          // errors.
          @Language("HTML")
          String responseStr =
              "<html>"
                  + "<head>"
                  + "<title>Hello, world</title>"
                  + "</head>"
                  + "<body>"
                  + "<h1>Hello, world</h1>"
                  + "<p>This is some introductory text.</p>"
                  + "</body>"
                  + "</html>";
          return responseStr;
        });

    get(
        "/hello2",
        (request, response) ->
            // This shows the exact same HTML as above, except
            // constructed with the j2html library.  This won't
            // compile until you've got it correct. For contrast, try
            // commenting out one of the lines above. You'll notice
            // that IntelliJ might or might not see the problem.
            // (The @Language annotation helps a lot, but isn't
            // a panacea.)
            html()
                .with(
                    head().with(title("Hello, world")),
                    body().with(h1("Hello, world"), p("This is some introductory text")))
                .renderFormatted());

    get(
        "/lowercase/",
        (request, response) -> {
          logSparkRequest(TAG, request);

          String commandLine = request.queryParams("input");
          Log.i(TAG, "commandLine: " + commandLine);

          // unfortunately, SparkJava returns null rather than using
          // Options to indicate things that are missing
          if (commandLine != null) {
            response.status(200); // okay!

            // no-cache because we're regenerating it every time
            response.header("cache-control", "no-cache");

            @Language("HTML")
            String responseStr = "<b>&gt; " + commandLine + "</b><br/>" + commandLine.toLowerCase();

            return jobject(jpair("response", responseStr)).toString();
          } else {
            Log.i(TAG, "empty command line");
            response.status(400); // bad request
            return jobject().toString(); // empty JSON object
          }
        });

    get(
        "/lcserver/",
        (request, response) -> commandLineTemplate("Lowercase!", "/lowercase/").renderFormatted());
  }
}
