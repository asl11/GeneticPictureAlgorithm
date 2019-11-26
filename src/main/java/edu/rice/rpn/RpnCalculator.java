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

import static edu.rice.json.Scanner.jsonNumberPattern;
import static edu.rice.regex.RegexScanner.scanPatterns;
import static edu.rice.rpn.RpnCalculator.OStack.error;
import static edu.rice.rpn.RpnCalculator.OStack.success;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.CLEAR;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.DIVIDE;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.DROP;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.DUP;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.EQUALS;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.FAIL;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.MINUS;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.NUMBER;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.PLUS;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.SWAP;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.TIMES;
import static edu.rice.rpn.RpnCalculator.RpnTokenPatterns.WHITESPACE;
import static edu.rice.vavr.Sequences.seqMatch;

import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import edu.rice.util.Log;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.intellij.lang.annotations.Language;

/**
 * The actual guts of the RPN Calculator go here.
 *
 * <p>Fun fact: "Polish notation" was indeed invented by a Polish mathematician
 * https://en.wikipedia.org/wiki/Jan_%C5%81ukasiewicz
 */
public class RpnCalculator {
  static final Map<RpnTokenPatterns, CalcOp> REGISTRY =
      HashMap.of(
          PLUS, RpnCalculator::add,
          TIMES, RpnCalculator::multiply,
          MINUS, RpnCalculator::subtract,
          DIVIDE, RpnCalculator::divide,
          DUP, RpnCalculator::dup,
          DROP, RpnCalculator::drop,
          SWAP, RpnCalculator::swap,
          EQUALS, RpnCalculator::noop,
          FAIL, RpnCalculator::fail,
          CLEAR, RpnCalculator::clear);

  @SuppressWarnings("unused")
  private static final String TAG = "RpnCalculator";

  private Seq<Double> rpnStack; // we'll be mutating this, so not final!

  /**
   * Construct an instance of an RPN calculator. This will maintain internal state that evolves as
   * its asked to do computation.
   */
  public RpnCalculator() {
    rpnStack = List.empty(); // initially empty
  }

  // And now, here are all the primitive functions that operate on
  // optional stacks. Obviously, if you're given an empty stack, there
  // isn't much to do but return another one, thus the flatMap
  // calls. This structure means that all of these functions are
  // UnaryOperators (and also CalcOp's), which makes it easy to fold
  // over them or otherwise use them with our functional lists.
  //
  // Fun fact: most of the methods here are package-scoped rather than
  // private-scope. This makes them accessible to unit tests from
  // within the same package, which we need to get test coverage.

  static OStack add(OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two
    // elements replaced with their sum.

    // if the input is error(), or if the stack doesn't have at least
    // two elements, the output will be error().

    return ostack.flatMap(
        stack ->
            seqMatch(
                stack,
                empty -> error(),
                (head, empty) -> error(),
                (head, second, tail) -> success(tail.prepend(head + second))));
  }

  static OStack multiply(OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two
    // elements replaced with their product.

    // if the input is error(), or if the stack doesn't have at least
    // two elements, the output will be error().

    return ostack.flatMap(
        stack ->
            seqMatch(
                stack,
                empty -> error(),
                (head, empty) -> error(),
                (head, second, tail) -> success(tail.prepend(head * second))));
  }

  static OStack subtract(OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two
    // elements replaced with the top element subtracted from the
    // element below it.

    // if the input is error(), or if the stack doesn't have at least
    // two elements, the output will be error().

    return ostack.flatMap(
        stack ->
            seqMatch(
                stack,
                empty -> error(),
                (head, empty) -> error(),
                (head, second, tail) ->
                    // ordering matters! "3 2 -" should yield 1
                    success(tail.prepend(second - head))));
  }

  static OStack divide(OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two
    // elements replaced with the second element divided by the top
    // element.

    // if the input is error(), or if the stack doesn't have at least
    // two elements, the output will be error().

    // in the event where division-by-zero would have occurred,
    // error() is returned to indicate the error.

    return ostack.flatMap(
        stack ->
            seqMatch(
                stack,
                empty -> error(),
                (head, empty) -> error(),

                // Engineering notes: division by zero is not defined
                // over the "real" numbers that you all know and
                // love. Technically, it *is* defined in IEEE floating
                // point arithmetic, which has three "special" values:
                // positive-Infinity, negative-Infinity, and
                // Not-a-Number. All floating-point arithmetic is
                // defined with these three special values in ways
                // that actually makes sense. That said, for the
                // purposes of this project, we'll make the executive
                // decision that we don't want to deal with "special"
                // numbers and we'll handle the error ourselves. We
                // had to worry about a similar issue with JSON
                // "numbers", which cannot be special either. See
                // Value.JNumber.of() for another engineering note.

                (denominator, numerator, tail) ->
                    (denominator == 0.0)
                        ? error()
                        : success(tail.prepend(numerator / denominator))));
  }

  static OStack dup(OStack ostack) {
    // required: a stack with one or more elements

    // returns: a new stack equal to the old one, with its top element
    // duplicated.

    // if the input is error(), or if the stack doesn't have at least
    // one element, the output will be error().

    return ostack.flatMap(
        stack -> seqMatch(stack, empty -> error(), (head, tail) -> success(stack.prepend(head))));
  }

  static OStack drop(OStack ostack) {
    // required: a stack with one or more elements

    // returns: a new stack equal to the old one, with its top element
    // removed.

    // if the input is error(), or if the stack doesn't have at least
    // one element, the output will be error().

    return ostack.flatMap(
        stack -> seqMatch(stack, empty -> error(), (head, tail) -> success(tail)));
  }

  static OStack swap(OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two
    // elements swapped.

    // if the input is error(), or if the stack doesn't have at least
    // two elements, the output will be error().

    return ostack.flatMap(
        stack ->
            seqMatch(
                stack,
                empty -> error(),
                (head, empty) -> error(),
                (head, second, tail) -> success(tail.prepend(head).prepend(second))));
  }

  static OStack noop(OStack ostack) {
    // required: no requirements.

    // returns: the output is the same as the input. (This is used by
    // the "=" command, which makes no changes to the stack at all,
    // but is useful for the user to type something and see what's
    // remaining on the top of the stack.)

    return ostack;
  }

  static OStack fail(@SuppressWarnings("UnusedParameters") OStack ostack) {
    // required: no requirements.

    // returns: error(), no matter what. (This is used when the user
    // enters an unknown command.)

    return error();
  }

  static OStack clear(@SuppressWarnings("UnusedParameters") OStack ostack) {
    // required: no requirements.

    // returns: an empty stack, no matter what.

    return of(); // empty stack
  }

  static CalcOp numberPusher(double number) {
    // This function is a little different from all the others: given
    // a double, it returns a CalcOp. (Yes, it's a function that
    // returns functions.) Like all the other CalcOps, the one you get
    // here takes an optional stack and returns one. This operation
    // happens to push the given number on top, and it does nothing if
    // the stack is an error().
    return ostack -> ostack.map(stack -> stack.prepend(number));
  }

  static Seq<Token<RpnTokenPatterns>> scan(String input) {
    return scanPatterns(input, RpnTokenPatterns.class, new Token<>(FAIL, ""))
        .filter(x -> x.type != WHITESPACE); // remove whitespace tokens; we don't care about them
  }

  /**
   * Given a token, return a function (from optional stacks to optional stacks) corresponding to
   * that token.
   */
  static CalcOp getFunction(Token<RpnTokenPatterns> token) {
    return token.type == NUMBER
        ? numberPusher(Double.parseDouble(token.data))
        : REGISTRY.get(token.type).getOrElse(RpnCalculator::fail);
  }

  /**
   * Given a list of tokens, return a function (from optional stacks to optional stacks)
   * corresponding to each token applied in sequence.
   */
  static CalcOp getFunction(Seq<Token<RpnTokenPatterns>> tokenList) {
    return tokenList.map(RpnCalculator::getFunction).foldLeft(CalcOp.identity(), CalcOp::andThen);
  }

  /**
   * Given a string of input, this will tokenize it then execute it on the internal RPN stack. The
   * value on the head of the stack is returned. If an error occurs, a suitable error message is
   * returned instead and the stack will have the same value as its initial state before calc() was
   * called.
   *
   * <p>Note: this method mutates the state of the class! If the input has no errors, then the
   * resulting stack state is saved internally. If the input has errors, then the stack state will
   * be unchanged.
   */
  public String calc(String input) {
    var tokenList = scan(input);

    var f = getFunction(tokenList);
    var resultStack = f.apply(success(rpnStack));

    if (!resultStack.isSuccess()) {
      return "Error!";
    }

    // we're now officially done with the prior stack, so we overwrite it
    rpnStack = resultStack.get();

    if (rpnStack.isEmpty()) {
      return "Empty stack";
    }

    return rpnStack.head().toString();
  }

  /**
   * Useful for testing: creates a "stack" with the elements present on it. The order of the
   * elements as passed here will be the order on the stack from top to bottom.
   */
  static OStack of(Double... elements) {
    return of(List.of(elements));
  }

  /**
   * Useful for testing: creates a "stack" with the elements present on it. The order of the
   * elements as passed here will be the order on the stack from top to bottom.
   */
  static OStack of(Seq<Double> elements) {
    return success(elements);
  }

  // Below is the tokenizing machinery.
  enum RpnTokenPatterns implements TokenPatterns {
    NUMBER(jsonNumberPattern), // same regex as we used for our JSON parser
    PLUS("\\+"),
    TIMES("\\*"),
    MINUS("-"),
    DIVIDE("/"),
    DUP("dup"),
    SWAP("swap"),
    DROP("drop"),
    CLEAR("clear"),
    EQUALS("="),
    WHITESPACE("\\s+"),
    FAIL(""); // if the matcher fails, you get one of these

    public final String pattern;

    RpnTokenPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }

  /**
   * We use this "option stack" class to represent the input and output type of all of our monadic
   * RPN functions, delegating to an internal Option&lt;Seq&lt;Double&gt;&gt;, so we can write
   * everything in RpnCalculator in terms of OStack rather than Option&lt;Seq&lt;Double&gt;&gt;.
   * This makes our code cleaner.
   *
   * <p>Note that this class is <b>not public</b>. It's package scope so we can see it in our unit
   * tests, but it's not meant to be externally visible.
   */
  static class OStack {
    // Engineering note: You might be wondering why we've built OStack
    // rather than just using Option<Seq<Double>> everywhere. The
    // short answer is that it's really tiring to have to write a
    // long type signature everywhere. It's cleaner and simpler to just
    // speak about OStack, and we've defined all the same method
    // names, so it feels the same and does the same thing.

    // But hey, Option is an *interface* type, so why don't we just
    // write OStack implements Option<Seq<Double>>?  That seems
    // attractive, as it would give us all the default methods from
    // there. Functionality for free!  Unfortunately, if you do that,
    // then the return types for map and flatMap would also need to be
    // Option<Seq<Double>> rather than OStack, getting rid of some
    // of the benefits of using OStack in the first place.

    // As described in the engineering note, below, for logWrap, what
    // we *really* want is a typedef, so we can just say that OStack
    // is a *shorthand* for Option<Seq<Double>>. Java has no such
    // feature now nor in its immediate future. Alas! So instead, we
    // take a delegation approach. Of course, if you really want the
    // Option<Seq<Double>>, then you can just call getOption().

    private static final OStack ERROR_SINGLETON = new OStack(Option.none());

    private final Option<Seq<Double>> ostack;

    private OStack(Option<Seq<Double>> ostack) {
      this.ostack = ostack;
    }

    public static OStack success(Seq<Double> stack) {
      return new OStack(Option.some(stack));
    }

    public static OStack error() {
      return ERROR_SINGLETON;
    }

    public boolean isSuccess() {
      return ostack.isDefined();
    }

    public boolean isError() {
      return ostack.isEmpty();
    }

    public Option<Seq<Double>> getOption() {
      return ostack;
    }

    public Seq<Double> get() {
      return ostack.get();
    }

    public <T> T match(
        Supplier<? extends T> errorFunc, Function<? super Seq<Double>, ? extends T> successFunc) {
      return ostack.fold(errorFunc, successFunc);
    }

    public OStack map(UnaryOperator<Seq<Double>> func) {
      return new OStack(ostack.map(func));
    }

    public OStack flatMap(Function<? super Seq<Double>, ? extends OStack> func) {
      return match(OStack::error, func);
    }

    @Override
    public String toString() {
      return ostack.toString();
    }

    @Override
    public int hashCode() {
      return ostack.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof OStack)) {
        return false;
      } else {
        return ((OStack) o).ostack.equals(ostack);
      }
    }
  }

  /**
   * Every RpnCalculator function, rather than being declared in terms of UnaryOperator or Function,
   * which gets a bit unwieldy, can instead be declared in terms of this much more straightforward
   * interface. This interface defines monadic composition ("andThen") and an identity operation,
   * allowing us to deal with calculator-operations as first-class objects, without having to apply
   * them to actual stacks of numbers until the last minute. This is sometimes called "points-free
   * notation" because we're writing code without mentioning the values ("points") that are
   * eventually operated upon.
   */
  @FunctionalInterface
  interface CalcOp {
    OStack apply(OStack input);

    // useful when trying to "cast" a lambda or method reference to a CalcOp
    static CalcOp of(CalcOp op) {
      return op;
    }

    static CalcOp identity() {
      return x -> x;
    }

    default CalcOp andThen(CalcOp op) {
      return stack -> op.apply(this.apply(stack));
    }

    /**
     * This is a front-end for Log.iwrap that turns any CalcOp into another CalcOp that dumps its
     * input and output to the log.
     *
     * @see Log#iwrap
     */
    default CalcOp logWrap(String tag) {
      // Engineering note: what's with the ::apply things below? When
      // you name the function like this, you're saying "Hey, Java
      // type system, try to coerce this particular function into
      // matching the functional type that's expected here for this
      // value."

      // We can't just return Log.iwrap(tag, this) because the return
      // type of Log.iwrap wants a Function as its input, not a
      // CalcOp, and similarly returns a Function, not a CalcOp. If
      // we had declared something like CalcOp extends
      // UnaryOperator<OStack>, which would make them somewhat
      // interchangeable, this would create other problems. Most
      // notably, we'd have type conflicts with all the default
      // methods on UnaryOperator, like "andThen", which expects to
      // return another UnaryOperator, not a CalcOp. (Or, we'd have to
      // have specialized names for them, like we do for
      // MonoLens.andThenMono.)

      // What we *really* want are "type aliases" or "typedefs", so we
      // could declare "CalcOp" to be a *shorthand* for
      // UnaryOperator<OStack>, and we could declare OStack as an alias
      // for Option<Seq<Double>>. Then the composition operators that
      // are already there in UnaryOperator would easily map back to CalcOp.

      // Java has no such feature, nor is it on the roadmap for later
      // Java versions, which forces us to do this messy stuff instead.

      // Fun experiment: try adding "extends Function<OStack, OStack>"
      // after "interface CalcOp". You'll see that some things simplify
      // and other things you wouldn't expect will break.
      return Log.iwrap(tag, this::apply)::apply;
    }
  }
}
