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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

class RandomGeneTreeTest {
  private static final String TAG = "RandomGeneTreeTest";

  @Test
  void randomTree() {
    // We're just going to generate 200 random trees of max depth 5
    // and assert that they are at least five deep. If something goes
    // horribly wrong, we'd get a smaller depth. This isn't a terribly
    // sophisticated test, but if something is really broken, this
    // might catch it.

    var testGenes = Stream.continually(() -> RandomGeneTree.randomTreeOption(5)).take(200);
    testGenes.forEach(
        ogene -> {
          assertTrue(ogene.isDefined());
          assertTrue(ogene.get().maxDepth() >= 5, ogene.get().toString());
        });
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void randomGene() {
    // We're going to ask for 200 random genes of each of several
    // arities (# children) and make sure that all the results indeed
    // have the correct arity. Cheesy, but it's a start.

    List.of(0, 1, 2, 3)
        .forEach(
            i ->
                assertEquals(
                    100,
                    Stream.continually(() -> RandomGeneTree.randomAllele(i))
                        .take(100)
                        .filter(gene -> gene.numChildren() == i)
                        .length()));

    Log.i(TAG, "expect a failure to be logged: trying to fine a gene that doesn't exist");
    assertThrows(IllegalArgumentException.class, () -> RandomGeneTree.randomAllele(4));
  }
}
