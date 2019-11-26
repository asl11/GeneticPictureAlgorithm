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

import java.util.Arrays;

/**
 * A simple letter guessing game. Computer picks a random word and the player has to guess the word
 * by guessing individual letters. Player has 10 attempts to guess the letters in the word. Correct
 * guesses do not count against the number of attempts.
 */
public class Letters {

  /** The mystery word that the player is attempting to guess. */
  private final String wordToGuess;

  /**
   * The player's current guess. The correctly guessed letters are in their positions as in the
   * mystery word. The still unknown letters are represented by '#'.
   */
  private String currentGuess;

  int guessesLeft;

  public String getWordToGuess() {
    return wordToGuess;
  }

  public String getCurrentGuess() {
    return currentGuess;
  }

  public int guessesLeft() {
    return guessesLeft;
  }

  /** Constructor for Letters. */
  public Letters() {
    wordToGuess = LettersIO.getRandomWord();
    var charArray = new char[wordToGuess.length()];
    Arrays.fill(charArray, '#');
    currentGuess = new String(charArray);
    guessesLeft = 10;
  }

  /** Constructor for Letters with a specific word. */
  public Letters(String word) {
    wordToGuess = word;
    var charArray = new char[wordToGuess.length()];
    Arrays.fill(charArray, '#');
    currentGuess = new String(charArray);
    guessesLeft = 10;
  }

  /**
   * Find whether wordToGuess contains letter.
   *
   * @param letter is the character that the player has guessed
   * @return the number of times letter appears in the wordToGuess. Return -1 if the letter already
   *     appears
   */
  public int guessLetter(char letter) {
    // REMOVE FROM HERE
    int i;

    for (i = 0; i < currentGuess.length(); i++) {
      if (currentGuess.charAt(i) == letter) {
        return -1;
      }
    }

    var correctGuesses = 0;
    var current = currentGuess.toCharArray();

    for (i = 0; i < wordToGuess.length(); i++) {
      if (wordToGuess.charAt(i) == letter) {
        correctGuesses++;
        current[i] = letter;
      }
    }

    if (correctGuesses == 0) {
      guessesLeft--;
    }

    return correctGuesses;
    // TO HERE
    // AND REPLACE WITH:
    // TODO: Implement this method
    // throw new RuntimeException("guessLetter method in Letters class not yet implemented!");
  }

  /**
   * Replace the '#'s in currentGuess with the letter on the positions it appears in wordToGuess.
   *
   * @param letter is the character that the player has guessed
   */
  public void addLetter(char letter) {
    // REMOVE FROM HERE
    int i;

    var current = currentGuess.toCharArray();

    for (i = 0; i < wordToGuess.length(); i++) {
      if (wordToGuess.charAt(i) == letter) {
        current[i] = letter;
      }
    }

    currentGuess = new String(current);
    // TO HERE
    // AND REPLACE WITH:
    // TODO: Implement this method
    // throw new RuntimeException("addLetter method in Letters class not yet implemented!");

  }

  public boolean gameWon() {
    return wordToGuess.equals(currentGuess);
  }

  /** Display the message to the player that has won the game. */
  public void displayWin() {
    LettersIO.println("Congratulations, you have guessed the word correctly!!!");
    LettersIO.println("The word we were looking for was \'" + wordToGuess + "\'");
    LettersIO.println("");
  }

  /** Display the message to the player that has lost the game. */
  public void displayLoss() {
    LettersIO.println("Sorry, you ran out of guesses. You lose the game!");
    LettersIO.println("The word we were looking for was \'" + wordToGuess + "\'");
    LettersIO.println("");
  }

  /** The main program for the Letters game. */
  public static void main(String[] args) {
    LettersIO.println("Welcome to the Letters game!");
    String answer;
    LettersIO.print("Would you like to play? (Y/N) ");
    while (true) {
      answer = LettersIO.nextLine();
      while (!answer.equals("Y")
          && !answer.equals("N")
          && !answer.equals("y")
          && !answer.equals("n")) {
        LettersIO.println("Please answer with Y or N");
        LettersIO.print("Would you like to play? (Y/N) ");
        answer = LettersIO.nextLine();
      }
      if (answer.equals("N") || answer.equals("n")) {
        return;
      }

      var letters = new Letters();

      var gameWon = false;

      while (letters.guessesLeft > 0 && !gameWon) {
        letters.displayStatus();
        var guessedLetter = LettersIO.getLetterFromPlayer();
        var numCorrectPositions = letters.guessLetter(guessedLetter);
        if (numCorrectPositions == -1) {
          LettersIO.println("You have already guessed that letter!!");
          LettersIO.println("");
        } else if (numCorrectPositions == 0) {
          LettersIO.println(
              "Sorry, the word does not contain the letter \'" + guessedLetter + "\'");
          LettersIO.println("");
        } else {
          LettersIO.println(
              "Correct, the word contains the letter \'"
                  + guessedLetter
                  + "\' in "
                  + numCorrectPositions
                  + " positions!");
          letters.addLetter(guessedLetter);
          gameWon = letters.gameWon();
          if (gameWon) {
            letters.displayWin();
          }
        }
      }

      if (!gameWon) {
        letters.displayLoss();
      }

      LettersIO.print("Would you like to play again? (Y/N) ");
    }
  }

  /** Display the current status of the game. */
  private void displayStatus() {
    LettersIO.println("Your current guess is \'" + currentGuess + "\'");
    LettersIO.println("You have " + guessesLeft + " guesses left");
    LettersIO.println("");
  }
}
