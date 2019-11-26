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

package edu.rice.qt;

import static edu.rice.qt.QtHelpers.integerSequences;
import static edu.rice.qt.QtHelpers.stringSequences;
import static edu.rice.qt.SequenceGenerators.sequences;
import static edu.rice.vavr.Sequences.seqIsSorted;
import static edu.rice.vavr.Sequences.seqMatch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.arrays;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.strings;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.util.Strings;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

/**
 * This class demonstrates some "theories" that test properties of VAVR's Seq classes. This is using
 * <a href="https://github.com/ncredinburgh/QuickTheories">QuickTheories</a>. Under the hood, it
 * will try thousands of different possibilities to see if it can make the unit test fail, and then
 * if it finds a failure input, it will then search to see if it can find a simpler / smaller input
 * that also causes failure.
 */
@GradeTopic(project = "Week06", topic = "Properties")
public class ListTheories {

  @Test
  public void reverseOneElementConstant() {
    // Reverse Theory 1: reversing a list with one element yields the
    // original list.
    qt().forAll(sequences().of(strings().basicLatinAlphabet().ofLengthBetween(0, 5)).ofSize(1))
        .checkAssert(list -> assertEquals(list, list.reverse()));
  }

  @Test
  public void reverseConcatVsConcatReverse() {
    // Reverse Theory 2: "different paths, same destination":
    // concatenate two lists then reverse == reverse them separately,
    // concatenate in the other order.
    qt().forAll(stringSequences(), stringSequences())
        .checkAssert(
            (list1, list2) ->
                assertEquals(
                    list1.appendAll(list2).reverse(), list2.reverse().appendAll(list1.reverse())));
  }

  @Test
  public void reverseTwiceSameResult() {
    // Reverse Theory 3: "There and back again": reversing twice
    // yields the original list.
    qt().forAll(stringSequences())
        .checkAssert(list -> assertEquals(list, list.reverse().reverse()));
  }

  @Test
  public void foldAddTest() {
    // "Different paths, same destination": three ways to compute the
    // sum of a list of integers.

    qt().forAll(integerSequences())
        .checkAssert(
            testList -> {
              // Traditional Java "enhanced for loop" with a
              // mutation-style approach to accumulating the result in
              // a counter. Bad style for Comp215, but useful as a
              // "different path".
              int sum = 0;

              for (int elem : testList) {
                sum += elem;
              }

              assertEquals(sum, (int) testList.foldLeft(0, Integer::sum));
              assertEquals(sum, (int) testList.foldRight(0, Integer::sum));
            });
  }

  @Test
  public void stringConcatenationIsAssociative() {
    // "Different paths, same destination": if we try to fold strings
    // with the "+" operator, we would expect the same results with
    // foldLeft vs. foldRight, so long as we're not commuting the
    // operations.

    // Using stringSequences() to generate your tests, assert
    // equality of a foldLeft and foldRight on the each using string
    // concatenation.

    qt().forAll(stringSequences())
        .checkAssert(
            list ->
                assertEquals(
                    list.foldLeft("", (a, b) -> a + b), list.foldRight("", (a, b) -> a + b)));
  }

  @Test
  @Grade(project = "Week06", topic = "Properties", points = 0.2)
  public void stringConcatenationIsNotCommutative() {
    // "Different paths, same destination": if we try to fold strings
    // with the "+" operator, but we commute the operations, then the
    // results will be backwards, so reversing the list first should
    // undo this.

    // Using stringSequences() to generate your tests, and noting
    // that a + b is backwards from b + a, you'll compare the results
    // of two foldLeft operations, where one changes the order of the
    // concatenation and the other reverses the order of the list.

    qt().forAll(stringSequences())
        .checkAssert(
            list ->
                assertEquals(
                    list.foldLeft("", (a, b) -> a + b),
                    list.reverse().foldLeft("", (a, b) -> b + a)));

    //    fail("stringConcatenationIsNotCommutative() not implemented yet");
  }

  @Test
  public void stringJoinTest() {
    // "Different paths, same destination": making sure that
    // mkString(), which does something fancy, behaves the
    // same as something "simpler" using folds.

    qt().forAll(stringSequences())
        .checkAssert(
            testList -> {

              // Each of these commented-out variants fail. Uncomment
              // them and see what QuickTheories can find as an input
              // that causes the test assertion to fail.

              // var result = testList.foldLeft("", (a, b) -> (a + ", " + b));

              // var result = testList.foldLeft("", (a, b) -> ((a.equals("")) ? b : a + ", " +
              // b));

              // var result = seqMatch(
              //   testList,
              //   emptyList -> "",
              //   (head, tail) -> head + tail.foldLeft(", ", (a, b) -> a + ", " + b));

              var result =
                  seqMatch(
                      testList,
                      emptyList -> "",
                      (head, tail) -> head + tail.foldLeft("", (a, b) -> a + ", " + b));

              assertEquals(testList.mkString(", "), result);
            });
  }

  @Test
  public void objectEqualityImpliesHashEquality() {
    // "Hard to prove, easy to verify": if two lists are equal, their
    // hashes should be equal.  This should work for Seq<String>
    // every bit as much as Seq<Integer> or anything else. If we get
    // it right for two types, we're probably happy for others.

    qt().forAll(integerSequences(), integerSequences())
        .checkAssert(
            (listA, listB) ->
                // either the lists are not equal, or if they are, then the hashCodes match
                assertTrue((!listA.equals(listB) || listA.hashCode() == listB.hashCode())));

    qt().forAll(stringSequences(), stringSequences())
        .checkAssert(
            (listA, listB) ->
                assertTrue((!listA.equals(listB) || listA.hashCode() == listB.hashCode())));
  }

  @Test
  @Grade(project = "Week06", topic = "Properties", points = 0.2)
  public void sortingNaturalOrder() {
    // "Hard to prove, easy to verify": sorting is fairly complex, but
    // checking that the list is in sorted order is simple.

    // Using integerSequences() to generate your tests, sort those
    // lists using VAVR's sorted() operator and verify that the
    // resulting list is sorted using our seqIsSorted() helper method.

    var naturalOrder = Comparator.<Integer>naturalOrder();
    var orderByInteger = Comparator.<Integer, Integer>comparing(x -> x);

    qt().forAll(integerSequences())
        .checkAssert(
            stringList -> {
              var sorted1 = stringList.sorted();
              var sorted2 = stringList.sorted(naturalOrder);
              var sorted3 = stringList.sorted(orderByInteger);
              assertTrue(seqIsSorted(sorted1, naturalOrder));
              assertTrue(seqIsSorted(sorted2, naturalOrder));
              assertTrue(seqIsSorted(sorted3, naturalOrder));
            });

    //    fail("sortingNaturalOrder() not implemented yet");
  }

  @Test
  public void sortAndReverseSameAsBackwardSort() {
    // "Different paths, same destination": sorting then reversing is
    // the same as sorting in reverse order. Also true if we just
    // flip the sense of the Comparator.

    qt().forAll(integerSequences())
        .checkAssert(
            list ->
                assertEquals(
                    list.sorted(Comparator.<Integer>naturalOrder().reversed()),
                    list.sorted(Comparator.comparingInt(x -> -x))));

    qt().forAll(integerSequences())
        .checkAssert(
            list ->
                assertEquals(
                    list.sorted().reverse(), list.sorted(Comparator.comparingInt(x -> -x))));
  }

  @Test
  public void fromIterableAndFromArrayBehaveTheSame() {
    // "Different paths, same destination": Two ways of converting an array to a list.

    qt().forAll(arrays().ofIntegers(integers().all()).withLengthBetween(0, 10))
        .checkAssert(
            array -> {
              var jlist = java.util.List.of(array);
              var list = List.of(array);

              assertEquals(list, List.ofAll(jlist));
            });
  }

  @Test
  public void listToStringAndBack() {
    // "There and back again": Converting lists of integers to and
    // from comma-separated strings.

    // VAVR's lists have an operation "mkString" which lets you join
    // together the individual elements of the list, specifying a
    // variety of formatting options, such as the delimiter to put
    // between each entry. Use this with sequences of integers of
    // *non-zero* size, and then use String's "split" operation to
    // break them back into a list again. (String.split actually
    // returns an array, which you can just pass to List.of().)

    // Strings.stringToOptionInteger is one of several helper functions
    // that we provide in edu.rice.util.Strings. Go read it to see
    // how you'd normally have to handle a conversion like this.

    qt().forAll(sequences().of(integers().all()).ofSizeBetween(1, 20))
        .checkAssert(
            list -> {
              var list2 =
                  List.of(list.mkString(",").split(","))
                      .map(str -> Strings.stringToOptionInteger(str).get());
              assertEquals(list, list2);
            });

    // String.split() on an empty-string returns an array with a
    // single empty-string, which would cause the above test to fail
    // if QT list size range included empty lists.
    assertEquals(List.of(""), List.of("".split(",")));
  }

  @Test
  public void cascadingIdenticalFiltersSameResult() {
    // "The more things change, the more they stay the same.":
    // Filtering a list with the same predicate more than once won't
    // change the result.

    // Generate sequences of integers or strings, whichever you
    // prefer, and come up with a predicate that will clearly remove
    // some but not all of the entries. The results of applying that
    // filter once versus twice should be identical.

    qt().forAll(integerSequences())
        .checkAssert(
            list ->
                assertEquals(list.filter(x -> x > 0), list.filter(x -> x > 0).filter(x -> x > 0)));
  }

  @Test
  public void cascadingSortsSameResult() {
    // "The more things change, the more they stay the same.": Sorting
    // twice, same result.

    var ordering = Comparator.<Integer>naturalOrder();
    var ordering2 = ordering.thenComparingInt(x -> x);

    qt().forAll(integerSequences())
        .checkAssert(
            list -> assertEquals(list.sorted(ordering), list.sorted(ordering).sorted(ordering2)));
  }

  @Test
  public void takeWhileGetsPositives() {
    // "Different paths, same destination": if the takeWhile() is
    // working properly, it will stop when it hits a negative number

    qt().forAll(
            sequences().of(integers().allPositive()).ofSizeBetween(0, 10),
            integers().allPositive().map(x -> x * -1), // forcibly negative numbers
            integerSequences())
        .checkAssert(
            (positiveList, negativeNumber, suffixList) -> {
              var listWithNegative = positiveList.appendAll(List.of(negativeNumber));
              var listWithNegativeAndSuffix = listWithNegative.appendAll(suffixList);
              assertEquals(positiveList, positiveList.takeWhile(x -> x >= 0));
              assertEquals(positiveList, listWithNegative.takeWhile(x -> x >= 0));
              assertEquals(positiveList, listWithNegativeAndSuffix.takeWhile(x -> x >= 0));

              // while we're at it, VAVR's span() method is like takeWhile() but is also
              // supposed to give you whatever's left after the predicate first fails.
              var spanResult = listWithNegativeAndSuffix.span(x -> x >= 0);
              assertEquals(positiveList, spanResult._1);
              assertEquals(suffixList.prepend(negativeNumber), spanResult._2);
            });
  }

  /**
   * Returns the "maximum" value from the sequence, assuming its length is non-zero, using its
   * "natural" ordering. For a zero-length list, defaultValue is returned.
   */
  private static <T extends Comparable<T>> T seqMaximum(Seq<T> seq, T defaultValue) {
    return seqMatch(
        seq,
        emptyList -> defaultValue,
        (head, tail) -> tail.foldLeft(head, (a, b) -> (a.compareTo(b) > 0) ? a : b));

    // Bugs to try introducing to see what happens:
    // - get the sense of the comparison wrong for the compareTo call
    // - get the fold wrong, so it includes the default value
    // - forget to handle the emptyList case and just do the fold

    // Here's the VAVR built-in max function, which we can also use for testing.
    // It normally returns Option<T>, so it can return Option.none() on empty input.
    //    return seq.max().getOrElse(defaultValue);
  }

  @Test
  public void maxReturnsDefaultWhenTheListIsEmpty() {
    // Base case: we get a defined result when asking for the maximum of an empty list.
    assertEquals(1, seqMaximum(List.empty(), 1));
  }

  @Test
  public void maxFindsAValueInTheList() {
    // "Hard to prove, easy to verify" : the value returned is part of the list, at least
    // when the list has at least one element in it.
    qt().forAll(sequences().of(integers().allPositive()).ofSizeBetween(1, 100))
        .check(seq -> seq.contains(seqMaximum(seq, -1)));
  }

  @Test
  public void maxFindsTheBiggest() {
    // "Hard to prove, easy to verify" : the value returned is greater than or equal to
    // every other value in the list.
    qt().forAll(sequences().of(integers().allPositive()).ofSizeBetween(1, 100))
        .check(
            seq -> {
              var max = seqMaximum(seq, -1);
              return seq.forAll(x -> max.compareTo(x) >= 0);
            });
  }
}
