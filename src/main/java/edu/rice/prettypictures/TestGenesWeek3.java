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
public class TestGenesWeek3 {
  private static final String TAG = "TestGenesWeek3";

  private final Seq<GeneTree> genes; // initialized in constructor

  /** Given the name of the resource file, loads the requested genes. */
  public TestGenesWeek3(int inputnumber) {
    // Week2 setup of test genes: note monadic error handling is used
    // here, so this is a big harder to read, but super awesome to
    // run.

    var temp =
        Files.readResource("prettypictures-week2.json")
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
    if (inputnumber == 3) {
      var temp1 = mutatehelper(List.<GeneTree>empty().append(temp.get(40)),29);
      var temp2 = mutatehelper(List.<GeneTree>empty().append(temp.get(41)), 29);
      genes = temp1.appendAll(temp2);
    } else {
      var temp1 = breedhelper(List.<GeneTree>empty().append(temp.get(40)).append(temp.get(41)),  29);
      var temp2 = breedhelper(List.<GeneTree>empty().append(temp.get(41)).append(temp.get(40)), 29);
      genes = temp1.appendAll(temp2.removeAt(0).removeAt(0));
    }
  }

  private Seq<GeneTree> mutatehelper(Seq<GeneTree> inputList, int timesLeft) {
    if (timesLeft == 0) {
      return inputList;
    } else {
      return mutatehelper(inputList.append(inputList.get(0).mutateTree()), timesLeft - 1);
    }
  }

  private Seq<GeneTree> breedhelper(Seq<GeneTree> inputList, int timesLeft) {
    if (timesLeft == 0) {
      return inputList;
    } else {
      return breedhelper(inputList.append(inputList.get(0).crossBreed(inputList.get(1))), timesLeft - 1);
    }
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
