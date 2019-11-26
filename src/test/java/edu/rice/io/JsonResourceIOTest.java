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

package edu.rice.io;

import static edu.rice.json.Operations.getPathOptionString;
import static edu.rice.json.Parser.parseJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Normally, we'd have to worry about these Option.get() or Try.get()
// calls failing, but in a testing situation, we *expect* them to
// succeed. If they fail, then JUnit makes the whole test fail, which
// is exactly what we want.
public class JsonResourceIOTest {
  @Test
  public void jsonTestDataFileHasExpectedContents() {
    // this test puts many different things together: our ability to
    // read resource files, then process them
    var jsonValue = parseJsonObject(Files.readResource("testdata.json").get()).get();

    assertEquals(
        "SGML",
        getPathOptionString(jsonValue, "glossary/GlossDiv/GlossList/GlossEntry/Acronym").get());
  }
}
