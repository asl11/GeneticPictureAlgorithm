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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Week9Lab {

  /** Unit tests to exercise {@link Employee}. */
  @Test
  public void testLenses() {

    // Make some companies and employees
    final var rice = new Company("Rice", new Address(new Street("Main"), 6100));
    final var uh = new Company("University of Houston", new Address(new Street("Calhoun"), 4800));
    final var zoran = new Employee("Zoran", 47, rice);
    final var dan = new Employee("Dan", 46, rice);

    // Lenses composition
    final var companyStreetName =
        Company.AddressLens.andThen(Address.StreetLens).andThen(Street.NameLens);

    final var streetName = Employee.CompanyLens.andThen(companyStreetName);

    final var streetNumber =
        Employee.CompanyLens.andThen(Company.AddressLens).andThen(Address.NumberLens);

    final var companyName = Employee.CompanyLens.andThen(Company.NameLens);

    // First, check that the getter methods work on lenses
    assertEquals("Rice", Company.NameLens.get(rice));
    assertEquals("Main", streetName.get(dan));
    assertEquals((Integer) 47, Employee.AgeLens.get(zoran));
    assertEquals((Integer) 6100, streetNumber.get(zoran));

    // Make some new employees by making changes to the existing ones
    final var danClone = companyName.set(streetName.set(dan, "Fannin"), "BCM");
    final var zoranClone = Employee.CompanyLens.set(zoran, uh);
    final var riceClone = Company.AddressLens.set(rice, Company.AddressLens.get(uh));

    // Check that the new employees and companies have the correct data
    assertEquals("Dan", Employee.NameLens.get(danClone));
    assertEquals("Fannin", streetName.get(danClone));
    assertEquals((Integer) 6100, streetNumber.get(danClone));
    assertEquals("BCM", companyName.get(danClone));
    assertEquals((Integer) 47, Employee.AgeLens.get(zoranClone));
    assertEquals((Integer) 4800, streetNumber.get(zoranClone));
    assertEquals("Calhoun", streetName.get(zoranClone));
    assertEquals("University of Houston", companyName.get(zoranClone));
    assertEquals("Calhoun", companyStreetName.get(riceClone));
  }
}
