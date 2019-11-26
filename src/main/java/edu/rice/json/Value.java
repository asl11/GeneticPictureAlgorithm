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

import static edu.rice.vavr.Sequences.seqUpdateOption;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.lens.Lens;
import edu.rice.lens.MonoLens;
import edu.rice.vavr.Maps;
import edu.rice.vavr.Sequences;
import io.vavr.Lazy;
import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.unbescape.json.JsonEscape;
import org.unbescape.json.JsonEscapeLevel;
import org.unbescape.json.JsonEscapeType;

/**
 * A JSON value can be many different things: a JSON object, array, string, number, boolean, or
 * null. This interface supports all these different JSON value types. If you're looking for a nice
 * fluent/pipelined way of dealing with typecasting, use the various "as" methods to do your casting
 * for you or check out the {@link #match(Function, Function, Function, Function, Function,
 * Function)} structural pattern matcher.
 *
 * <p>If you wish to parse a String into a JSON Value, check out the {@link Parser} class.
 *
 * <p>If you wish to build up a JSON structure from scratch, perhaps for pretty-printing or whatever
 * else, check out the {@link Builders} class, which has convenient functions for this purpose.
 *
 * <p>If you wish to treat a JSON structure as a database that you can query and update (in a
 * functional way, of course), then check out the {@link Operations} class, which offers many such
 * features.
 */
public interface Value {
  /**
   * General-purpose structural pattern matching on a JSON value, with one lambda per concrete type
   * of the Value. Typical usage:
   *
   * <pre>
   * Value val = ... ;
   * Option&lt;Whatever&gt; oresult = val.match(
   *     jObject -&gt; Option.none(),
   *     jArray -&gt; Option.none(),
   *     jstring -&gt; Option.some(jstring.something()),
   *     jNumber -&gt; Option.some(jNumber.somethingElse()),
   *     jBoolean -&gt; Option.none(),
   *     jNull -&gt; Option.none());
   * </pre>
   */
  default <Q> Q match(
      Function<? super JObject, ? extends Q> jobjectF,
      Function<? super JArray, ? extends Q> jarrayF,
      Function<? super JString, ? extends Q> jstringF,
      Function<? super JNumber, ? extends Q> jnumberF,
      Function<? super JBoolean, ? extends Q> jboolF,
      Function<? super JNull, ? extends Q> jnullF) {

    // Engineering note: Some purists would say that we shouldn't do
    // this cascading if-then-else here, but should instead dispatch
    // to a one-line method in each of the classes, just like we
    // do in Option and many other classes. If we did that here,
    // then each class would need this match method with the huge
    // type signature (repeated code... yuck!), even though the
    // implementations would be cleaner and all the instanceof checks
    // would disappear. But then all the code is spread out in six
    // different places.

    // The Java designers recognize that this situation isn't ideal,
    // and have a plan of action to deal with pattern matching like this.
    // See "Patterns in multi-way conditionals" at the link below, which
    // discusses this and many other issues.
    // https://cr.openjdk.java.net/~briangoetz/amber/pattern-match.html

    if (this instanceof JObject) {
      return jobjectF.apply(asJObject());
    } else if (this instanceof JArray) {
      return jarrayF.apply(asJArray());
    } else if (this instanceof JString) {
      return jstringF.apply(asJString());
    } else if (this instanceof JNumber) {
      return jnumberF.apply(asJNumber());
    } else if (this instanceof JBoolean) {
      return jboolF.apply(asJBoolean());
    } else if (this instanceof JNull) {
      return jnullF.apply(asJNull());
    } else {
      // This will never actually happen, but we're being suitably paranoid.
      throw new RuntimeException(
          "this should never happen! unexpected JSON value type: " + this.getClass().getName());
    }
  }

  /**
   * If you know that this Value is <b>definitely</b> a JObject, this method does the casting for
   * you in a nice, pipelined fashion. If the concrete type isn't JObject, this will throw a runtime
   * {@link ClassCastException}, so don't use this if you're not sure. If you think it <b>might</b>
   * be a JObject, then you might prefer the Option-variant, {@link #asJObjectOption()}. If you have
   * no idea what the concrete type might be and you need to handle each case differently, then use
   * {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JObject asJObject() {
    return (JObject) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JArray, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JArray, this will throw a runtime
   * {@link ClassCastException}, so don't use this if you're not sure. If you think it <b>might</b>
   * be a JArray, then you might prefer the Option-variant, {@link #asJArrayOption()}. If you have
   * no idea what the concrete type might be and you need to handle each case differently, then use
   * {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JArray asJArray() {
    return (JArray) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JString, this method does the casting for
   * you in a nice, pipelined fashion. If the concrete type isn't JString, this will throw a runtime
   * {@link ClassCastException}, so don't use this if you're not sure. If you think it <b>might</b>
   * be a JString, then you might prefer the Option-variant, {@link #asJStringOption()}. If you have
   * no idea what the concrete type might be and you need to handle each case differently, then use
   * {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JString asJString() {
    return (JString) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JNumber, this method does the casting for
   * you in a nice, pipelined fashion. If the concrete type isn't JNumber, this will throw a runtime
   * {@link ClassCastException}, so don't use this if you're not sure. If you think it <b>might</b>
   * be a JNumber, then you might prefer the Option-variant, {@link #asJNumberOption()}. If you have
   * no idea what the concrete type might be and you need to handle each case differently, then use
   * {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JNumber asJNumber() {
    return (JNumber) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JBoolean, this method does the casting for
   * you in a nice, pipelined fashion. If the concrete type isn't JBoolean, this will throw a
   * runtime {@link ClassCastException}, so don't use this if you're not sure. If you think it
   * <b>might</b> be a JBoolean, then you might prefer the Option-variant, {@link
   * #asJBooleanOption()}. If you have no idea what the concrete type might be and you need to
   * handle each case differently, then use {@link #match(Function, Function, Function, Function,
   * Function, Function)}.
   */
  default JBoolean asJBoolean() {
    return (JBoolean) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a JNull, this method does the casting for you
   * in a nice, pipelined fashion. If the concrete type isn't JNull, this will throw a runtime
   * {@link ClassCastException}, so don't use this if you're not sure. If you think it <b>might</b>
   * be a JNull, then you might prefer the Option-variant, {@link #asJNullOption()}. If you have no
   * idea what the concrete type might be and you need to handle each case differently, then use
   * {@link #match(Function, Function, Function, Function, Function, Function)}.
   */
  default JNull asJNull() {
    return (JNull) this;
  }

  /**
   * If you think this Value <b>might</b> be a JObject, but you're not sure, then this will return
   * an {@link Option#some(Object)} if it is, or {@link Option#none()} if the Value is some other
   * concrete type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function,
   * Function)}.
   */
  default Option<JObject> asJObjectOption() {
    // We could alternatively use VAVR's Option.when(), except we'll get warnings about
    // unchecked typecasting.
    return (this instanceof JObject) ? some((JObject) this) : none();
  }

  /**
   * If you think this Value <b>might</b> be a JArray, but you're not sure, then this will return an
   * {@link Option#some(Object)} if it is, or {@link Option#none()} if the Value is some other
   * concrete type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function,
   * Function)}.
   */
  default Option<JArray> asJArrayOption() {
    return (this instanceof JArray) ? some((JArray) this) : none();
  }

  /**
   * If you think this Value <b>might</b> be a JString, but you're not sure, then this will return
   * an {@link Option#some(Object)} if it is, or {@link Option#none()} if the Value is some other
   * concrete type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function,
   * Function)}.
   */
  default Option<JString> asJStringOption() {
    return (this instanceof JString) ? some((JString) this) : none();
  }

  /**
   * If you think this Value <b>might</b> be a JNumber, but you're not sure, then this will return
   * an {@link Option#some(Object)} if it is, or {@link Option#none()} if the Value is some other
   * concrete type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function,
   * Function)}.
   */
  default Option<JNumber> asJNumberOption() {
    return (this instanceof JNumber) ? some((JNumber) this) : none();
  }

  /**
   * If you think this Value <b>might</b> be a JBoolean, but you're not sure, then this will return
   * an {@link Option#some(Object)} if it is, or {@link Option#none()} if the Value is some other
   * concrete type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function,
   * Function)}.
   */
  default Option<JBoolean> asJBooleanOption() {
    return (this instanceof JBoolean) ? some((JBoolean) this) : none();
  }

  /**
   * If you think this Value <b>might</b> be a JNull, but you're not sure, then this will return an
   * {@link Option#some(Object)} if it is, or {@link Option#none()} if the Value is some other
   * concrete type. If you have no idea what the concrete type might be and you need to handle each
   * case differently, then use {@link #match(Function, Function, Function, Function, Function,
   * Function)}.
   */
  default Option<JNull> asJNullOption() {
    return (this instanceof JNull) ? some((JNull) this) : none();
  }

  /**
   * Convert to a nicely indented JSON string.
   *
   * <p>You may alternatively wish to use the classic {@link Object#toString()} method, which will
   * give you everything on the same line, without any pretty indentation.
   *
   * @see Object#toString()
   */
  default String toIndentedString() {
    return toIndentedString("");
  }

  /**
   * Convert to a nicely indented JSON string with the given String prefix applied before every
   * line.
   *
   * <p>You may alternatively wish to use the classic toString() method, which will give you
   * everything on the same line, without any pretty indentation.
   *
   * @see Object#toString()
   */
  default String toIndentedString(String prefix) {
    // Engineering note: This method is something that we expect some
    // JSON production types to override while others are perfectly
    // happy with this default implementation.

    // In an ideal world, this wouldn't be part of the outward-facing
    // interface of Value. We only really want it as part of our
    // recursive internal implementation.

    // Turns out, Java's  "private default method",
    // doesn't exactly solve our problem, because we want to
    // be able to override this for certain types but not others.
    // So, fine. We'll leave it in our public interface.

    // Hey, how come we're not prepending the prefix here? Because
    // we're only doing that when a newline is being introduced. If
    // you look downward to the JObject and JArray versions of this
    // function, you'll see more interesting behavior.

    // Deeper thoughts: What's going on here is called "pretty
    // printing", and it has a long and fun history. Here's a paper
    // from 1973 that talks about how the early LISP systems solved
    // the same problem:

    // http://www.softwarepreservation.org/projects/LISP/MIT/AIM-279-Goldstein-Pretty_Printing.pdf

    // If we tried to be more sophisticated about our pretty-printing,
    // as this paper suggests, it might look good for JSON arrays --
    // which are pretty much the same thing as LISP s-expressions, but
    // it wouldn't work nearly as well for JSON objects, where the
    // key/value tuples really should be stacked vertically. Since
    // JSON can mix it up with objects and arrays, we'd have to work a
    // lot harder to come up with a good pretty printer that used less
    // vertical space.

    return toString();
  }

  /**
   * Helper function to avoid saying <code>lens.get(Option.some(jvalue))</code>, when it would be
   * more convenient to just write <code>jvalue.lensGet(lens)</code>.
   *
   * @see Operations#lensPath(String)
   * @see Lens#get(Object)
   */
  default Option<Value> lensGet(MonoLens<Option<Value>> lens) {
    return lens.get(some(this));
  }

  /**
   * Helper function to avoid saying <code>lens.set(Option.some(jvalue), newValue)</code>, when it
   * would be more convenient to just write <code>jvalue.lensSet(lens, newValue)</code>.
   *
   * @see Operations#lensPath(String)
   * @see Lens#set(Object, Object)
   */
  default Option<Value> lensSet(MonoLens<Option<Value>> lens, Option<Value> newValue) {
    return lens.set(some(this), newValue);
  }

  /**
   * Helper function to avoid saying <code>lens.update(Option.some(jvalue), modFunc)</code>, when it
   * would be more convenient to just write <code>jvalue.lensUpdate(lens, modFunc)</code>.
   *
   * @see Operations#lensPath(String)
   * @see Lens#update(Object, UnaryOperator)
   */
  default Option<Value> lensUpdate(MonoLens<Option<Value>> lens, UnaryOperator<Option<Value>> mod) {
    return lens.update(some(this), mod);
  }

  /**
   * A JObject is a set of key/value tuples, where the keys are strings and the values can be any
   * JSON value, including another object.
   */
  class JObject implements Value, PartialFunction<String, Value> {
    // Engineering notes: There are several design decisions happening
    // here. Let's break them down.
    //
    // 1) Why separate mapVal and seqVal? Why lazy?
    //
    // A JSON Object is fundamentally a mapping from strings to JSON
    // values. So, it makes sense to have the mapVal. Most operations
    // will just delegate to that map. However, some operations will
    // want to have a list of key-value pairs. Recomputing that each
    // time could be expensive, especially since we want to return them
    // in a canonical order (sorted). Memoization makes sure that we
    // only compute these things once, and only if actually used.
    //
    // 2) Where did the JKeyValues go? Isn't an object just a collection of those?
    //
    // The parser certainly knows all about JKeyValue, but when we're
    // manipulating JSON data after it's been parsed, the rest of our
    // program is more likely to be written in terms of a vanilla Java
    // String rather than JString. That's why we have the map go from
    // String to Value, rather than JString to Value.  Similarly,
    // we're saving a list of Tuple2<String, Value>, rather than
    // JKeyValue (no type parameters).  The only time we're ever going
    // to need to go back to JKeyValue is as part of our toString()
    // and toIndentedString() methods. We certainly *could* keep
    // around a Supplier<Seq<JKeyValue>> just for those, but it's
    // not clear that this happens enough to justify the extra code
    // and effort.  While we're at it, we could even use a
    // Supplier<Integer> to hang onto the results of computing a
    // hashCode().  Again, it's unclear that the performance
    // optimization is worth the extra storage.

    private final Supplier<Map<String, Value>> mapVal;
    private final Supplier<Seq<Tuple2<String, Value>>> seqVal; // sorted by key when returned

    private JObject(Seq<JKeyValue> contents) {
      mapVal = Lazy.of(() -> contents.toMap(JKeyValue::toTuple));
      seqVal = Lazy.of(() -> mapVal.get().toList().sortBy(kv -> kv._1));
    }

    private JObject(Map<String, Value> contents) {
      mapVal = () -> contents;
      seqVal = Lazy.of(() -> contents.toList().sortBy(kv -> kv._1));
    }

    /**
     * Given a list of JKeyValue tuples, such as you might construct in a JSON parser, return a
     * JObject instance corresponding to those tuples.
     */
    public static JObject fromSeq(Seq<JKeyValue> contents) {
      return new JObject(contents);
    }

    /**
     * Given a mapping from Strings to JSON Values, return a JObject instance corresponding to that
     * mapping.
     */
    public static JObject fromMap(Map<String, Value> map) {
      return new JObject(map);
    }

    /**
     * Replace a given key/value tuple in this object with some other key/value tuple. Note that the
     * String argument here is <b>unescaped</b> (i.e., it's a normal Java string). The function maps
     * from the old value to the new value. If there's no value already there, {@link Option#none()}
     * will be passed as the argument to updateFunc. If updateFunc returns {@link Option#none()},
     * that will be treated as an instruction to remove the key/value (i.e., this is a useful way to
     * remove a key as well as to change it).
     */
    public Value updateKeyValue(String key, UnaryOperator<Option<Value>> updateFunc) {
      return new JObject(Maps.updateMap(getMap(), key, updateFunc));
    }

    /**
     * Looks for the key in the JObject. If it's there, the corresponding Value is returned in an
     * {@link Option#some(Object)}. If it's absent, {@link Option#none()} is returned. If you're
     * <b>absolutely sure</b> that a value should be present, then you might prefer to use {@link
     * #apply(String)}.
     */
    public Option<Value> get(String key) {
      return getMap().get(key);
    }

    @Override
    public Value apply(String key) {
      return get(key)
          .getOrElseThrow(
              () ->
                  new NoSuchElementException("key " + key + " isn't present in this JSON object"));
    }

    @Override
    public boolean isDefinedAt(String key) {
      return get(key).isDefined();
    }

    /**
     * Converts from the internal representation to a VAVR {@link Map} of the key/value pairs stored
     * within.
     */
    public Map<String, Value> getMap() {
      return mapVal.get();
    }

    /** Looks for any keys matching the predicate and returns the key-value tuples in a list. */
    public Seq<Tuple2<String, Value>> getMatching(Predicate<String> keyPredicate) {
      return getMap().filterKeys(keyPredicate).toList();
    }

    /**
     * Converts from the internal representation to a list of {@link Tuple2}, suitable for all sorts
     * of general-purpose processing.
     */
    public Seq<Tuple2<String, Value>> getContents() {
      return seqVal.get();
    }

    @Override
    public int hashCode() {
      return getMap().hashCode();
    }

    @Override
    public String toString() {
      return getContents().map(kv -> JKeyValue.of(kv).toString()).mkString("{", ", ", "}");
    }

    @Override
    public String toIndentedString(String prefix) {
      var nextPrefix = prefix + "  ";
      return getContents()
          .map(kv -> JKeyValue.of(kv).toIndentedString(nextPrefix))
          .mkString("{ ", ",\n" + nextPrefix, " }");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JObject)) {
        return false;
      }

      JObject jObject = (JObject) o;

      return getMap().equals(jObject.getMap());
    }
  }

  /**
   * These are used internally for JSON "Objects", which consist of a list of key/value pairs.
   *
   * @see JObject#getMap()
   */
  class JKeyValue {
    private final JString string;
    private final Value value;

    /** Don't use this constructor. Use one of the of() methods instead. */
    private JKeyValue(JString string, Value value) {
      this.string = string;
      this.value = value;
    }

    public static JKeyValue of(JString key, Value value) {
      return new JKeyValue(key, value);
    }

    public static JKeyValue of(Tuple2<String, Value> kv) {
      return of(JString.of(kv._1), kv._2);
    }

    @Override
    public String toString() {
      return string + ": " + value.toString();
    }

    /**
     * Sometimes you want to convert from JKeyValue, which is in the guts of the parser, to a {@link
     * Tuple2}, which works nicely with other VAVR classes. This helps you do that.
     */
    public Tuple2<String, Value> toTuple() {
      return Tuple.of(string.toUnescapedString(), value);
    }

    /**
     * Used as part of the recursive conversion of JSON objects to indented strings.
     *
     * @see Value#toIndentedString()
     */
    String toIndentedString(String prefix) {
      // we're recursively calling toIndentedLines(), but we're not
      // changing the prefix because if the value is an object or an
      // array, they'll make the prefix larger on their own; we'd like
      // the closing bracket to line up with the beginning of our
      // keyvalue tuple, and this is the way to do it.
      return string + ": " + value.toIndentedString(prefix);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JKeyValue)) {
        return false;
      }

      JKeyValue jKeyValue = (JKeyValue) o;

      return string.equals(jKeyValue.string) && value.equals(jKeyValue.value);
    }

    @Override
    public int hashCode() {
      return string.hashCode() * 31 + value.hashCode();
    }
  }

  /** This class represents an array of JSON values. */
  class JArray implements Value, PartialFunction<Integer, Value> {
    private final Seq<Value> values;
    private final int length;

    private final Supplier<Seq<Tuple2<Integer, Value>>> kvSeq;
    private final Supplier<Map<Integer, Value>> kvMap;

    private JArray(Seq<Value> values) {
      this.values = values;
      this.length = values.length();

      // we're pairing up sequential integers with the elements of the
      // array, so a JSON array like ["hello", "rice", "owls"] becomes
      // a map like: { 0 -> hello, 1 -> rice, 2 -> owls }
      this.kvSeq =
          Lazy.of(
              () -> getSeq().zipWith(Stream.range(0, length), (val, num) -> Tuple.of(num, val)));

      this.kvMap = Lazy.of(() -> getKVSeq().toMap(x -> x));
    }

    /** Given a list of values, returns a JArray corresponding to that list. */
    public static JArray fromSeq(Seq<Value> values) {
      return new JArray(values);
    }

    /** Returns a list of the Values in the JArray. */
    public Seq<Value> getSeq() {
      return values;
    }

    /**
     * Returns a list of {@link Tuple2} tuples from integers to Values, where the integers represent
     * the index of each value in the array. Useful if you want to pretend the JArray is something
     * like a JObject.
     */
    public Seq<Tuple2<Integer, Value>> getKVSeq() {
      return kvSeq.get();
    }

    /**
     * Returns a map from integers to Values, where the integers represent the index of each value
     * in the array. Useful if you want to pretend the JArray is something like a JObject.
     */
    public Map<Integer, Value> getMap() {
      return kvMap.get();
    }

    /** Returns the nth value in the array, if present. */
    public Option<Value> get(int i) {
      try {
        return Option.of(values.get(i));
      } catch (IndexOutOfBoundsException e) {
        return none();
      }
    }

    @Override
    public Value apply(Integer i) {
      return values.get(i);
    }

    @Override
    public boolean isDefinedAt(Integer i) {
      return get(i).isDefined();
    }

    /**
     * Update the nth value in the array, if present, with <code>updateFunc</code> applied to the
     * original value. If the replacement is {@link Option#none}, then the nth value of the array is
     * removed.
     *
     * @see Sequences#seqUpdateOption(Seq, int, Function)
     */
    public JArray update(int n, Function<Value, Option<? extends Value>> updateFunc) {
      return new JArray(seqUpdateOption(values, n, updateFunc));
    }

    @Override
    public String toString() {
      return values.mkString("[", ", ", "]");
    }

    @Override
    public String toIndentedString(String prefix) {
      String nextPrefix = prefix + "  ";
      return values
          .map(value -> value.toIndentedString(nextPrefix))
          .mkString("[ ", ",\n" + nextPrefix, " ]");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JArray)) {
        return false;
      }

      JArray jArray = (JArray) o;

      return values.equals(jArray.values);
    }

    @Override
    public int hashCode() {
      return values.hashCode();
    }
  }

  /**
   * This class represents JSON strings. Note there are two separate ways of reading out the results
   * here: {@link JString#toString()} and {@link JString#toUnescapedString()}. The former gives you
   * something suitable for JSON output, so it includes the surrounding quotation marks. The latter
   * gives you something suitable for use as a regular Java {@link String} in your program.
   * Backslash-escapes and other such things are converted to their proper meaning.
   */
  class JString implements Value, Comparable<JString> {
    /**
     * This string is in the JSON wire format, i.e., it potentially has escapes (backslashes) in it.
     * If you're starting from an "unescaped" String (i.e., a native Java string that might have
     * newlines or whatnot in it), then DON'T STORE THAT HERE.
     *
     * @see #ofEscaped(String)
     * @see #of(String)
     */
    private final String string;

    private JString(String string) {
      this.string = string;
    }

    private static String escapeJsonHelper(String input) {
      // Engineering note: The JSON escaping library we're using
      // (Unbescape: http://www.unbescape.org/) has a number of
      // different ways of escaping a JSON string. We're using this
      // library's least invasive mode of operation. The more extreme
      // options deal with environments where Unicode isn't properly
      // supported, simplifying everything back into old-school 7-bit
      // ASCII characters.  We don't need this in Comp215, but you can
      // imagine that there are many environments which predate
      // Unicode.

      // Fun times: In prior years, we used a different escaping
      // library (Apache Commons Text) which has similar features, but
      // when we wrote some QuickTheories tests, QT came up with a
      // string that had a "delete" control character (\u007f) in
      // it. The Apache library didn't correctly escape this
      // character, violating the JSON spec, and causing our JSON
      // scanner to fail when it read these strings. Unbescape passes
      // the same test. See edu.rice.json.ValueTheories for these
      // seemingly simple tests.

      return JsonEscape.escapeJson(
          input,
          JsonEscapeType.SINGLE_ESCAPE_CHARS_DEFAULT_TO_UHEXA,
          JsonEscapeLevel.LEVEL_1_BASIC_ESCAPE_SET);
    }

    /**
     * JSON has very particular rules about backslashes. This method takes as input a Java string.
     * The Java string is assumed to <b>already be unescaped</b>. For example, it might have actual
     * newline characters in it rather than a backslash followed by a 'n'. This is the method you'll
     * use when you're dealing with regular Java strings.
     *
     * @param string a fully unescaped string, such as you might naturally deal with in Java
     * @return a JString corresponding to the input
     */
    public static JString of(String string) {
      return new JString(escapeJsonHelper(string));
    }

    /**
     * JSON has very particular rules about backslashes. This method takes as input a JSON string,
     * as it might appear on disk or from a network transmission. This string is <b>escaped</b>. For
     * example, it won't have any newline characters but might instead have a blackslash followed by
     * an 'n'. Note that there should <b>not</b> be quotation marks around this string, even though
     * the JSON specification says that JSON strings have quotation marks. (We're assuming those
     * quotation marks have already been removed.)
     *
     * @param string a raw JSON string, such as you might get from a text file on disk or from a
     *     network packet
     * @return a JString corresponding to the input
     */
    public static JString ofEscaped(String string) {
      // Engineering note: this is subtle. We're unescaping the raw
      // JSON input, then escaping it back. Why?

      // - we want to *normalize* JSON strings (see, e.g., the
      //   backslash/slash issue)

      // - normalized strings ensure that the JString.equals() method
      //   can just delegate to String.equals()

      // - we've decided that the internal string needs to be in
      //   normalized and escaped format, but the input we're getting
      //   here is *not* normalized, so therefore,

      // - a round-trip through the unescape/escape routines will
      //   ensure that we have the same final representation, no
      //   matter the input format
      return of(JsonEscape.unescapeJson(string));
    }

    /**
     * Converts your JString to a regular Java {@link String}, suitable for all the ways you might
     * want to process any other Java string. You're guaranteed that any escaped characters will be
     * resolved, so for example, you'll have real newlines, as opposed to a blaskslash followed by
     * an 'n'.
     */
    public String toUnescapedString() {
      return JsonEscape.unescapeJson(string);
    }

    @Override
    public int compareTo(JString other) {
      return string.compareTo(other.string);
    }

    /**
     * Converts your JString to a <i>properly escaped</i> JSON String, including surrounding
     * quotation marks, suitable for transmission across a network or whatever else.
     */
    @Override
    public String toString() {
      return "\"" + string + "\"";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JString)) {
        return false;
      }

      JString jString = (JString) o;

      return string.equals(jString.string);
    }

    @Override
    public int hashCode() {
      return string.hashCode();
    }
  }

  /**
   * This class represents JSON numbers. While the external representation in a JSON string may be
   * an integer, the internal representation is always a Java double.
   */
  class JNumber implements Value, Comparable<JNumber> {
    private final double number;

    private JNumber(double number) {
      this.number = number;
    }

    /** Constructs a JNumber representation of a given number. */
    public static JNumber of(double number) {
      // Engineering note: There are some doubles that can't be
      // represented as JSON: NaN (not-a-number), +Infinity,
      // -Infinity. We have to deal with these. Probably the best way
      // is to refuse to let them in and throw an exception. The
      // alternative is to make this method return Option<JNumber>,
      // but then it's inconsistent with every other method
      // here. That's no fun.

      // Fun times: In our original implementation of this method, we
      // hadn't actually worried about this case, but a QuickTheories
      // test, creating random JSON values converting to string, then
      // back to JSON again, had errors in the scanner. The scanner
      // was failing some of its input! The culprit was that
      // QuickTheories tried to use +Infinity, which we originally
      // accepted here. See edu.rice.json.ValueTheories for these
      // seemingly simple tests.

      if (!Double.isFinite(number)) {
        throw new IllegalArgumentException("JSON can only represent finite numbers, not " + number);
      }

      return new JNumber(number);
    }

    /** Gives you back the number inside the JNumber as a double. */
    public double get() {
      return number;
    }

    @Override
    public String toString() {
      String tmp = Double.toString(number);
      if (tmp.endsWith(".0")) {
        return tmp.substring(0, tmp.length() - 2); // chop off the .0 for integers
      } else {
        return tmp;
      }
    }

    @Override
    public int compareTo(JNumber other) {
      // Java's standard library provides us with exactly what we
      // need, so we'll just delegate to it.
      return Double.compare(number, other.number);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JNumber)) {
        return false;
      }

      JNumber other = (JNumber) o;

      return other.number == this.number;
    }

    @Override
    public int hashCode() {
      // Java's standard library provides us with exactly what we
      // need, so we'll just delegate to it.
      return Double.hashCode(number);
    }
  }

  /** This class represents the JSON 'true' and 'false' values with two singleton objects. */
  class JBoolean implements Value {
    private final boolean bool;

    // Engineering note: we're using these two singletons here to
    // represent JBoolean true and false and that's it. That means
    // that pointer equality will be sufficient to determine JBoolean
    // equality.  This also means that we really need the constructor
    // to be private.

    private static final JBoolean TRUE = new JBoolean(true);
    private static final JBoolean FALSE = new JBoolean(false);

    private JBoolean(boolean bool) {
      this.bool = bool;
    }

    /** Given a boolean, return a JBoolean corresponding to its value. */
    public static JBoolean of(boolean bool) {
      return bool ? TRUE : FALSE;
    }

    public boolean get() {
      return bool;
    }

    @Override
    public String toString() {
      return Boolean.toString(bool);
    }

    @Override
    public boolean equals(Object o) {
      return this == o;
    }

    @Override
    public int hashCode() {
      return (bool ? 1 : 2);
    }
  }

  /**
   * This class represents the JSON 'null' value as a singleton object, distinct from Java's null.
   */
  class JNull implements Value {
    private static final JNull SINGLETON = new JNull();

    private JNull() {}

    /**
     * Creates a JSON "null" value, which is emphatically not the same thing as the Java null value.
     */
    public static JNull make() {
      return SINGLETON;
    }

    @Override
    public String toString() {
      return "null";
    }

    @Override
    public boolean equals(Object o) {
      return this == o;
    }

    @Override
    public int hashCode() {
      return 3;
    }
  }
}
