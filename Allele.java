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

package edu.rice.prettypictures;

import static edu.rice.prettypictures.RgbColor.color;
import static java.lang.Math.getExponent;
import static java.lang.Math.scalb;

import edu.rice.util.TriFunction;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * <b>al·lele</b> <i>/əˈlēl/</i> <i>noun</i> (GENETICS)
 *
 * <p><i>An allele is a variant form of a given gene. Sometimes, different alleles can result in
 * different observable phenotypic traits, such as different pigmentation. A good example of this
 * trait of color variation is the work Gregor Mendel did with the white and purple flower colors in
 * pea plants, discovering that each color was the result of a "pure line" trait which could be used
 * as a control for future experiments. However, most genetic variations result in little or no
 * observable variation.</i> (Definition via <a
 * href="https://en.wikipedia.org/wiki/Allele">Wikipedia</a>.)
 *
 * <p>Our Allele interface is very simple. Each Allele has a name ({@link #getName()} and a
 * parameter string ({@link #getParam()}. The parameter is commonly an empty string, except for
 * alleles where it makes sense to have something else, like a constant color's value or an external
 * image's filename.
 *
 * <p>We have a large number of helper methods to create alleles. They all take a lambda of some
 * form. For example, {@link #oneChild(String, UnaryOperator)} takes a function from double to
 * double, and adapts that function to operate piecewise on the red, green, and blue values in a
 * {@link RgbColor}. If you need to deal with the RgbColor instances directly, we have helpers like
 * {@link #oneChildRgb(String, UnaryOperator)}.
 *
 * <p>Once built, the only thing you can really do with an Allele is to assemble it via {@link
 * #assemble(Seq)} or {@link #assemble(ImageFunction...)} with other Alleles to yield an {@link
 * ImageFunction} suitable for rendering.
 *
 * <p>Equality and hashCode of Alleles are based on the name and parameter. The lambdas are ignored.
 *
 * <p>Notably absent from the Allele class is any support to save, load, mutate, or breed trees of
 * Alleles. These features are supported separately; you'll implement them in the second week of the
 * pretty-pictures project.
 */
public interface Allele {
  OpenSimplexNoise simplexNoise = new OpenSimplexNoise(); // using default random seed

  /** Convenient mapping from name to individual genes. */
  Map<String, Allele> INTRINSIC_ALLELE_MAP =
      List.of(
              oneChild("sine", Math::sin),
              oneChild("cosine", Math::cos),
              oneChild("arctan", Math::atan),
              oneChild("tanh", Math::tanh),
              oneChild("exp", Math::exp),
              oneChild("log", Math::log),
              oneChild("negate", x -> -x),

              // some decidedly non-linear functions: these can lead to visually interesting results
              oneChild("abs", Math::abs),
              twoChild("max", Math::max),
              twoChild("min", Math::min),

              // like Math::signum, but without the zero in the middle
              oneChild("sign", x -> x < 0.0 ? -1.0 : 1.0),
              oneChild("clamp-negative", x -> x < 0.0 ? 0.0 : x),
              oneChild("clamp-positive", x -> x > 0.0 ? 0.0 : x),
              oneChild("floor", Math::floor),
              oneChild("ceiling", Math::ceil),
              oneChild(
                  "wrap",
                  x -> {
                    var y = x / 2 + 0.5;
                    var z = y - Math.floor(y);
                    return z * 2 - 1;
                  }),
              oneChild(
                  "clip",
                  x -> {
                    if (x > 1.0) {
                      return 1.0;
                    } else if (x < -1.0) {
                      return -1.0;
                    } else {
                      return x;
                    }
                  }),
              oneChild("mantissa", Allele::getMantissa),

              // Here's the official spec from the ITU for JPEG:
              // (http://www.itu.int/rec/T-REC-T.871)
              // Y = Min( Max( 0, Round( 0.299 * R + 0.587 * G + 0.114 * B ) ), 255 )
              // CB = Min(Max(0,Round(( −0.299*R −0.587*G +0.886*B )/1.772 +128 )),255)
              // CR = Min(Max(0,Round(( 0.701*R −0.587*G −0.114*B )/1.402 +128 )),255)
              oneChildRgb(
                  "rgb-to-ycrcb",
                  in ->
                      color(
                          .299 * in.r + .587 * in.g + .114 * in.b,
                          .701 / 1.402 * in.r - .587 / 1.402 * in.g - .114 / 1.402 * in.b,
                          -.299 / 1.772 * in.r - .587 / 1.772 * in.g + .886 / 1.772 * in.b)),

              // More ITU official specs:
              // R = Min(Max(0,Round(Y +1.402*(CR −128) )),255)
              // G = Min(Max(0,Round(Y−(0.114*1.772*(CB −128)+0.299*1.402*(CR −128))/0.587)),255)
              // B = Min(Max(0,Round(Y +1.772*(CB −128) )),255)

              oneChildRgb(
                  "ycrcb-to-rgb",
                  in ->
                      color(
                          in.r + 1.402 * in.g,
                          in.r - .299 * 1.402 / .587 * in.g - .114 * 1.772 / .587 * in.b,
                          in.r + 1.772 * in.b)),
              oneChildRgb(
                  "grey-perlin",
                  in -> {
                    // single RGB as coordinates for the noise function
                    var noiseVal = simplexNoise.eval(in.r, in.g, in.b);
                    return color(noiseVal, noiseVal, noiseVal); // greyscale output
                  }),

              // this will call the noise function three times, piecewise
              threeChild("color-perlin", simplexNoise::eval),

              // red channel --> all channels
              oneChildRgb("red-channel", in -> color(in.r, in.r, in.r)),
              // green channel --> all channels
              oneChildRgb("green-channel", in -> color(in.g, in.g, in.g)),
              // blue channel --> all channels
              oneChildRgb("blue-channel", in -> color(in.b, in.b, in.b)),

              // red from input1, green from input2, blue from input3
              threeChildRgb("color-mix", (in1, in2, in3) -> color(in1.r, in2.g, in3.b)),
              twoChild("arctan2", Math::atan2),
              twoChild("div", (x, y) -> (y == 0.0) ? 0 : x / y),
              twoChild("mul", (x, y) -> x * y),
              twoChild("add", (x, y) -> x + y),
              twoChild("sub", (x, y) -> x - y),
              twoChildRgb(
                  "inner-product", (c1, c2) -> color(c1.r * c2.r, c1.g * c2.g, c1.b * c2.b)),
              threeChild("dissolve", (x, y, t) -> (1.0 - t) * x + t * y),
              zeroChild("x", (x, y) -> color(x, x, x)),
              zeroChild("y", (x, y) -> color(y, y, y)),
              zeroChild("0xy", (x, y) -> color(0, x, y)),
              zeroChild("0yx", (x, y) -> color(0, y, x)),
              zeroChild("x0y", (x, y) -> color(x, 0, y)),
              zeroChild("y0x", (x, y) -> color(y, 0, x)),
              zeroChild("xy0", (x, y) -> color(x, y, 0)),
              zeroChild("yx0", (x, y) -> color(y, x, 0)),
              zeroChild("black", (x, y) -> color(-1, -1, -1)),
              zeroChild("white", (x, y) -> color(1, 1, 1)),
              zeroChild("red", (x, y) -> color(1, -1, -1)),
              zeroChild("green", (x, y) -> color(-1, 1, -1)),
              zeroChild("blue", (x, y) -> color(-1, -1, 1)))
          .toMap(Allele::getName, imageGene -> imageGene);

  /** Describes how many children this particular gene requires. */
  int numChildren();

  /** Every gene has a name. */
  String getName();

  /**
   * Some genes have a "parameter" that is used to initialize them, such as a file name for
   * external-images. By default, it's an empty string.
   */
  default String getParam() {
    return "";
  }

  /** Allows for combining different primitives into an ImageFunction. */
  default ImageFunction assemble(ImageFunction... children) {
    return assemble(List.of(children));
  }

  /** Allows for combining different primitives into an ImageFunction. */
  ImageFunction assemble(Seq<ImageFunction> children);

  /**
   * Given a function of (z x, y) -- the actual (x, y) Cartesian coordinates of the pixel being
   * evaluated -- returns an allele that accepts no children when being assembled. Useful for leaf
   * nodes that want to use the x or y coordinates.
   */
  static Allele zeroChild(String name, BiFunction<Double, Double, RgbColor> operator) {
    return zeroChild(name, "", operator);
  }

  /**
   * Given a function of (x, y) -- the actual (x, y) Cartesian coordinates of the pixel being
   * evaluated -- returns an allele that accepts no children when being assembled. Useful for leaf
   * nodes that want to use the x or y coordinates but also have a parameter string.
   */
  static Allele zeroChild(
      String name, String parameter, BiFunction<Double, Double, RgbColor> operator) {
    return new ZeroChild(name, parameter, operator);
  }

  /**
   * Given a unary operator on doubles (e.g., sine, cosine), return an allele that accepts one child
   * when being assembled, treating that child's RGB output as a three-vector and applying the given
   * operator piecewise.
   */
  static Allele oneChild(String name, UnaryOperator<Double> operator) {
    return oneChild(name, "", operator);
  }

  /**
   * Given a unary operator on doubles (e.g., sine, cosine), return an allele that accepts one child
   * when being assembled, treating that child's RGB output as a three-vector and applying the given
   * operator piecewise.
   */
  static Allele oneChild(String name, String parameter, UnaryOperator<Double> operator) {
    return oneChildRgb(
        name,
        parameter,
        rgb -> color(operator.apply(rgb.r), operator.apply(rgb.g), operator.apply(rgb.b)));
  }

  /**
   * Given a general-purpose unary operator on RgbColor, return an allele that accepts one child
   * when being assembled, using the given operator.
   */
  static Allele oneChildRgb(String name, UnaryOperator<RgbColor> operator) {
    return oneChildRgb(name, "", operator);
  }

  /**
   * Given a general-purpose unary operator on RgbColor, return an allele that accepts one child
   * when being assembled, using the given operator.
   */
  static Allele oneChildRgb(String name, String parameter, UnaryOperator<RgbColor> operator) {
    return new OneChild(name, parameter, operator);
  }

  /**
   * Given a binary operator on doubles (e.g., add, multiply), return an allele that accepts two
   * children when being assembled, treating the children's RGB outputs as three-vectors and
   * applying the given operator piecewise.
   */
  static Allele twoChild(String name, BinaryOperator<Double> operator) {
    return twoChild(name, "", operator);
  }

  /**
   * Given a binary operator on doubles (e.g., add, multiply), return an allele that accepts two
   * children when being assembled, treating the children's RGB outputs as three-vectors and
   * applying the given operator piecewise.
   */
  static Allele twoChild(String name, String parameter, BinaryOperator<Double> operator) {
    return twoChildRgb(
        name,
        parameter,
        (rgb0, rgb1) ->
            color(
                operator.apply(rgb0.r, rgb1.r),
                operator.apply(rgb0.g, rgb1.g),
                operator.apply(rgb0.b, rgb1.b)));
  }

  /**
   * Given a general-purpose binary operator on RgbColor, return an allele that accepts two children
   * when being assembled, using the given operator to combine them.
   */
  static Allele twoChildRgb(String name, BinaryOperator<RgbColor> operator) {
    return twoChildRgb(name, "", operator);
  }

  /**
   * Given a general-purpose binary operator on RgbColor, return an allele that accepts two children
   * when being assembled, using the given operator to combine them.
   */
  static Allele twoChildRgb(String name, String parameter, BinaryOperator<RgbColor> operator) {
    return new TwoChild(name, parameter, operator);
  }

  /**
   * Given a trinary operator on doubles, return an allele that accepts three children when being
   * assembled, treating the children's RGB outputs as three-vectors and applying the given operator
   * piecewise.
   */
  static Allele threeChild(String name, TriFunction<Double, Double, Double, Double> operator) {
    return threeChildRgb(
        name,
        (rgb0, rgb1, rgb2) ->
            color(
                operator.apply(rgb0.r, rgb1.r, rgb2.r),
                operator.apply(rgb0.g, rgb1.g, rgb2.g),
                operator.apply(rgb0.b, rgb1.b, rgb2.b)));
  }

  /**
   * Given a general-purpose trinary operator on RgbColor, return an allele that accepts three
   * children when being assembled, using the given operator to combine them.
   */
  static Allele threeChildRgb(
      String name, TriFunction<RgbColor, RgbColor, RgbColor, RgbColor> operator) {
    return new ThreeChild(name, operator);
  }

  /** Given an {@link RgbColor}, yields an Allele that renders to exactly that color. */
  static Allele constantRgb(RgbColor color) {
    return new ZeroChild("constant-color", color.toHexColor(), (x, y) -> color);
  }

  /** Given a number, yields an Allele that uses that number for all three color channels. */
  static Allele constantNumber(double number) {
    return new ZeroChild(
        "constant-number", Double.toString(number), (x, y) -> color(number, number, number));
  }

  /**
   * Handy routine to get the mantissa from a double, adapted for use as an allele. Source: <a
   * href="https://www.ibm.com/developerworks/library/j-math2/">
   * https://www.ibm.com/developerworks/library/j-math2/</a>
   */
  static double getMantissa(double x) {
    var exponent = getExponent(x);
    var result = x / scalb(1.0, exponent);

    // Mantissas range from 1.0 to 1.999 or -1.0 to -1.999, except the mantissa of 0 is 0.
    // To make it prettier, we'll drag these numbers to fit within [-1, 1].
    if (result == 0.0) {
      return 0.0;
    } else if (result < 0.0) {
      return result + 1.0;
    } else {
      return result - 1.0;
    }
  }

  abstract class AnyChild implements Allele {
    protected final String name;
    protected final String parameter;

    private AnyChild(String name, String parameter) {
      this.name = name;
      this.parameter = parameter;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getParam() {
      return parameter;
    }

    @Override
    public String toString() {
      String param = getParam();
      return "Gene(" + name + (param.equals("") ? "" : ", " + param) + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }

      if (!(o instanceof Allele)) {
        return false;
      }

      Allele other = (Allele) o;

      if (other.getName().equals("constant-color") && getName().equals("constant-color")) {
        // special-case handling, since colors have their own equals methods that has
        // some slack to deal with roundoff error

        return this.assemble().render(0, 0).equals(other.assemble().render(0, 0));
      } else {
        return o.toString().equals(toString());
      }
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }
  }

  class ZeroChild extends AnyChild {
    protected final BiFunction<Double, Double, RgbColor> operator;

    private ZeroChild(
        String name, String parameter, BiFunction<Double, Double, RgbColor> operator) {
      super(name, parameter);
      this.operator = operator;
    }

    @Override
    public int numChildren() {
      return 0;
    }

    @Override
    public ImageFunction assemble(Seq<ImageFunction> children) {
      if (!children.isEmpty()) {
        throw new RuntimeException("exactly zero children required");
      }

      return operator::apply;
    }
  }

  class OneChild extends AnyChild {
    protected final UnaryOperator<RgbColor> operator;

    private OneChild(String name, String parameter, UnaryOperator<RgbColor> operator) {
      super(name, parameter);
      this.operator = operator;
    }

    @Override
    public int numChildren() {
      return 1;
    }

    @Override
    public ImageFunction assemble(Seq<ImageFunction> children) {
      if (children.length() != 1) {
        throw new RuntimeException("exactly one child is required");
      }

      return (x, y) -> operator.apply(children.head().render(x, y));
    }
  }

  class TwoChild extends AnyChild {
    protected final BinaryOperator<RgbColor> operator;

    private TwoChild(String name, String parameter, BinaryOperator<RgbColor> operator) {
      super(name, parameter);
      this.operator = operator;
    }

    @Override
    public int numChildren() {
      return 2;
    }

    @Override
    public ImageFunction assemble(Seq<ImageFunction> children) {
      if (children.length() != 2) {
        throw new RuntimeException("exactly two children required");
      }

      var child0 = children.head();
      var child1 = children.get(1);
      return (x, y) -> operator.apply(child0.render(x, y), child1.render(x, y));
    }
  }

  class ThreeChild extends AnyChild {
    private final TriFunction<RgbColor, RgbColor, RgbColor, RgbColor> operator;

    private ThreeChild(String name, TriFunction<RgbColor, RgbColor, RgbColor, RgbColor> operator) {
      super(name, "");
      this.operator = operator;
    }

    @Override
    public int numChildren() {
      return 3;
    }

    @Override
    public ImageFunction assemble(Seq<ImageFunction> children) {
      if (children.length() != 3) {
        throw new RuntimeException("exactly three children required");
      }

      var child0 = children.head();
      var child1 = children.get(1);
      var child2 = children.get(2);
      return (x, y) ->
          operator.apply(child0.render(x, y), child1.render(x, y), child2.render(x, y));
    }
  }
}
