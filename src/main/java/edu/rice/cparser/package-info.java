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

/**
 * {@link edu.rice.cparser.ParserFunction}s are building blocks for parsers. These are also called
 * <i>parser combinators</i> or <i>monadic parsers</i>. The core idea is that parsers can be
 * <i>combined</i> with other parsers, and that parsers are just functions so you can define them
 * normally and make them recursive if necessary.
 *
 * <p>For an example that shows how to fit all these pieces together, check out the code for {@link
 * edu.rice.cparser.SExpression}, which builds a parser for s-expressions and then maps the
 * resulting {@link edu.rice.cparser.Expression} to its own data class that keeps the internal list
 * and ignores the rest.
 *
 * <p><b>Don't panic!</b> If you do a web search for "parser combinators" you'll find examples of
 * this for a variety of different programming languages, some simple and others much more
 * complicated. Of note, here's <a
 * href="http://unpetitaccident.com/pub/compeng/languages/Haskell/monparsing.pdf">a paper that
 * explains the general theory behind parser combinators</a>. (Requires some comfort reading the
 * Haskell programming language.) Alternatively, here's a <a
 * href="https://fsharpforfunandprofit.com/parser/">tutorial for a parser combinator in F#</a> which
 * you may find more accessible, and which even wraps up with a parser-combinator for JSON. (That
 * author uses the JSON parser to directly handle the ugly parts of the scanner, while we prefer to
 * separately tokenize the input before parsing it, allowing us to simplify the grammar.)
 *
 * <p>There are several "industrial strength" parser-combinator libraries for Java itself. See, for
 * example, these two parsers based on the sophisticated Haskell Parsec library:
 *
 * <ul>
 *   <li><a href="https://github.com/jparsec/jparsec">JParsec,
 *       https://github.com/jparsec/jparsec</a>
 *   <li><a href="https://github.com/jon-hanson/parsecj">ParsecJ,
 *       https://github.com/jon-hanson/parsecj</a>
 * </ul>
 *
 * <p>So is this the best possible way to implement a parser? Well that depends on what your goals
 * are. Parsers built with this exact library doesn't have particularly good error handling. All you
 * get back is {@link edu.rice.cparser.Result.ParserError}, without any indication of why the parser
 * failed. Similarly, this library doesn't support <i>streaming</i>, wherein you might have an
 * infinite volume of input and want to get parser output along the way as the input continues.
 * Also, if you're parsing a <b>lot</b> of input, then performance and memory use will be issues.
 * Here, we have none of those helpful properties, but at least you can write the code for a parser
 * in a very concise way!
 */
@javax.annotation.ParametersAreNonnullByDefault
@javax.annotation.CheckReturnValue
package edu.rice.cparser;

// See src/main/java/edu/rice/package-info.java for details
// on what these annotations are doing.
