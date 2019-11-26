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

package edu.rice.week3subsets;

import edu.rice.week3lists.GList;

/** This class implements subsets of lists of integers, and has a performance test as well. */
public class Subsets {
  /**
   * Given a list of values, returns a list of lists of values: one for each possible subset of the
   * input list.
   */
  public static <T> GList<GList<T>> subsets(GList<T> input) {
    // Engineering note: Compare this code to the Python equivalent and to the
    // week2 version. You'll notice how the code here maps one to one with the
    // Python code. This style is easier to write and easier to debug.

    if (input.isEmpty()) {
      var emptyList = GList.<GList<T>>empty();
      return emptyList.prepend(GList.empty());
    } else {
      var children = subsets(input.tail());
      var childrenPlus = children.map(child -> child.prepend(input.head()));
      return children.appendAll(childrenPlus);
    }
  }

  // on my laptop, n=14 worked, and we got a stack overflow afterward
  public static final int MAX = 100;

  /**
   * Very simple benchmark: tries to run the subsets function on input lists as large as 100
   * entries.
   */
  public static void main(String[] args) {
    System.out.println("GList subset performance test");
    var bigArray = GList.rangeClosed(0, MAX);

    // Java will spend time optimizing your code, while it's running, which we don't want to
    // mess with our benchmark numbers, so we'll run it in advance, once, and ignore the result.
    var ignored = subsets(bigArray.take(10));

    for (var i = 0; i < MAX; i++) {
      // There's a chance that this will run out of memory, so we're including a try/catch
      // block to catch it and fail gracefully. We'll talk more about error handling
      // later in the semester.

      try {
        var startTime = System.nanoTime();
        var results = subsets(bigArray.take(i));
        var endTime = System.nanoTime();

        var delta = (endTime - startTime) / 1_000_000_000.0;

        System.out.printf("%d,%g\n", i, delta);
        if (delta > 3.0) {
          System.exit(0);
        }
      } catch (StackOverflowError err) {
        // Nothing really to do here except quit.
        System.out.println(
            "Ran out of memory; add these flags to your \"VM options\": -Xss1g -Xms1g");
        System.out.println("See your assignment PDF for details.");
        System.exit(0);
      }
    }
  }
}
