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

package edu.rice.tree;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

@GradeTopic(project = "Week10", topic = "Treaps")
public class DTreapTest {
  /** Run the full test suite on a deterministic treap. */
  @TestFactory
  @Grade(project = "Week10", topic = "Treaps", points = 0.5, maxPoints = 0.5)
  public Seq<DynamicTest> testSuite() {
    // insert 1000 sequential numbers, expected maxDepth <= 30
    // fun question: why is the integer tree unbalanced?
    return TreeSuite.allTreeTests("dtreap", DTreap.empty(), DTreap.empty(), false)
        .appendAll(
            TreeSuite.sundayDepthTests("dtreap", DTreap.empty(), DTreap.empty(), 1000, 1001, 30));
  }
}
