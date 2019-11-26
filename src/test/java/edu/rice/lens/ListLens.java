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

package edu.rice.lens;

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.vavr.Sequences;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

/** Make sure our lenses on lists work. */
public class ListLens {
  @Test
  public void lensTest() {
    final var negfive = Sequences.<String>lensSeq(-5);
    final var zero = Sequences.<String>lensSeq(0);
    final var two = Sequences.<String>lensSeq(2);
    final var five = Sequences.<String>lensSeq(5);

    final var fourStrings = List.of("Alice", "Bob", "Charlie", "Dorothy");
    final var charlieToCharles = List.of("Alice", "Bob", "Charles", "Dorothy");
    final var noCharlie = List.of("Alice", "Bob", "Dorothy");

    assertEquals(none(), negfive.get(fourStrings));
    assertEquals(some("Alice"), zero.get(fourStrings));
    assertEquals(some("Charlie"), two.get(fourStrings));
    assertEquals(none(), five.get(fourStrings));

    assertEquals(charlieToCharles, two.set(fourStrings, some("Charles")));
    assertEquals(noCharlie, two.set(fourStrings, none()));

    // now verify out-of-range updates are no-ops
    assertEquals(fourStrings, negfive.set(fourStrings, some("Nobody")));
    assertEquals(fourStrings, five.set(fourStrings, some("Nobody")));
  }
}
