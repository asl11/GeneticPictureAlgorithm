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

package edu.rice.week10covariance;

import static edu.rice.week10covariance.CarsAndLenses.AstonMartin;
import static edu.rice.week10covariance.CarsAndLenses.AstonMartinDB5;
import static edu.rice.week10covariance.CarsAndLenses.BondDB5;
import static edu.rice.week10covariance.CarsAndLenses.Car;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CarsAndLensesTest {
  final AstonMartin rapide = new AstonMartin("Rapide", 2017);
  final BondDB5 bondCar = new BondDB5(5);

  final AstonMartin newerRapide = AstonMartin.YearLens.set(rapide, 2018);
  final AstonMartin rapideS = AstonMartin.ModelLens.set(rapide, "Rapide S");
  final AstonMartin updatedBondCar = AstonMartinDB5.YearLens.set(bondCar, 2018);
  final AstonMartinDB5 updatedBondCar2 = AstonMartinDB5.YearLens.set(bondCar, 2018);
  // BondDB5 updatedBondCar3 = AstonMartinDB5.YearLens.set(bondCar, 2018);
  // -- type error, doesn't compile!

  @Test
  public void bondDB5Test() {
    // Note that when we use a lens for AstonMartinDB5 and run it on a
    // BondDB5, it doesn't know anything about the specialness of the
    // Bond model, so what we get back doesn't know how to shoot
    // anything.
    assertEquals("Click.", updatedBondCar.fireMachineGuns());
    assertEquals("Click.", updatedBondCar2.fireMachineGuns());

    // But the original Bond car has bullets
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Click.", bondCar.fireMachineGuns());

    assertEquals((Integer) 0, BondDB5.BulletsLens.get(bondCar));

    // And, while we're at it, notice how the broader lenses work just
    // fine even on a very specific car
    assertEquals("Aston Martin", Car.MakeLens.get(bondCar));
  }
}
