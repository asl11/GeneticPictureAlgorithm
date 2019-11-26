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

/** A simple class to hold employee info. */
public class Employee {
  private final String name;
  private final Integer age;
  private final Company company;

  /** Constructor. */
  public Employee(String name, Integer age, Company company) {
    this.name = name;
    this.age = age;
    this.company = company;
  }

  public static final Lens<Employee, String> NameLens =
      lens(
          employee -> employee.name,
          (employee, name) -> new Employee(name, employee.age, employee.company));

  public static final Lens<Employee, Integer> AgeLens =
      lens(
          employee -> employee.age,
          (employee, age) -> new Employee(employee.name, age, employee.company));

  public static final Lens<Employee, Company> CompanyLens =
      lens(
          employee -> employee.company,
          (employee, company) -> new Employee(employee.name, employee.age, company));
}
