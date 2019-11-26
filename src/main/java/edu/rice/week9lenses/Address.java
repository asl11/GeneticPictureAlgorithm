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

package edu.rice.week9lenses;

import static edu.rice.lens.Lens.lens;

import edu.rice.lens.Lens;

public class Address {
  private final Street street;
  private final Integer number;

  public Address(Street street, Integer number) {
    this.street = street;
    this.number = number;
  }

  public static final Lens<Address, Street> StreetLens =
      lens(address -> address.street, (address, street) -> new Address(street, address.number));

  public static final Lens<Address, Integer> NumberLens =
      lens(address -> address.number, (address, number) -> new Address(address.street, number));
}
