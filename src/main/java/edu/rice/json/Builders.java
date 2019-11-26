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

import static edu.rice.json.Value.JArray;
import static edu.rice.json.Value.JBoolean;
import static edu.rice.json.Value.JKeyValue;
import static edu.rice.json.Value.JNull;
import static edu.rice.json.Value.JNumber;
import static edu.rice.json.Value.JObject;
import static edu.rice.json.Value.JString;

import edu.rice.util.Strings;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

/**
 * These functions help you build JSON expressions in a "fluent" way. For example:
 *
 * <pre>
 * import static edu.rice.json.Builders.*;
 *
 * Value simpleObject =
 *     jobject(
 *         jpair("x", 1),
 *         jpair("y", 2));
 * </pre>
 *
 * <p>Once you've made a {@link Value} this way, you might then convert it to JSON using the {@link
 * Object#toString()} or {@link Value#toIndentedString()} methods, or you might compare it to
 * something that you've parsed, for equality/testing purposes.
 *
 * <p>Note about strings: there are two ways to build a jstring: from a "Java string" and from a
 * "JSON string". The former represents something you might have in a Java data structure somewhere.
 * The latter represents raw input such as you might have from a network or other source of JSON
 * data. The essential difference comes down to special characters like newline. If you expect the
 * input to have a literal backslash and a literal n, then use the JSON string version ({@link
 * #jstringEscaped(String)}. If you expect it to have a real newline character, then use the Java
 * version ({@link #jstring(String)}.
 *
 * <p>You'll also notice that there are several versions of the jpair() function. JSON defines an
 * object's key/value pairs as mapping from strings to arbitrary JSON values. For your convenience,
 * you can either use some sort of jstring directly (via jstringEscaped or jstring) or you can give
 * a literal Java string to the jpair() function. In this case, it uses {@link #jstring(String)}
 * internally.
 *
 * <p>Also, jpair() is heavily overloaded to take native Java types for either of its arguments
 * whenever possible. You can generally avoid needing jboolean(), jnumber(), or
 * jstring/jstringEscaped() as the second argument of jpair().
 *
 * <p>Of course, if what you have is a collection of raw JSON text, then you won't be using the
 * Builders at all. You would instead use one of the {@link Parser} methods like {@link
 * Parser#parseJsonObject(String)} or {@link Parser#parseJsonValue(String)}.
 *
 * <p><i>One last cool feature</i>: if you have a Java string in your code that contains a JSON
 * string and you want to statically verify that it's well-formed JSON and/or have IntelliJ do
 * syntax highlighting, just use the {@link Strings#json(String)} method. At runtime it does nothing
 * but pass its input to its output, but IntelliJ will do syntax hightlighting and checking on the
 * string within.
 */
public interface Builders {
  /** Fluent builder for {@link JObject}. */
  static JObject jobject(JKeyValue... values) {
    return jobject(List.of(values));
  }

  /** Fluent builder for {@link JObject}. */
  static JObject jobject(Seq<JKeyValue> values) {
    return JObject.fromSeq(values);
  }

  /** Fluent builder for {@link JObject}. */
  static JObject jobject(Map<String, Value> valueMap) {
    return JObject.fromMap(valueMap);
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method uses the {@link Tuple2}
   * class, used in our list and map classes (not to be confused with {@link JKeyValue}), and
   * assumes the key string is <b>unescaped</b> (i.e., that backslashed special characters have
   * already been converted). If you want to start with raw JSON strings, with escaped special
   * characters, then you need to use the {@link #jstringEscaped(String)} builder instead.
   */
  static JKeyValue jpair(Tuple2<String, Value> kv) {
    return JKeyValue.of(kv);
  }

  /** Fluent builder for {@link JKeyValue} pairs. */
  static JKeyValue jpair(JString key, Value value) {
    return JKeyValue.of(key, value);
  }

  /** Fluent builder for {@link JKeyValue} pairs. */
  static JKeyValue jpair(JString key, boolean bool) {
    return jpair(key, jboolean(bool));
  }

  /** Fluent builder for {@link JKeyValue} pairs. */
  static JKeyValue jpair(JString key, double number) {
    return jpair(key, jnumber(number));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the value string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If
   * you want to start with raw JSON strings, with escaped special characters, then you need to use
   * the {@link #jstringEscaped(String)} builder and the corresponding {@link #jpair(Value.JString,
   * Value)} method.
   */
  static JKeyValue jpair(JString key, String value) {
    return jpair(key, jstring(value));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If
   * you want to start with raw JSON strings, with escaped special characters, then you need to use
   * the {@link #jstringEscaped(String)} builder and the corresponding {@link #jpair(Value.JString,
   * Value)} method.
   */
  static JKeyValue jpair(String key, Value value) {
    return jpair(jstring(key), value);
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If
   * you want to start with raw JSON strings, with escaped special characters, then you need to use
   * the {@link #jstringEscaped(String)} builder and the corresponding {@link #jpair(Value.JString,
   * Value)} method.
   */
  static JKeyValue jpair(String key, boolean bool) {
    return jpair(jstring(key), jboolean(bool));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If
   * you want to start with raw JSON strings, with escaped special characters, then you need to use
   * the {@link #jstringEscaped(String)} builder and the corresponding {@link #jpair(Value.JString,
   * Value)} method.
   */
  static JKeyValue jpair(String key, double number) {
    return jpair(jstring(key), jnumber(number));
  }

  /**
   * Fluent builder for {@link JKeyValue} pairs; this convenience method assumes the key and value
   * strings are <b>unescaped</b> (i.e., that backslashed special characters have already been
   * converted). If you want to start with raw JSON strings, with escaped special characters, then
   * you need to use the {@link #jstringEscaped(String)} builder and the corresponding {@link
   * #jpair(Value.JString, Value)} method.
   */
  static JKeyValue jpair(String key, String value) {
    return jpair(jstring(key), jstring(value));
  }

  /** Fluent builder for {@link JArray}. */
  static JArray jarray(Value... values) {
    return jarray(List.of(values));
  }

  /** Fluent builder for {@link JArray}. */
  static JArray jarray(Seq<Value> values) {
    return JArray.fromSeq(values);
  }

  /**
   * Fluent builder for {@link JString}.
   *
   * <p>JSON has very particular rules about backslashes. This method takes as input a JSON string.
   * The JSON string might, for example, have a backslash "escape" followed by an 'n' character,
   * which you might prefer to have "unescaped" to a single newline character. If this is the
   * behavior you want, then this is the method for you.
   *
   * @param string a raw JSON string, with escapes, such as you might get from a text file on disk
   *     or from a network message
   * @return a JString corresponding to the input
   */
  static JString jstringEscaped(String string) {
    return JString.ofEscaped(string);
  }

  /**
   * Fluent builder for {@link JString}.
   *
   * <p>JSON has very particular rules about backslashes. This method takes as input a Java string.
   * The Java string is assumed to <b>already be unescaped</b>. For example, it might have actual
   * newline characters in it rather than a backslash followed by a 'n'. If that's what you have,
   * then this is the method for you.
   *
   * @param string a fully unescaped string, such as you might naturally deal with in Java
   * @return a JString corresponding to the input
   */
  static JString jstring(String string) {
    return JString.of(string);
  }

  /** Fluent builder for {@link JNumber}. */
  static JNumber jnumber(double number) {
    return JNumber.of(number);
  }

  /** Fluent builder for {@link JBoolean}. */
  static JBoolean jboolean(boolean bool) {
    return JBoolean.of(bool);
  }

  /** Fluent builder for {@link JNull}. */
  static JNull jnull() {
    return JNull.make();
  }
}
