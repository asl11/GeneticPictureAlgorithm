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

package edu.rice.io;

import static edu.rice.io.Files.read;
import static edu.rice.io.Files.remove;
import static edu.rice.io.Files.write;
import static io.vavr.control.Try.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.util.Log;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.nio.file.NoSuchFileException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class FilesTest {
  @Test
  public void readingWritingAndDeletingFiles() {
    var result1 = write("testData.txt", "Hello, world!");
    var result2 = write("testData2.txt", "Hello, Rice!");
    assertTrue(result1.isSuccess());
    assertTrue(result2.isSuccess());

    var read1 = read("testData.txt");
    var read2 = read("testData2.txt");

    assertEquals(success("Hello, world!"), read1);
    assertEquals(success("Hello, Rice!"), read2);

    // overwrite the old file: should succeed
    var result3 = write("testData.txt", "Hello, hello, hello!");
    var read3 = read("testData.txt");
    assertTrue(result3.isSuccess());
    assertEquals(success("Hello, hello, hello!"), read3);

    // now, delete the first file
    var result4 = remove("testData.txt");
    assertTrue(result4.isSuccess());

    // and try reading it again, which should fail
    var result5 = read("testData.txt");
    assertTrue(result5.isFailure());

    assertTrue(result5.getCause() instanceof NoSuchFileException);

    // and lastly, try removing both files; the first one should
    // generate an exception since it's not there
    var result6 = remove("testData.txt");
    var result7 = remove("testData2.txt");

    assertTrue(result6.isFailure());

    assertTrue(result6.getCause() instanceof NoSuchFileException);
    assertTrue(result7.isSuccess());
  }

  @Test
  public void readingDirectories() {
    // On a Mac or Windows box, we seem to be running at the root of
    // the project.  We're going to try creating and destroying a file
    // of the same name.  but first we'll create some hello world
    // files and make sure they're there
    var result1 = write("testData.txt", "Hello, world!");
    assertTrue(result1.isSuccess());

    var fileNames = Files.readdir(".").getOrElse(List.empty());
    fileNames.forEach(x -> System.out.println("Found file: " + x));

    assertTrue(fileNames.contains("./testData.txt"));

    var result2 = remove("./testData.txt");
    assertTrue(result2.isSuccess());
  }

  @Test
  public void readingResourceDirectories() {
    var shouldBeEmpty = Files.readResourceDir("emptyDirectory");
    shouldBeEmpty
        .get()
        .forEach(name -> Log.i("testReadResourceDir", "(empty) found resource: (" + name + ")"));
    assertTrue(shouldBeEmpty.isSuccess());
    assertEquals(0, shouldBeEmpty.get().length());

    var testDirectory = Files.readResourceDir("testDirectory");
    assertTrue(testDirectory.isSuccess());

    testDirectory
        .get()
        .forEach(name -> Log.i("testReadResourceDir", "(test) found resource: (" + name + ")"));

    assertEquals(2, testDirectory.get().length());
  }

  @Test
  public void readingOurCoolImagesDirectory() {
    var imagesDirectory = Files.readResourceDir("cool-images");
    assertTrue(imagesDirectory.isSuccess());

    var txtFiles =
        imagesDirectory
            .map(list -> list.filter(name -> name.endsWith(".txt")))
            .getOrElse(List.empty());
    Log.iformat(
        "testReadResourceImagesDir",
        "found %d .txt files: (%s)",
        txtFiles.length(),
        txtFiles.mkString(","));

    assertTrue(imagesDirectory.get().contains("cool-images/README.txt"));
  }

  @Test
  public void correctHandlingOfDirectoriesWithSpacesInTheirNames() {
    var spacesDirectory = Files.readResourceDir("test directory spaces");
    assertTrue(spacesDirectory.isSuccess());

    var txtFiles =
        spacesDirectory
            .map(list -> list.filter(name -> name.endsWith(".txt")))
            .getOrElse(List.empty());
    Log.iformat(
        "testResourcesWithSpaces",
        "found %d .txt files: (%s)",
        txtFiles.length(),
        txtFiles.mkString(","));

    // we normalize paths to have forward slashes, even on Windows
    assertTrue(spacesDirectory.get().contains("test directory spaces/spaces in a file.txt"));
  }

  /** We try to reject any absolute file paths. */
  @TestFactory
  public Seq<DynamicTest> properlyRejectingFileNamesWithAbsolutePaths() {
    var badNames =
        List.of(
            "/etc/passwd",
            "etc\\passwd",
            "\\etc\\passwd",
            "C:\\Windows\\Whatever",
            "file://etc/passwd");

    return badNames.map(
        name ->
            DynamicTest.dynamicTest(
                name,
                () ->
                    assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                          var ignored = Files.read(name);
                        })));
  }
}
