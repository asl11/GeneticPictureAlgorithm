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

package edu.rice.week5sorting;

import static edu.rice.qt.SequenceGenerators.sequences;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

import java.util.Comparator;
import org.junit.jupiter.api.Test;

class HeapSorterTest {
  // Engineering notes: these unit tests are comparing your sorting
  // implementation, which uses a binary heap, with the sorting
  // implementation that's built into VAVR, which uses an algorithm
  // called TimSort (https://en.wikipedia.org/wiki/Timsort). What's
  // perhaps more interesting is that the code here is using a library
  // called QuickTheories
  // (https://github.com/quicktheories/QuickTheories), which generates
  // lots of random inputs and then runs the tests for each one. This
  // sort of unit test is called "property-based testing"; we'll
  // explain it in detail in week 6.

  // Fun fact: If you dive into the VAVR code to see how they did
  // sorting, you'll notice that VAVR doesn't implement its own
  // sorting algorithm. Instead, it just converts sequences to Java
  // Streams, and then asks the Java core library to do the sorting,
  // which is where the Timsort code can be found. VAVR's author
  // clearly decided that there was no need to reinvent what the
  // Oracle team had done.

  @Test
  void basicSortingSameAsVavr() {
    qt().forAll(sequences().of(strings().basicLatinAlphabet().ofLength(5)).ofSizeBetween(0, 100))
        .check(seq -> seq.sorted().equals(HeapSorter.sort(seq)));
  }

  @Test
  void sortingWithComparatorSameAsVavr() {
    var reverseOrder = Comparator.<String>naturalOrder().reversed();
    qt().forAll(sequences().of(strings().basicLatinAlphabet().ofLength(5)).ofSizeBetween(0, 100))
        .check(seq -> seq.sorted(reverseOrder).equals(HeapSorter.sort(seq, reverseOrder)));
  }
}
