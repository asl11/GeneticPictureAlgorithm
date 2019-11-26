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

import static edu.rice.cparser.Expression.exprNothing;
import static edu.rice.cparser.Expression.exprPair;
import static edu.rice.cparser.Expression.exprTerminal;
import static edu.rice.cparser.ParserFunction.parseTerminal;
import static edu.rice.cparser.ParserFunction.sequence;
import static edu.rice.cparser.Result.resultError;
import static edu.rice.cparser.Result.resultOk;
import static edu.rice.sexpr.Scanner.SexprPatterns.CLOSE;
import static edu.rice.sexpr.Scanner.SexprPatterns.OPEN;
import static edu.rice.sexpr.Scanner.SexprPatterns.WORD;
import static edu.rice.sexpr.Scanner.scanSexpr;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.json.Scanner;
import edu.rice.json.Scanner.JsonPatterns;
import edu.rice.regex.Token;
import edu.rice.sexpr.Scanner.SexprPatterns;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;

/** Basic unit tests to exercise the parser combinator with a balanced-parens sort of language. */
public class ParserFunctionTest {
  final Seq<Token<SexprPatterns>> parens = scanSexpr("((()))");
  final Seq<Token<SexprPatterns>> wordAlice = scanSexpr("alice");
  final Seq<Token<SexprPatterns>> wordBob = scanSexpr("bob");
  final Seq<Token<SexprPatterns>> wordCharlie = scanSexpr("charlie");
  final Seq<Token<SexprPatterns>> wordsBobCharlie = scanSexpr("bob charlie");
  final Seq<Token<SexprPatterns>> words = scanSexpr("alice bob charlie");
  final ParserFunction<SexprPatterns> aliceParser = parseTerminal(WORD, "alice");
  final ParserFunction<SexprPatterns> bobParser = parseTerminal(WORD, "bob");
  final ParserFunction<SexprPatterns> wordParser = parseTerminal(WORD); // any WORD
  final Expression<SexprPatterns> aliceTerminal = exprTerminal(WORD, "alice");
  final Expression<SexprPatterns> bobTerminal = exprTerminal(WORD, "bob");
  final Expression<SexprPatterns> charlieTerminal = exprTerminal(WORD, "charlie");

  @Test
  public void composingWithThen() {
    final var aliceThenBobParser = aliceParser.then(bobParser);

    assertEquals(resultError(), aliceThenBobParser.parse(wordAlice));
    assertEquals(resultError(), aliceThenBobParser.parse(wordBob));
    assertEquals(
        resultOk(exprPair(aliceTerminal, bobTerminal), wordCharlie),
        aliceThenBobParser.parse(words));
  }

  @Test
  public void composingWithOr() {
    final var aliceOrBobParser = aliceParser.or(bobParser);

    assertEquals(resultError(), aliceOrBobParser.parse(wordCharlie));
    assertEquals(resultOk(aliceTerminal, List.empty()), aliceOrBobParser.parse(wordAlice));
    assertEquals(resultOk(bobTerminal, List.empty()), aliceOrBobParser.parse(wordBob));
    assertEquals(resultOk(aliceTerminal, wordsBobCharlie), aliceOrBobParser.parse(words));
  }

  @Test
  public void composingWithOrElse() {
    final var aliceOrBobParser = aliceParser.orElse(bobParser);
    // should behave the same as above, which isn't looking at the case where both
    // parsers accept their input.

    assertEquals(resultError(), aliceOrBobParser.parse(wordCharlie));
    assertEquals(resultOk(aliceTerminal, List.empty()), aliceOrBobParser.parse(wordAlice));
    assertEquals(resultOk(bobTerminal, List.empty()), aliceOrBobParser.parse(wordBob));
    assertEquals(resultOk(aliceTerminal, wordsBobCharlie), aliceOrBobParser.parse(words));
  }

  @Test
  public void composingWithList() {
    final var words = scanSexpr("alice alice alice bob");
    final var manyAlice = aliceParser.list();

    assertEquals(resultOk(exprNothing(), wordsBobCharlie), manyAlice.parse(wordsBobCharlie));

    assertEquals(
        resultOk(exprPair(aliceTerminal, exprNothing()), List.empty()), manyAlice.parse(wordAlice));

    assertEquals(
        resultOk(
            exprPair(
                aliceTerminal, exprPair(aliceTerminal, exprPair(aliceTerminal, exprNothing()))),
            wordBob),
        manyAlice.parse(words));

    assertEquals(
        List.of(aliceTerminal, aliceTerminal, aliceTerminal),
        manyAlice.parse(words).match(ok -> ok.production.asList(), error -> List.empty()));

    assertEquals(
        resultOk(
            exprPair(
                exprPair(
                    aliceTerminal, exprPair(aliceTerminal, exprPair(aliceTerminal, exprNothing()))),
                bobTerminal),
            List.empty()),
        manyAlice.then(bobParser).parse(words));
  }

  final Seq<Token<JsonPatterns>> jsonTokens0 = Scanner.scanJson("[]");
  final Seq<Token<JsonPatterns>> jsonTokens1 = Scanner.scanJson("[1, 2, 3, 4]");

  final Expression<JsonPatterns> open = exprTerminal(JsonPatterns.OPENSQUARE, "[");
  final Expression<JsonPatterns> close = exprTerminal(JsonPatterns.CLOSESQUARE, "]");
  final Expression<JsonPatterns> one = exprTerminal(JsonPatterns.NUMBER, "1");
  final Expression<JsonPatterns> two = exprTerminal(JsonPatterns.NUMBER, "2");
  final Expression<JsonPatterns> three = exprTerminal(JsonPatterns.NUMBER, "3");
  final Expression<JsonPatterns> four = exprTerminal(JsonPatterns.NUMBER, "4");

  @Test
  public void compositingWithListsAndSeparators() {

    final var simpleParser =
        parseTerminal(JsonPatterns.OPENSQUARE)
            .then(
                parseTerminal(JsonPatterns.NUMBER).separatedList(parseTerminal(JsonPatterns.COMMA)))
            .then(parseTerminal(JsonPatterns.CLOSESQUARE));

    assertEquals(
        resultOk(exprPair(exprPair(open, exprNothing()), close), List.empty()),
        simpleParser.parse(jsonTokens0));

    // notice how the commas disappear? that's a helpful feature of the separatedList method

    assertEquals(
        resultOk(
            exprPair(
                exprPair(
                    open,
                    exprPair(one, exprPair(two, exprPair(three, exprPair(four, exprNothing()))))),
                close),
            List.empty()),
        simpleParser.parse(jsonTokens1));
  }

  @Test
  public void composingWithSequences() {
    // Parse tree will be different from above, but will have nice asList() behavior.
    final var sequenceParser =
        sequence(
            parseTerminal(JsonPatterns.OPENSQUARE),
            parseTerminal(JsonPatterns.NUMBER).separatedList(parseTerminal(JsonPatterns.COMMA)),
            parseTerminal(JsonPatterns.CLOSESQUARE));

    assertEquals(
        resultOk(exprPair(open, exprPair(exprNothing(), close)), List.empty()),
        sequenceParser.parse(jsonTokens0));

    var result1 = sequenceParser.parse(jsonTokens1);
    assertEquals(
        resultOk(
            exprPair(
                open,
                exprPair(
                    exprPair(one, exprPair(two, exprPair(three, exprPair(four, exprNothing())))),
                    close)),
            List.empty()),
        result1);

    assertEquals(
        List.of(one, two, three, four), result1.asOk().production.asList().get(1).asList());
  }

  @Test
  public void terminalsAreHandledCorrectly() {
    assertEquals(resultOk(aliceTerminal, List.empty()), aliceParser.parse(wordAlice));
    assertEquals(resultError(), aliceParser.parse(wordBob));
    assertEquals(resultOk(aliceTerminal, wordsBobCharlie), aliceParser.parse(words));
    assertEquals(resultError(), aliceParser.parse(parens));

    assertEquals(resultOk(aliceTerminal, List.empty()), wordParser.parse(wordAlice));
    assertEquals(resultOk(bobTerminal, List.empty()), wordParser.parse(wordBob));
    assertEquals(resultError(), wordParser.parse(parens));
  }

  @Test
  public void detectingTheEndOfInput() {
    assertEquals(resultOk(aliceTerminal, List.empty()), aliceParser.thenEnd().parse(wordAlice));
    assertEquals(resultError(), aliceParser.thenEnd().parse(words));
  }

  final Seq<Token<SexprPatterns>> deeperExpression = scanSexpr("(alice bob charlie)");
  final Expression<SexprPatterns> openParenTerminal = exprTerminal(OPEN, "(");
  final Expression<SexprPatterns> closeParenTerminal = exprTerminal(CLOSE, ")");
  final ParserFunction<SexprPatterns> openParenParser = parseTerminal(OPEN, "(");
  final ParserFunction<SexprPatterns> closeParenParser = parseTerminal(CLOSE, ")");

  @Test
  public void testParensAndList() {
    // Note how slightly different ways of writing the grammar can
    // yield two very different parse trees.  The first form is pretty
    // much what you want to be using, such that you end up with your
    // productions following the same structure as our lists, and then
    // the asList() method will work as desired.  See below.  The
    // third form is a more pleasant way to write the first form, and
    // the fourth form automatically ditches the parentheses since
    // you're not particularly interested in them anyway.

    final var openWordsCloseParser = openParenParser.then(wordParser.list().then(closeParenParser));
    final var openWordsCloseParser2 =
        openParenParser.then(wordParser.list()).then(closeParenParser);

    final var openWordsCloseParser3 =
        sequence(openParenParser, wordParser.list(), closeParenParser);
    final var openWordsCloseParser4 = wordParser.list().between(openParenParser, closeParenParser);

    final var result1 = openWordsCloseParser.parse(deeperExpression);

    assertEquals(
        resultOk(
            exprPair(
                openParenTerminal,
                exprPair(
                    exprPair(
                        aliceTerminal,
                        exprPair(bobTerminal, exprPair(charlieTerminal, exprNothing()))),
                    closeParenTerminal)),
            List.empty()),
        result1);

    assertEquals(result1, openWordsCloseParser3.parse(deeperExpression));

    // we're going to coerce this into list format, which should be a list with three elements
    final Seq<Expression<SexprPatterns>> asList =
        result1.match(ok -> ok.production.asList(), error -> List.empty());
    assertEquals(3, asList.length());
    assertEquals(openParenTerminal, asList.head());
    assertEquals(closeParenTerminal, asList.get(2));
    final var expectedTokens = List.of(aliceTerminal, bobTerminal, charlieTerminal);
    assertEquals(expectedTokens, asList.get(1).asList());

    // Notice how with result4 we don't have to ask for nth(1) as we
    // do above? That's the benefit of using the "between" operator.
    final var result4 = openWordsCloseParser4.parse(deeperExpression);
    assertEquals(
        expectedTokens, result4.match(ok -> ok.production.asList(), error -> List.empty()));

    // Note how this structure differs from how the first parser does it.
    assertEquals(
        resultOk(
            exprPair(
                exprPair(
                    openParenTerminal,
                    exprPair(
                        aliceTerminal,
                        exprPair(bobTerminal, exprPair(charlieTerminal, exprNothing())))),
                closeParenTerminal),
            List.empty()),
        openWordsCloseParser2.parse(deeperExpression));
  }
}
