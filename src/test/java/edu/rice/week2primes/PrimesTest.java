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

import static edu.rice.week2primes.Primes.allPrimes;
import static edu.rice.week2primes.Primes.isPrime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PrimesTest {

  @Test
  void basicPrimeNumberTests() {
    assertFalse(isPrime(1));

    assertTrue(isPrime(2));
    assertTrue(isPrime(3));
    assertTrue(isPrime(5));
    assertTrue(isPrime(7));
    assertTrue(isPrime(11));

    assertFalse(isPrime(0));
    assertFalse(isPrime(-1));
    assertFalse(isPrime(4));
    assertFalse(isPrime(6));
  }

  @Test
  void validatePrimesLessThanN() {
    assertEquals(List.of(2, 3, 5, 7, 11), allPrimes(11));
    assertEquals(
        List.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53), allPrimes(56));
  }
}
