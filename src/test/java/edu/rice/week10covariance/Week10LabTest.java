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

import static edu.rice.week10covariance.CarExamples.Car;
import static edu.rice.week10covariance.CarExamples.Chevy;
import static edu.rice.week10covariance.CarExamples.Corvette;
import static edu.rice.week10covariance.CarExamples.Porsche;
import static edu.rice.week10covariance.CarExamples.Porsche911;
import static io.vavr.control.Option.none;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class Week10LabTest {
  private final Seq<Porsche> carList1 = List.of(new Porsche911(1971), new Porsche("944", 1985));
  private final Seq<Corvette> carList2 = List.of(new Corvette(1985));
  private final Seq<Car> carList3 = List.of(new Corvette(1977), new Chevy("Citation", 1980));
  private final Seq<Car> carList4 = carList3.appendAll(carList1).appendAll(carList2);
  private final Seq<Car> carList5 = carList4.appendAll(carList4);

  /**
   * Find all cars from a list sharing the build year with a given car.
   *
   * @param list a list of cars
   * @param car the car whose model year we are looking for
   * @return the model of the first car found in the list with the same year as car, "" if nothing
   *     is found.
   */
  //  public static Object findSameYear(Object list,  Object car) {
  public static String findSameYear(Seq<? extends Car> list, Car car) {
    //    return null;
    return findAndApply(list, c -> c.model, c -> car.year == c.year).getOrElse("");
  }

  /**
   * Find the first element in a list satisfying a predicate test and then apply the given function
   * to it, returning an optional result, depending on whether the list entry was found.
   *
   * @param list : a generic list of things
   * @param function : a function to apply on the found element
   * @param test : a predicate that returns true if the element is found
   * @return an Option.some() of the result of applying function f to the found element, if an
   *     element is found, Option.none() otherwise
   */
  //  public static Object findAndApply(Object list, Object function, Object test) {
  public static <T, R> Option<R> findAndApply(
      Seq<? extends T> list, Function<? super T, ? extends R> function, Predicate<? super T> test) {
    return Option.narrow(list.filter(test).map(function).headOption());
    //    return null;
  }

  @Test
  public void testFindSameYear() {
    var car = new Car("Chevy", "Caprice", 1971);

    // TODO: Uncomment these
    assertEquals("911", findSameYear(carList1, car));
    assertEquals("", findSameYear(carList2, car));

    var porsche = new Porsche911(1980);
    assertEquals("Citation", findSameYear(carList5, porsche));

    // TODO: Comment this out
    //    fail();
  }

  @Test
  public void testFindAndApply() {
    Predicate<Car> isModernChevy = elem -> elem.make.equals("Chevy") && elem.year >= 1980;
    Function<Car, String> model = elem -> elem.model;

    // TODO: Uncomment these
    assertEquals(none(), findAndApply(carList1, model, isModernChevy));
    assertEquals("Corvette", findAndApply(carList2, model, isModernChevy).get());
    assertEquals("Citation", findAndApply(carList3, model, isModernChevy).get());
    assertEquals("Citation", findAndApply(carList4, model, isModernChevy).get());
    assertEquals("Citation", findAndApply(carList5, model, isModernChevy).get());

    var porsche = new Porsche("928", 1988);
    assertEquals(
        "911", findAndApply(carList5, x -> x.model, x -> x.make.equals(porsche.make)).get());

    assertEquals(
        Integer.valueOf(1985),
        findAndApply(carList1, x -> x.year, x -> x.model.equals("944")).get());
    assertEquals(
        Integer.valueOf(1985),
        findAndApply(carList5, x -> x.year, x -> x.model.equals("944")).get());

    Function<Porsche, Porsche911> to911 = elem -> new Porsche911(elem.year);
    assertEquals("Porsche", findAndApply(carList1, to911, x -> x.model.equals("944")).get().make);

    // TODO: comment this out
    //    fail();
  }
}
