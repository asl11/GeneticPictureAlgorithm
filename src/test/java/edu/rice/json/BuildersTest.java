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
import static edu.rice.json.Builders.jboolean;
import static edu.rice.json.Builders.jnull;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.json.Builders.jstring;
import static edu.rice.json.Builders.jstringEscaped;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

public class BuildersTest {
  @Test
  public void listBuilderAndVarargsBuilderSameResult() {
    assertEquals(
        jarray(jnull(), jboolean(true), jboolean(false)),
        jarray(List.of(jnull(), jboolean(true), jboolean(false))));
  }

  @Test
  public void jsonVsJavaStringSameResults() {
    assertEquals(jstring("simple"), jstringEscaped("simple"));
    assertEquals(jstring("simple\n"), jstringEscaped("simple\\n"));
    assertEquals(jpair("simple", true), jpair(jstringEscaped("simple"), true));
    assertEquals(jpair("sim\nple", true), jpair(jstringEscaped("sim\\nple"), true));
    assertEquals(
        jobject(jpair("sim\nple", true)), jobject(jpair(jstringEscaped("sim\\nple"), true)));
  }
}
