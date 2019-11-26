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

package edu.rice.week2primes;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;

/** This class will help you compute all the prime numbers less than a given maximum. */
public class Primes {
  /** Checks if the given number is prime or not. Runs in O(sqrt n) time. */
  public static boolean isPrime(int number) {
    // Implementation hint: for any number to be composite (i.e., not prime), it must have
    // a factor less than or equal to its square root. Search for any possible factors
    // less than or equal to the square root. For extra speed, check for divisibility by 2 up front;
    // thereafter, only consider odd numbers. (Factor of two performance speedup!)

    // Useful things to know:
    // - The number one is not a prime.
    // - Math.sqrt() will return a floating point number, which might not be exact.
    // - Math.ceil() will round a floating point up to the nearest integer.
    // - Math.floor() will round a floating point down to the nearest integer.
    // - If you've got a floating point number that you know is an integer, perhaps
    //   because you got it from Math.ceil() or Math.floor(), then you can just cast
    //   it to an integer by putting (int) beforehand, and then store the resulting
    //   value in a local variable with an int type.

    final var maxFactor = (int) ceil(sqrt(number));

    if (number <= 0) {
      return false;
    }

    switch (number) {
      case 1:
        return false;
      case 2:
      case 3:
        return true;
      default:
        break;
    }

    if (number % 2 == 0) {
      return false;
    }

    for (var test = 3; test <= maxFactor; test += 2) {
      if (number % test == 0) {
        return false;
      }
    }

    return true;
  }

  /** Gets all primes less than or equal to a given maximum prime. Runs in O(n sqrt n) time. */
  public static List<Integer> allPrimes(int maxPrime) {
    // Make an empty array list and add numbers to it that are prime. Use isPrime().
    // Don't start coding this function until you've finished isPrime().

    var result = new ArrayList<Integer>();
    for (int i = 1; i <= maxPrime; i++) {
      if (isPrime(i)) {
        result.add(i);
      }
    }

    return result;
  }
}
