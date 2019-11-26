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

import static edu.rice.prettypictures.Allele.INTRINSIC_ALLELE_MAP;
import static edu.rice.prettypictures.Allele.constantRgb;
import static edu.rice.prettypictures.ExternalImageAlleles.EXTERNAL_IMAGE_ALLELE_MAP;
import static edu.rice.prettypictures.RgbColor.color;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.doubles;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quicktheories.core.Gen;

class AlleleTest {
  private static final String TAG = "AlleleTest";

  final RgbColor greenColor = color(-1, 1, -1);
  final RgbColor greyColor = color(0, 0, 0);
  final Allele greyConstant =
      constantRgb(greyColor); // "constants" have their color in the parameter
  final Allele cosine = INTRINSIC_ALLELE_MAP.apply("cosine");
  final Allele add = INTRINSIC_ALLELE_MAP.apply("add");
  final Allele div = INTRINSIC_ALLELE_MAP.apply("div");
  final Allele negate = INTRINSIC_ALLELE_MAP.apply("negate");
  final Allele green = INTRINSIC_ALLELE_MAP.apply("green");
  final Allele mexico = EXTERNAL_IMAGE_ALLELE_MAP.apply("cool-images/mexican-coat-of-arms.png");
  final Allele x = INTRINSIC_ALLELE_MAP.apply("x");
  final Allele y = INTRINSIC_ALLELE_MAP.apply("y");

  @Test
  void numArgs() {
    assertEquals(0, green.numChildren());
    assertEquals(1, cosine.numChildren());
    assertEquals(2, add.numChildren());
    assertEquals(2, mexico.numChildren());
    assertEquals(0, greyConstant.numChildren());
  }

  @Test
  void getName() {
    assertEquals("green", green.getName());
    assertEquals("cosine", cosine.getName());
    assertEquals("add", add.getName());
    assertEquals("constant-color", greyConstant.getName());
    assertEquals("external-image", mexico.getName());
  }

  @Test
  void getParams() {
    assertEquals("ffffff", constantRgb(color(0xffffff)).getParam());
    assertEquals(
        "808080", greyConstant.getParam()); // constantColors have their color in the parameter
    assertEquals("", green.getParam()); // but the intrinsic color genes don't
    assertEquals("", cosine.getParam()); // intrinsics like this have no parameters at all
    assertEquals("", add.getParam());
    assertEquals("cool-images/mexican-coat-of-arms.png", mexico.getParam());
  }

  @Test
  void testAssemble() {
    // green plus grey, should yield same result at any (x, y)
    var f1 =
        add.assemble(List.of(green.assemble(List.empty()), greyConstant.assemble(List.empty())));
    assertEquals(greenColor, f1.render(0, 0));
    assertEquals(greenColor, f1.render(.5, 0));
    assertEquals(greenColor, f1.render(0, .5));
    assertEquals(greenColor, f1.render(-.5, -.5));

    // -(x div y) (here, using the cleaner varargs variant)
    var f2 = negate.assemble(div.assemble(x.assemble(), y.assemble()));
    assertEquals(color(-0.5, -0.5, -0.5), f2.render(1, 2));
    assertEquals(color(2, 2, 2), f2.render(-2, 1));
    assertEquals(color(0, 0, 0), f2.render(-2, 0)); // make sure div-by-zero is handled correctly
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Engineering note: Below are two different ways of testing the
  // same property. What we want to verify is that rgb-to-ycrcb and
  // ycrcb-to-rgb, when composed together in either order, represent
  // the identity function. In the first version of the test, we have
  // a fixed list of six colors that we'll try, and we'll test four
  // different x, y locations, returning a list of dynamic tests,
  // i.e., a TestFactory.

  // The second test for the same property uses QuickTheories, with an
  // RgbColor generator as well as considering a wide variety of
  // different x and y coordinates.

  // Our initial implementation passed the "factory" version and
  // failed the "theory" version with the following output:

  // {RgbColor(0.000, 22.496, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 31.689, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 101.138, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 181.531, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 190.797, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 190.797, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 190.797, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 190.797, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 190.797, 0.000), 0.0, 0.0}
  // {RgbColor(0.000, 190.797, 0.000), 0.0, 0.0}

  // This suggests that there's something going wrong with our
  // handling of the "green" color channel!

  // But, if you look at the detailed message, it says that it
  // expected <0> and got <1.0505506939484377E-5>.  We were initially
  // enforcing a "delta" below of 0.00001, which is 1E-5, and we ended
  // up just slightly outside of that bound! Double-checking our
  // numbers for the alleles, we had the correct numbers.  No typos.
  // Ergo, we were being too precise in how "almost equal" we were
  // willing to be. Right?

  // Relaxing the "delta" even as far as 0.1, QuickTheories still
  // managed to find inputs that violated our bounds.  This, in turn,
  // led us away from the Wikipedia page to the *actual* JPEG specs,
  // which have the "real" equations
  // (http://www.itu.int/rec/T-REC-T.871) which have three decimal
  // places but also have division in them, yielding much longer and
  // uglier numbers if you wanted to spell them out in base-10. So we
  // put *those* in, and QuickTheories promptly found some failures
  // that only occurred when the blue value is huge:

  // {RgbColor(0.000, 0.000, 1001283914637784.600), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 1523680402428667.800), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 1693948657704459.200), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 10036780662617908.000), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 1007019825985188480.000), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 1105820348073036800.000), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 112249394756104320000.000), 0.0, 0.0}
  // {RgbColor(0.000, 0.000, 112249394756104320000.000), 6.9E-323, 0.0}
  // {RgbColor(0.000, 0.000, 7334355604801299000000.000), 6.9E-323, 0.0}
  // {RgbColor(0.000, 0.000, 7334355604801299000000.000), 2.77E-322, 0.0}

  // Now what? These numbers are so large that we're losing precision
  // when doing the multiplies.  Consequently, the inverses don't
  // match up. Will these occur in practice? Maybe. Does this behavior
  // represent a bug in our theory? Nope. The theory is proven for all
  // sorts of "reasonable" numbers, so we can respond by excluding
  // these extreme numbers from our theory, replacing doubles.any()
  // with something narrower and calling it a day.

  // Performance note: the "factory" version runs in 1ms. The "theory"
  // version runs in 69ms. The extra time comes because the theory
  // version tried 999 different test values. This additional cost is
  // firmly in the "so what, who cares?" zone, especially since it
  // helped us explore subtle bugs.
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Useful for unit testing: if the colors are "close enough" to being equal. */
  static void assertAlmostEquals(RgbColor expected, RgbColor actual) {
    final double delta = 0.000001;

    assertEquals(expected.r, actual.r, delta, "red");
    assertEquals(expected.g, actual.g, delta, "green");
    assertEquals(expected.b, actual.b, delta, "blue");
  }

  @TestFactory
  Seq<DynamicTest> yCrCbIdentityFactory() {
    var testColors =
        List.of(
            color(1, 1, 1),
            color(-1, -1, -1),
            color(0, 0, 0),
            color(.5, 0, -.5),
            color(-.5, .5, 0),
            color(-.2, -.7, -1));
    var toYCrCb = INTRINSIC_ALLELE_MAP.apply("rgb-to-ycrcb");
    var toRgb = INTRINSIC_ALLELE_MAP.apply("ycrcb-to-rgb");

    return testColors.map(
        c ->
            dynamicTest(
                c.toString(),
                () -> {
                  var identityFunc1 = toYCrCb.assemble(toRgb.assemble(constantRgb(c).assemble()));
                  var identityFunc2 = toRgb.assemble(toYCrCb.assemble(constantRgb(c).assemble()));

                  // these assembled functions ignore x and y
                  assertAlmostEquals(c, identityFunc1.render(0, 0));
                  assertAlmostEquals(c, identityFunc2.render(0, 0));

                  // but we'll test others as well
                  assertAlmostEquals(c, identityFunc1.render(1, 1));
                  assertAlmostEquals(c, identityFunc1.render(100, -100));
                }));
  }

  static Gen<Double> reasonableDoubles() {
    return doubles().between(-10000, 10000);
  }

  static Gen<RgbColor> reasonableColors() {
    // Gen.map() comes in many versions, here replicating the Gen<Double> for each of the three
    // arguments.
    return reasonableDoubles().map((r, g, b) -> RgbColor.color(r, g, b));
  }

  @Test
  void yCrCbIdentityTheory() {
    var toYCrCb = INTRINSIC_ALLELE_MAP.apply("rgb-to-ycrcb");
    var toRgb = INTRINSIC_ALLELE_MAP.apply("ycrcb-to-rgb");

    qt().forAll(reasonableColors(), reasonableDoubles(), reasonableDoubles())
        .checkAssert(
            (color, x, y) -> {
              var identityFunc1 = toYCrCb.assemble(toRgb.assemble(constantRgb(color).assemble()));
              var identityFunc2 = toRgb.assemble(toYCrCb.assemble(constantRgb(color).assemble()));

              // these assembled functions should return the same as their input color
              assertAlmostEquals(color, identityFunc1.render(x, y));
              assertAlmostEquals(color, identityFunc2.render(x, y));
            });
  }
}
