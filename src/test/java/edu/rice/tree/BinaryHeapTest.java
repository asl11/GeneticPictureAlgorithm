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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

public class BinaryHeapTest {
  @Test
  public void testBasics() {
    var heap1 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    var heap2 = BinaryHeap.of(5, 2, 9, 7, 5, 10, 100, 5, -2); // note that "5" occurs three times

    assertEquals(9, heap1.size());
    assertEquals(9, heap2.size());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(-2, (int) heap1.getMin());
    assertEquals(-2, (int) heap2.getMin());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(2, (int) heap1.getMin());
    assertEquals(2, (int) heap2.getMin());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(3, (int) heap1.getMin());
    assertEquals(5, (int) heap2.getMin());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(4, (int) heap1.getMin());
    assertEquals(5, (int) heap2.getMin());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(5, (int) heap1.getMin());
    assertEquals(5, (int) heap2.getMin());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(7, (int) heap1.getMin());
    assertEquals(7, (int) heap2.getMin());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());

    assertEquals(3, heap1.size());
    assertEquals(3, heap2.size());
    assertTrue(heap1.isValid());
    assertTrue(heap2.isValid());
  }

  @Test
  public void testFancy() {
    var heap3 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);

    // we'll verify the toString() method while we're at it: note that
    // we're getting the internal order, not in-order
    assertEquals("BinaryHeap(-2, 2, 9, 3, 5, 10, 100, 7, 4)", heap3.toString());

    // toString() shouldn't change the internal state of the heap
    assertEquals("BinaryHeap(-2, 2, 9, 3, 5, 10, 100, 7, 4)", heap3.toString());

    // now we're going to extract eight things, leaving one behind
    assertEquals(Stream.of(-2, 2, 3, 4, 5, 7, 9, 10), Stream.continually(heap3::getMin).take(8));
    assertEquals("BinaryHeap(100)", heap3.toString());

    // now we stress-test generating the list when it empties out at the end
    var heap4 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    assertEquals("BinaryHeap(-2, 2, 9, 3, 5, 10, 100, 7, 4)", heap4.toString());
    assertEquals(
        Stream.of(-2, 2, 3, 4, 5, 7, 9, 10, 100), Stream.continually(heap4::getMin).take(9));
    assertEquals("BinaryHeap()", heap4.toString());

    // binary heaps are a convenient way to sort a list, right?
    var numbers = List.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    var sortedNumbers = numbers.sorted();

    var heap = BinaryHeap.<Integer>empty();
    numbers.forEach(heap::insert);
    assertEquals(sortedNumbers, heap.drainToStream());
    assertTrue(heap.isEmpty());
  }

  @Test
  public void testEqualsAndHashcode() {
    var heap3 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    var heap4 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4);
    final int origHeap3Hashcode = heap3.hashCode();

    assertEquals(-2, (int) heap3.getMin());
    assertEquals(heap4, heap3);
    assertEquals(heap4.hashCode(), heap3.hashCode());
    assertNotEquals(origHeap3Hashcode, heap3.hashCode());
  }
}
