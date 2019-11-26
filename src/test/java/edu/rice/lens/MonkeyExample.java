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

package edu.rice.lens;

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.lens.Lens.lens;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.rice.json.Value;
import org.junit.jupiter.api.Test;

/**
 * Example code derived from: <a
 * href="http://davids-code.blogspot.com/2014/02/immutable-domain-and-lenses-in-java-8.html">http://davids-code.blogspot.com/2014/02/immutable-domain-and-lenses-in-java-8.html</a>
 *
 * <p>This class includes some nested data classes. Monkeys have names and hats. Those hats have
 * colors and feathers. The feathers also have a color. You can see that each class also defines a
 * series of lenses. So the Monkey class defines two lenses: HatLens and NameLens, each of which
 * have getters and functional setters. These lenses can be composed with other lenses, which you
 * can see in the unit tests at the bottom of the file.
 */
public class MonkeyExample {
  public static class Monkey {
    public static final Lens<Monkey, Hat> HatLens =
        lens(Monkey::getHat, (monkey, hat) -> new Monkey(monkey.name, hat));

    public static final Lens<Monkey, String> NameLens =
        lens(Monkey::getName, (monkey, name) -> new Monkey(name, monkey.hat));

    private final String name;
    private final Hat hat;

    public Monkey(String name, Hat hat) {
      this.name = name;
      this.hat = hat;
    }

    public String getName() {
      return name;
    }

    public Hat getHat() {
      return hat;
    }

    public Value toJson() {
      return jobject(jpair("name", name), jpair("hat", hat.toJson()));
    }

    @Override
    public String toString() {
      return toJson().toIndentedString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Monkey)) {
        return false;
      }

      var monkey = (Monkey) o;

      return monkey.name.equals(name) && monkey.hat.equals(hat);
    }

    @Override
    public int hashCode() {
      return name.hashCode() * 31 + hat.hashCode();
    }
  }

  public static class Hat {
    public static final Lens<Hat, Feather> FeatherLens =
        lens(Hat::getFeather, (hat, feather) -> new Hat(hat.color, feather));

    public static final Lens<Hat, Color> ColorLens =
        lens(Hat::getColor, (hat, color) -> new Hat(color, hat.feather));

    private final Color color;
    private final Feather feather;

    public Hat(Color color, Feather feather) {
      this.color = color;
      this.feather = feather;
    }

    public Color getColor() {
      return color;
    }

    public Feather getFeather() {
      return feather;
    }

    public Value toJson() {
      return jobject(jpair("color", color.toJson()), jpair("feather", feather.toJson()));
    }

    @Override
    public String toString() {
      return toJson().toIndentedString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Hat)) {
        return false;
      }

      var hat = (Hat) o;

      return hat.color.equals(color) && hat.feather.equals(feather);
    }

    @Override
    public int hashCode() {
      return color.hashCode() * 31 + feather.hashCode();
    }
  }

  public static class Feather {
    public static final Lens<Feather, Color> ColorLens =
        lens(Feather::getColor, (feather, color) -> new Feather(color));

    private final Color color;

    public Feather(Color color) {
      this.color = color;
    }

    public Color getColor() {
      return color;
    }

    public Value toJson() {
      return jobject(jpair("color", color.toJson()));
    }

    @Override
    public String toString() {
      return toJson().toIndentedString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Feather)) {
        return false;
      }

      var feather = (Feather) o;

      return feather.color.equals(color);
    }

    @Override
    public int hashCode() {
      return color.hashCode();
    }
  }

  public static class Color {
    public static final Lens<Color, String> NameLens =
        lens(Color::getName, (color, name) -> new Color(name));

    private final String name;

    public Color(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public Value toJson() {
      return jobject(jpair("name", name));
    }

    @Override
    public String toString() {
      return toJson().toIndentedString();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Color)) {
        return false;
      }

      var color = (Color) o;

      return color.name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

  // Here begin the unit tests

  final Value monkey1Json =
      jobject(
          jpair("name", "Ham"),
          jpair(
              "hat",
              jobject(
                  jpair("color", jobject(jpair("name", "yellow"))),
                  jpair("feather", jobject(jpair("color", jobject(jpair("name", "blue"))))))));

  final Monkey monkey1 =
      new Monkey("Ham", new Hat(new Color("yellow"), new Feather(new Color("blue"))));

  @Test
  public void testLenses() {
    assertEquals(monkey1Json, monkey1.toJson());

    // Here's a lens that zooms all the way in from the monkey to its hat to its feather to the
    // color of that feather!
    final var monkeyHatFeatherColor =
        Monkey.HatLens.andThen(Hat.FeatherLens).andThen(Feather.ColorLens);

    // We'll use that lens to update the color of the feather
    final var monkey2 = monkeyHatFeatherColor.set(monkey1, new Color("white"));

    // We'll verify that the original feather's color is unchanged
    assertEquals("blue", monkeyHatFeatherColor.get(monkey1).getName());

    // And the new feather's color is the same
    assertEquals("white", monkeyHatFeatherColor.get(monkey2).getName());

    // While we're at it, we'll verify the whole original monkey is unchanged
    assertEquals(monkey1Json, monkey1.toJson());

    // And, how about that whole "identity" lens?
    final var monkeyHatFeatherColor2 = monkeyHatFeatherColor.andThen(Lens.identity());
    assertEquals(monkey2, monkeyHatFeatherColor2.set(monkey1, new Color("white")));

    // an identity lens, when "setting" a value, returns the new value, ignoring the old one
    // entirely!
    assertEquals(monkey2, Lens.identity().set(monkey1, monkey2));
  }
}
