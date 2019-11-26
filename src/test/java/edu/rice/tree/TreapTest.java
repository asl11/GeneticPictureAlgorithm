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
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

@GradeProject(name = "Week05", description = "Treaps / Knapsack", warningPoints = 1.0)
@GradeTopic(project = "Week05", topic = "Treap")
public class TreapTest {
  /** Tests for the treap class. */
  @TestFactory
  @Grade(project = "Week05", topic = "Treap", points = 0.2, maxPoints = 3.0)
  public Seq<DynamicTest> testSuite() {
    return TreeSuite.allTreeTests("treap", Treap.empty(), Treap.empty(), true);
  }

  /** Tests specifically for treap maximum depth. */
  @TestFactory
  @Grade(project = "Week05", topic = "Treap", points = 1.0, maxPoints = 2.0)
  public Seq<DynamicTest> testMaxDepth() {
    return TreeSuite.sundayDepthTests("treap", Treap.empty(), Treap.empty(), 1000, 30, 30);
  }
}
