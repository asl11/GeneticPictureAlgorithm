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

package edu.rice.stream;

import static java.util.stream.Collectors.toList;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * These static methods are adapters to go from {@link Seq}, {@link Set}, and {@link Map} to Java's
 * {@link Stream}. Each static method here supports a <code>parallel</code> flag. If true, you get
 * more speed. If false, you get laziness (which could be faster in some circumstances).
 */
public interface StreamHelpers {
  /**
   * Given a {@link Stream}, converts it to an {@link Seq} of the same type. If <em>parallel</em> is
   * true, this will evaluate the computation in the stream eagerly, possibly using more RAM, but
   * also potentially running in parallel (assuming the underlying Stream supports parallel
   * evaluation). Conversely, if <em>parallel</em> is false, the Stream is evaluated lazily,
   * single-threaded, and potentially using less RAM.
   */
  static <T> Seq<T> streamToList(Stream<T> stream, boolean parallel) {
    if (parallel) {
      // The "collection" process will wind its way through the entire
      // stream and extract a java.util.ArrayList Experimentally, if
      // the stream is parallel(), this will run at full blast. We're
      // then wrapping that list and reading it out lazily.

      return io.vavr.collection.Stream.ofAll(stream.collect(toList()));
    } else {
      // Experimentally, asking a Stream for its iterator results in
      // lazy behavior. Computation doesn't happen on the stream until
      // you read its iterator, which happens in single-threaded
      // fashion, regardless of whether the Stream is parallel.
      return io.vavr.collection.Stream.ofAll(stream);
    }
  }

  /**
   * Specialized version of {@link #streamToList(Stream, boolean)}, for dealing with {@link
   * IntStream}.
   */
  static Seq<Integer> intStreamToList(IntStream stream, boolean parallel) {
    if (parallel) {
      // Engineering note: There's not really a huge amount of parallelism
      // to be had here, but experimentally I've seen 2 CPU cores busy, so
      // it's better than nothing.

      return io.vavr.collection.Stream.ofAll(stream.toArray());
    } else {
      // What's the exact opposite of parallel computation? Lazy computation --
      // which is sometimes exactly what you want.

      return streamToList(stream.boxed(), false);
    }
  }

  /**
   * Specialized version of {@link #streamToList(Stream, boolean)}, for dealing with {@link
   * DoubleStream}.
   */
  static Seq<Double> doubleStreamToList(DoubleStream stream, boolean parallel) {
    if (parallel) {
      return io.vavr.collection.Stream.ofAll(stream.toArray());
    } else {
      return streamToList(stream.boxed(), false);
    }
  }

  /**
   * Given a {@link Stream}, converts it to an {@link Set} of the same type.
   *
   * @param parallel If true, runs in parallel and will eagerly consume the whole stream. If false,
   *     runs lazily with no parallelism.
   */
  static <T extends Comparable<T>> Set<T> streamToSet(Stream<T> stream, boolean parallel) {
    return HashSet.ofAll(streamToList(stream, parallel));
  }

  /**
   * Given a {@link Stream} of {@link Tuple2} tuples, converts it to an {@link Map} of the same
   * type.
   *
   * @param parallel If true, runs in parallel and will eagerly consume the whole stream. If false,
   *     runs lazily with no parallelism.
   */
  static <K, V> Map<K, V> streamToMap(Stream<Tuple2<K, V>> stream, boolean parallel) {
    return streamToList(stream, parallel).toMap(x -> x);
  }
}
