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

package edu.rice.week1letters;

import static java.nio.charset.StandardCharsets.UTF_8;

import edu.rice.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import spark.utils.IOUtils;

/**
 * Some helper code to handle I/O for the Letters game. You don't have to understand how these work
 * internally. We'll explain many of these details later in the semester.
 */
public class LettersIO {
  // used to read text from the user
  private static final Scanner cin = new Scanner(System.in, UTF_8);

  // the 10000 most common words in English language (courtesy of Google)
  private static final String[] words = readResource("google-10000-long.txt");

  // random number generator
  private static final Random rnd = new Random();

  /**
   * Get a random word from the array of words.
   *
   * @return a randomly selected word
   */
  public static String getRandomWord() {
    // REMOVE FROM HERE
    while (true) {
      var candidate = words[rnd.nextInt(words.length)];
      if (candidate.length() < 3) {
        continue; // we're not interested in really short words, so try another one
      }
      return candidate;
    }
    // TO HERE
    // REPLACE WITH:
    // TODO: Implement this method, use the random number generator // (rng) and the array of words,
    // defined above
    // throw new RuntimeException(" ... not yet implemented!");
  }

  /**
   * Get a single letter input from the player as a guess. Reject any characters that are not
   * between 'a' and 'z'
   *
   * @return the character that has been inputted the player
   */
  public static char getLetterFromPlayer() {
    boolean correctAnswerForm;
    do {
      System.out.print("Guess a letter: ");
      var line = cin.nextLine();
      correctAnswerForm = (line.length() == 1 && line.charAt(0) >= 'a' && line.charAt(0) <= 'z');
      if (correctAnswerForm) {
        return line.charAt(0);
      }
      System.out.println("Please, enter only a single lowercase letter between \'a\' and \'z\'!");
    } while (true);
  }

  /** Fetch the next line of text from the user. */
  public static String nextLine() {
    return cin.nextLine();
  }

  /** Print a line of text to the user, including a linebreak at the end. */
  public static void println(String str) {
    System.out.println(str);
  }

  /**
   * Print a line of text to the user, but leaving the cursor at the end of the line rather than
   * starting a new line.
   */
  public static void print(String str) {
    System.out.print(str);
  }

  /**
   * Read the contents of a "resource" file (in the src/main/resources directory). If the file is
   * not present, an exception will be thrown and the program will stop running.
   *
   * @return an array of strings, one entry per line of text in the file
   */
  public static String[] readResource(String filename) {
    // Engineering notes: we're using a weird construction, introduced
    // in Java7, called "try with resources", that ensures that this
    // particular input stream is always closed, when we're done
    // reading it, even if something goes wrong. You don't really need
    // to understand the details of what's going on here, except to
    // understand that when you try to read something from disk, you
    // have to be prepared for it to be absent. Later in the semester,
    // we'll have fancier ways of dealing with this problem.

    // Also of possible interest: Mac, Windows, and Unix all have
    // different ways of encoding the end of a line of text in a
    // file. Yes, they're all different. The "replaceAll" calls ensure
    // that we're always dealing with Unix-style linebreaks, where
    // lines end with a newline character ("\n") rather than some
    // combination of that with a carriage-return ("\r"), which makes
    // our lives easier when we try to split the file into its
    // constituent lines.

    // And yes, this isn't the most efficient solution to the problem,
    // but for this week, we don't care at all.

    try (InputStream is = ClassLoader.getSystemResourceAsStream(filename)) {
      if (is == null) {
        return new String[] {}; // empty array of string
      } else {
        var contents = IOUtils.toByteArray(is);
        return new String(contents, StandardCharsets.UTF_8)
            .replaceAll("\r\n", "\n") // Windows CR-LF -> Unix LF
            .replaceAll("\r", "\n") // Mac CR -> Unix LF;
            .split("\n"); // splits from one big string into an array of strings
      }
    } catch (NullPointerException | IOException ioe) {
      Log.e("LettersIO", "Failed to read resource: " + filename);
      throw new RuntimeException("readResource failed");
    }
  }
}
