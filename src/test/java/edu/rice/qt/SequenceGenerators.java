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

package edu.rice.qt;

import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;
import org.quicktheories.generators.Lists;
import org.quicktheories.generators.ListsDSL;
import org.quicktheories.generators.SourceDSL;

/**
 * This is a QuickTheories <i>generator</i> for VAVR sequences, analogous to the QuickTheories
 * generator for Java lists: {@link SourceDSL#lists()}, making it easier to generate {@link
 * io.vavr.collection.Seq} instances of the desired length. Typical usage might be something like:
 * <code>sequences().of(booleans().all()).ofSizesBetween(0, 10)</code>, yielding a generator for
 * Seq&lt;Boolean&gt; of lengths between 0 and 10.
 *
 * <p>Note that these sequences will be <i>lazy</i>, so the work to generate the values within will
 * be delayed until necessary. This may impact benchmark timing; if you're doing that, be sure to
 * use {@link Seq#toList()} to force the evaluation of the full generated sequence prior to starting
 * the clock.
 */
public interface SequenceGenerators {
  // Engineering notes: You'll see a number of Javadoc comments here
  // that say "Analogous to" and have a Javadoc hyperlink into the
  // QuickTheories codebase. If you follow those links (e.g., hitting
  // Command-B on a Mac), you can compare their code to ours. You'll
  // hopefully notice how the code here follows their pattern but is
  // significantly simpler. This is, in part, because they're dealing
  // with Java streams, which require them to convert lists to
  // streams, do their operations, then "collect" the results back
  // into a list again. VAVR sequences don't require any of this extra
  // work.

  // We're copying QT's "DSL" (domain specific language) coding
  // pattern as well. All we *really* need is the sequencesOf()
  // method, but this isn't particularly pleasant to use.  Instead, a
  // coder starts off by asking for sequences(), and then they've only
  // got one available method from SeqDSL: the of() method which
  // forces them to ask the question "of what?" and thus specify an
  // appropriate generator. Then, they're given an instance of the
  // SeqGeneratorBuilder, which next auto-completes to three
  // ofSize() variants, forcing our coder to answer the question "how
  // many?". At that point, the QT generator is returned.

  /**
   * Used to generate {@link Seq} lists of values. This is the typical starting point when writing a
   * QuickTheories theory for Seq. Analogous to {@link SourceDSL#lists()}.
   */
  static SeqDSL sequences() {
    return new SeqDSL();
  }

  /**
   * Used to generate lists of the values in the given value generator, with another generator for
   * the sizes of the lists. Analogous to {@link Lists}'s <code>listsOf(Gen, Gen)</code> method,
   * except that method isn't public and this method is.
   */
  static <T> Gen<Seq<T>> sequencesOf(Gen<T> values, Gen<Integer> sizes) {
    Gen<Seq<T>> gen =
        prng -> Stream.continually(() -> values.generate(prng)).take(sizes.generate(prng));

    return gen.describedAs(Seq::toString);
  }

  /**
   * A Class for creating Seq Sources that will produce Seq objects of either fixed or bounded size.
   * Analogous to {@link ListsDSL}.
   */
  class SeqDSL {
    // no public constructor
    private SeqDSL() {}

    /**
     * Creates a SeqGeneratorBuilder. Analogous to {@link ListsDSL#of(Gen)}.
     *
     * @param <T> type to generate
     * @param source a Source of type T for the items in the list
     * @return a SeqGeneratorBuilder of type T
     */
    public <T> SeqGeneratorBuilder<T> of(Gen<T> source) {
      return new SeqGeneratorBuilder<>(source);
    }
  }

  /**
   * SeqGeneratorBuilder enables the creation of Sources for Lists of fixed and bounded size.
   * Analogous to {@link ListsDSL.ListGeneratorBuilder}.
   */
  class SeqGeneratorBuilder<T> {
    private final Gen<T> source;

    // no public constructor
    private SeqGeneratorBuilder(Gen<T> source) {
      this.source = source;
    }

    /**
     * Generates a Seq of objects, where the size of the Seq is fixed.
     *
     * @param size size of lists to generate
     * @return a Source of Lists of type T
     */
    public Gen<Seq<T>> ofSize(int size) {
      return ofSizeBetween(size, size);
    }

    /**
     * Generates a Seq of objects, where the size of the Seq is bounded by minimumSize and
     * maximumSize (inclusive).
     *
     * @param minimumSize - inclusive minimum size of List
     * @param maximumSize - inclusive maximum size of List
     * @return a Source of Lists of type T
     */
    public Gen<Seq<T>> ofSizeBetween(int minimumSize, int maximumSize) {
      if (minimumSize > maximumSize || minimumSize < 0) {
        throw new IllegalArgumentException(
            String.format(
                "The minimumSize (%d) is longer than the maximumSize (%d) or is less than zero",
                minimumSize, maximumSize));
      } else {
        return ofSizes(Generate.range(minimumSize, maximumSize));
      }
    }

    /**
     * Generates a Seq of objects, where the size of the Seq is drawn from the given generator of
     * integers.
     */
    public Gen<Seq<T>> ofSizes(Gen<Integer> sizes) {
      return sequencesOf(source, sizes);
    }
  }
}
