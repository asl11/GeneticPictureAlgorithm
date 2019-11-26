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

package edu.rice.week2lists;

import java.util.NoSuchElementException;

/** Interface for functional lists of objects. */
public interface ObjectList {
  //
  // Data definition: an ObjectList is either:
  // - Cons: an Object and another ObjectList
  // - Empty: no contents
  //

  // Engineering notes: Where did we get a goofy word like "Cons" to
  // represent these two things going together? Turns out, it's a historic
  // term that go all the way back to a particular IBM computer of the late
  // 1950's. "Cons", and other terms from that computer, are still widely used:
  //
  //     https://en.wikipedia.org/wiki/CAR_and_CDR
  //
  // In Comp215, we'll say "cons" often, but we won't be using "car" or "cdr".
  // For those, we'll instead say "head" and "tail", since those are more intuitive.
  // You may well bump into car/cdr at some point in your career, though, so you
  // can at least say you saw it here first.

  /** Creates a new empty object-list. */
  static ObjectList empty() {
    return Empty.SINGLETON;
  }

  /** Returns whether the object-list is empty. */
  boolean isEmpty();

  /** Returns the first value in the object-list. Throws an exception if the list is empty. */
  Object head();

  /**
   * Returns a object-list with everything but the head value. Throws an exception if the list is
   * empty.
   */
  ObjectList tail();

  /** Returns a new object-list with the new value at the front of the list. */
  default ObjectList prepend(Object o) {
    // Observation: this method body is *exactly* the same for this being Cons
    // or this being Empty, so we can "promote" this to a "default method", which
    // will then be the same for both cases. Don't repeat yourself!
    return new Cons(o, this);
  }

  /** Returns the length of the object-list. */
  int length();

  /** Returns whether the object-list contains the given object. */
  boolean contains(Object o);

  /** Helper method for toString. */
  private static String toStringHelper(ObjectList list, String prefix) {
    if (list.isEmpty()) {
      return "ObjectList(" + prefix + ")";
    } else if (prefix.isEmpty()) {
      return toStringHelper(list.tail(), list.head().toString());
    } else {
      return toStringHelper(list.tail(), prefix + ", " + list.head().toString());
    }
  }

  /** Functional lists over objects. */
  class Cons implements ObjectList {
    // a list is one of two things:
    // 1) a value and another list
    // 2) an empty list
    //
    // class ObjectList implements case #1
    // class ObjectList.Empty implements case #2

    private final Object headVal;

    private final ObjectList tailVal;

    private Cons(Object headVal, ObjectList tailVal) {
      this.headVal = headVal;
      this.tailVal = tailVal;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public Object head() {
      return headVal;
    }

    @Override
    public ObjectList tail() {
      return tailVal;
    }

    @Override
    public int length() {
      return 1 + tailVal.length();
    }

    @Override
    public boolean contains(Object o) {
      return headVal.equals(o) || tailVal.contains(o);
    }

    @Override
    public String toString() {
      return toStringHelper(this, "");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof Cons)) {
        return false;
      }

      var that = (Cons) o;

      return headVal.equals(that.headVal) && tailVal.equals(that.tailVal);
    }

    @Override
    public int hashCode() {
      return 31 * headVal.hashCode() + tailVal.hashCode();
    }
  }

  /** Empty list of objects. */
  class Empty implements ObjectList {
    // Constructor isn't for public use. Use empty() instead.
    private Empty() {}

    private static final ObjectList SINGLETON = new Empty();

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public Object head() {
      throw new NoSuchElementException("can't take head of an empty list");
    }

    @Override
    public ObjectList tail() {
      throw new NoSuchElementException("can't take tail of an empty list");
    }

    @Override
    public int length() {
      return 0;
    }

    @Override
    public boolean contains(Object o) {
      return false;
    }

    @Override
    public String toString() {
      return toStringHelper(this, "");
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      } else if (o instanceof ObjectList) {
        return ((ObjectList) o).isEmpty();
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }
}
