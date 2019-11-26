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

import static edu.rice.stream.StreamHelpers.streamToList;
import static edu.rice.stream.StreamHelpers.streamToMap;
import static edu.rice.stream.StreamHelpers.streamToSet;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import java.security.MessageDigest;
import java.util.Random;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class StreamHelpersTest {
  @Test
  public void testListToIterator() {
    var list = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    var iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("Alice", iterator.next());
    assertEquals("Bob", iterator.next());
    assertEquals("Charlie", iterator.next());
    assertEquals("Dorothy", iterator.next());
    assertEquals("Eve", iterator.next());
    assertFalse(iterator.hasNext());

    // And, while we're at it, let's make sure that the iterator()
    // method works, using the older/weirder Java5 "enhanced for" syntax
    var result = new StringBuilder();
    for (var s : list) {
      result.append(s);
    }
    assertEquals("AliceBobCharlieDorothyEve", result.toString());
  }

  @Test
  public void testListLaziness() {
    var evenStream = Stream.iterate(0, x -> x + 2);
    var evenList = io.vavr.collection.Stream.iterate(0, x -> x + 2);

    // if this test never completes, then the resulting list from
    // streamToList was eager
    assertEquals(evenList.take(20), streamToList(evenStream, false).take(20));
  }

  @Test
  public void testSetToIterator() {
    var set = HashSet.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    var iterator = set.iterator();
    assertTrue(iterator.hasNext());
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertTrue(set.contains(iterator.next()));
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testMapToIterator() {
    var map = HashMap.of("Alice", 10, "Bob", 20);
    var iterator = map.iterator();

    var total = 0;
    assertTrue(iterator.hasNext());
    total += iterator.next()._2;
    total += iterator.next()._2;
    assertFalse(iterator.hasNext());
    assertEquals(30, total);
  }

  @Test
  public void testMapToStream() {
    final var simpleMap = HashMap.of("Alice", 10, "Bob", 20);

    // multiply the value by two: dumb but whatever
    UnaryOperator<Tuple2<String, Integer>> squareValOp = kv -> Tuple.of(kv._1, kv._2 * 2);

    var resultStreamMapScalar = streamToMap(simpleMap.toJavaStream().map(squareValOp), false);
    var resultStreamMapParallel = streamToMap(simpleMap.toJavaStream().map(squareValOp), true);
    var resultStreamMapParallelArray =
        streamToMap(simpleMap.toJavaParallelStream().map(squareValOp), true);
    var resultNormalMap = simpleMap.toList().toMap(squareValOp);

    assertEquals(resultNormalMap, resultStreamMapScalar);
    assertEquals(resultNormalMap, resultStreamMapParallel);
    assertEquals(resultNormalMap, resultStreamMapParallelArray);
  }

  @Test
  public void testListToStream() {
    var list = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    var result = streamToList(list.toJavaStream().map(String::toUpperCase), false);

    assertEquals(
        list.map(String::toUpperCase),
        result); // test that ordering is preserved for non-parallel streams
  }

  @Test
  public void testSetToStream() {
    var list = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    var upperCaseList = list.map(String::toUpperCase);
    var set = HashSet.ofAll(list);
    var allCapsSet = HashSet.ofAll(upperCaseList);

    var stream = set.toJavaStream().map(String::toUpperCase);
    var result = streamToSet(stream, false);

    assertEquals(allCapsSet, result); // set contents should be the same
  }

  @Test
  public void testParallelSpeedup() {
    System.out.println(
        "Parallel speedup tests! Number of available CPUs: "
            + Runtime.getRuntime().availableProcessors());

    // The command below hypothetically lets you tell Java to use more
    // threads; making it bigger doesn't help.  Making it smaller,
    // however, definitely slows things down.

    // System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "30");

    // Further reading on concurrency in Java
    // http://blog.takipi.com/forkjoin-framework-vs-parallel-streams-vs-executorservice-the-ultimate-benchmark/
    // http://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream

    // Warm up, to give HotSpot a chance to do some optimizations
    // before we print anything.
    testParallel(1000, 1, false);

    testParallel(1000, 1, true);
    testParallel(1000, 10, true);
    testParallel(1000, 100, true);
    testParallel(100, 1000, true);
  }

  private void testParallel(int listLength, int hashRepeats, boolean printThings) {
    // Engineering note: we need an operation that's amazingly slow,
    // such that when we run it in parallel we'll be observing the
    // speedup from parallel dispatch, rather than measuring the
    // overheads associated with streams.  So what then? Applying
    // SHA-256 and doing it over and over again? Yeah, that will be
    // slow. The "hashRepeats" variable lets us dial in the slowness.
    Function<Integer, Long> slowFunction =
        val -> {
          MessageDigest md;

          try {
            // We need a distinct instance of the message digest every
            // time because there's a ton of internal state that
            // mutates as the digest is doing its work. If we had a
            // common instance, we'd get weird results. These things
            // are *not* functional.

            md = MessageDigest.getInstance("SHA-256");
          } catch (Throwable throwable) {
            fail("can't find hash function!");
            return 0L;
          }

          var result = longToBytes(val);
          for (var i = 0; i < hashRepeats; i++) {
            result = md.digest(result);
          }
          return bytesToLong(result);
        };

    if (printThings) {
      System.out.printf(
          "=========== List vs. Stream performance (listLength = %d, hashRepeats = %d) =========== \n",
          listLength, hashRepeats);
    }

    // first, we'll insert random numbers; performance should be similar
    var random = new Random();
    var numberList =
        io.vavr.collection.Stream.continually(random::nextInt).take(listLength).toList();
    // one million random numbers, pre-computed so not to influence what we're benchmarking

    final var result1 =
        nanoBenchmarkVal(() -> numberList.toMap(x -> x, slowFunction))
            .apply(
                (time, result) -> {
                  if (printThings) {
                    System.out.printf(
                        " regular List      : %7.3f μs per hash\n", time / (listLength * 1000.0));
                  }
                  return result;
                });

    final var time2 =
        nanoBenchmarkVal(
                () ->
                    streamToMap(
                        numberList
                            .toJavaStream()
                            .map(val -> Tuple.of(val, slowFunction.apply(val))),
                        false))
            .apply(
                (time, result) -> {
                  if (printThings) {
                    System.out.printf(
                        " sequential stream : %7.3f μs per hash\n", time / (listLength * 1000.0));
                  }
                  assertEquals(result1, result);
                  return time;
                });

    final var time3 =
        nanoBenchmarkVal(
                () ->
                    streamToMap(
                        numberList
                            .toJavaParallelStream()
                            .map(val -> Tuple.of(val, slowFunction.apply(val))),
                        true))
            .apply(
                (time, result) -> {
                  if (printThings) {
                    System.out.printf(
                        " parallel stream   : %7.3f μs per hash\n", time / (listLength * 1000.0));
                  }
                  assertEquals(result1, result);
                  return time;
                });

    // and now, a version using ArrayList, which should be about as
    // fast as these things can go
    final var ignored =
        nanoBenchmarkVal(
                () ->
                    streamToMap(
                        numberList
                            .toJavaList()
                            .parallelStream()
                            .map(val -> Tuple.of(val, slowFunction.apply(val))),
                        true))
            .apply(
                (time, result) -> {
                  if (printThings) {
                    System.out.printf(
                        " par-array stream  : %7.3f μs per hash\n", time / (listLength * 1000.0));
                    System.out.printf(
                        "PARALLEL  STREAM SPEEDUP: %.3fx\n", (double) time2 / (double) time3);
                    System.out.printf(
                        "PAR-ARRAY STREAM SPEEDUP: %.3fx\n", (double) time2 / (double) time);
                  }
                  assertEquals(result1, result);
                  return 0; // ignored
                });
  }

  private static byte[] longToBytes(long val) {
    var result = new byte[8];
    var i = 0;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i++] = (byte) (val & 0xff);
    val >>= 8;
    result[i] = (byte) (val & 0xff);

    return result;
  }

  private static long bytesToLong(byte[] bytes) {
    var val = 0;
    // inefficient, but who cares?
    for (var i = 0; i < 8; i++) {
      val = val | (bytes[i] & 0xFF);
      val = val << 8;
    }
    return val;
  }
}
