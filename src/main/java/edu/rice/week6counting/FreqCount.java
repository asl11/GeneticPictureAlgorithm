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

package edu.rice.week6counting;

import static edu.rice.vavr.Maps.mapFromSeq;

import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.Comparator;

/** Week 6 lab assignment. */
public class FreqCount {
  /**
   * Given a string of text as input, this will tokenize the string into its component words in a
   * fairly simplistic way, by splitting on whitespace or punctuation. Capitalization is ignored as
   * well. The result will be a map from those words to the integer frequency of their occurrence.
   */
  public static Map<String, Integer> count(String input) {
    // Note: you should be splitting on whitespace and
    // punctuation. The words that come out should be free of any such
    // things. You're writing a regular expression to match all the
    // things that can come *between* words.

    // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html

    // Be sure to check out edu.rice.vavr.Maps.mapFromSeq(), which
    // has all the necessary machinery to help you convert from
    // a sequence to a map, including merging the values.

    var regex = "[\\s\\p{Punct}]+";

    var inputList = Stream.of(input.split(regex)).map(String::toLowerCase);

    return mapFromSeq(inputList, string -> string, string -> 1, Integer::sum);
  }

  /**
   * Given a mapping from strings to integers, such as count() might return, return a list of
   * key-value tuples sorted from most frequent to least frequent.
   */
  public static Seq<Tuple2<String, Integer>> mostFrequent(Map<String, Integer> freqMap) {
    var comparator =
        Comparator.<Tuple2<String, Integer>, Integer>comparing(Tuple2::_2)
            .reversed()
            .thenComparing(Tuple2::_1);
    return freqMap.toList().sorted(comparator);
  }
}

// Engineering note: why do we need to specify a type parameter
// for Comparator.comparing?  We could actually do this in
// several ways. We could instead replace the Tuple2::_2
// method reference with a more explicit definition like:
// (Tuple2<String, Integer> kv) -> kv._2 and Java would
// otherwise be able to figure out the return type of our call to
// comparing().

// And, once we know the return type of comparing(),
// then we don't need any additional help in determining the types
// involved in .thenComparing().

// Alternately, when we aren't using .thenComparing() but are just doing
// comparing() by itself without any function composition, we don't
// need the type parameter at all because it can be inferred from context.

// Okay. Fine. But why, you may ask, can't Java know that the list
// being sorted is a list of Tuple2<String,Integer>, so
// therefore the type can flow from there? Indeed, this is deeply
// annoying.

// What's going on, in essence, is that Java is limited in how it
// flows type parameters.  Once you're doing the whole thenComparing()
// operation, Java cannot flow the types from the outside to the
// inside of the expression. We work around this by explicitly
// specifying the type parameter, and everything becomes happy
// again.

// The top StackOverflow answer for a variant on this issue was
// written by Brian Goetz, who works on the Java language at
// Oracle. https://stackoverflow.com/a/24442897/4048276
