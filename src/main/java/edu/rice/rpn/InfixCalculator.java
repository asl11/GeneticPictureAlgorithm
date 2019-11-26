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

package edu.rice.rpn;

import static edu.rice.cparser.ParserFunction.exactlyOneOf;
import static edu.rice.cparser.ParserFunction.parseTerminal;
import static edu.rice.cparser.ParserFunction.pfWithString;
import static edu.rice.cparser.ParserFunction.sequence;
import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jnumber;
import static edu.rice.json.Builders.jstring;
import static edu.rice.regex.RegexScanner.scanPatterns;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.CLOSEPAREN;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.DIVIDE;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.EQUALS;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.FAIL;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.MINUS;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.NUMBER;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.OPENPAREN;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.PLUS;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.TIMES;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.VARIABLE;
import static edu.rice.rpn.InfixCalculator.InfixPatterns.WHITESPACE;
import static edu.rice.vavr.Maps.updateMap;
import static edu.rice.vavr.Options.optionLift;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.cparser.Expression;
import edu.rice.cparser.ParserFunction;
import edu.rice.cparser.Result;
import edu.rice.json.Scanner;
import edu.rice.json.Value;
import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import edu.rice.util.Log;
import edu.rice.util.TriFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.intellij.lang.annotations.Language;

/**
 * This is an infix calculator. You instantiate it ({@link #InfixCalculator()}) and thereafter call
 * {@link #calc(String)}. The input string is parsed and the resulting expression is evaluated and
 * returned as a string.
 */
public class InfixCalculator {
  private static final String TAG = "InfixCalculator";

  /** Construct an instance of an infix calculator. */
  public InfixCalculator() {}

  // Engineering note: This class contains a number of separate
  // sections. While we could have separated these into different Java
  // classes, none of them are really so large that it's really worth
  // the bother. It's nicer to have everything in once place. You
  // should sit down and read this code over before you try to write
  // anything using it. Make sure you understand each section.
  // Your work goes in the last section.

  // The code here builds on our own "parser-combinator" (edu.rice.cparser),
  // which you don't especially need to understand, but you'll see,
  // in section 3, how it make it straightforward to write the code
  // for a reasonably sophisticated grammar by transliterating from
  // the EBNF notation directly into library calls that assemble
  // the parser.

  ////////////////// SECTION 1: The Tokenizer //////////////////

  /**
   * Given a string, return a list of tokens. If there's a failure in the tokenizer, there will be a
   * FAIL token at the point of the failure. Also note that whitespace tokens are filtered out. We
   * don't care about them.
   */
  static Seq<Token<InfixPatterns>> scanInfixExpr(String input) {
    return scanPatterns(input, InfixPatterns.class, new Token<>(FAIL, ""))
        .filter(x -> x.type != WHITESPACE); // remove whitespace tokens; we don't care about them
  }

  enum InfixPatterns implements TokenPatterns {
    NUMBER(Scanner.jsonNumberPattern), // borrowed from our JSON scanner
    VARIABLE("[a-zA-Z][a-zA-Z0-9]*"), // must start with a letter before any numbers
    EQUALS("="),
    OPENPAREN("\\("),
    CLOSEPAREN("\\)"),
    PLUS("\\+"),
    MINUS("-"),
    TIMES("\\*"),
    DIVIDE("/"),
    WHITESPACE("\\s+"),
    FAIL(""); // if the matcher fails, you get one of these

    public final String pattern;

    InfixPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }

  // Engineering note: If we parse the string "1-2-3", we end up with
  // three tokens: {1, -2, -3} rather than what we'd prefer, which is
  // {1, -, 2, -, 3}. We have two choices for how to resolve this: in
  // the tokenizer, or in the grammar. We're taking the approach of
  // solving it in the tokenizer by recognizing when we have adjacent
  // pairs of numbers where the second one is a negative number.  In
  // that case, we'll insert a "+" token between those two numbers.
  // Afterward, the recursive-descent parser operates as normal.

  // Also, note that our lexical definition of a NUMBER, which is
  // borrowed from JSON's definition, allows for an optional minus
  // sign at the front, to indicate a negative number, but there's no
  // optional + sign. So "+3.0" is not a valid number, but "-3.0"
  // is. This means that we don't have to worry about "1+2+3" turning
  // into anything other than {1, +, 2, +, 3}.

  static Seq<Token<InfixPatterns>> fixAdjacentNumbers(Seq<Token<InfixPatterns>> tokens) {
    if (tokens.isEmpty() || tokens.tail().isEmpty()) {
      return tokens;
      // Nothing for us to do, so we'll return early.
      // Otherwise, we're guaranteed that tokens.tail(), below, will succeed.
    }

    final var addToken = new Token<>(PLUS, "+");
    final var zeroToken = new Token<>(NUMBER, "0");

    // We want to pair each token up with its successor, so we can
    // focus on the specific case of a number followed by a negative
    // number.  But what should we do with the end of the list? The
    // zipAll method lets us specify what to use when one list ends
    // before the other -- we'll just use a zero for the final
    // "successor".

    // And, of course, the cool part of using a flatMap here is that
    // it lets us return one or two tokens every time, concatenating
    // them together.
    final var tokenPairs = tokens.zipAll(tokens.tail(), zeroToken, zeroToken);

    return tokenPairs.flatMap(
        pair ->
            pair.apply(
                (token, successor) ->
                    token.type == NUMBER
                            && successor.type == NUMBER
                            && successor.data.startsWith("-")
                        ? List.of(token, addToken)
                        : List.of(token)));
  }

  ////////////////// SECTION 2: The Expression Data Structure //////////////////

  // Next up, we have four case classes for our parser's output. All
  // implement ArithExpression, which is compatible with other
  // Expression types within the parser-combinator, allowing us to
  // later on use the mapExpression operator to build a tree of these
  // ArithExpression objects. You don't really need to understand the
  // parser-combinator parts, but you do need to understand that there
  // are four different kinds of arithmetic expressions and how they
  // fit together.

  // Data definition: An ArithExpression is:
  // - a number
  // - a variable
  // - a binary expression (lhs, op, rhs ---- where lhs and rhs are also ArithExpressions)
  // - an assignment statement (variable = expr)
  // And an "op" is one of the allowed operator tokens (+, -, *, /)

  // Skip down below to the parser, and you'll see how we translate
  // this data definition into an EBNF grammar.

  /**
   * All infix expression classes implement this interface, which makes those expressions
   * interchangeable with our parser-combinator's {@link Expression} interface.
   */
  interface ArithExpression extends Expression<InfixPatterns> {
    /**
     * General-purpose pattern matching on arithmetic expressions with four lambdas. Note that the
     * name, <code>matchArith</code> is deliberately different from <code>match</code> to avoid
     * confusion with {@link Expression#match(Function, Function, Function, Function)}.
     *
     * @param numberFunc If the expression is a number, this lambda is invoked with a double.
     * @param variableFunc If the expression is a variable name, this lambda is invoked with the
     *     String.
     * @param binaryOpFunc If the expression is a binary operation, this lambda is invoked with lhs
     *     expression, operation-token, and rhs expression.
     * @param assignmentFunc If the expression is an assignment, this lambda is invoked with the
     *     String for the target variable, and the rhs expression.
     */
    default <R> R matchArith(
        Function<Double, R> numberFunc,
        Function<String, R> variableFunc,
        TriFunction<ArithExpression, Token<InfixPatterns>, ArithExpression, R> binaryOpFunc,
        BiFunction<String, ArithExpression, R> assignmentFunc) {
      if (this instanceof NumberArithExpression) {
        return numberFunc.apply(((NumberArithExpression) this).value);
      } else if (this instanceof VariableArithExpression) {
        return variableFunc.apply(((VariableArithExpression) this).name);
      } else if (this instanceof BinaryArithExpression) {
        BinaryArithExpression be = (BinaryArithExpression) this;
        return binaryOpFunc.apply(be.lhs, be.operation.token, be.rhs);
      } else if (this instanceof AssignmentArithExpression) {
        AssignmentArithExpression ae = (AssignmentArithExpression) this;
        return assignmentFunc.apply(ae.name, ae.rhs);
      } else {
        throw new RuntimeException("unexpected ArithExpression type: " + this.getClass().getName());
      }
    }
  }

  // Engineering note: we're also providing four maker-methods that
  // know about the results of the parser that they'll use to
  // construct an ArithExpression. Note the absence of error checking
  // here. If the parser succeeds, then we're guaranteed that the
  // resulting Expressions will satisfy the constraints we care
  // about. These are not public. They will not have to reject
  // incorrectly formed input.

  private static ArithExpression makeNumber(Expression<InfixPatterns> expr) {
    var value = expr.asTerminal().token;
    return new NumberArithExpression(value);
  }

  private static ArithExpression makeVariable(Expression<InfixPatterns> expr) {
    var value = expr.asTerminal().token;
    return new VariableArithExpression(value);
  }

  private static ArithExpression makeBinary(Expression<InfixPatterns> exprList) {
    var list = exprList.asList();
    var lhs = (ArithExpression) list.head();
    var rhsList = list.tail();

    // Engineering note: This fold is dealing with the result of
    // P_EXPR1 or P_EXPR2, which both encode left-associativity using
    // a standard hack that's required for a recursive-descent
    // parser. Both rules, when they match, return a list that starts
    // with the LHS and then has zero or more tuples of operation /
    // value (each tuple is a separate entry in the list).  By
    // processing with foldLeft, we're applying each operation,
    // left-to-right, which follows the left-associative behavior that
    // we want.

    // Note that if we had a *right associative* operation we wanted
    // to support, like exponentiation, then we'd need a different
    // makeBinary() method, and the grammar would get more complicated
    // as well.

    return rhsList.foldLeft(
        lhs,
        (lhsExprSoFar, newExpr) -> {
          var op = newExpr.asExprPair().exprA.asTerminal();
          var rhs = (ArithExpression) newExpr.asExprPair().exprB;
          return new BinaryArithExpression(op, lhsExprSoFar, rhs);
        });
  }

  private static ArithExpression makeAssignment(Expression<InfixPatterns> exprList) {
    // We're assuming a list with exactly three entries: lhs, =, and rhs.
    var list = exprList.asList();
    var lhs = (VariableArithExpression) list.head();
    var rhs = (ArithExpression) list.get(2);

    return new AssignmentArithExpression(lhs, rhs);
  }

  /** Represents a variable appearing in an arithmetic expression. */
  static class VariableArithExpression implements ArithExpression {
    public final String name;

    private VariableArithExpression(Token<InfixPatterns> t) {
      this.name = t.data;
    }

    @Override
    public Value toJson() {
      return jstring(name);
    }

    @Override
    public String toString() {
      return toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof VariableArithExpression)) {
        return false;
      } else {
        var oexpr = (VariableArithExpression) o;
        return oexpr.name.equals(name);
      }
    }

    @Override
    public int hashCode() {
      return toJson().hashCode();
    }
  }

  /** Represents a number appearing in an arithmetic expression. */
  static class NumberArithExpression implements ArithExpression {
    public final double value;

    private NumberArithExpression(Token<InfixPatterns> token) {
      this.value = Double.parseDouble(token.data);
    }

    @Override
    public Value toJson() {
      return jnumber(value);
    }

    @Override
    public String toString() {
      return toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof NumberArithExpression)) {
        return false;
      } else {
        var oexpr = (NumberArithExpression) o;
        return oexpr.value == value;
      }
    }

    @Override
    public int hashCode() {
      return toJson().hashCode();
    }
  }

  /** Represents an assignment statement (variable = expression). */
  static class AssignmentArithExpression implements ArithExpression {
    public final String name;
    public final ArithExpression rhs;

    private AssignmentArithExpression(VariableArithExpression lhs, ArithExpression rhs) {
      this.name = lhs.name;
      this.rhs = rhs;
    }

    @Override
    public Value toJson() {
      return jarray(jstring("assign"), jstring(name), rhs.toJson());
    }

    @Override
    public String toString() {
      return toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof AssignmentArithExpression)) {
        return false;
      } else {
        var oexpr = (AssignmentArithExpression) o;
        return oexpr.name.equals(name) && oexpr.rhs.equals(rhs);
      }
    }

    @Override
    public int hashCode() {
      return toJson().hashCode();
    }
  }

  /** Represents a binary (two-argument) arithmetic expression. */
  static class BinaryArithExpression implements ArithExpression {
    public final Terminal<InfixPatterns> operation;
    public final ArithExpression lhs;
    public final ArithExpression rhs;

    private BinaryArithExpression(
        Terminal<InfixPatterns> operation, ArithExpression lhs, ArithExpression rhs) {
      this.operation = operation;
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public Value toJson() {
      // LISP-style prefix notation
      return jarray(jstring(operation.token.data), lhs.toJson(), rhs.toJson());
    }

    @Override
    public String toString() {
      return toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BinaryArithExpression)) {
        return false;
      } else {
        var oexpr = (BinaryArithExpression) o;
        return oexpr.operation.equals(operation) && oexpr.lhs.equals(lhs) && oexpr.rhs.equals(rhs);
      }
    }

    @Override
    public int hashCode() {
      return toJson().hashCode();
    }
  }

  ////////////////// SECTION 3: The Recursive Descent Parser //////////////////

  // Finally, it's time for the actual recursive-descent parser, this
  // time using our parser-combinator library, rather than a
  // hand-built parser. Several things are worth noting.

  // - When we write EBNF grammars, there's no problem having the same
  // value on the lhs and rhs of a production.  Java doesn't allow
  // anything like this when defining static variables. Each must be
  // defined only in terms of what came beforehand, which forces us to
  // have the "expr1" function, which can be referenced from anywhere,
  // and thus we've got P_EXPR1_R (the recursive version) versus
  // P_EXPR1 (the not-quite-recursive version).

  // - We're using several features of our parser-combinator library,
  // including sequence() -- recognizing a sequence of tokens -- as
  // well as exactlyOneOf(), which evaluates every production and
  // fails unless only one production matches. We also use list(),
  // which matches zero or more instances of a given production, to
  // deal with the repeating multiplies/divides/adds/subtracts that
  // the grammar requires.

  // - We're using mapExpression() to convert from our general-purpose
  // Expression results to the specific ArithExpression classes above.

  // - And we're using the named() method to provide pretty-printers
  // for the grammar rules, which is handy when debugging them. (The
  // parser-combinator methods all try to attach helpful toString()
  // methods but the results for mapExpression, by default, are quite
  // verbose, and we can simplify things.)

  // - And lastly, the grammar itself had to be engineered to work
  // with our recursive-descent parser.  A "natural" recursive-
  // descent-compatible grammar for infix arithmetic would end up
  // being right-associative, which is incorrect for common usage: we
  // need left-associativity. That, in turn, requires the
  // transformation we did for EXPR1 and EXPR2, which take lists of
  // pairs of operations and expressions. If we were working with an
  // LR(1) table-generated parser, this wouldn't have been necessary.
  // You'll see those if you take Comp412 (or equivalent).

  // Here's an EBNF grammar of what we'll accept:

  // <MultiplyOrDivide> ::= ( "*" | "/" )
  // <AddOrSubtract> ::= ( "+" | "-" )
  // <Variable> ::= (an alphanumeric word)
  // <Number> ::= (a floating point number, same as a JSON number)
  // <Expr3> ::= "(" <Expr1> ")" | <Number> | <Variable>
  // <Expr2> ::= <Expr3> { <MultiplyOrDivide> <Expr3> }
  // <Expr1> ::= <Expr2> { <AddOrSubtract> <Expr2> }
  // <Assignment> ::= <Variable> "=" <Expr1>
  // <Goal> ::= <Expr1> <END-OF-INPUT> | <Assignment> <END-OF-INPUT>

  // The curly braces represent "zero or more instances of this
  // sequence", just like the Kleene-* in a regex.

  private static Result<InfixPatterns> expr1(Seq<Token<InfixPatterns>> tokens) {
    return P_EXPR1.parse(tokens);
  }

  private static final ParserFunction<InfixPatterns> P_EXPR1_R =
      pfWithString(InfixCalculator::expr1, () -> "EXPR1");

  // <MultiplyOrDivide> ::= ( "*" | "/" )
  private static final ParserFunction<InfixPatterns> MULTIPLY_OR_DIVIDE =
      parseTerminal(TIMES).or(parseTerminal(DIVIDE));

  // <AddOrSubtract> ::= ( "+" | "-" )
  private static final ParserFunction<InfixPatterns> ADD_OR_SUBTRACT =
      parseTerminal(PLUS).or(parseTerminal(MINUS));

  // <Number> ::= (a floating point number, same as a JSON number)
  private static final ParserFunction<InfixPatterns> P_NUMBER =
      parseTerminal(NUMBER).mapExpression(InfixCalculator::makeNumber).named(() -> "<NUMBER>");

  // <Variable> ::= (an alphanumeric word)
  private static final ParserFunction<InfixPatterns> P_VARIABLE =
      parseTerminal(VARIABLE)
          .mapExpression(InfixCalculator::makeVariable)
          .named(() -> "<VARIABLE>");

  // <Expr3> ::= "(" <Expr1> ")" | <Number> | <Variable>
  private static final ParserFunction<InfixPatterns> P_EXPR3 =
      exactlyOneOf(
          P_NUMBER,
          P_VARIABLE,
          P_EXPR1_R
              .between(parseTerminal(OPENPAREN), parseTerminal(CLOSEPAREN))
              .named(() -> "\"(\" <EXPR1> \")\""));

  // <Expr2> ::= <Expr3> { <MultiplyOrDivide> <Expr3> }
  private static final ParserFunction<InfixPatterns> P_EXPR2 =
      P_EXPR3
          .then(sequence(MULTIPLY_OR_DIVIDE, P_EXPR3).list())
          .mapExpression(InfixCalculator::makeBinary)
          .named(() -> "<EXPR3> { * or / <EXPR2> }");

  // <Expr1> ::= <Expr2> { <AddOrSubtract> <Expr2> }
  private static final ParserFunction<InfixPatterns> P_EXPR1 =
      P_EXPR2
          .then(sequence(ADD_OR_SUBTRACT, P_EXPR2).list())
          .mapExpression(InfixCalculator::makeBinary)
          .named(() -> "<EXPR2> { + or - <EXPR1> }");

  // <Assignment> ::= <Variable> "=" <Expr1>
  private static final ParserFunction<InfixPatterns> P_ASSIGNMENT =
      sequence(P_VARIABLE, parseTerminal(EQUALS), P_EXPR1)
          .mapExpression(InfixCalculator::makeAssignment)
          .named(() -> "<Variable> \"=\" <EXPR1>");

  // Exercise for the reader: if we left out the thenEnd() rules, then the grammar would be
  // ambiguous. Why?
  // <Goal> ::= <Expr1> <END-OF-INPUT> | <Assignment> <END-OF-INPUT>
  private static final ParserFunction<InfixPatterns> GOAL =
      exactlyOneOf(P_EXPR1.thenEnd(), P_ASSIGNMENT.thenEnd());

  /**
   * This function puts the whole scanner and parser together, returning an {@link ArithExpression}
   * inside an {@link Option}, so if the parser fails, you get back {@link Option#none()}.
   */
  Option<ArithExpression> parseTree(String input) {
    var tokens = scanInfixExpr(input);
    var fixedTokens = fixAdjacentNumbers(tokens);
    var parseTree = GOAL.parse(fixedTokens);

    return parseTree.match(okVal -> some((ArithExpression) okVal.production), errVal -> none());
  }

  ////////////////// SECTION 4: The Expression Evaluator //////////////////

  static Double binaryOpHelper(double lhs, Token<InfixPatterns> operation, double rhs) {
    switch (operation.type) {
      case PLUS:
        return lhs + rhs;
      case TIMES:
        return lhs * rhs;
      case DIVIDE:
        return lhs / rhs; // div by zero handled below
      case MINUS:
        return lhs - rhs;
      default:
        // this should never happen; the parser would have already rejected the input
        throw new RuntimeException("unexpected operation token in binary expression!");
    }
  }

  /**
   * Evaluates this arithmetic expression with the given environment, mapping variable names to
   * their values, and returns the result (<code>Option&lt;Double&gt;</code>) as well as a possibly
   * updated environment (<code>Map&lt;String, Double&gt;</code>) in a tuple. If the expression
   * evaluation fails, the resulting value will be {@link Option#none()}.
   */
  static Tuple2<Option<Double>, Map<String, Double>> eval(
      ArithExpression ae, Map<String, Double> environment) {
    // TODO: You're given an arithmetic expression, which might be a number,
    //   a variable, an expression, or an assignment. And, of course, it's
    //   a recursive structure, as described above. Evaluate the result of
    //   the expression in the given environment, returning the result and
    //   environment (which is either the same as the original environment,
    //   or maybe you had to update a variable).

    // FWIW, our implementation of this function is 13 lines here, plus a
    // 14 line helper function. If your solution is enormously longer,
    // you should think carefully about this. It's recursive. It has a
    // small number of cases. What happens for each one?

    // Also, don't forget that we have the ArithExpression.matchArith()
    // method, specifically to help you deal with each of these cases.

    return ae.matchArith(
        number -> Tuple.of(some(number), environment),
        variable -> Tuple.of(environment.get(variable), environment),
        (lhs, op, rhs) ->
            Tuple.of(
                optionLift(InfixCalculator::binaryOpHelper)
                    .apply(eval(lhs, environment)._1, some(op), eval(rhs, environment)._1)
                    .filter(x -> !x.isInfinite() && !x.isNaN()),
                environment),
        (variable, rhs) -> {
          var rhsE = eval(rhs, environment)._1;
          return Tuple.of(rhsE, updateMap(environment, variable, ignored -> rhsE));
        });
  }

  /**
   * State of every "variable" in the system. After each input from the user, the environment will
   * change if the input was an assignment statement. (Mutation!)
   */
  private Map<String, Double> environment = HashMap.empty();

  /**
   * Given some input from the user, run an infix calculation and return the result, maintaining an
   * internal environment for the state of all variable assignments.
   */
  public String calc(String input) {
    // TODO: You're given a string input. Parse it using parseTree(), then
    //   evaluate it, with your eval() function. You'll want to
    //   add an instance member variable to the InfixCalculator class
    //   to maintain the current environment, since some inputs will
    //   update variables in the environment if there are no errors.

    // In the event that the input wasn't a legitimate expression,
    // please return the string "Parse failure!". If something
    // failed while evaluating the expression, e.g., it referenced
    // a variable that was undefined in the environment, then
    // please return the string "Undefined". Otherwise, please
    // convert the resulting number to a string and return that.

    var oexpr = parseTree(input);
    var evalResult = oexpr.map(expr -> eval(expr, environment));
    var stringResult =
        evalResult.fold(
            () -> "Parse failure!",
            result -> {
              // Update the environment with changes from the
              // computation -- note that this is the only place in
              // the entire infix calculator that mutation is
              // necessary. More on this below.
              environment = result._2;
              return result._1.fold(() -> "Undefined", Object::toString);
            });

    Log.i(
        TAG,
        oexpr
            .map(expr -> expr.toJson().toString() + " --> " + stringResult)
            .getOrElse("Parse failure!"));

    //    Log.i(TAG, () -> "environment: " + environment.toString());
    return stringResult;
  }
}

// Advanced engineering note (YOU DON'T NEED TO UNDERSTAND THIS FOR
// COMP215) but it's really cool:

// In our expression evaluator, you'll see where we mutate the
// environment to save the result of the computation (e.g., assigning
// a value to a variable). That seems like something that we could
// have done functionally as well. Imagine that the calc() function
// took an extra argument -- the environment -- and returned a tuple
// just like we did with eval(), containing the resulting string as
// well as the resulting environment. Then you could imagine getting a
// list of input strings and then doing some sort of "fold" operation,
// yielding a new environment after each step: Totally functional!
// But we can't write our code like that because the list of inputs
// *doesn't exist yet*.

// You could imagine creating a VAVR Stream of the inputs, so it's
// lazy and all, but the problem is that the control flow seems to be
// going the wrong way. We want to *produce* the input strings in the
// web server and then *consume* them by mapping or whatever on the
// Stream, which feels like we want the web server to drive the
// computation. This just doesn't fit an API like Stream.iterate().
// What to do?

// Java9 added a "Flow" API:
// https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/Flow.html
// Notice how it's got concepts of "publishers" and "subscribers"?
// To adapt our design to work there, the web server would "publish"
// inputs that our calculator would "subscribe" to, generating results
// that are sent back to the browser.

// Here's a library called RxJava that takes this further:
// https://github.com/ReactiveX/RxJava

// Notice how RxJava has a bunch of operators that you've seen before
// like map() and flatMap()? You already understand these! RxJava and
// other libraries like this also have a variety of features to
// support parallel / concurrent execution. This is handy when you
// want to allow lots of requests coming in to the web server and
// potentially farm them out to different threads for computation.
// Parallelism and/or concurrency are generally beyond the scope of
// Comp215, but it's useful just to know that the functional ideas you
// learn here keep working when you really need them.
