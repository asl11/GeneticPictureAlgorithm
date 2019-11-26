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

import static edu.rice.qt.SequenceGenerators.sequences;
import static edu.rice.vavr.Sequences.seqIsSorted;
import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;

import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.quicktheories.core.Gen;

/**
 * Static methods useful for testing Trees, Treaps, or anything else that implements ITree.
 * Typically you pass these the empty tree that's used for subsequent work.
 */
class TreeSuite {
  private final String name;
  private final ITree<String> emptyStringTree;
  private final ITree<Integer> emptyIntegerTree;

  /** Not for external use. See {@link #allTreeTests(String, ITree, ITree, boolean)}. */
  private TreeSuite(String name, ITree<String> emptyStringTree, ITree<Integer> emptyIntegerTree) {
    this.name = name;
    this.emptyStringTree = emptyStringTree;
    this.emptyIntegerTree = emptyIntegerTree;
  }

  // Engineering note: this week we don't have @Test in front of each
  // test; see the bottom of the file for how these tests are
  // invoked. We're using JUnit5's support for a "test factory" which
  // is to say, a method that returns a list of "dynamic tests", each
  // of which has a name and a lambda. The lambdas are then unit tests
  // that will be run independently. This allows us to reuse these
  // tests for trees, treaps, and anything else that implements the
  // ITree interface.

  void testInsertSimple() {
    // Write a unit test that inserts two strings into a tree, then
    // queries the tree to see whether those values are present. Also,
    // verify that a third string is absent from the tree.

    var testTree = emptyStringTree.add("Alice").add("Bob");
    assertTrue(testTree.find("Alice").isDefined());
    assertTrue(testTree.find("Bob").isDefined());
    assertTrue(testTree.find("Charlie").isEmpty());

    //    fail("testInsert not implemented yet (project 5)");
  }

  void testRemoveSimple() {
    // Write a unit test that inserts two strings into a tree, then
    // removes one of them. Verify that both strings are still present
    // in the original tree, and that the post-removal tree is indeed
    // missing the value you removed.

    var testTree = emptyStringTree.add("Alice").add("Bob");
    var testTree2 = testTree.remove("Bob");
    assertTrue(testTree2.find("Alice").isDefined());
    assertTrue(testTree2.find("Bob").isEmpty());
    assertTrue(testTree.find("Bob").isDefined());
    assertTrue(testTree2.find("Charlie").isEmpty());

    //    fail("testInsert not implemented yet (project 5)");
  }

  void testGreaterThanSimple() {
    // Write a unit test that inserts five strings into the tree, then
    // make two queries against the tree, against one of those
    // strings. Use "inclusive" for one query and don't use it for the
    // other.  Verify that the results have the correct members.

    final var testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final var testTree = testVectorList.foldLeft(emptyStringTree, ITree::add);
    final var geqTree = testTree.greaterThan("Charlie", true);
    final var gtrTree = testTree.greaterThan("Charlie", false);

    assertTrue(geqTree.find("Alice").isEmpty());
    assertTrue(geqTree.find("Bob").isEmpty());
    assertTrue(geqTree.find("Charlie").isDefined());
    assertTrue(geqTree.find("Dorothy").isDefined());
    assertTrue(geqTree.find("Eve").isDefined());

    assertTrue(gtrTree.find("Alice").isEmpty());
    assertTrue(gtrTree.find("Bob").isEmpty());
    assertTrue(gtrTree.find("Charlie").isEmpty());
    assertTrue(gtrTree.find("Dorothy").isDefined());
    assertTrue(gtrTree.find("Eve").isDefined());

    //    fail("testInsert not implemented yet (project 5)");
  }

  void testLessThanSimple() {
    // Same as GreaterThan, but do it for LessThan.

    final var testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final var testTree = testVectorList.foldLeft(emptyStringTree, ITree::add);
    final var leqTree = testTree.lessThan("Charlie", true);
    final var lessTree = testTree.lessThan("Charlie", false);

    assertTrue(leqTree.find("Alice").isDefined());
    assertTrue(leqTree.find("Bob").isDefined());
    assertTrue(leqTree.find("Charlie").isDefined());
    assertTrue(leqTree.find("Dorothy").isEmpty());
    assertTrue(leqTree.find("Eve").isEmpty());

    assertTrue(lessTree.find("Alice").isDefined());
    assertTrue(lessTree.find("Bob").isDefined());
    assertTrue(lessTree.find("Charlie").isEmpty());
    assertTrue(lessTree.find("Dorothy").isEmpty());
    assertTrue(lessTree.find("Eve").isEmpty());

    //    fail("testInsert not implemented yet (project 5)");
  }

  void testInsertList() {
    var testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    var tree1 = emptyStringTree.add("Alice").add("Bob").add("Charlie").add("Dorothy").add("Eve");
    var tree2 = emptyStringTree.addAll(testVectorList);

    // we have to convert to a list, first, because treaps might have
    // different memory layouts, but toSeq() does an in-order
    // traversal, which we expect to give us consistent results.
    assertEquals(tree1.toSeq().toString(), tree2.toSeq().toString());

    assertEquals(tree1.toSeq(), tree2.toSeq());
    assertEquals(tree2.toSeq(), tree1.toSeq());

    var tree3 = tree1.removeAll(List.of("Alice", "Charlie"));

    assertEquals(List.of("Bob", "Dorothy", "Eve"), tree3.toSeq());
  }

  void testInorder() {
    var testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    var tree = emptyStringTree.addAll(testVectorList);

    StringBuilder result = new StringBuilder();
    tree.inorder(result::append);

    assertEquals("AliceBobCharlieDorothyEve", result.toString());
  }

  void testToList() {
    final var testVectorList1 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final var testVectorList2 = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final var testVectorList3 = List.of("Eve", "Bob", "Alice", "Dorothy");
    final var tree1 = emptyStringTree.addAll(testVectorList1);
    final var tree2 = emptyStringTree.addAll(testVectorList2);
    final var tree3 = emptyStringTree.addAll(testVectorList3);

    final var elist = emptyStringTree.toSeq();
    final var list1 = tree1.toSeq();
    final var list2 = tree2.toSeq();
    final var list3 = tree3.toSeq();

    assertEquals(list1, list2);
    assertEquals(list2, list1);
    assertEquals(elist, elist);
    assertNotEquals(list1, elist);
    assertNotEquals(list1, list3);
    assertNotEquals(list3, list1);
  }

  void testRemove() {
    final var testVectorList =
        List.of("Charlie", "Hao", "Eve", "Gerald", "Bob", "Alice", "Frank", "Dorothy");
    final var tree = emptyStringTree.addAll(testVectorList);
    final var treeR1 = tree.remove("Alice");
    final var treeR2 = tree.remove("Bob");
    final var treeR3 = tree.remove("Gerald");

    assertEquals(
        List.of("Bob", "Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), treeR1.toSeq());
    assertEquals(
        List.of("Alice", "Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), treeR2.toSeq());
    assertEquals(
        List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve", "Frank", "Hao"), treeR3.toSeq());
    assertEquals(emptyStringTree, emptyStringTree.remove("Alice"));
    assertEquals(tree, tree.remove("Nobody"));

    assertTrue(emptyStringTree.isValid());
    assertTrue(tree.isValid());
    assertTrue(treeR1.isValid());
    assertTrue(treeR2.isValid());
    assertTrue(treeR3.isValid());
  }

  void testRange() {
    var testVectorList =
        List.of("Charlie", "Hao", "Eve", "Gerald", "Bob", "Alice", "Frank", "Dorothy");
    var tree = emptyStringTree.addAll(testVectorList);

    assertEquals(List.of("Alice", "Bob", "Charlie"), tree.lessThan("Charlie", true).toSeq());
    assertEquals(List.of("Alice", "Bob"), tree.lessThan("Charlie", false).toSeq());
    assertEquals(
        List.of("Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"),
        tree.greaterThan("Charlie", true).toSeq());
    assertEquals(
        List.of("Dorothy", "Eve", "Frank", "Gerald", "Hao"),
        tree.greaterThan("Charlie", false).toSeq());
    assertEquals(
        List.of("Dorothy", "Eve", "Frank"),
        tree.greaterThan("Charlie", false).lessThan("Frank", true).toSeq());

    var r1 = tree.lessThan("Charlie", true);
    assertEquals(List.of("Alice", "Bob", "Charlie"), r1.toSeq());
    assertTrue(r1.isValid());

    var r2 = tree.lessThan("Charlie", false);
    assertEquals(List.of("Alice", "Bob"), r2.toSeq());
    assertTrue(r2.isValid());

    var r3 = tree.greaterThan("Charlie", true);
    assertEquals(List.of("Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), r3.toSeq());
    assertTrue(r3.isValid());

    var r4 = tree.greaterThan("Charlie", false);
    assertEquals(List.of("Dorothy", "Eve", "Frank", "Gerald", "Hao"), r4.toSeq());
    assertTrue(r4.isValid());

    var r5 = tree.greaterThan("Charlie", false).lessThan("Frank", true);
    assertEquals(List.of("Dorothy", "Eve", "Frank"), r5.toSeq());
    assertTrue(r5.isValid());
  }

  void testEquals() {
    final var testVectorList1 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final var testVectorList2 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final var testVectorList3 = List.of("Eve", "Bob", "Alice", "Dorothy");
    final var tree1 = emptyStringTree.addAll(testVectorList1);
    final var tree2 = emptyStringTree.addAll(testVectorList2);
    final var tree3 = emptyStringTree.addAll(testVectorList3);

    assertEquals(tree1, tree1);
    assertEquals(tree1, tree2);
    assertEquals(tree2, tree1);
    assertNotEquals(emptyStringTree, tree1);
    assertNotEquals(tree1, emptyStringTree);
    assertNotEquals(tree1, tree3);

    // and now, we'll insert in different orders; carefully chosen to yield the same result for an
    // unbalanced
    // tree, but should also work for a deterministic treap
    final var testVectorList4 = List.of("Charlie", "Bob", "Alice", "Eve", "Dorothy");
    final var tree4 = emptyStringTree.addAll(testVectorList4);

    assertEquals(tree1, tree4);
  }

  void testSize() {
    var testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    var tree = emptyStringTree.addAll(testVectorList);

    assertEquals(emptyStringTree.size(), 0);
    assertEquals(tree.size(), 5);
  }

  void testRemoveMin() {
    var tree = emptyStringTree.addAll(List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy"));
    var failure = Tuple.of("Fail", emptyStringTree);

    var resultTree =
        tree.removeMin()
            .getOrElse(failure)
            .apply(
                (minVal, remainingTree) -> {
                  assertEquals("Alice", minVal);
                  assertFalse(remainingTree.isEmpty());
                  return remainingTree;
                })
            .removeMin()
            .getOrElse(failure)
            .apply(
                (minVal, remainingTree) -> {
                  assertEquals("Bob", minVal);
                  assertFalse(remainingTree.isEmpty());
                  return remainingTree;
                })
            .removeMin()
            .getOrElse(failure)
            .apply(
                (minVal, remainingTree) -> {
                  assertEquals("Charlie", minVal);
                  assertFalse(remainingTree.isEmpty());
                  return remainingTree;
                })
            .removeMin()
            .getOrElse(failure)
            .apply(
                (minVal, remainingTree) -> {
                  assertEquals("Dorothy", minVal);
                  assertFalse(remainingTree.isEmpty());
                  return remainingTree;
                })
            .removeMin()
            .getOrElse(failure)
            .apply(
                (minVal, remainingTree) -> {
                  assertEquals("Eve", minVal);
                  assertTrue(remainingTree.isEmpty());
                  return remainingTree;
                });

    assertNotEquals(failure, resultTree);
    assertFalse(resultTree.removeMin().isDefined());
  }

  void testMaxDepth() {
    assertEquals(0, emptyStringTree.maxDepth());
    var oneElem = emptyStringTree.add("Hello");
    assertEquals(1, oneElem.maxDepth());
    var twoElem = oneElem.add("Rice");
    assertEquals(2, twoElem.maxDepth());

    // once we insert a third element, we might have a two-level tree
    // or we might have a three-level tree, depending on how the
    // balancing went
    var threeElem = twoElem.add("Owls!");
    int depth = threeElem.maxDepth();
    assertTrue(depth == 2 || depth == 3);
  }

  void testMaxDepth2(int ninserts, int maxDepthInteger, int maxDepthString) {
    System.out.println("=========== " + name + " depth test =========== ");
    var stree = emptyStringTree.addAll(Stream.rangeClosed(1, ninserts).map(Object::toString));
    var itree = emptyIntegerTree.addAll(Stream.rangeClosed(1, ninserts));

    int sdepth = stree.maxDepth();
    int idepth = itree.maxDepth();

    System.out.printf("With %d sequential integer inserts, max depth = %d\n", ninserts, idepth);
    System.out.printf("With %d sequential string inserts, max depth = %d\n", ninserts, sdepth);

    // minimum conceivable depth, for a perfectly balanced tree: round
    // up to nearest power of two, take the log-base-2.
    int minDepth = (int) ceil(log(ninserts) / log(2));

    assertTrue(
        sdepth >= minDepth,
        "If this test fails, then your tree depth is too small; something is very wrong with your tree"
            + " (found: "
            + sdepth
            + " vs. min: "
            + minDepth
            + ")");
    assertTrue(
        idepth >= minDepth,
        "If this test fails, then your tree depth is too small; something is very wrong with your tree"
            + " (found: "
            + sdepth
            + " vs. min: "
            + minDepth
            + ")");
    assertTrue(
        sdepth <= maxDepthString,
        "If this test fails, then your tree depth is too large; your tree rebalancing isn't working"
            + " (found: "
            + sdepth
            + " vs. max: "
            + maxDepthString
            + ")");
    assertTrue(
        idepth <= maxDepthInteger,
        "If this test fails, then your tree depth is too large; your tree rebalancing isn't working"
            + " (found: "
            + idepth
            + " vs. max: "
            + maxDepthInteger
            + ")");
  }

  /////// And now some QuickTheories, starting off with some
  /////// generators that will be helpful for us.

  // We'll talk all about QuickTheories next week in class. For now,
  // all you need to know is that QuickTheories is a library that
  // generates *lots* of random inputs, and checks any assertion you
  // want against all possible inputs. It's searching for a
  // *counter-example* that breaks your assertions.

  Gen<String> reasonableStrings() {
    return strings().basicLatinAlphabet().ofLengthBetween(0, 10).describedAs(x -> "\"" + x + "\"");
  }

  Gen<Seq<String>> reasonableStringLists(int minListLength) {
    return sequences()
        .of(reasonableStrings())
        .ofSizeBetween(minListLength, 10)
        .describedAs(seq -> seq.mkString("Seq(\"", "\", \"", "\")"));
  }

  Gen<ITree<String>> reasonableStringTrees(int minSize) {
    return reasonableStringLists(minSize)
        .map(emptyStringTree::addAll)
        .describedAs(tree -> tree.toSeq().mkString("Tree(\"", "\", \"", "\")"));
  }

  void treesAreValid() {
    // "The test oracle": make sure that all our trees are "valid".

    qt().forAll(reasonableStringTrees(0)).checkAssert(tree -> assertTrue(tree.isValid()));
  }

  void findingSomethingThatsThere() {
    // "Hard to prove, easy to verify": make sure that something we
    // inserted can be found again.

    qt().forAll(reasonableStringLists(1))
        .checkAssert(
            list -> assertTrue(emptyStringTree.addAll(list).find(list.head()).isDefined()));
  }

  void findingSomethingThatsNotThere() {
    // "Hard to prove, easy to verify": make sure that something we
    // didn't insert cannot be found.

    qt().forAll(reasonableStrings(), reasonableStringLists(0))
        .assuming((entry, list) -> !list.contains(entry))
        .checkAssert(
            (entry, list) -> assertTrue(emptyStringTree.addAll(list).find(entry).isEmpty()));
  }

  void insertingAndRemovingYieldsEmpty() {
    // "Hard to prove, easy to verify": removing everything after it's
    // added should yield an empty tree.

    qt().forAll(reasonableStringLists(0))
        .checkAssert(list -> assertTrue(emptyStringTree.addAll(list).removeAll(list).isEmpty()));
  }

  void insertingAndRemovingYieldsSameTree() {
    // "Hard to prove, easy to verify": adding one thing then removing
    // it should yield the same tree as beforehand.

    qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(1, 10), reasonableStringTrees(3))
        .assuming((str, tree) -> tree.find(str).isEmpty())
        .checkAssert(
            (str, tree) -> {
              var tree2 = tree.add(str);
              var tree3 = tree2.remove(str);
              var tree4 = tree3.remove(str); // nothing should change here!
              assertNotEquals(tree, tree2);
              assertEquals(tree, tree3);
              assertEquals(tree, tree4);

              // strings and hashes should also match up
              assertNotEquals(tree.toString(), tree2.toString());
              //          assertNotEquals(tree.hashCode(), tree2.hashCode());
              // there's a tiny probability that the hashcodes might be equal

              assertEquals(tree.toString(), tree3.toString());
              assertEquals(tree.hashCode(), tree3.hashCode());

              assertEquals(tree.toString(), tree4.toString());
              assertEquals(tree.hashCode(), tree4.hashCode());
            });
  }

  void removalPreservesValidity() {
    // "Hard to prove, easy to verify": removing something should
    // preserve tree validity.

    qt().forAll(reasonableStringLists(1))
        .checkAssert(
            list -> assertTrue(emptyStringTree.addAll(list).remove(list.head()).isValid()));
  }

  void lessThanValid() {
    // "Hard to prove, easy to verify": removing entries less than a
    // given target should preserve the resulting tree's validity.
    // Also, everything we get back should indeed be less than or
    // equal to the query.

    qt().forAll(reasonableStrings(), reasonableStringTrees(0))
        .checkAssert(
            (query, tree) -> {
              var exclusive = tree.lessThan(query, false);
              var inclusive = tree.lessThan(query, true);

              assertTrue(exclusive.isValid());
              assertTrue(inclusive.isValid());

              exclusive.toSeq().forEach(entry -> assertTrue(entry.compareTo(query) < 0));
              inclusive.toSeq().forEach(entry -> assertTrue(entry.compareTo(query) <= 0));
            });
  }

  void greaterThanValid() {
    // "Hard to prove, easy to verify": removing entries greater than
    // a given target should preserve the resulting tree's validity.
    // Also, everything we get back should indeed be greater than or
    // equal to the query.

    qt().forAll(reasonableStrings(), reasonableStringTrees(0))
        .checkAssert(
            (query, tree) -> {
              var exclusive = tree.greaterThan(query, false);
              var inclusive = tree.greaterThan(query, true);

              assertTrue(exclusive.isValid());
              assertTrue(inclusive.isValid());

              exclusive.toSeq().forEach(entry -> assertTrue(entry.compareTo(query) > 0));
              inclusive.toSeq().forEach(entry -> assertTrue(entry.compareTo(query) >= 0));
            });
  }

  void removeMinFindsSmallest() {
    // "Different paths, same destination": we can sort a list and
    // find its smallest element, or we can ask a tree to remove its
    // minimum.

    qt().forAll(reasonableStringTrees(1))
        .checkAssert(
            initialTree -> {
              var result = initialTree.removeMin().get();

              // toSeq() should return sorted output, but we're being
              // extra paranoid here
              assertEquals(initialTree.toSeq().sorted().head(), result._1);
              assertEquals(initialTree.toSeq().head(), result._1);

              // while we're at it, make sure the resulting tree
              // doesn't have the minimum in it any more
              assertEquals(initialTree.size() - 1, result._2.size());
              assertFalse(result._2.find(result._1).isDefined());
            });
  }

  void toSeqIsSorted() {
    // "Hard to prove, easy to verify": the toSeq() method on a tree
    // should return "natural order" sorted output.

    qt().forAll(reasonableStringTrees(0))
        .checkAssert(tree -> assertTrue(seqIsSorted(tree.toSeq())));
  }

  void toSeqHasCorrectSize() {
    // "Different paths, same destination": the toSeq() method should
    // return the same number of entries as in the tree.

    qt().forAll(reasonableStringLists(0))
        .checkAssert(
            list ->
                assertEquals(
                    emptyStringTree.addAll(list).size(),
                    emptyStringTree.addAll(list).toSeq().length()));
  }

  void toSeqIsLazy() {
    // VAVR Stream's toString() method will stop and print a ? for anything that
    // hasn't yet been evaluated. We're counting on that for this test.

    qt().forAll(sequences().of(integers().between(0, 99)).ofSizeBetween(10, 20))
        .checkAssert(
            list -> {
              var tree = list.foldLeft(emptyIntegerTree, ITree::add);
              var sorted = tree.toSeq();
              assertTrue(sorted.isLazy());

              var min = sorted.head();
              var lazyString = sorted.toString();
              assertEquals(String.format("Stream(%d, ?)", min), lazyString);

              // the code below looks at the whole list, so no more ?'s
              assertEquals(sorted.min().get(), min);
              var joinedStr = sorted.mkString(", ");
              assertEquals(String.format("Stream(%s)", joinedStr), sorted.toString());
            });
  }

  /**
   * Produces a list of JUnit5 "dynamic tests" for a given implementation of {@link ITree}. This can
   * be returned from a JUnit5 {@link org.junit.jupiter.api.TestFactory}.
   *
   * @param name Name of the algorithm (e.g., "Treap")
   * @param emptyStringTree An empty tree of String
   * @param emptyIntegerTree An empty tree of Integer
   * @param randomized If a tree is randomized, then we shouldn't run certain equality tests
   */
  public static Seq<DynamicTest> allTreeTests(
      String name,
      ITree<Integer> emptyIntegerTree,
      ITree<String> emptyStringTree,
      boolean randomized) {
    TreeSuite suite = new TreeSuite(name, emptyStringTree, emptyIntegerTree);

    return List.of(
        dynamicTest(name + " insert simple", suite::testInsertSimple),
        dynamicTest(name + " remove simple", suite::testRemoveSimple),
        dynamicTest(name + " greater than", suite::testGreaterThanSimple),
        dynamicTest(name + " less than", suite::testLessThanSimple),
        dynamicTest(name + " insert list", suite::testInsertList),
        dynamicTest(name + " in-order", suite::testInorder),
        dynamicTest(name + " to-list", suite::testToList),
        dynamicTest(name + " remove", suite::testRemove),
        dynamicTest(name + " range", suite::testRange),
        dynamicTest(name + " size", suite::testSize),
        dynamicTest(name + " remove-min", suite::testRemoveMin),
        dynamicTest(
            name + " equals",
            () -> {
              if (!randomized) {
                suite.testEquals();
              }
            }),
        dynamicTest(name + " valid theory", suite::treesAreValid),
        dynamicTest(name + " empty-after-removal theory", suite::insertingAndRemovingYieldsEmpty),
        dynamicTest(
            name + " add-then-remove-same-tree theory", suite::insertingAndRemovingYieldsSameTree),
        dynamicTest(name + " removal-preserves-validity theory", suite::removalPreservesValidity),
        dynamicTest(name + " less-than-works theory", suite::lessThanValid),
        dynamicTest(name + " greater-than-works theory", suite::greaterThanValid),
        dynamicTest(name + " to-list-is-sorted theory", suite::toSeqIsSorted),
        dynamicTest(name + " to-list-is-lazy theory", suite::toSeqIsLazy),
        dynamicTest(name + " to-list-has-correct-size theory", suite::toSeqHasCorrectSize),
        dynamicTest(
            name + " finding-something-thats-there theory", suite::findingSomethingThatsThere),
        dynamicTest(
            name + " finding-something-thats-not-there theory",
            suite::findingSomethingThatsNotThere),
        dynamicTest(name + " remove-min-finds-smallest theory", suite::removeMinFindsSmallest));
  }

  /**
   * Thursday-only version of {@link #allTreeTests(String, ITree, ITree, boolean)}.
   *
   * @param name Name of the algorithm (e.g., "Treap")
   * @param emptyStringTree An empty tree of String
   * @param emptyIntegerTree An empty tree of Integer
   */
  public static Seq<DynamicTest> thursdayTreeTests(
      String name, ITree<Integer> emptyIntegerTree, ITree<String> emptyStringTree) {
    TreeSuite suite = new TreeSuite(name, emptyStringTree, emptyIntegerTree);

    return List.of(
        // tests that students write
        dynamicTest(name + " insert simple", suite::testInsertSimple),
        dynamicTest(name + " remove simple", suite::testRemoveSimple),
        dynamicTest(name + " greater than", suite::testGreaterThanSimple),
        dynamicTest(name + " less than", suite::testLessThanSimple),

        // our own tests that exercise lessThan and greaterThan
        dynamicTest(name + " range", suite::testRange),
        dynamicTest(name + " less-than-works theory", suite::lessThanValid),
        dynamicTest(name + " greater-than-works theory", suite::greaterThanValid));
  }

  /**
   * Sunday-specific MaxDepth test, taken from {@link #allTreeTests(String, ITree, ITree, boolean)}.
   *
   * @param name Name of the algorithm (e.g., "Treap")
   * @param emptyStringTree An empty tree of String
   * @param emptyIntegerTree An empty tree of Integer
   * @param ninserts for the tree depth test, how many sequential inserts to conduct
   * @param maxDepthInteger expected maximum depth for the depth test on a tree of sequential
   *     integers
   * @param maxDepthString expected maximum depth for the depth test on a tree of strings (made from
   *     those integers)
   */
  public static Seq<DynamicTest> sundayDepthTests(
      String name,
      ITree<Integer> emptyIntegerTree,
      ITree<String> emptyStringTree,
      int ninserts,
      int maxDepthInteger,
      int maxDepthString) {
    TreeSuite suite = new TreeSuite(name, emptyStringTree, emptyIntegerTree);

    return List.of(
        dynamicTest(name + " max-depth", suite::testMaxDepth),
        dynamicTest(
            name + " max-depth 2",
            () -> suite.testMaxDepth2(ninserts, maxDepthInteger, maxDepthString)));
  }
}
