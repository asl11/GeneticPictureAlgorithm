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

import static edu.rice.lens.Lens.lens;

import edu.rice.lens.Lens;

public interface CarsAndLenses {
  /** The basic Car class, which other sorts of cars will subclass. */
  class Car {
    public final String make;
    public final String model;
    public final int year;

    /** Construct a new car of a given make, model, and year. */
    public Car(String make, String model, int year) {
      this.make = make;
      this.model = model;
      this.year = year;
    }

    public String honk() {
      return "Honk!";
    }

    public String fireMachineGuns() {
      return "Click.";
    }

    public static final Lens<Car, String> MakeLens =
        lens(car -> car.make, (car, newMake) -> new Car(newMake, car.model, car.year));

    public static final Lens<Car, String> ModelLens =
        lens(car -> car.model, (car, newModel) -> new Car(car.make, newModel, car.year));

    public static final Lens<Car, Integer> YearLens =
        lens(car -> car.year, (car, newYear) -> new Car(car.make, car.model, newYear));
  }

  /** Aston Martin: for the wealthy car buyer who wants to spend more money for less performance. */
  class AstonMartin extends Car {
    /** Construct a new Porsche of a given model, and year. */
    public AstonMartin(String model, int year) {
      super("Aston Martin", model, year);
    }

    public static final Lens<AstonMartin, String> ModelLens =
        lens(car -> car.model, (car, newModel) -> new AstonMartin(newModel, car.year));

    public static final Lens<AstonMartin, Integer> YearLens =
        lens(car -> car.year, (car, newYear) -> new AstonMartin(car.model, newYear));
  }

  /** Aston DB5: the classic. */
  class AstonMartinDB5 extends AstonMartin {
    /** Construct a new AstonMartinDB5 of a given year. */
    public AstonMartinDB5(int year) {
      super("DB5", year);
    }

    public static final Lens<AstonMartinDB5, Integer> YearLens =
        lens(car -> car.year, (car, newYear) -> new AstonMartinDB5(newYear));
  }

  /**
   * The paradigmatic DB5 for the paradigmatic secret agent. <a
   * href="http://www.autoblog.com/2010/10/27/james-bonds-aston-martin-db5-sells-for-4-6-million/">James
   * Bond's DB5: with machine guns!</a>
   */
  class BondDB5 extends AstonMartinDB5 {
    private int numBullets;

    public static final Lens<BondDB5, Integer> BulletsLens =
        lens(car -> car.numBullets, (car, numBullets) -> new BondDB5(numBullets));

    /** Construct a beautiful 1964 Aston Martin DB5, with machine guns. Shaken, not stirred. */
    public BondDB5(int numBullets) {
      super(1964);
      this.numBullets = numBullets;
    }

    @Override
    public String fireMachineGuns() {
      if (numBullets > 0) {
        numBullets--; // mutation!
        return "Bang!";
      } else {
        return super.fireMachineGuns(); // which, presumably doesn't do very much
      }
    }

    /**
     * Adds the given number of bullets to the BondDB5's weapons bay. If you'd prefer to leave the
     * original BondDB5 alone, you may prefer the {@link #BulletsLens} lens.
     */
    public void reloadBullets(int numBullets) {
      this.numBullets += numBullets;
    }
  }
}
