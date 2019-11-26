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
import java.util.NoSuchElementException;

/**
 * This class represents a particular car model from a given {@link Manufacturer}. Each model has a
 * {@link Manufacturer}, a modelName (e.g., "Corvette"), and a year (e.g., 2018), along with methods
 * to fetch them, but not change them. (They don't need to change, so we won't allow changes.)
 */
public class Model {
  private final Manufacturer manufacturer;
  private final String modelName;
  private final int modelYear;

  /**
   * Constructs a new car model.
   *
   * @param manufacturer Any instance of {@link Manufacturer}
   * @param modelName The model name (e.g., "Corvette")
   * @param modelYear The model year (e.g., 2018)
   */
  public Model(Manufacturer manufacturer, String modelName, int modelYear) {
    this.manufacturer = manufacturer;
    this.modelName = modelName;
    this.modelYear = modelYear;
  }

  /** Fetches the {@link Manufacturer} of this particular model. */
  public Manufacturer getManufacturer() {
    return manufacturer;
  }

  /** Fetches the name of this particular model (e.g., "Corvette") */
  public String getModelName() {
    return modelName;
  }

  public int getModelYear() {
    return modelYear;
  }

  /** Compute the average age of all the cars in the garage. */
  public static int averageAge(Collection<Model> models, int currentYear) {
    // CUT FROM HERE
    var sum = 0;
    if (models.isEmpty()) {
      throw new NoSuchElementException("no models present, so no average age");
    }
    for (var model : models) {
      sum = sum + (currentYear - model.getModelYear());
    }
    return sum / models.size();
    // TO HERE. Replace with:
    // TODO: Implement this method
    // throw new RuntimeException("averageAge method in class Model not yet implemented!");
  }

  @Override
  public String toString() {
    return String.format(
        "Manufacturer: %s, Model: %s, Year: %d", manufacturer.toString(), modelName, modelYear);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Model)) {
      return false;
    }

    var model = (Model) o;

    return manufacturer.equals(model.manufacturer)
        && modelName.equals(model.modelName)
        && modelYear == model.modelYear;
  }

  @Override
  public int hashCode() {
    return manufacturer.hashCode() * 31 + modelName.hashCode() * 7 + modelYear;
  }
}
