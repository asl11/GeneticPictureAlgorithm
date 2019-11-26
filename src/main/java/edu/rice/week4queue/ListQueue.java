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

package edu.rice.week4queue;

import static edu.rice.vavr.Sequences.seqMatch;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.util.Log;
import edu.rice.util.Strings;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Use VAVR's {@link io.vavr.collection.List} class to implement a functional queue. Behaves
 * similarly to VAVR's own {@link io.vavr.collection.Queue}, but please don't look at that while
 * writing your code this week.
 */
public interface ListQueue<T> {
  /** Adds the new element to the end of the queue. */
  ListQueue<T> enqueue(T t);

  /** Adds several events in order, from head to tail of the list. */
  default ListQueue<T> enqueueAll(Seq<T> list) {
    return list.foldLeft(this, ListQueue<T>::enqueue);
  }

  /** Gets the front of the queue; throws an exception if empty. */
  T peek();

  /** Gets everything but the head of the queue; throws an exception if empty. */
  ListQueue<T> tail();

  /** Gets the front and the remainder of the queue; throws an exception if empty. */
  default Tuple2<T, ListQueue<T>> dequeue() {
    return dequeueOption()
        .getOrElseThrow(() -> new NoSuchElementException("Can't call dequeue() on an empty queue"));
  }

  /**
   * Optional getter: returns the front and the remainder of the queue, or {@link Option#none()} if
   * it's an empty queue. You may prefer the structural pattern matching variant {@link
   * ListQueue#match(Function, BiFunction)}.
   */
  Option<Tuple2<T, ListQueue<T>>> dequeueOption();

  /** Returns how many elements are in the queue. */
  int size();

  /**
   * General-purpose deconstructing structural pattern matching on a queue.
   *
   * @param emptyFunc called if the queue is empty
   * @param nonEmptyFunc called if the queue is non-empty, gives the front element of the queue and
   *     a queue with the remainder
   * @param <Q> the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of whichever function matches
   */
  default <Q> Q match(
      Function<? super ListQueue<T>, ? extends Q> emptyFunc,
      BiFunction<? super T, ? super ListQueue<T>, ? extends Q> nonEmptyFunc) {
    if (isEmpty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(peek(), tail());
    }
  }

  /** Returns whether or not there are any contents in the queue. */
  default boolean isEmpty() {
    return false;
  }

  /** Returns a lazy stream that iterates over the queue in FIFO order. */
  default Stream<T> toStream() {
    return match(emptyQueue -> Stream.empty(), (head, tail) -> Stream.cons(head, tail::toStream));
  }

  /** Creates an empty list queue of the given type parameter. */
  @SuppressWarnings("unchecked")
  static <T> ListQueue<T> empty() {
    return (ListQueue<T>) Empty.SINGLETON;
  }

  /**
   * Variadic helper function, creates a list-queue from the arguments given. The first argument
   * will be at the front of the queue.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T> ListQueue<T> of(T... vals) {
    if (vals.length == 0) {
      // special case when the user asks for ListQueue.of()
      return empty();
    } else {
      // vals, when we convert them to a list with List.of, will be
      // in the proper FIFO order, so we'll put them in the outbox and
      // start with an empty inbox.
      return new Queue<>(List.empty(), List.of(vals));
    }
  }

  class Queue<T> implements ListQueue<T> {
    private final List<T> inbox;
    private final List<T> outbox;

    // Don't call this externally; use the "of" method or the empty() method instead.
    private Queue(List<T> inbox, List<T> outbox) {
      this.inbox = inbox;
      this.outbox = outbox;
    }

    @Override
    public int hashCode() {
      return toStream().hashCode(); // let the list's hash worry about it
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ListQueue)) {
        return false;
      }

      var otherQueue = (ListQueue<?>) o;

      return otherQueue == this || toStream().equals(otherQueue.toStream());
    }

    @Override
    public String toString() {
      return toStream().map(Strings::objectToEscapedString).mkString("Queue(", ", ", ")");
    }

    @Override
    public ListQueue<T> enqueue(T t) {
      return new Queue<>(inbox.prepend(t), outbox).fixup();
    }

    // call this whenever you make a new Queue; if the outbox has
    // nothing in it, then it will reverse the inbox and add that in
    // the outbox. This ensures a useful property:
    //
    // IF there's something to dequeue, THEN it will be the head of the outbox.
    //
    // and:
    //
    // IF the queue would be entirely empty, THEN the SINGLETON empty queue is returned
    //
    private ListQueue<T> fixup() {
      return seqMatch(
          outbox,
          emptyOutbox ->
              seqMatch(
                  inbox,
                  emptyInbox -> ListQueue.empty(), // the queue is completely empty
                  (head, tail) -> new Queue<>(List.empty(), inbox.reverse())),
          (head, tail) -> this); // there's still something in the outbox, so we do nothing
    }

    @Override
    public T peek() {
      return outbox.head();
    }

    @Override
    public ListQueue<T> tail() {
      return new Queue<>(inbox, outbox.tail()).fixup();
    }

    @Override
    public Option<Tuple2<T, ListQueue<T>>> dequeueOption() {
      return some(Tuple.of(peek(), tail()));
    }

    @Override
    public int size() {
      return inbox.length() + outbox.length();
    }
  }

  /**
   * Empty queues, however implemented, have a lot of things in common. Since we're big believers in
   * not repeating ourselves, they'll all share this interface, and thus pick up all these default
   * methods.
   */
  class Empty<T> implements ListQueue<T> {
    private static final ListQueue<?> SINGLETON = new ListQueue.Empty<>();

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public T peek() {
      Log.e("ListQueue.Empty", "can't take peek() of an empty queue");
      throw new NoSuchElementException("can't take peek() of an empty queue");
    }

    @Override
    public ListQueue<T> tail() {
      Log.e("ListQueue.Empty", "can't take tail() of an empty queue");
      throw new NoSuchElementException("can't take tail() of an empty queue");
    }

    @Override
    public Option<Tuple2<T, ListQueue<T>>> dequeueOption() {
      return none();
    }

    @Override
    public int size() {
      return 0;
    }

    // external users, don't call this; use empty()
    private Empty() {}

    @Override
    public ListQueue<T> enqueue(T newbie) {
      return ListQueue.of(newbie);
    }

    @Override
    public String toString() {
      return "Queue()";
    }
  }
}
