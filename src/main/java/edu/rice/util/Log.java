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

package edu.rice.util;

import static edu.rice.util.Strings.objectToEscapedString;
import static edu.rice.util.Strings.objectToString;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simplified version of the Android logging system (<a
 * href="http://developer.android.com/reference/android/util/Log.html">http://developer.android.com/reference/android/util/Log.html</a>)
 * that uses slf4j / <a href="https://logback.qos.ch/">Logback</a> as its backend.
 *
 * <p>Also notable: this code is extremely fast when a log level is disabled. Furthermore, you can
 * delay the computation of a log string by putting it in a lambda which supplies the string. The
 * lambda will only be called if the log level is enabled.
 *
 * <pre>
 * Log.i(TAG, "result of computation: " + result.toString()); // always computes result.toString()
 * Log.i(TAG, () -&gt; "result of computation: " + result.toString()); // more efficient when logging is disabled
 * </pre>
 *
 * <p>Also available is a string-formatting variant ({@link #iformat(String, String, Object...)} and
 * {@link #eformat(String, String, Object...)}) that acts like {@link
 * java.io.PrintStream#printf(String, Object...)} or {@link String#format(String, Object...)},
 * constructing the string to be logged only if the logging level is enabled.
 *
 * <p>There are two ways you can change the logging level. You can call {@link Log#setLogLevel(int)}
 * somewhere in your program, or you can edit the resources/logback.xml configuration, which also
 * allows you to turn on and off logging for any given tag.
 *
 * <p>See the logback configuration manual for details: <a
 * href="http://logback.qos.ch/manual/configuration.html">http://logback.qos.ch/manual/configuration.html</a>
 */
public class Log {
  private Log() {} // this class should never be instantiated

  // We need to maintain one "logger" per "tag". We keep all of that inside this loggerMap.
  private static final Map<String, Logger> loggerMap = new ConcurrentHashMap<>();

  /** logging level: everything goes into the log. */
  public static final int ALL = 1;
  /** logging level: only errors go into the log. */
  public static final int ERROR = 0;
  /** logging level: nothing is logged at all. */
  public static final int NOTHING = -1;

  private static final String TAG = "Log";
  private static int logLevel = ALL;

  static {
    i(TAG, "Comp215 log support ready!");

    var properties =
        List.of(
            "java.version",
            "java.vm.version",
            "java.runtime.name",
            "java.home",
            "java.vendor",
            "java.vm.name",
            "user.dir");

    properties.forEach(
        str -> iformat(TAG, "System property: %-17s -> %s", str, System.getProperty(str)));
  }

  private static Logger logger(String tag) {
    // Once we have a Logback logger for a tag, we don't want to make a new one, so we save
    // the old one. Java's HashMap supports exactly this sort of functionality via it's
    // computeIfAbsent method. In other words, we're *memoizing*, which we'll talk about more
    // later in the semester.
    return loggerMap.computeIfAbsent(tag, LoggerFactory::getLogger);
  }

  /**
   * Set the log level.
   *
   * @param level (one of Log.ALL, Log.ERROR, or Log.NOTHING)
   */
  public static void setLogLevel(int level) {
    if (level == ALL || level == ERROR || level == NOTHING) {
      logLevel = level;
    } else {
      throw new IllegalStateException("Unknown log level: " + level);
    }
  }

  /**
   * Many of the logging functions let you delay the computation of the log string, such that if
   * logging is turned off, then that computation will never need to happen. That means hiding the
   * computation inside a lambda. So far so good.
   *
   * <p>Normally, we'd just call msgFunc.get() to fetch the string behind the lambda, but what if
   * there's an exception generated in the process of returning that string? We don't want the Log
   * library to ever throw an exception. Solution? We quietly eat exceptions here and, when they do
   * occur, the ultimate log string will reflect that failure, but THE SHOW MUST GO ON!
   */
  private static String safeGet(Supplier<?> msgFunc) {
    try {
      return objectToString(msgFunc.get());
    } catch (Throwable throwable) {
      return String.format("Log string supplier failure!: %s", throwable);
    }
  }

  /**
   * Information logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   * @param th Throwable, exception, error, etc. to be included in the log
   */
  public static void i(String tag, Supplier<?> msgFunc, Throwable th) {
    // Engineering / performance note:
    //
    // This logging function and every other logging function tries to
    // bail out as early as possible, to avoid any unnecessary
    // computation if the logging level is disabled.
    //
    // There are actually two opportunities for us to detect when a
    // log event will never happen.  First, we can check the logLevel,
    // which is internal to edu.rice.util.Log. After that, Logback has
    // its own checking that it will do. We make both checks
    // explicitly here before calling safeGet() to extract the string
    // we're about to log.
    //
    // Elsewhere in Comp215, you shouldn't go to the level of trouble
    // that we do in edu.rice.util.Log, especially since it appears to
    // violate our "don't repeat yourself" principle, but since it's
    // our goal to make these functions outrageously cheap when
    // logging is disabled, we need to go through some extra hoops.

    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(safeGet(msgFunc), th);
      }
    }
  }

  /**
   * Information logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   */
  public static void i(String tag, Supplier<?> msgFunc) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(safeGet(msgFunc));
      }
    }
  }

  /**
   * Information logging. Logs the message.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg String or object to be logged
   */
  public static void i(String tag, Object msg) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(objectToString(msg));
      }
    }
  }

  /**
   * Information logging with string formatting. Uses the same {@link java.util.Formatter} syntax as
   * used in {@link String#format(String, Object...)} or {@link java.io.PrintStream#printf(String,
   * Object...)} for constructing the message to be logged.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg Formatting string to be logged
   * @param args Optional formatting arguments
   */
  @FormatMethod
  public static void iformat(String tag, @FormatString String msg, Object... args) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(String.format(msg, args));
      }
    }
  }

  /**
   * Error logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   */
  public static void e(String tag, Supplier<?> msgFunc) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(safeGet(msgFunc));
      }
    }
  }

  /**
   * Error logging. Logs the message.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg String or object to be logged
   */
  public static void e(String tag, Object msg) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(objectToString(msg));
      }
    }
  }

  /**
   * Error logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   * @param th Throwable, exception, error, etc. to be included in the log
   */
  public static void e(String tag, Supplier<?> msgFunc, Throwable th) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(safeGet(msgFunc), th);
      }
    }
  }

  /**
   * Error logging. Logs the message.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg String or object to be logged
   * @param th Throwable, exception, error, etc. to be included in the log
   */
  public static void e(String tag, Object msg, Throwable th) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(objectToString(msg), th);
      }
    }
  }

  /**
   * Error logging with string formatting. Uses the same {@link java.util.Formatter} syntax as used
   * in {@link String#format(String, Object...)} or {@link java.io.PrintStream#printf(String,
   * Object...)} for constructing the message to be logged.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg Formatting string to be logged
   * @param args Optional formatting arguments
   */
  @FormatMethod
  public static void eformat(String tag, @FormatString String msg, Object... args) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(String.format(msg, args));
      }
    }
  }

  /**
   * Error logging with string formatting. Uses the same {@link java.util.Formatter} syntax as used
   * in {@link String#format(String, Object...)} or {@link java.io.PrintStream#printf(String,
   * Object...)} for constructing the message to be logged. The error message is logged <b>and</b>
   * also included in a {@link RuntimeException} which is thrown.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg Formatting string to be logged
   * @param args Optional formatting arguments
   * @throws RuntimeException with the given message
   */
  @CanIgnoreReturnValue
  @FormatMethod
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <T> T ethrow(String tag, @FormatString String msg, Object... args) {
    // Engineering note: see that weird return type? We're working around
    // a weird problem, which is that Log.ethrow() doesn't actually return
    // anything -- it instead throws an exception. However, it needs to
    // be able to go anywhere that a value of any type is expected. The
    // hack to solve the problem is to say that it will return any possible
    // type (which is, of course, impossible), but the compiler doesn't mind
    // since it will look down to the last line and see that there's no need
    // for a return statement when an exception is being thrown.

    // ErrorProne *does* have a problem with this, since it sees the type
    // parameter being unused. That's why we're disabling that specific
    // warning here.

    // THAT SAID, there's one case where ethrow won't work: if you're trying
    // to return the result of ethrow() when the function returns a Java primitive
    // type (int, double, boolean, etc.). ethrow() will fit anywhere that a
    // Java object type can go, but not a primitive type. In such a case, then
    // you'll just have to have two separate statements: one to do the logging,
    // and one to throw the exception.

    // If you're reading this early in the semester, don't panic! We'll explain
    // this better as the semester goes along.

    // Also of note: there's no point having a lambda-variant of ethrow,
    // since we're *always* going to compute the string so we can put it
    // in the exception.
    var s = String.format(msg, args);
    e(tag, s);
    throw new RuntimeException(s);
  }

  /**
   * This higher-order function wraps / decorates an input function, such that every time the
   * resulting function is called, its input and output are logged (via Log.i) with the given tag.
   * If, for example, you wanted to log all the input/output pairs while mapping over a list of
   * strings, you might say something like:
   *
   * <pre>
   * val source = List.of("Hello", "World", "My", "Friends");
   * val result = source.map(Log.iwrap(TAG, x -&gt; x.length()));
   * </pre>
   *
   * <p>The result would still have the mapping, as usual (i.e., (5, 5, 2, 7)) while the logging
   * system would print:
   *
   * <pre>
   * "Hello" -&gt; 5
   * "World" -&gt; 5
   * "My" -&gt; 2
   * "Friends" -&gt; 7
   * </pre>
   *
   * <p>The internal values are converted to strings by calling their toString() methods, with
   * special handling for Strings.
   *
   * @see Strings#objectToEscapedString(Object)
   */
  public static <A, B> Function<A, B> iwrap(String tag, Function<A, B> func) {
    return input -> {
      var output = func.apply(input);
      i(
          tag,
          () ->
              String.format(
                  "%s -> %s", objectToEscapedString(input), objectToEscapedString(output)));
      return output;
    };
  }

  /**
   * This higher-order function wraps / decorates a binary input function, such that every time the
   * resulting function is called, its inputs and output are logged (via {@link Log#i(String,
   * Object)}) with the given tag. If, for example, you wanted to log all the input/output pairs
   * while folding a list of strings, you might write something like this:
   *
   * <pre>
   * val source = List.of("Hello", "World", "My", "Friends");
   * val result = source.foldLeft(Log.iwrap(TAG, (sum, elem) -&gt; sum + elem.length()), 0);
   * </pre>
   *
   * <p>The result would still have the fold's result, as usual (i.e., 19 which equals 5+5+2+7)
   * while the logging system would log the following lines:
   *
   * <pre>
   * (0, "Hello") -&gt; 5
   * (5, "World") -&gt; 10
   * (10, "My") -&gt; 12
   * (12, "Friends") -&gt; 19
   * </pre>
   *
   * <p>The internal values are converted to strings by calling their toString() methods, with
   * special handling for Strings.
   *
   * @see Strings#objectToEscapedString(Object)
   */
  public static <A, B, C> BiFunction<A, B, C> iwrap(String tag, BiFunction<A, B, C> func) {
    return (input1, input2) -> {
      var output = func.apply(input1, input2);
      i(
          tag,
          () ->
              String.format(
                  "(%s, %s) -> %s",
                  objectToEscapedString(input1),
                  objectToEscapedString(input2),
                  objectToEscapedString(output)));
      return output;
    };
  }
}
