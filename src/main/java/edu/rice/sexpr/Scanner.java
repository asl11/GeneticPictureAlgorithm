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

package edu.rice.sexpr;

import static edu.rice.regex.RegexScanner.scanPatterns;

import edu.rice.regex.RegexScanner;
import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import io.vavr.collection.Stream;
import org.intellij.lang.annotations.Language;

/**
 * This class takes a string and tokenizes it for a simple s-expression parser. {@link RegexScanner}
 * does all the heavy lifting.
 */
public interface Scanner {
  /** Given an input string, returns a list of tokens. */
  static Stream<Token<SexprPatterns>> scanSexpr(String input) {
    return scanPatterns(input, SexprPatterns.class, new Token<>(SexprPatterns.FAIL, ""))
        // remove whitespace tokens; we don't care about them
        .filter(x -> x.type != SexprPatterns.WHITESPACE);
  }

  enum SexprPatterns implements TokenPatterns {
    OPEN("\\("),
    CLOSE("\\)"),
    WORD("\\w+"), // one or more letters, numbers, and underscores
    WHITESPACE("\\s+"),
    FAIL(""); // if the matcher fails, you get one of these

    public final String pattern;

    SexprPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }
}
