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

package edu.rice.week4queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

@GradeProject(
    name = "Week04",
    description = "Functional Queues / Merge / Subsets",
    warningPoints = 1.0)
@GradeTopic(project = "Week04", topic = "Thursday Tests", maxPoints = 3.0)
@GradeTopic(project = "Week04", topic = "Sunday Correctness", maxPoints = 3.0)
public class ListQueueTest {
  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testEmpty() {
    // write a test that creates an empty queue with
    // ListQueue.empty() and verifies that it's empty
    var emptyQueue = ListQueue.empty();
    assertTrue(emptyQueue.isEmpty());

    // fail("testEmpty not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testQueueOfEmpty() {
    // write a test that creates an empty queue with ListQueue.of()
    // and verifies that it's empty
    var emptyQueue = ListQueue.of();
    assertTrue(emptyQueue.isEmpty());

    // fail("testQueueOfEmpty not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testEmptySize() {
    // write a test that creates an empty queue and verifies that it's
    // length is zero
    var emptyQueue = ListQueue.empty();
    assertEquals(0, emptyQueue.size());

    // fail("testEmptySize not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testSizeEight() {
    // write a test that creates a queue with eight *integers* in it,
    // using ListQueue.of(...) and verifies that the size is 8
    var queue = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals(8, queue.size());

    // fail("testSizeEight not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testToStringEight() {
    // Make a queue using ListQueue.of(), as above, convert it to a
    // string with the toString() method (provided for you), and test
    // that you get the string you were expecting.

    var queue = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals("Queue(1, 2, 3, 4, 5, 6, 7, 8)", queue.toString());

    // fail("testToStringEight not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testSizeInsertOne() {
    // write a test that creates an empty queue, adds one thing to it
    // with the enqueue() method, then verifies the size
    var queue = ListQueue.<Integer>empty().enqueue(5);
    assertEquals(1, queue.size());

    // fail("testSizeInsertOne not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testHeadOfThreeInserts() {
    // write a test that creates an empty queue, adds three things to
    // it with the enqueue() method, then verifies the head() is the
    // first thing that went in

    var queue = ListQueue.<String>empty().enqueue("Hello").enqueue("Rice").enqueue("Owls");
    assertEquals("Hello", queue.peek());

    // fail("testHeadOfThreeInserts() not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testHeadAndTailOfThreeInserts() {
    // write a test that creates an empty queue, adds three things to
    // it with the enqueue() method, testing the head() as above, but
    // also takes the tail() of the queue and verifies the head() of
    // the result, doing this for each entry until you get an empty
    // queue (and verifying that the queue is indeed empty).

    var queue = ListQueue.<String>empty().enqueue("Hello").enqueue("Rice").enqueue("Owls");
    assertEquals("Hello", queue.peek());
    assertEquals("Rice", queue.tail().peek());
    assertEquals("Owls", queue.tail().tail().peek());
    assertTrue(queue.tail().tail().tail().isEmpty());

    // fail("testHeadAndTailOfThreeInserts() not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testSingularEmptyQueue() {
    // write a test that creates an empty queue, adds one thing, and
    // then removes it. Verify that the resulting empty queue and the
    // original empty queue point to the identical object in memory
    // (i.e., use assertTrue(a == b) or assertSame(a, b) rather than
    // assertEquals(a, b)).

    var emptyQueue = ListQueue.empty();
    var emptyQueue2 = emptyQueue.enqueue("Hello").tail();

    assertSame(emptyQueue, emptyQueue2);

    // fail("testSingularEmptyQueue() not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Thursday Tests", points = 0.3)
  public void testToStream() {
    // Write a test that exercises ListQueue's toStream method. This
    // test should verify correct behavior for empty queues and
    // non-empty queues. Try to be clever and make sure that the queue
    // you're converting to a lazy list has something in its inbox and
    // its outbox, which means that you'll also be exercising your
    // rebalancing logic.

    var queue = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8).enqueue(9).enqueue(10);
    var stream = queue.toStream();

    // At this point, the stream shouldn't be fully evaluated yet, which
    // we can only test because its toString() method reveals to us where
    // it's pending by showing '?'.
    assertEquals("Stream(1, ?)", stream.toString());

    // This will force the full stream to be evaluated.
    assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), stream);
    assertEquals("Stream(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)", stream.toString());

    // fail("testToStream() not implemented yet");
  }

  @Test
  @Grade(project = "Week04", topic = "Sunday Correctness", points = 1.0)
  public void testEqualsAndHashcode() {
    var queue1 = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8);
    var queue2 = ListQueue.of(3, 4, 5, 6, 7, 8);
    assertEquals(queue1.tail().tail(), queue2);
    assertEquals(queue1.tail().tail().tail(), queue2.tail());
    assertEquals(queue1.tail().tail().tail().enqueue(9), queue2.tail().enqueue(9));
    assertNotEquals(queue1, queue2);
    assertNotEquals(queue1.tail(), queue2);
    assertNotEquals(queue1.tail().tail().enqueue(9), queue2);

    assertEquals(queue1.tail().tail().hashCode(), queue2.hashCode());
    assertEquals(queue1.tail().tail().tail().hashCode(), queue2.tail().hashCode());
    assertEquals(
        queue1.tail().tail().tail().enqueue(9).hashCode(), queue2.tail().enqueue(9).hashCode());

    // hashCodes *might* collide, but it's going to be highly unlikely
    assertNotEquals(queue1.hashCode(), queue2.hashCode());
    assertNotEquals(queue1.tail().tail().hashCode(), queue2.tail().hashCode());
    assertNotEquals(
        queue1.tail().tail().enqueue(9).hashCode(), queue2.tail().enqueue(9).hashCode());
  }

  @Test
  @Grade(project = "Week04", topic = "Sunday Correctness", points = 2.0)
  public void testEverything() {
    // this test, provided for you, does a bunch of inserts, then
    // fetches, then more inserts, over and over, to exercise the
    // inbox/outbox reversing logic.

    var testNumbers = Stream.range(0, 10); // numbers 0 through 9 inclusive
    var testQueue = ListQueue.empty();
    var resultQueue = ListQueue.empty();

    // Engineering note: you might look at this code and say "ah ha!
    // mutation!" and indeed, our local variables for testQueue and
    // resultQueue are changing, but the underlying queues they point
    // to are still functional.  We could certainly reengineer this
    // test, itself, to be purely functional, but would that make it
    // easier or harder for you to read?

    for (var i = 0; i < 5; i++) {
      // first, enqueue the numbers 0 through 9
      testQueue = testNumbers.foldLeft(testQueue, ListQueue::enqueue);

      // now, extract the first three numbers from the queue
      for (var j = 0; j < 3; j++) {
        resultQueue = resultQueue.enqueue(testQueue.peek());
        testQueue = testQueue.tail();
      }
    }

    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4), resultQueue.toStream());
  }
}
