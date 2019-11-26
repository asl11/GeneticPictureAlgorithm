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

/**
 * Mutating list variant that internally uses an ObjectList.
 *
 * @see ObjectList
 */
public class MList {
  private ObjectList list = ObjectList.empty(); // initially empty

  /** Adds a new element to the front of the list. */
  public void push(Object o) {
    list = list.prepend(o);
  }

  /**
   * Checks whether an object that is equal to <code>o</code> (via {@link Object#equals(Object)} is
   * in the list.
   */
  public boolean contains(Object o) {
    return list.contains(o);
  }

  /** Returns whether the list has zero elements within. */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /** Removes the head element from the list and returns it. */
  public Object pop() {
    var headVal = list.head(); // will throw an exception if the list is empty
    list = list.tail();
    return headVal;
  }

  // Engineering note: For the three core methods that all Java classes should provide,
  // we're using a common coding strategy called *delegation*. We have perfectly useful
  // methods in the ObjectList class that know how to do everything we want for MList,
  // so why reimplement them? The only downside of this strategy is that the toString()
  // method from ObjectList sticks the string "ObjectList" in front, when we might prefer
  // to have "MList" in front. If we really cared, we'd expose the toStringHelper()
  // inside of ObjectList and pass in the desired prefix string.

  @Override
  public String toString() {
    return list.toString();
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof MList) && ((MList) o).list.equals(list);
  }
}
