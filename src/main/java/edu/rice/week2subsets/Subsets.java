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

package edu.rice.week2subsets;

import java.util.ArrayList;
import java.util.List;

/** This class implements subsets of lists of integers, and has a performance test as well. */
public class Subsets {
  /**
   * Given a list of integers, returns a list of lists of integers: one for each possible subset of
   * the input list.
   */
  public static List<List<Integer>> subsets(List<Integer> input) {
    // Here's some Python code. Your Java code is meant to behave in
    // exactly the same way.

    // def subsets(input):
    //  if not input:
    //    return [[]]
    //  else:
    //    children = subsets(input[1:])
    //    childrenPlus = [[input[0]] + x for x in children]
    //    return children + childrenPlus

    // Example:
    // > subsets([0,1,2])
    // => [[], [2], [1], [1, 2], [0], [0, 2], [0, 1], [0, 1, 2]]

    // This code is in the file "subsets.py" in the same directory as
    // this file and you're welcome to run it on your own computer.

    // Note that we don't care about the ordering of the resulting
    // list (i.e., [2] could come before or after [1]) but we *do*
    // care about the ordering within each list (i.e., [0,1,2] is good
    // and [2,1,0] is an error).

    int size = input.size();
    var results = new ArrayList<List<Integer>>(); // initially empty

    if (size == 0) {
      results.add(new ArrayList<>());
    } else {
      var head = input.get(0);
      var children = subsets(input.subList(1, size));

      for (var child : children) {
        results.add(child);
        var childPlus = new ArrayList<>(child); // childPlus is a *copy* of child
        //        var childPlus = child;
        // if you do this, rather than the previous line, it won't work; why?
        childPlus.add(0, head);
        results.add(childPlus);
      }
    }
    return results;
  }

  public static final int MAX = 100; // on my laptop, n=24 took 14 seconds to run

  /**
   * Very simple benchmark: tries to run the subsets function on input lists as large as 100
   * entries.
   */
  public static void main(String[] args) {
    System.out.println("ArrayList subset performance test");
    var bigArray = new ArrayList<Integer>();
    for (var i = 0; i < MAX; i++) {
      bigArray.add(i);
    }

    // Java will spend time optimizing your code, while it's running,
    // which we don't want to mess with our benchmark numbers, so
    // we'll run it in advance, once, and ignore the result.
    var ignored = subsets(bigArray.subList(0, 10));

    for (var i = 0; i < MAX; i++) {
      var startTime = System.nanoTime();
      var results = subsets(bigArray.subList(0, i));
      var endTime = System.nanoTime();
      var delta = (endTime - startTime) / 1_000_000_000.0;

      System.out.printf("%d,%g\n", i, delta);
      if (delta > 3.0) {
        System.exit(0);
      }
    }
  }
}
