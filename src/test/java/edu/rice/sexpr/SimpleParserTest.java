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

package edu.rice.sexpr;

import static edu.rice.sexpr.Parser.parseSexpr;
import static edu.rice.sexpr.SexprGenerators.sexprs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;

import edu.rice.util.Log;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

public class SimpleParserTest {
  private static final String TAG = "SimpleParserTest";

  @Test
  public void basicTest() {
    var value = parseSexpr("(add (multiply 3 4) 5)").get();
    assertTrue(value instanceof Value.Sexpr);

    var sexpr = value.asSexpr();
    assertEquals("add", sexpr.getSeq().head().asWordString());
    assertEquals("5", sexpr.apply(2).asWord().get()); // apply(i) gets the ith entry in the list

    assertTrue(sexpr.get(0).isDefined());
    assertTrue(sexpr.get(1).isDefined());
    assertTrue(sexpr.get(2).isDefined());
    assertTrue(sexpr.get(3).isEmpty());

    assertEquals(
        List.of("multiply", "3", "4"),
        sexpr
            .apply(1)
            .asSexpr()
            .getSeq()
            // at this point, we have a list of Sexpr's that we know are really Words
            .map(Value::asWordString));

    // Now some tests to see whether parser accepts/rejects different constructs
    assertTrue(parseSexpr("(() ()()(()))").isDefined());
    assertTrue(parseSexpr("word").isDefined());

    Log.e(TAG, "testing the parser with erroneous inputs; expect errors in the log!");
    assertFalse(parseSexpr("").isDefined()); // empty string should *not* be a valid s-expression
    assertFalse(parseSexpr("   ").isDefined()); // nor should whitespace with nothing else
    assertFalse(parseSexpr("(() ()()(())) word").isDefined());
    assertFalse(parseSexpr("word (() ()()(()))").isDefined());
    assertFalse(parseSexpr("(() ()()(())").isDefined());
  }

  @Test
  public void parseRandomSexpressionsWorks() {
    qt().forAll(sexprs(5))
        .checkAssert(expr -> assertEquals(expr, parseSexpr(expr.toString()).get()));
  }
}
