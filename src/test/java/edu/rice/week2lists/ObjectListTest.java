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

package edu.rice.week2lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class ObjectListTest {
  @Test
  public void testBasics() {
    var emptyList = ObjectList.empty();
    assertTrue(emptyList.isEmpty());
    assertEquals(0, emptyList.length());
    assertFalse(emptyList.prepend("Hello").isEmpty());
    assertTrue(emptyList.prepend("Hello").tail().isEmpty());

    var helloRice = emptyList.prepend("Hello").prepend("Rice").prepend("Owls");
    assertEquals(3, helloRice.length());
    assertEquals("Owls", helloRice.head());
    assertEquals("Rice", helloRice.tail().head());
    assertEquals("Hello", helloRice.tail().tail().head());
  }

  @Test
  public void testToString() {
    var emptyList = ObjectList.empty();
    assertEquals("ObjectList()", emptyList.toString());

    var helloRice = emptyList.prepend("Hello").prepend("Rice").prepend("Owls");
    assertEquals("ObjectList(Owls, Rice, Hello)", helloRice.toString());
  }

  @Test
  public void testEmptyHead() {
    var emptyList = ObjectList.empty();

    assertThrows(
        NoSuchElementException.class,
        () -> {
          String noHead = (String) emptyList.head();
        });
  }

  @Test
  public void testContains() {
    var emptyList = ObjectList.empty();
    var helloRice = emptyList.prepend("Hello").prepend("Rice").prepend("Owls");

    assertTrue(helloRice.contains("Rice"));
    assertTrue(helloRice.contains("Hello"));
    assertTrue(helloRice.contains("Owls"));
    assertFalse(helloRice.contains("Aggies"));

    assertFalse(emptyList.contains("Anybody"));
  }
}
