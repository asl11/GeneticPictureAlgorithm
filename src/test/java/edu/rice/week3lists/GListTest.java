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

package edu.rice.week3lists;

import static edu.rice.week3lists.GList.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class GListTest {
  @Test
  public void emptyListBehavesNormally() {
    GList<String> emptyList = empty();
    assertTrue(emptyList.isEmpty());
    assertEquals(0, emptyList.length());
    assertFalse(emptyList.prepend("Hello").isEmpty());
    assertTrue(emptyList.prepend("Hello").tail().isEmpty());
  }

  @Test
  public void stackOrderingForAddAndTail() {
    GList<String> emptyList = empty();
    var helloRice = emptyList.prepend("Hello").prepend("Rice").prepend("Owls");
    assertEquals(3, helloRice.length());
    assertEquals("Owls", helloRice.head());
    assertEquals("Rice", helloRice.tail().head());
    assertEquals("Hello", helloRice.tail().tail().head());
  }

  @Test
  public void listOfEquivalentToPrepending() {
    var helloRice = GList.<String>empty().prepend("Hello").prepend("Rice").prepend("Owls");
    var helloRice2 = GList.of("Owls", "Rice", "Hello");

    assertEquals(helloRice, helloRice2);
  }

  @Test
  public void stackOrderingForToString() {
    GList<String> emptyList = empty();
    assertTrue(emptyList.isEmpty());
    assertEquals("GList()", emptyList.toString());

    var helloRice = GList.of("Hello", "Rice", "Owls");
    assertEquals("GList(Hello, Rice, Owls)", helloRice.toString());
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void headOfEmptyListFails() {
    GList<String> emptyList = empty();

    assertThrows(NoSuchElementException.class, emptyList::head);
  }

  @Test
  public void listContainsForPresentAndAbsentValues() {
    var helloRice = GList.of("Hello", "Rice", "Owls");

    assertTrue(helloRice.contains("Rice"));
    assertTrue(helloRice.contains("Hello"));
    assertTrue(helloRice.contains("Owls"));
    assertFalse(helloRice.contains("Aggies"));

    assertFalse(GList.<String>empty().contains("Anybody"));
  }

  @Test
  public void filteringEvenNumbersFromAListOfNumbers() {
    var numbers = GList.rangeClosed(1, 6);
    var evens = numbers.filter(x -> (x % 2) == 0);
    assertEquals(3, evens.length());

    var alsoEven = GList.rangeClosedBy(2, 6, 2);
    assertEquals(evens, alsoEven);
  }

  @Test
  public void nestedFilterExpressionsUsingLexicalScope() {
    var favoriteMajors = GList.of("COMP", "ELEC", "FWIS");

    var manyClasses = GList.of("COMP140", "COMP182", "XYZY100", "ELEC220", "POLI450");

    var favoriteClasses = manyClasses.filter(c -> !favoriteMajors.filter(c::startsWith).isEmpty());

    assertTrue(favoriteClasses.contains("ELEC220"));
    assertTrue(favoriteClasses.contains("COMP182"));
    assertTrue(favoriteClasses.contains("COMP140"));
    assertEquals(3, favoriteClasses.length());
  }

  @Test
  public void testFoldlRollingAccAvg() {
    var numbers = GList.of(8.0, 4.0, 0.0);

    assertEquals(
        3.0,
        numbers.foldLeft(numbers.isEmpty() ? 0.0 : numbers.head(), (x, y) -> (x + y) / 2),
        0.01);
  }
}
