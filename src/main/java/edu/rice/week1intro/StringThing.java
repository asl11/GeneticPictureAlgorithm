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

package edu.rice.week1intro;

import edu.rice.util.Log;

/** You'll be putting some basic stuff in here to make sure everything is working. */
public class StringThing {
  private static final String TAG = "StringThing";

  private final String name;
  private final String classYear;

  private final String riceNetID;
  private final String preferredEmail;

  public static final StringThing YOUR_STUDENT_RECORD =
      new StringThing("Dan Wallach", "2019", "dwallach", "dwallach@rice.edu");

  /** Straightforward constructor for our StringThing. */
  public StringThing(String name, String classYear, String riceNetID, String preferredEmail) {
    this.name = name;
    this.classYear = classYear;
    this.riceNetID = riceNetID;
    this.preferredEmail = preferredEmail;
  }

  /** Log some things about this class. */
  public void logMe() {
    Log.i(TAG, "Student: " + name);
    Log.i(TAG, "Class Year: " + classYear);
    Log.i(TAG, "NetID: " + riceNetID);
    Log.i(TAG, "Preferred Email: " + preferredEmail);
    lambdaTest();
  }

  /** Exercise the Log class. */
  public void lambdaTest() {
    // This method won't compile unless you've installed the right version of Java.

    // Also, this line is too long! Make sure you run the 'googleJavaFormat' action.
    Log.i(
        TAG,
        () ->
            "If this is your username, then you've got Java set up right: "
                + String.format("%s <%s>", name, preferredEmail));

    var x = "Hello, World".charAt(0);
  }

  public String getName() {
    return name;
  }

  public String getNetID() {
    //    return classYear; // a bug! change this to riceNetID
    return riceNetID;
  }

  public String getClassYear() {
    return classYear;
  }

  public String getPreferredEmail() {
    return preferredEmail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof StringThing)) {
      return false;
    }

    StringThing that = (StringThing) o;

    return getName().equals(that.getName())
        && getNetID().equals(that.getNetID())
        && getClassYear().equals(that.getClassYear())
        && getPreferredEmail().equals(that.getPreferredEmail());
  }

  @Override
  public String toString() {
    return getName() + ", " + getNetID() + ", " + getClassYear() + ", " + getPreferredEmail();
  }

  @Override
  public int hashCode() {
    return toString().hashCode(); // we'll let String.hashCode figure it out
  }
}
