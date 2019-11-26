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

package edu.rice.week12mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class Week12LabTest {

  public static class Cities {
    private final Map<Integer, String> map;

    public Cities(Map<Integer, String> map) {
      this.map = map;
    }

    public void addCity(Integer zip, String name) {
      map.put(zip, name);
    }

    public boolean hasCity(String city) {
      return map.containsValue(city);
    }

    /**
     * Get a City given a zip code.
     *
     * @param zip code of the city
     * @return the String with the name of the city
     */
    public String cityFromZip(Integer zip) {
      String result = map.get(zip);
      return result == null ? "" : result;
    }
  }

  @Test
  public void testMockObject() {

    // Suppress the compiler warning about unchecked type assignment
    @SuppressWarnings("unchecked")
    Map<Integer, String> mockMap = mock(Map.class);

    Cities cities = new Cities(mockMap);

    // Some interactions with our cities object here
    cities.addCity(77001, "Houston");
    cities.addCity(75001, "Dallas");
    cities.addCity(73301, "Austin");
    cities.addCity(78006, "San Antonio");
    cities.addCity(79901, "El Paso");

    // TODO: Implement the behavior of the mockMap so that it passes the tests below
    when(mockMap.get(77001)).thenReturn("Houston");
    when(mockMap.get(73301)).thenReturn("Austin");
    when(mockMap.get(79901)).thenReturn("El Paso");

    assertEquals("Houston", cities.cityFromZip(77001));
    assertEquals("Austin", cities.cityFromZip(73301));
    assertEquals("El Paso", cities.cityFromZip(79901));
    assertEquals("", cities.cityFromZip(75001));
    assertEquals("", cities.cityFromZip(78006));

    // TODO: Verify that the get method was called exactly 5 times on the mockMap
    verify(mockMap, times(5)).get(anyInt());
  }

  @Test
  public void testSpyObject() {

    // Suppress the compiler warning about unchecked type assignment
    Map<Integer, String> spiedMap = new HashMap<>();
    var spyMap = spy(spiedMap);

    var cities = new Cities(spyMap);

    // Some interactions with our cities object here
    cities.addCity(77001, "Houston");
    cities.addCity(75001, "Dallas");
    cities.addCity(73301, "Austin");
    cities.addCity(78006, "San Antonio");
    cities.addCity(79901, "El Paso");

    // TODO: Verify that the put method was called exactly 5 times on the spyMap
    verify(spyMap, times(5)).put(anyInt(), anyString());

    // TODO: Using argument capture, verify that all key-value pairs above have been put into spyMap
    var argumentCaptorKey = ArgumentCaptor.forClass(Integer.class);
    var argumentCaptorValue = ArgumentCaptor.forClass(String.class);
    verify(spyMap, times(5)).put(argumentCaptorKey.capture(), argumentCaptorValue.capture());
    var keys = new Integer[5];
    argumentCaptorKey.getAllValues().toArray(keys);
    var values = new String[5];
    argumentCaptorValue.getAllValues().toArray(values);
    Integer[] expectedKeys = {77001, 75001, 73301, 78006, 79901};
    assertArrayEquals(expectedKeys, keys);
    String[] expectedValues = {"Houston", "Dallas", "Austin", "San Antonio", "El Paso"};
    assertArrayEquals(expectedValues, values);

    // TODO: Implement the augmented behavior of the spyMap so that it passes the tests below
    when(spyMap.get(90210)).thenReturn("Houston");
    when(spyMap.get(79901)).thenReturn("Dallas");
    when(spyMap.get(77584)).thenReturn("El Paso");

    assertEquals("Houston", cities.cityFromZip(90210));
    assertEquals("Houston", cities.cityFromZip(77001));
    assertEquals("Dallas", cities.cityFromZip(79901));
    assertEquals("Dallas", cities.cityFromZip(75001));
    assertEquals("Austin", cities.cityFromZip(73301));
    assertEquals("San Antonio", cities.cityFromZip(78006));
    assertEquals("El Paso", cities.cityFromZip(77584));
    assertEquals("", cities.cityFromZip(77005));
  }
}
