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

package edu.rice.week13newspaper;

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.io.Files;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

public class DBTest {
  @Test
  public void testBuildAuthorDB() {
    var jObject =
        jobject(
            jpair(
                "authors",
                jarray(
                    jobject(jpair("name", "Alice"), jpair("email", "alice@alice")),
                    jobject(jpair("name", "Bob"), jpair("email", "bob@bob")))));
    var oresult = DB.makeAuthorDB(jObject);

    assertTrue(oresult.isDefined());
    assertEquals(Option.some("Alice"), oresult.get().get("alice@alice").map(author -> author.name));
    assertEquals(Option.some("Bob"), oresult.get().get("bob@bob").map(author -> author.name));
  }

  @Test
  public void testLoad() {
    var thresherJson = Files.readResource("thresher.json").getOrElse("failed!");
    assertNotEquals("failed!", thresherJson);
    var oresult = DB.load(thresherJson);

    assertTrue(oresult.isDefined());
    assertEquals(3, oresult.get()._2.length()); // three articles
  }
}
