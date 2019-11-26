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

package edu.rice.vavr;

import static edu.rice.lens.Lens.lens;

import edu.rice.lens.Lens;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.TreeMap;
import io.vavr.control.Option;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Helper methods for working with VAVR {@link Map} types, including {@link HashMap} and {@link
 * TreeMap}.
 */
public interface Maps {
  /**
   * Replace a given key/value entry in this map with a new value. The given function maps from the
   * old value to the new value. If there's no value already there, {@link Option#none()} will be
   * passed as the argument to <code>updateFunc</code>. If <code>updateFunc
   * </code> returns {@link Option#none()}, that will be treated as an instruction to remove the
   * key/value (i.e., this method can be used to add or remove a key as well as to change its
   * value).
   */
  static <K, V> Map<K, V> updateMap(Map<K, V> map, K key, UnaryOperator<Option<V>> updateFunc) {
    var oldValue = map.get(key);
    var newValue = updateFunc.apply(oldValue);

    if (oldValue.isDefined() && newValue.isEmpty()) {
      return map.remove(key);
    } else if (newValue.isDefined()) {
      return map.put(key, newValue.get());
    } else {
      // wasn't there beforehand and won't be there afterward
      return map;
    }
  }

  /** Provides a lens from a map to the values within. */
  static <K, V> Lens<Map<K, V>, Option<V>> lensMap(K key) {
    return lens(map -> map.get(key), (map, ov) -> updateMap(map, key, ignored -> ov));
  }

  /**
   * Converts from a sequence to a map. Unlike the similar {@link Seq#toMap(Function, Function)}
   * method, this version provides a lambda, <code>valueMerger</code>, to merge multiple values that
   * might be associated with the same key. For example, when counting the frequency of terms in a
   * sequence of strings, you might use: <code>
   * mapFromSeq(stringSequence, k -&gt; k, v -&gt; 1, (v1, v2) -&gt; v1 + v2);</code>
   */
  static <T, K, V> Map<K, V> mapFromSeq(
      Seq<T> input,
      Function<T, K> keyExtractor,
      Function<T, V> valueExtractor,
      BinaryOperator<V> valueMerger) {
    return mapFromTuples(
        input.map(t -> Tuple.of(keyExtractor.apply(t), valueExtractor.apply(t))), valueMerger);
  }

  /**
   * Converts from a sequence of key-value tuples to a map. A lambda, <code>valueMerger</code>,
   * specifies how to merge values if the same key happens twice.
   */
  static <K, V> Map<K, V> mapFromTuples(Seq<Tuple2<K, V>> input, BinaryOperator<V> valueMerger) {
    return input.foldLeft(HashMap.empty(), (map, kv) -> map.put(kv, valueMerger));
  }
}
