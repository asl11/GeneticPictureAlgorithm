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

package edu.rice.week2cars;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class represents an automotive manufacturer. Each manufacturer has a name and a homepageUrl,
 * along with methods to fetch them, but not change them. (They don't need to change, so we won't
 * allow changes.) To find a particular manufacturer, use the static methods {@link #exists(String)}
 * or {@link #lookup(String)}. You can also get all the names at once with {@link #allNames()} and
 * get all the Manufacturer's at once with {@link #allManufacturers()}.
 */
public class Manufacturer {
  private final String name;
  private final String homepageUrl;

  /**
   * Construct a new automotive manufacturer. Not public because we expect external users to look
   * them up via {@link #lookup(String)}.
   *
   * @param name Name of the company (e.g., "Chevrolet")
   * @param homepageUrl URL for the company (e.g., "http://www.chevrolet.com")
   */
  private Manufacturer(String name, String homepageUrl) {
    this.name = name;
    this.homepageUrl = homepageUrl;
  }

  /** Fetches the name of the automotive manufacturer (e.g., "Chevrolet"). */
  public String getName() {
    return name;
  }

  /** Fetches the homepage URL of the automotive manufacturer (e.g., "http://www.chevrolet.com"). */
  public String getHomepageUrl() {
    return homepageUrl;
  }

  @Override
  public String toString() {
    return name + " (" + homepageUrl + ")";
  }

  @Override
  public int hashCode() {
    return name.hashCode() * 31 + homepageUrl.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return this
        == o; // we can get away with this because we don't allow more than one instance of each
    // manufacturer
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Everything below here is *static*, meaning that these are not
  // *instance* methods on Manufacturer objects, but rather are just
  // some globally visible functions and/or variables that happen to
  // be associated with Manufacturer objects (here, helping you look
  // them up in an internal database that we're maintaining).
  ////////////////////////////////////////////////////////////////////////////////

  private static final List<Manufacturer> manufacturers =
      List.of(
          new Manufacturer("Acura", "https://www.acura.com"),
          new Manufacturer("Alfa Romeo", "https://www.alfaromeousa.com"),
          new Manufacturer("Ariel", "https://www.arielna.com"),
          new Manufacturer("Aston Martin", "https://global.astonmartin.com/en-us"),
          new Manufacturer("Audi", "https://www.audiusa.com"),
          new Manufacturer("Bentley", "https://www.bentleymotors.com/en.html"),
          new Manufacturer("BMW", "https://www.bmwusa.com"),
          new Manufacturer("Bugatti", "https://www.bugatti.com"),
          new Manufacturer("Buick", "http://www.buick.com"),
          new Manufacturer("Cadillac", "http://www.cadillac.com"),
          new Manufacturer("Campagna", "https://campagnamotors.com"),
          new Manufacturer("Caterham", "http://us.caterhamcars.com"),
          new Manufacturer("Chevrolet", "http://www.chevrolet.com"),
          new Manufacturer("Chrysler", "https://www.chrysler.com"),
          new Manufacturer("DeLorean", "https://www.delorean.com"),
          new Manufacturer("Dodge", "https://www.dodge.com"),
          new Manufacturer("Ferrari", "https://www.ferrari.com/en-US"),
          new Manufacturer("Fiat", "https://www.fiatusa.com"),
          new Manufacturer("Ford", "https://www.ford.com"),
          new Manufacturer("GMC", "http://www.gmc.com"),
          new Manufacturer("Honda", "https://www.honda.com"),
          new Manufacturer("Hyundai", "https://www.hyundaiusa.com"),
          new Manufacturer("Jaguar", "https://www.jaguarusa.com"),
          new Manufacturer("Jeep", "https://www.jeep.com"),
          new Manufacturer("Kia", "https://www.kia.com/us/en"),
          new Manufacturer("Koenigsegg", "https://www.koenigsegg.com/"),
          new Manufacturer("Lamborghini", "https://www.lamborghini.com/en-en"),
          new Manufacturer("Land Rover", "https://www.landroverusa.com"),
          new Manufacturer("Lexus", "http://www.lexus.com"),
          new Manufacturer("Lincoln", "https://www.lincoln.com"),
          new Manufacturer("Lotus", "http://www.lotuscars.com"),
          new Manufacturer("Infiniti", "https://www.infinitiusa.com"),
          new Manufacturer("Maserati", "https://www.maseratiusa.com/maserati/us/en"),
          new Manufacturer("Mazda", "https://www.mazdausa.com"),
          new Manufacturer("McClaren", "http://cars.mclaren.com"),
          new Manufacturer("Mercedes-Benz", "https://www.mbusa.com"),
          new Manufacturer("Mini", "https://www.miniusa.com"),
          new Manufacturer("Mitsubishi", "https://www.mitsubishicars.com"),
          new Manufacturer("Morgan", "https://www.morgancars-usa.com"),
          new Manufacturer("Nissan", "https://www.nissanusa.com"),
          new Manufacturer("Porsche", "https://www.porsche.com/usa/"),
          new Manufacturer("Rolls-Royce", "https://www.rolls-roycemotorcars.com/en-US"),
          new Manufacturer("smart", "https://www.smartusa.com"),
          new Manufacturer("Subaru", "https://www.subaru.com"),
          new Manufacturer("Tesla", "https://www.tesla.com"),
          new Manufacturer("Toyota", "https://www.toyota.com"),
          new Manufacturer("Volkswagen", "http://www.vw.com"),
          new Manufacturer("Volvo", "https://www.volvocars.com/us"));

  private static final Map<String, Manufacturer> registry = new HashMap<>(); // initialized below

  static {
    // Initialize registry to have a mapping from each manufacturer's name (e.g., "Nissan") to
    // its Manufacturer object (from the list above).

    for (var m : manufacturers) {
      registry.put(m.getName(), m);
    }
  }

  /** Looks up whether a given manufacturer exists in the database, and returns true or false. */
  public static boolean exists(String name) {
    return registry.containsKey(name);
  }

  /**
   * Returns a {@link Manufacturer} instance, if the manufacturer exists in the database.
   *
   * @throws NoSuchElementException if the manufacturer is not present
   */
  public static Manufacturer lookup(String name) {
    var result = registry.get(name);
    if (result == null) {
      throw new NoSuchElementException(name + " not present");
    } else {
      return result;
    }
  }

  /** Returns a {@link Collection} of the names of every {@link Manufacturer}. */
  public static Collection<String> allNames() {
    // Note: a "Collection" is an interface. Every "Set" is also a "Collection".
    return registry.keySet();
  }

  /** Returns a {@link Collection} of every {@link Manufacturer}. */
  public static Collection<Manufacturer> allManufacturers() {
    // Engineering note: We didn't require you to do this in week 2,
    // but notice that Java allows us to return an "unmodifiable view"
    // of a collection? This is incredibly handy because we then don't
    // have to worry about our caller modifying our internal state,
    // and we also don't have to spend O(n) time to make a copy of it
    // before returning it.
    return Collections.unmodifiableCollection(manufacturers);
  }

  /**
   * A function that takes a collection of car models and keeps a count of how often each
   * manufacturer shows up. Returns a mapping from manufacturers to integers.
   */
  public static Map<Manufacturer, Integer> manufacturerFrequencyCount(Collection<Model> models) {
    // For this week, you won't be using any of the fancy new methods
    // on Map that use lambdas like Map.merge or Map.compute. All you
    // need is containsKey(), get(), and put().

    // CUT FROM HERE

    var result = new HashMap<Manufacturer, Integer>();

    for (Model model : models) {
      var m = model.getManufacturer();
      if (result.containsKey(m)) {
        int oldCount = result.get(m);
        result.put(m, oldCount + 1);
      } else {
        result.put(m, 1);
      }
    }

    return result;
    // TO HERE. Replace with:
    // TODO: Implement this method
    // throw new RuntimeException("manufacturerFrequencyCount in Manufacturer class not yet
    // implemented!");
  }

  /**
   * A function that takes a collection of car models and returns the most popular manufacturer. If
   * there are multiple models with equal popularity, any of them might be returned.
   *
   * @throws NoSuchElementException if the input is empty.
   */
  public static Manufacturer mostPopularManufacturer(Collection<Model> models) {
    // Use the previous function to get a mapping from manufacturers to counts, then
    // write some additional code that identifies the most popular manufacturer.
    // All you will need is keySet() and get().
    // CUT FROM HERE

    var freqCount = manufacturerFrequencyCount(models);

    if (models.isEmpty()) {
      throw new NoSuchElementException("no models present, so no most-popular manufacturer");
    }

    var biggestCountSoFar = -1;
    Manufacturer mostPopular =
        null; // initially null, but guaranteed to change because we know models is non-empty

    for (var m : freqCount.keySet()) {
      int count = freqCount.get(m);
      if (count > biggestCountSoFar) {
        biggestCountSoFar = count;
        mostPopular = m;
      }
    }

    return mostPopular;
    // TO HERE. Replace with:
    // TODO: Implement this method
    // throw new RuntimeException("mostPopularManufacturer not yet implemented!");
  }
}
