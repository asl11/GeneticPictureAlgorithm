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

import static edu.rice.week4queue.Merge.seqMerge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week04", topic = "Merge")
class MergeTest {
  @Test
  @Grade(project = "Week04", topic = "Merge", points = 0.5)
  void seqMergeTest() {
    var a = List.of(8, 2, 3, 1, 4, 9, 2, 7);
    var b = a.map(x -> x + 1);

    var expected = a.appendAll(b).sorted();
    var aSorted = a.sorted();
    var bSorted = b.sorted();

    assertEquals(expected, seqMerge(aSorted, bSorted));
  }

  @Test
  @Grade(project = "Week04", topic = "Merge", points = 0.5)
  void mergeEmpties() {
    var a = List.of("Hello", "Rice");
    var b = List.<String>empty();

    assertEquals(a, seqMerge(a, b));
    assertEquals(a, seqMerge(b, a));
  }

  @Test
  @Grade(project = "Week04", topic = "Merge", points = 1.0)
  void mergesAreLazy() {
    var evens = Stream.iterate(0, x -> x + 2);
    var odds = Stream.iterate(1, x -> x + 2);

    // If any of these operations runs more than one second, then
    // it's probably going to run forever, so we terminate it early
    // and fail the test. Similarly, if there's a stack overflow,
    // that's another symptom that the function isn't lazy.

    var nats =
        assertTimeoutPreemptively(
            Duration.ofSeconds(1),
            () -> seqMerge(evens, odds),
            "Lazy operations on infinite input should run fast");

    var natsFirst6 =
        assertTimeoutPreemptively(
            Duration.ofSeconds(1),
            () -> nats.take(6),
            "Lazy operations on infinite input should run fast");

    assertEquals(List.of(0, 1, 2, 3, 4, 5), natsFirst6);
  }
}
