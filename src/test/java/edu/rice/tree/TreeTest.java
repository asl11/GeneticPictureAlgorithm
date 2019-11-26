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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@GradeTopic(project = "Week05", topic = "Thursday")
public class TreeTest {
  /** Tests for the basic tree class. */
  @TestFactory
  public Seq<DynamicTest> testSuite() {
    // insert 1000 sequential numbers, expected maxDepth <= 1001
    // fun question: why is the string tree balanced?
    return TreeSuite.allTreeTests("tree", Tree.empty(), Tree.empty(), false)
        .appendAll(TreeSuite.sundayDepthTests("tree", Tree.empty(), Tree.empty(), 1000, 1001, 30));
  }

  /** Tests specifically for the Thursday deadline. */
  @TestFactory
  @Grade(project = "Week05", topic = "Thursday", points = 1.0, maxPoints = 2.0)
  public Seq<DynamicTest> thursdayTestSuite() {
    return TreeSuite.thursdayTreeTests("tree", Tree.empty(), Tree.empty());
  }

  @Test
  public void testRotate() {
    // We can't have a corresponding test like this for Treap because
    // we have no deterministic way of predicting how the priorities
    // will force rotations.

    // Bob on top, Alice to the left, Charlie to the right
    var tree1 = Tree.of("Bob", "Alice", "Charlie");

    // Charlie on top, Bob to the left, Alice to the left
    var tree2 = Tree.of("Charlie", "Bob", "Alice");

    // Alice on top, Bob to the right, Charlie to the right
    var tree3 = Tree.of("Alice", "Bob", "Charlie");

    if (!(tree1 instanceof Tree.Node<?>)) {
      fail("not a node type");
    }
    if (!(tree2 instanceof Tree.Node<?>)) {
      fail("not a node type");
    }
    if (!(tree3 instanceof Tree.Node<?>)) {
      fail("not a node type");
    }
    var tree1Right = ((Tree.Node<String>) tree1).rotateRight();
    var tree1Left = ((Tree.Node<String>) tree1).rotateLeft();

    assertEquals(tree2, tree1Left);
    assertEquals(tree3, tree1Right);
  }
}
