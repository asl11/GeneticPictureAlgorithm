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

package edu.rice.json;

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jnumber;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.json.Builders.jstring;
import static edu.rice.json.Operations.lensPath;
import static edu.rice.json.OperationsTest.BIG_JSON_QTY_PLUS_TEN;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import edu.rice.lens.MonoLens;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Week09", topic = "Lenses")
public class LensTest {
  final Value bigDB2 =
      jobject(
          jpair("itemCount", 2),
          jpair("subtotal", "$15.50"),
          jpair(
              "items",
              jarray(
                  jobject(
                      jpair("title", "The Big Book of Foo"),
                      jpair("description", "Bestselling book of Foo by A.N. Other"),
                      jpair("imageUrl", "/images/books/12345.gif"),
                      jpair("price", "$10.00"),
                      jpair("qty", 11)),
                  jobject(
                      jpair("title", "Javascript Pocket Reference"),
                      jpair(
                          "description",
                          "Handy pocket-sized reference for the Javascript language"),
                      jpair("imageUrl", "/images/books/56789.gif"),
                      jpair("price", "$5.50"),
                      jpair("qty", 10))))); // made it two smaller

  final Value missingDB =
      jobject(
          jpair("itemCount", 2),
          jpair("subtotal", "$15.50"),
          jpair(
              "items",
              jarray(
                  jobject(
                      jpair("title", "The Big Book of Foo"),
                      jpair("description", "Bestselling book of Foo by A.N. Other"),
                      jpair("imageUrl", "/images/books/12345.gif"),
                      jpair("price", "$10.00"),
                      jpair("qty", 11)))));

  final Value missingBook =
      jobject(
          jpair("title", "Javascript Pocket Reference"),
          jpair("description", "Handy pocket-sized reference for the Javascript language"),
          jpair("imageUrl", "/images/books/56789.gif"),
          jpair("price", "$5.50"),
          jpair("qty", 12));

  final Value missingBookPlus =
      jobject(
          jpair("title", "Javascript Pocket Reference"),
          jpair("description", "Handy pocket-sized reference for the Javascript language"),
          jpair("imageUrl", "/images/books/56789.gif"),
          jpair("price", "$5.50"),
          jpair("edition", "limited platinum"),
          jpair("qty", 1));

  final MonoLens<Option<Value>> pocketRefQtyLens = lensPath("items/1/qty");

  @Test
  @Grade(project = "Week09", topic = "Lenses", points = 0.6)
  public void basicGetterTest() {
    // First, we'll test the getter functionality: note that value.lensGet(lens) is
    // just a convenience function returns lens.get(Option.some(value)). Also,
    // since these lenses take and return Option<Value>, we're calling .get() at
    // the end to convert to a Value() for testing. If the Option was none rather
    // than some, these get() calls would fail and the unit test would fail.

    assertEquals(jstring("$5.50"), missingBook.lensGet(lensPath("price")).get());
    assertEquals(jnumber(12), BIG_JSON_QTY_PLUS_TEN.lensGet(pocketRefQtyLens).get());
    assertEquals(jnumber(10), bigDB2.lensGet(pocketRefQtyLens).get());
    assertEquals(none(), missingDB.lensGet(pocketRefQtyLens));
  }

  @Test
  @Grade(project = "Week09", topic = "Lenses", points = 0.6)
  public void basicSetterTest() {
    // Verify that we can change something that's already there
    assertEquals(bigDB2, BIG_JSON_QTY_PLUS_TEN.lensSet(pocketRefQtyLens, some(jnumber(10))).get());
  }

  @Test
  @Grade(project = "Week09", topic = "Lenses", points = 0.6)
  public void setterTestObject() {
    // One of the cool parts of our Lens<Option<Value>, Option<Value>> structure is that
    // we can have the setter return Option.none() as an indication that we want to delete
    // the value. Very nice.
    assertEquals(
        missingBook,
        missingBookPlus
            .lensSet(lensPath("edition"), none())
            .get() // delete the "edition" key/value pair
            .lensSet(lensPath("qty"), some(jnumber(12)))
            .get()); // edit the "qty" key/value pair
  }

  @Test
  @Grade(project = "Week09", topic = "Lenses", points = 0.6)
  public void setterDeleteTestArray() {
    // JSON arrays are a bit trickier to handle for edits, so we'll have a separate test.
    assertEquals(missingDB, BIG_JSON_QTY_PLUS_TEN.lensSet(lensPath("items/1"), none()).get());
  }

  final Value names =
      jarray(
          jstring("Alice"), jstring("Bob"), jstring("Charlie"), jstring("Dorothy"), jstring("Eve"));

  final Value namesNoCharlie =
      jarray(jstring("Alice"), jstring("Bob"), jstring("Dorothy"), jstring("Eve"));

  final Value namesCharles =
      jarray(
          jstring("Alice"), jstring("Bob"), jstring("Charles"), jstring("Dorothy"), jstring("Eve"));

  @Test
  @Grade(project = "Week09", topic = "Lenses", points = 0.6)
  public void setterEditTestArray() {
    // edit an array entry
    assertEquals(namesCharles, names.lensSet(lensPath("2"), some(jstring("Charles"))).get());

    // remove an entry from the array
    assertEquals(namesNoCharlie, names.lensSet(lensPath("2"), none()).get());
  }
}
