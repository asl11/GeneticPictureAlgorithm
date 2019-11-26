package edu.rice.tree;

import static edu.rice.util.Performance.nanoBenchmarkVal;
import static edu.rice.vavr.Sequences.seqIsSorted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.week4queue.ListQueue;
import io.vavr.collection.List;
import io.vavr.collection.PriorityQueue;
import io.vavr.collection.Stream;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Random;
import org.junit.jupiter.api.Test;

@SuppressWarnings("JdkObsolete")
public class QueuePerformance {
  @Test
  public void testPerf() {
    final int queueSize = 1000000;
    final int fetches = 10000;
    var random = new Random();
    System.out.println("=========== Priority Queue Performance =========== ");

    // one million random numbers
    final var numberList = Stream.continually(random::nextInt).take(queueSize);

    // causes the random number generator to be called 1M times, so it's not part of the timing
    assertEquals(queueSize, numberList.length());

    final var resultListBH =
        nanoBenchmarkVal(
                () -> {
                  var priorityQueue = BinaryHeap.<Integer>empty(Comparator.naturalOrder());
                  numberList.forEach(priorityQueue::insert);
                  var result = Stream.continually(priorityQueue::getMin).take(fetches);
                  assertEquals(fetches, result.length());
                  return result;
                })
            .apply(
                (time, result) -> {
                  System.out.printf(
                      "Rice BinaryHeap  : %d inserts, %d fetches: %7.3f μs per insert\n",
                      queueSize, fetches, time / (1e3 * queueSize));

                  assertEquals(fetches, result.length());
                  assertTrue(seqIsSorted(result));

                  return result;
                });

    final var ignoredResult =
        nanoBenchmarkVal(
                () -> {
                  var priorityQueue2 = new java.util.PriorityQueue<Integer>();
                  numberList.forEach(priorityQueue2::add);
                  var result = Stream.continually(priorityQueue2::poll).take(fetches);
                  assertEquals(fetches, result.length());
                  return result;
                })
            .apply(
                (time, result) -> {
                  System.out.printf(
                      "j.u.PriorityQueue: %d inserts, %d fetches: %7.3f μs per insert\n",
                      queueSize, fetches, time / (1e3 * queueSize));
                  assertEquals(fetches, result.length());
                  assertTrue(seqIsSorted(result));
                  assertEquals(resultListBH, result);

                  return result;
                });

    final var ignoredResultX =
        nanoBenchmarkVal(
                () -> {
                  var priorityQueue2 =
                      numberList.foldLeft(PriorityQueue.<Integer>empty(), PriorityQueue::enqueue);
                  var result = List.ofAll(priorityQueue2.take(fetches));
                  assertEquals(fetches, result.length());
                  return result;
                })
            .apply(
                (time, result) -> {
                  System.out.printf(
                      "i.v.PriorityQueue: %d inserts, %d fetches: %7.3f μs per insert\n",
                      queueSize, fetches, time / (1e3 * queueSize));
                  assertEquals(fetches, result.length());
                  assertTrue(seqIsSorted(result));
                  assertEquals(resultListBH, result);

                  return result;
                });

    System.out.println("=========== FIFO Queue Performance =========== ");

    final var ignoredResult2 =
        nanoBenchmarkVal(
                () -> {
                  var resultListQueue = numberList.foldLeft(ListQueue.empty(), ListQueue::enqueue);
                  var result = resultListQueue.toStream().take(fetches);
                  assertEquals(fetches, result.length());
                  return result;
                })
            .apply(
                (time, result) -> {
                  System.out.printf(
                      "Rice ListQueue   : %d inserts, %d fetches: %7.3f μs per insert\n",
                      queueSize, fetches, time / (1e3 * queueSize));

                  // double check that the queue is FIFO
                  assertEquals(numberList.take(fetches), result);
                  return numberList;
                });

    // okay, now for good-old-fashioned java.util.List
    final var ignoredResult3 =
        nanoBenchmarkVal(
                () -> {
                  var llist = new java.util.LinkedList<Integer>();
                  numberList.forEach(llist::add);
                  var result = Stream.continually(llist::remove).take(fetches);
                  assertEquals(fetches, result.length());
                  return result;
                })
            .apply(
                (time, result) -> {
                  System.out.printf(
                      "j.u.LinkedList   : %d inserts, %d fetches: %7.3f μs per insert\n",
                      queueSize, fetches, time / (1e3 * queueSize));

                  // double check that the queue is FIFO
                  assertEquals(numberList.take(fetches), result);
                  return numberList;
                });

    // finally, the fancier java.util.ArrayDeque
    final var ignoredResult4 =
        nanoBenchmarkVal(
                () -> {
                  var arrayDQ = new ArrayDeque<Integer>();
                  numberList.forEach(arrayDQ::add);
                  var result = Stream.continually(arrayDQ::remove).take(fetches);
                  assertEquals(fetches, result.length());
                  return result;
                })
            .apply(
                (time, result) -> {
                  System.out.printf(
                      "j.u.ArrayDeque   : %d inserts, %d fetches: %7.3f μs per insert\n",
                      queueSize, fetches, time / (1e3 * queueSize));

                  // double check that the queue is FIFO
                  assertEquals(numberList.take(fetches), result);
                  return numberList;
                });
  }
}
