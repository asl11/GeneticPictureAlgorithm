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

package edu.rice.cparser;

import static edu.rice.cparser.Expression.exprTerminal;
import static edu.rice.cparser.Result.resultError;
import static edu.rice.cparser.Result.resultOk;
import static edu.rice.cparser.SExpression.parseSexpr;
import static edu.rice.sexpr.Scanner.SexprPatterns.WORD;
import static edu.rice.sexpr.SexprGenerators.sexprs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quicktheories.QuickTheory.qt;

import edu.rice.sexpr.Scanner.SexprPatterns;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class SexprTest {
  final Expression<SexprPatterns> aliceTerminal = exprTerminal(WORD, "alice");
  final Expression<SexprPatterns> bobTerminal = exprTerminal(WORD, "bob");
  final Expression<SexprPatterns> charlieTerminal = exprTerminal(WORD, "charlie");

  @Test
  public void testBasics() {
    assertEquals(resultError(), parseSexpr("(alice bob charlie) alice"));
    assertEquals(resultError(), parseSexpr("alice"));
    assertEquals(resultError(), parseSexpr("("));
    assertEquals(resultError(), parseSexpr("(()"));
    assertEquals(resultError(), parseSexpr("())"));

    assertEquals(
        resultOk(
            SExpression.make(List.of(aliceTerminal, bobTerminal, charlieTerminal)), List.empty()),
        parseSexpr("(alice bob charlie)"));
  }

  @Test
  public void testDeeper() {
    assertEquals(
        resultOk(
            SExpression.make(
                List.of(aliceTerminal, bobTerminal, SExpression.make(List.of(charlieTerminal)))),
            List.empty()),
        parseSexpr("(alice bob (charlie))"));
  }

  /**
   * Tests several known good and bad strings, on both our hand-coded recursive descent parser and
   * also on our parser-combinator.
   */
  @TestFactory
  public Seq<DynamicTest> testToSexprValue() {
    // we're going to run the original s-expression parser and the new one and verify they return
    // the same answer
    var goodExprs = List.of("()", "(  alice)", "(alice    bob  ( charlie ) dorothy)");

    var badExprs = List.of("", "( ) alice)", "( ) alice", "(", "((()");

    return goodExprs
        .map(
            expr ->
                dynamicTest(
                    expr,
                    () -> {
                      assertTrue(parseSexpr(expr).isOk()); // parser succeeded
                      assertEquals(
                          edu.rice.sexpr.Parser.parseSexpr(expr).get(),
                          ((SExpression) parseSexpr(expr).asOk().production)
                              .toSexprValue()); // we got the same vale as the classical parser
                    }))
        .appendAll(
            badExprs.map(
                expr ->
                    dynamicTest(
                        expr.equals("") ? "empty-string" : expr,
                        () -> {
                          assertTrue(edu.rice.sexpr.Parser.parseSexpr(expr).isEmpty());
                          assertTrue(parseSexpr(expr).isError());
                        })));
  }

  @Test
  public void testRandomSexpressionsParseCorrectly() {
    qt().forAll(sexprs(5))
        .checkAssert(
            expr -> {
              assertTrue(parseSexpr(expr.toString()).isOk());
              assertEquals(
                  expr,
                  ((SExpression) parseSexpr(expr.toString()).asOk().production).toSexprValue());
              assertEquals(
                  expr,
                  edu.rice.sexpr.Parser.parseSexpr(expr.toString())
                      .get()); // verify for classical parser as well
            });
  }
}
