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

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;

import edu.rice.io.Files;
import edu.rice.json.Parser;
import edu.rice.json.Value;
import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;

/** This class loads the genes described in resources/prettypictures-week2.json or elsewhere. */
public class TestGenesWeek2 {
  private static final String TAG = "TestGenesWeek2";

  private final Seq<GeneTree> genes; // initialized in constructor

  /** Given the name of the resource file, loads the requested genes. */
  public TestGenesWeek2(String resourceFileName) {
    // Week2 setup of test genes: note monadic error handling is used
    // here, so this is a big harder to read, but super awesome to
    // run.

    genes =
        Files.readResource(resourceFileName)
            .onFailure(err -> Log.e(TAG, "couldn't find disk file", err))
            .toOption()
            .flatMap(Parser::parseJsonObject)
            .onEmpty(() -> Log.e(TAG, "JSON error"))
            .getOrElse(jobject(jpair("testGenes", jarray())))
            .get("testGenes")
            .flatMap(Value::asJArrayOption)
            .map(Value.JArray::getSeq)
            .getOrElse(List.empty())
            .map(
                json ->
                    GeneTree.of(json)
                        .onEmpty(() -> Log.e(TAG, "failure to parse gene: " + json.toString())))
            .flatMap(x -> x);

    Log.i(TAG, "Found " + genes.length() + " test genes");
  }

  /** Gets all of the test genes. */
  public Seq<GeneTree> getGenes() {
    return genes;
  }

  /** Gets the number of test genes. */
  public int getNumGenes() {
    return genes.length();
  }

  /** Gets the nth test gene. */
  public GeneTree getNthGene(int n) {
    return genes.get(n);
  }

  /** Creates a requested number of completely random genetic trees. */
  public static Seq<GeneTree> randomTrees(int n) {
    return Stream.iterate(() -> RandomGeneTree.randomTreeOption(6)).take(n);
  }
}
