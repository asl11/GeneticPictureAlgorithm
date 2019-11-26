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

import static edu.rice.qt.SequenceGenerators.sequences;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;
import org.quicktheories.generators.SourceDSL;

/**
 * This is a QuickTheories <i>generator</i> for VAVR maps, analogous to the QuickTheories generator
 * for Java maps: {@link SourceDSL#maps()}, making it easier to generate {@link
 * io.vavr.collection.Map} instances of the desired size. Typical usage might be something like:
 * <code>maps().of(integers.all(), booleans().all()).ofSizesBetween(0, 10)</code>, yielding a
 * generator for Map&lt;Integer, Boolean&gt; with from 0 to 10 pairs of integers and booleans,
 * chosen at random.
 *
 * <p>Note that if the underlying generator of keys and values for the map produces duplicate keys,
 * then the resulting map may be smaller than desired, since there can be only one value per key.
 */
public interface MapGenerators {
  static MapDSL maps() {
    return new MapDSL();
  }

  class MapDSL {
    // no public constructor
    private MapDSL() {}

    /**
     * Creates a MapGeneratorBuilder. Analogous to {@link
     * org.quicktheories.generators.MapsDSL#of(Gen, Gen)}, but builds a VAVR {@link
     * io.vavr.collection.Map} rather than a Java {@link java.util.Map}.
     *
     * @param <K> key type to generate
     * @param <V> value type to generate
     * @param keySource a Source of type K for the items in the map
     * @param valueSource a Source of type V for the items in the map
     */
    public <K, V> MapGeneratorBuilder<K, V> of(Gen<K> keySource, Gen<V> valueSource) {
      return new MapGeneratorBuilder<>(keySource, valueSource);
    }
  }

  class MapGeneratorBuilder<K, V> {
    private final Gen<K> keySource;
    private final Gen<V> valueSource;

    private MapGeneratorBuilder(Gen<K> keySource, Gen<V> valueSource) {
      this.keySource = keySource;
      this.valueSource = valueSource;
    }

    /**
     * Generates a Map of objects, where the size of the Map is fixed.
     *
     * @param size size of map to generate
     * @return a Source of Maps
     */
    public Gen<Map<K, V>> ofSize(int size) {
      return ofSizeBetween(size, size);
    }

    /**
     * Generates a Map, where the size of the Map is bounded by minimumSize and maximumSize
     * (inclusive).
     *
     * @param minimumSize - inclusive minimum size of Map
     * @param maximumSize - inclusive maximum size of Map
     * @return a Source of Maps
     */
    public Gen<Map<K, V>> ofSizeBetween(int minimumSize, int maximumSize) {
      if (minimumSize > maximumSize || minimumSize < 0) {
        throw new IllegalArgumentException(
            String.format(
                "The minimumSize (%d) is larger than the maximumSize (%d) or is less than zero",
                minimumSize, maximumSize));
      } else {
        return ofSizes(Generate.range(minimumSize, maximumSize));
      }
    }

    /** Generates a Map, where the size of the Map is drawn from the given generator of integers. */
    public Gen<Map<K, V>> ofSizes(Gen<Integer> sizes) {
      return sizes.flatMap(
          size -> {
            var keyGen = sequences().of(keySource).ofSize(size);
            var valGen = sequences().of(valueSource).ofSize(size);

            return keyGen.zip(valGen, (keySeq, valSeq) -> HashMap.ofEntries(keySeq.zip(valSeq)));
          });
    }
  }
}
