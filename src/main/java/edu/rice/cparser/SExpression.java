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

import static edu.rice.cparser.ParserFunction.oneOf;
import static edu.rice.cparser.ParserFunction.parseTerminal;
import static edu.rice.cparser.ParserFunction.pfWithString;
import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jstring;
import static edu.rice.sexpr.Scanner.SexprPatterns.CLOSE;
import static edu.rice.sexpr.Scanner.SexprPatterns.OPEN;
import static edu.rice.sexpr.Scanner.SexprPatterns.WORD;
import static edu.rice.sexpr.Scanner.scanSexpr;

import edu.rice.regex.Token;
import edu.rice.sexpr.Scanner.SexprPatterns;
import edu.rice.sexpr.Value;
import edu.rice.util.Log;
import io.vavr.collection.Seq;

/**
 * SExpression implements the {@link Expression} interface to handle s-expression parsing. When
 * using {@link SExpression#parseSexpr(String)}, the results will be compatible with and composable
 * against other parsers over {@link SexprPatterns} tokens. If you wish to convert from an
 * SExpression to a {@link edu.rice.sexpr.Value}, use the {@link #toSexprValue()} method.
 */
public class SExpression implements Expression<SexprPatterns> {
  private static final String TAG = "SExpression";

  // Engineering notes: Compare this to edu.rice.sexpr.Parser, and the striking difference
  // is this parser is maybe 15 lines of code! The only complexity is that we want a new data class
  // for SExpression, distinct from the general-purpose Expressions used by our parser combinator.

  // Note as well how SExpression.toJson() shows how easy it can be to convert from one of
  // these things to a value type that wasn't specifically engineered to deal with the Expression
  // type of our parser-combinator.

  /**
   * Given a raw Java string, scan it into tokens and then parse it as an s-expression. See also,
   * {@link #PARSER} for a {@link ParserFunction}-composable parser.
   */
  public static Result<SexprPatterns> parseSexpr(String input) {
    // If the input has anything after the s-expression, this composition will treat it as an error.
    return PARSER.thenEnd().parse(scanSexpr(input));
  }

  // Engineering notes: The grammar for an s-expression is defined recursively. We can't just define
  // PARSER in terms of itself, nor can we define PARSER_RECURSIVE in terms of PARSER. Java requires
  // static members like this to have no forward references to other static members, but those rules
  // don't apply for static methods, which can refer to anything beforehand or afterward.

  // Note as well that we're associating a string with the internal parser to make sure that these
  // will behave nicely when toString() is called.
  private static final ParserFunction<SexprPatterns> PARSER_RECURSIVE =
      pfWithString(SExpression::parseInternal, () -> "SExpression");

  private static Result<SexprPatterns> parseInternal(Seq<Token<SexprPatterns>> tokens) {
    return PARSER.parse(tokens);
  }

  /**
   * This s-expression parser is fully composable with any other {@link ParserFunction}. If
   * successful, PARSER yields an {@link SExpression} inside its {@link Result.Ok}, providing easy
   * access to the list of {@link Expression} within.
   *
   * <p>If the input to PARSER has any tokens after the end of a valid s-expression, then the {@link
   * Result.Ok} will contain those remaining tokens. If you wish to require the input to be exactly
   * one s-expression with no remaining tokens, then you should use <code>PARSER.thenEnd()</code>.
   * This behavior is also available from {@link #parseSexpr(String)}.
   */
  public static final ParserFunction<SexprPatterns> PARSER =
      // Recall that an s-expression is an open-paren a list of word-or-sexps and then a
      // close-paren. Notice how easily this turns into code? The only messy part is the
      // need for recursion, which we handle above.

      oneOf(parseTerminal(WORD), PARSER_RECURSIVE)
          .list()
          .between(parseTerminal(OPEN), parseTerminal(CLOSE))

          // This last step lets us lift the exprPair() structure up to an SExpression with an
          // internal Stream, and it happens as part of the PARSER itself. Very convenient.
          .mapExpression(expr -> make(expr.asList()));

  // Data definition: an s-expression is defined as a list of expressions. An expression
  // can be a terminal (WORD) or an s-expression. The only difference between this definition
  // and the grammar above is that the grammar worries about OPEN and CLOSE parens, which we
  // are filtered out by the time we get here.
  private final Seq<Expression<SexprPatterns>> contents;

  private SExpression(Seq<Expression<SexprPatterns>> contents) {
    this.contents = contents;
  }

  /** Builder method for making an s-expression given a list of expressions. */
  public static SExpression make(Seq<Expression<SexprPatterns>> contents) {
    return new SExpression(contents);
  }

  /**
   * Given an SExpression from our parser, recursively converts it to a {@link
   * edu.rice.sexpr.Value}.
   */
  public Value toSexprValue() {
    return Value.sexpr(
        contents.map(
            expr ->
                expr.match(
                    terminal -> Value.word(terminal.token.data),
                    nothing -> Log.ethrow(TAG, "Unexpected token type: nothing"),
                    exprPair -> Log.ethrow(TAG, "Unexpected token type: expr-pair"),
                    otherExpr -> ((SExpression) otherExpr).toSexprValue())));

    // Engineering note: We're assuming that the expression is either
    // a terminal or it's been pre-processed into an SExpression, and
    // thus, there should be no possibility of finding other
    // expressions types. If that did happen, an exception will be
    // thrown. We've tested this via extensive unit testing with
    // QuickTheories, which has supported this assumption.
  }

  @Override
  public edu.rice.json.Value toJson() {
    // This will use JSON arrays, so the result will feel a lot like
    // an S-expression, but with JSON syntax.
    return jarray(
        contents.map(
            expr ->
                expr.match(
                    terminal -> jstring(terminal.token.data),
                    nothing -> Log.ethrow(TAG, "Unexpected token type: nothing"),
                    exprPair -> Log.ethrow(TAG, "Unexpected token type: expr-pair"),
                    Expression::toJson)));
  }

  @Override
  public Seq<Expression<SexprPatterns>> asList() {
    return contents;
  }

  @Override
  public String toString() {
    return contents.mkString("(", " ", ")");
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SExpression)) {
      return false;
    }

    var other = (SExpression) o;
    return other.contents.equals(contents);
  }

  @Override
  public int hashCode() {
    return contents.hashCode();
  }
}
