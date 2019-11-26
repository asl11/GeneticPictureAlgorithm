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
import java.util.List;

/** This class represents Dr. Wallach's dream garage. Or at least part of it. Dream big. */
public class DreamGarage {
  private static final Collection<Model> cars =
      List.of(
          new Model(Manufacturer.lookup("Cadillac"), "Coupe de Ville", 1959),
          new Model(Manufacturer.lookup("Chevrolet"), "Corvette", 1957),
          new Model(Manufacturer.lookup("DeLorean"), "DMC-12", 1983),
          new Model(Manufacturer.lookup("Ferrari"), "250GT Cabriolet", 1957),
          new Model(Manufacturer.lookup("Ferrari"), "Dino 246GTS", 1972),
          new Model(Manufacturer.lookup("Ferrari"), "Testarossa Spyder", 1986),
          new Model(Manufacturer.lookup("Ford"), "GT40", 1966),
          new Model(Manufacturer.lookup("Lamborghini"), "Miura P400SV", 1971),
          new Model(Manufacturer.lookup("Nissan"), "Fairlady 1500", 1962),
          new Model(Manufacturer.lookup("Porsche"), "356", 1951),
          new Model(Manufacturer.lookup("Porsche"), "Carrera GT", 2007),
          new Model(Manufacturer.lookup("Toyota"), "2000GT", 1967));

  /** Gets all of the cars from Dr. Wallach's dream garage. */
  public static Collection<Model> getCars() {
    // This makes it impossible for the caller to modify our dream car garage!
    return Collections.unmodifiableCollection(cars);
  }
}
