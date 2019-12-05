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

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jnumber;
import static edu.rice.json.Builders.jstring;
import static edu.rice.json.Value.JArray;
import static edu.rice.json.Value.JString;
import static edu.rice.prettypictures.Allele.INTRINSIC_ALLELE_MAP;
import static edu.rice.prettypictures.Allele.constantNumber;
import static edu.rice.prettypictures.Allele.constantRgb;
import static edu.rice.prettypictures.ExternalImageAlleles.EXTERNAL_IMAGE_ALLELE_MAP;
import static edu.rice.prettypictures.RgbColor.color;
import static edu.rice.util.Strings.stringToOptionDouble;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.json.Value;
import edu.rice.lens.Lens;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.Random;
import java.util.function.Supplier;

/**
 * A GeneTree is our <b>genotype</b> data structure (i.e., trees of {@link Allele}). It includes
 * conversion to and from JSON (via {@link #toJson()} and {@link #of(Value)}, respectively) as well
 * conversion to an {@link ImageFunction} via {@link #toImageFunction()}.
 *
 * <p>This class also includes a number of handy builder-methods:
 *
 * <ul>
 *   <li>{@link #constantColorTree(RgbColor)} makes "leaf" functions of a given constant color
 *   <li>{@link #geneLeaf(String)} makes zero-argument "leaf" image-functions from a gene of the
 *       given name
 *   <li>{@link #geneTree(String, GeneTree...)} makes one-or-more-argument image-functions from a
 *       gene of the given name, with a varargs constructor to pass in the children.
 *   <li>{@link #geneTree(String, Option[])} lets you pass in Option&lt;GeneTree&gt; rather than
 *       GeneTree, connecting nicely with the Option&lt;GeneTree&gt; outputs of most builder-methods
 *       here.
 *   <li>{@link #geneTree(String, Seq)} lets you pass in a list of children.
 *   <li>{@link #externalImageTree(String, GeneTree, GeneTree)} lets you load an external image and
 *       takes two children
 *   <li>{@link #externalImageTree(String, Seq)} and {@link #externalImageTree(String, Option,
 *       Option)} provide list and option-based alternatives, as with geneTree.
 * </ul>
 *
 * <p>Generally speaking, all of these types return <code>Option&lt;GeneTree&gt;</code> rather than
 * <code>GeneTree</code>, even if they're unlikely to ever fail. Why? Because many of these methods,
 * and others, take <code>Option&lt;GeneTree&gt;</code> as inputs as well. This design allows you to
 * have a large, recursive expression to construct GeneTrees and each builder method will
 * (monadically!) absorb any errors and continue onward.
 */
public class GeneTree {
  private static final String TAG = "GeneTree";

  // Data definition! A GeneTree is:
  // - An Allele of N args and a list of N GeneTree children
  // - At the leaves of the tree, N = 0, so the list will be empty

  // Invariant: the gene's number of arguments and the length of the
  // list will be the same.

  // The design decision to have the children represented by
  // List<GeneTree> simplifies the process of handling trees with
  // varying numbers of children per node. We can handle all of them
  // with only one GeneTree class! Also, This design pairs nicely with
  // the way Allele.assemble() works, since one of its variants takes
  // a list of children.

  // The data definition's invariant is checked by every maker-method
  // below.  This ensures that every GeneTree is "well-formed" and
  // thus, any issues with incorrect numbers of children will be
  // discovered immediately during GeneTree construction rather than
  // later on when we're rendering images.

  private Random random = new Random();
  private final Allele gene;
  private final Seq<GeneTree> children;
  private final Supplier<ImageFunction> imageFunctionMemo; // lazy: we only compute it once

  private GeneTree(Allele gene, Seq<GeneTree> children) {
    this.gene = gene;
    this.children = children;

    // the recursion will terminate when we hit a "leaf" node with no children, and the map will do
    // nothing
    imageFunctionMemo = Lazy.of(() -> gene.assemble(children.map(GeneTree::toImageFunction)));
  }

  /** Builder-method to get a zero-argument gene (a "leaf" in a gene tree). */
  public static Option<GeneTree> geneLeaf(String geneName) {
    return geneTree(geneName, List.empty());
  }

  /**
   * Builder-method to help construct a GeneTree, here using a gene by its name, and allowing the
   * children to be Option&lt;GeneTree&gt;. This builder makes it easier to construct complex trees
   * without having to worry about calling {@link Option#get()} everywhere.
   *
   * <p>If any of the children are {@link Option#none()}, then this call to geneTree also return
   * {@link Option#none()}. Likewise, if the number of children isn't the same as the requested gene
   * expects, {@link Option#none()} is returned.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static Option<GeneTree> geneTree(String geneName, Option<GeneTree>... ochildren) {
    return INTRINSIC_ALLELE_MAP
        .get(geneName)
        .flatMap(gene -> geneTreeOption(gene, List.of(ochildren)));
  }

  /**
   * Builder-method to help construct a GeneTree, here using a gene by its name. If the number of
   * children isn't the same as the requested gene expects, {@link Option#none()} is returned.
   */
  public static Option<GeneTree> geneTree(String geneName, GeneTree... children) {
    return geneTree(geneName, List.of(children));
  }

  /**
   * Builder-method to help construct a GeneTree, here using a gene by its name. If the number of
   * children isn't the same as the requested gene expects, {@link Option#none()} is returned.
   */
  public static Option<GeneTree> geneTree(String geneName, Seq<GeneTree> children) {
    return INTRINSIC_ALLELE_MAP.get(geneName).flatMap(gene -> geneTree(gene, children));
  }

  /**
   * Builder-method to help construct a GeneTree, here using an {@link Allele} rather than a name.
   * This can be helpful when you're already working with Allele instances. This method ensures that
   * the list of children is the proper length for the gene, otherwise it return {@link
   * Option#none()}.
   */
  public static Option<GeneTree> geneTree(Allele gene, Seq<GeneTree> children) {
    return children.length() == gene.numChildren() ? some(new GeneTree(gene, children)) : none();
  }

  /**
   * Builder-method to help construct a GeneTree, here using Option&lt;Allele&gt; rather than a
   * name. This method ensures that the list of children is the proper length for the gene, and all
   * of them are {@link Option#some(Object)}.
   */
  public static Option<GeneTree> geneTreeOption(Allele gene, Seq<Option<GeneTree>> ochildren) {
    var children = ochildren.flatMap(x -> x); // filters out any Option.none() from the children
    return ochildren.length() == gene.numChildren() && children.length() == gene.numChildren()
        ? geneTree(gene, children)
        : none();
  }

  /**
   * Builder-method to help construct a GeneTree, here using an external image by file name, and
   * allowing the children to be Option&lt;GeneTree&gt;. If any of the children are {@link
   * Option#none()}, then this call to geneTree also return {@link Option#none()}. This builder
   * makes it easier to construct complex trees without having to worry about calling {@link
   * Option#get()} everywhere.
   */
  public static Option<GeneTree> externalImageTree(
      String filename, Option<GeneTree> ochildX, Option<GeneTree> ochildY) {
    var children = List.of(ochildX, ochildY).flatMap(x -> x);
    return externalImageTree(filename, children);
  }

  /**
   * Builder-method to help construct a GeneTree, here using an external image with a given
   * filename.
   */
  public static Option<GeneTree> externalImageTree(
      String filename, GeneTree childX, GeneTree childY) {
    return externalImageTree(filename, List.of(childX, childY));
  }

  /**
   * Builder-method to help construct a GeneTree, here using an external image with a given
   * filename.
   */
  public static Option<GeneTree> externalImageTree(String filename, Seq<GeneTree> children) {
    return children.length() == 2
        ? EXTERNAL_IMAGE_ALLELE_MAP.get(filename).flatMap(gene -> geneTree(gene, children))
        : none();
  }

  /** Builder-method to help construct a GeneTree, here using a color constant. */
  public static Option<GeneTree> constantColorTree(RgbColor color) {
    return some(new GeneTree(constantRgb(color), List.empty()));
  }

  /** Builder-method to help construct a GeneTree, here using a number constant. */
  public static Option<GeneTree> constantNumberTree(double number) {
    return some(new GeneTree(constantNumber(number), List.empty()));
  }

  /** Converts a GeneTree to an ImageFunction, suitable for rendering to a BufferedImage. */
  public ImageFunction toImageFunction() {
    // we only want to compute this once; allows the JVM to do some optimizations!
    return imageFunctionMemo.get();
  }

  /** Returns a JSON representation of this GeneTree. */
  public Value toJson() {
    switch (gene.getName()) {
      case "external-image": // external-image file-name x-child y-child
        return jarray(
            children
                .map(GeneTree::toJson)
                .prepend(jstring(gene.getParam())) // file name stored in gene parameter
                .prepend(jstring("external-image")));

        // Engineering note: We made an important design decision here
        // that you should notice. The underlying RgbColor class has
        // an internal representation of three doubles (r, g, b), but
        // we have two separate genes that build on this:
        // constant-color and constant-number, as made by
        // constantRgb() and constantNumber(), respectively. The
        // RgbColor class has no idea it's being used in two very
        // different ways, so we can't just put a toJson() method in
        // there and expect it work, nor do we want to have two
        // separate implementations of a hypothetical RgbColor
        // interface to manage these differences. Instead, we're
        // handling the distinction between constant-color and
        // constant-number entirely within the GeneTree class. This
        // design decision may end up complicating things later on
        // when we deal with mutating and cross-breeding a GeneTree.

        // Another design alternative could have been to put our JSON
        // logic into the Allele class, but we decided that we wanted
        // to separate the logic for managing the *expression* of
        // genes (i.e., getting out an ImageFunction that we can
        // render) from the logic for managing *trees* of genes (i.e.,
        // reading, writing, mutating, and cross-breeding). We did
        // include a String parameter in the ImageGenes to hang onto
        // something externally useful like a filename or color
        // constant, which turns out to be very handy, as you can see
        // from all the gene.getParam() calls in this class.

      case "constant-color": // constant "rrggbb"
        // We store the color, as a hexString, in the "param" field.
        return jarray(jstring("constant-color"), jstring(gene.getParam()));

      case "constant-number": // constant n.nnnnn
        // In this case, the parameter has our number, represented as
        // a string, which we'll convert back to a number; We're going
        // to assume that this string is a well-formed number and
        // ignore possible errors here.  This means that we need to be
        // super careful to make sure garbage never gets into the
        // param.
        return jarray(
            jstring("constant-number"), jnumber(stringToOptionDouble(gene.getParam()).get()));

      default: // gene-name child1 child2 ...
        return jarray(children.map(GeneTree::toJson).prepend(jstring(gene.getName())));
    }
  }

  /** Given a JSON representation, convert back to a GeneTree, with tons of error checking. */
  public static Option<GeneTree> of(Value json) {
    // Engineering note: you'll notice that this method doesn't follow
    // our usual War Against If-Statements.  But what if it did? We'd
    // have three levels deep of match/map/flatMap before we got to
    // the switch.  And then we'd have no easy way of dealing with the
    // length checks on the lists.

    // If we really, really wanted to force things, we could keep
    // everything inside an Option, and then apply a series of
    // filter() and flatMap() operators. Even then, we'd either have
    // to find a way to have deep nesting (so we could see earlier
    // values that showed up in lambda parameters) or we'd still end
    // up introducing code blocks with curly braces.

    // Sometimes, "straight line code" like this is the preferable
    // alternative. Even then, we're still careful to write the code
    // in a "functional" way, never overwriting any variable after
    // creating it.

    // Here's a strawman alternative design. We could have treated
    // these three cases in separate classes, each with its own
    // builder method. We did something like this in our JSON value
    // and parser design, with completely distinct parsers for each
    // subtype of JSON. Sadly, this would have resulted in a ton of
    // code repetition, as each gene-builder would need all the same
    // initial logic to tear apart the JSON array. Note that we *do*
    // have separate JSON logic inside RgbColor that we delegate to
    // when handling the "constant-color" gene.

    final var contents = json.asJArrayOption().map(JArray::getSeq).getOrElse(List.empty());
    if (contents.isEmpty()) {
      return none();
    }

    final var ogeneName = contents.head().asJStringOption().map(JString::toUnescapedString);
    if (ogeneName.isEmpty()) {
      return none();
    }

    // Engineering note: for some of you, this will be the first time
    // you've seen a switch statement used quite like this. Unlike C,
    // Java's switch statement understands strings, letting you avoid
    // having to construct an if-then-else chain. As with C, Java's
    // switch statement is not an *expression*.  You can't write x =
    // switch(...), but if you use a switch statement full of return
    // statements, as we do below, then the switch statement becomes a
    // helpful way to split a function like GeneTree.of() into its
    // constituent parts, acting in many ways like the "structured
    // pattern matching" that we introduced earlier in the semester.

    final var geneName = ogeneName.get();
    final var contentLength = contents.length();
    switch (geneName) {
      case "external-image": // external-image file-name x-child y-child
        if (contentLength != 4) {
          return none();
        }

        final var ofileName = contents.get(1).asJStringOption().map(JString::toUnescapedString);
        if (ofileName.isEmpty()) {
          return none();
        }

        return externalImageTree(ofileName.get(), contents.drop(2).flatMap(GeneTree::of));

      case "constant-number": // constant-number n.nnnn
        if (contentLength != 2) {
          return none();
        }

        return contents.get(1).asJNumberOption().flatMap(n -> constantNumberTree(n.get()));

      case "constant-color": // constant-color "rrggbb"
        if (contentLength != 2) {
          return none();
        }

        // leveraging the color builder's handy JSON input
        return color(contents.get(1)).flatMap(GeneTree::constantColorTree);

      default: // gene-name child1 child2 ...
        final var children = contents.tail();
        final var childTrees = children.flatMap(GeneTree::of);

        // if any children failed to parse, then we fail
        if (children.length() != childTrees.length()) {
          return none();
        }

        return geneTree(geneName, childTrees);
    }
  }

  /** Returns the maximum depth of the gene-tree. Useful for unit testing. */
  public int maxDepth() {
    // if there are no children, this returns 1
    return 1 + children.map(GeneTree::maxDepth).foldLeft(0, Integer::max);
  }

  /** Returns the total number of GeneTree nodes and leaves within this tree. */
  public int numNodes() {
    return 1 + children.map(GeneTree::numNodes).sum().intValue();
  }

  //////////////////////////////////////////////////// Here begins the week 3 section

  ///////////// Step 1: Build getters and setters for the gene, and for the children.
  public Allele getGene() {
    return this.gene;
  }

  public Seq<GeneTree> getChildren() {
    return this.children;
  }

  public GeneTree setGene(Allele input) {
    return new GeneTree(input, this.children);
  }

  public GeneTree setChildren(Seq<GeneTree> children) {
    GeneTree randchild = children.get(random.nextInt(children.length()));
    int index = random.nextInt(this.children.length());
    Seq<GeneTree> newchildren = this.children.replace(this.children.get(index), randchild);
    return new GeneTree(this.gene, newchildren);
  }

  ///////////// Step 2: Build lenses that can compose to allow getting/setting/updating deep into a
  // GeneTree.

  public final Lens<GeneTree, Allele> geneLens = Lens.lens(GeneTree::getGene, GeneTree::setGene);

  public final Lens<GeneTree, Seq<GeneTree>> childrenLens =
      Lens.lens(GeneTree::getChildren, GeneTree::setChildren);

  ///////////// Step 3: Build a getRandomLens() function that will point to a valid GeneTree within
  // "this" current GeneTree.

  /*
  getRandomLens return a tuple of lenses, where the first element is a genelens and the second element
  is a childrenlens
   */
  public Tuple2<Lens<GeneTree, Allele>, Lens<GeneTree, Seq<GeneTree>>> getRandomLens() {
    var json = this.toJson();
    return this.children.length() != 0
        ? getRandomLensHelper(json)
        : Tuple.of(geneLens, childrenLens);
  }

  private Tuple2<Lens<GeneTree, Allele>, Lens<GeneTree, Seq<GeneTree>>> getRandomLensHelper(
      Value input) {
    // System.out.println(input);
    Seq<Value> inputList = input.asJArray().getSeq();
    int index = random.nextInt(inputList.length());
    if (!inputList.get(index).asJArrayOption().isDefined()) {
      GeneTree temp = GeneTree.of(input).get();
      // System.out.println(temp);
      return Tuple.of(temp.geneLens, temp.childrenLens);
    } else {
      return getRandomLensHelper(inputList.get(index));
    }
  }

  ///////////// Step 4: Build a crossBreed() method that uses random lenses to implement Karl Sims's
  // cut-and-paste cross-breeding.

  public GeneTree crossBreed(GeneTree other) {
    var thisrl = this.getRandomLens();
    var otherrl = other.getRandomLens();
    return thisrl._2().set(this, otherrl._2().get(other));
  }

  ///////////// Step 5: Build a mutateNode() method that replaces the current gene with another,
  // keeping the original children. Again, use your random lens.

  public GeneTree mutateNode() {
    var thisrl = this.getRandomLens();
    int childReq = thisrl._1().get(this).numChildren();
    return thisrl._1().set(this, RandomGeneTree.randomAllele(childReq));
  }

  ///////////// Step 6: Build a mutateTree() method that visits some fraction of the tree, mutating
  // it, using the method above.

  public GeneTree mutateTree() {
    double percentMutation = 0.25;
    return mutateTreeHelper((int) (percentMutation * this.numNodes()));
  }

  private GeneTree mutateTreeHelper(int timesLeft) {
    if (timesLeft < 0) {
      return this;
    } else {
      return this.mutateNode().mutateTreeHelper(timesLeft - 1);
    }
  }

  ///////////// Step 7: Build the control logic to drive your mutation engine from the web GUI! (Not
  // in this file...)

  //////////////////////////////////////////////////// Here ends the week 3 section

  @Override
  public String toString() {
    return "GeneTree" + toJson().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof GeneTree)) {
      return false;
    }

    var other = (GeneTree) o;
    return other.gene.equals(gene) && other.children.equals(children);
  }

  @Override
  public int hashCode() {
    return gene.hashCode() * 7 + children.hashCode();
  }
}
