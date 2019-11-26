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

import static edu.rice.util.Strings.regex;
import static edu.rice.util.Strings.regexToPredicate;
import static edu.rice.util.Strings.stringToUTF8;
import static edu.rice.vavr.Sequences.enumerationToSeq;
import static edu.rice.vavr.Tries.tryOfNullable;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.rice.util.Log;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This class contains simple alternatives to all the crazy ways of reading/writing files and
 * resources in Java. Unlike the official Java ways of doing file IO, these static methods never
 * throw an exception. Instead, they use {@link Try}, which either has the successful value inside
 * (much like an {@link Option#some(Object)} or has an exception within (akin to {@link
 * Option#none()}, but with the exception). This structure keeps your code cleaner than with
 * classical Java try/catch blocks.
 *
 * <p>Note that Comp215 has a few rules about files to make sure that your code runs correctly when
 * your grader is checking your work. In particular:
 *
 * <ul>
 *   <li>If you're naming files in a subdirectory, you shall use a forward-slash as the separator.
 *       No backslashes!
 *   <li>No absolute paths, i.e., nothing that starts with a slash on Unix/Mac and nothing that
 *       starts with "C:\" on Windows.
 *   <li>No URL-style paths, i.e., nothing that starts with "file:" or "http:"
 * </ul>
 *
 * <p>Instead, all paths will be <i>relative</i>. When you run your program, the starting directory
 * will be the top of your IntelliJ project, which is a perfectly reasonable place to read or write
 * files for projects.
 *
 * <p>If you want a <b>read-only file</b>, that should go into the <i>resources</i> directory in
 * <i>src/main/resources</i> or <i>src/test/resources</i>, and you can then read those files using
 * {@link #readResource(String)}, {@link #readResourceBytes(String)}, or {@link
 * #readResourceDir(String)}.
 */
@SuppressWarnings("StreamResourceLeak") // ErrorProne doesn't know about VAVR's Try.withResources
public class Files {
  private static final String TAG = "Files";

  private Files() {} // this class should never be instantiated

  private static Try<InputStream> resourceToStream(String resourceName) {
    validatePath(resourceName);
    // If ClassLoader.getSystemResourceAsStream finds nothing, it
    // returns null, which we have to deal with.
    return tryOfNullable(() -> ClassLoader.getSystemResourceAsStream(resourceName))
        .onFailure(
            th -> Log.e(TAG, "getSystemResources failed for resource(" + resourceName + ")", th));
  }

  /**
   * Given a resource name, which typically maps to a file in the "resources" directory, read it in
   * and return a String. This method assumes that the resource file is encoded as a UTF-8 string.
   * If you want to get raw bytes rather than a string, use {@link #readResourceBytes(String)}
   * instead.
   *
   * @return a {@link Try#success(Object)} of the file contents as a String, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   */
  public static Try<String> readResource(String resourceName) {
    return readResourceBytes(resourceName).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Get the contents of an <code>InputStream</code> as an array of bytes. The stream is closed
   * after being read.
   */
  private static Try<byte[]> streamToByteArray(InputStream input) {
    // We need to "auto-close" the input before we're done. VAVR's
    // Try knows how to this as a functional alternative to
    // Java's "try with resources" statement.
    // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html

    return Try.withResources(() -> input)
        .of(
            is -> {
              ByteArrayOutputStream os = new ByteArrayOutputStream();
              byte[] buf = new byte[1024];
              for (int n = is.read(buf); n != -1; n = is.read(buf)) {
                os.write(buf, 0, n);
              }
              return os.toByteArray();
            });
  }

  /**
   * Given a resource name, which typically maps to a file in the "resources" directory, read it in
   * and return an array of bytes. If you want the result as a String rather than an array of raw
   * bytes, use {@link #readResource(String)} instead.
   *
   * @return a {@link Try#success(Object)} of the file contents as a byte array, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   */
  public static Try<byte[]> readResourceBytes(String resourceName) {
    return resourceToStream(resourceName).flatMap(Files::streamToByteArray);
  }

  /**
   * Given a directory path into the resources, returns a list of resource names suitable for then
   * passing to {@link #readResource(String)}, {@link #resourceToStream(String)}, etc.
   *
   * @return a {@link Try#success(Object)} of the list of resource names, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   */
  public static Try<Seq<String>> readResourceDir(String dirPath) {
    validatePath(dirPath);

    // Engineering note: What's an Enumeration? It's a relic from some
    // *very* old Java APIs. Enumeration was mostly replaced with
    // Iterator, but this particular method, getSystemResources()
    // wasn't updated.
    return Try.of(() -> enumerationToSeq(ClassLoader.getSystemResources(dirPath)))
        .onFailure(err -> Log.e(TAG, "getSystemResources failed for path(" + dirPath + ")", err))

        // Engineering note: Map vs. flatMap?
        // ClassLoader.getSystemResources gives us a *list* of URLs
        // (for example, one directory from the main resources and
        // another from the test resources). The first map() just
        // unpacks the Seq<URL> from within the Try. The flatMap()
        // after that operates on the Seq, where we map each URL to
        // a list of files available at that URL. For that reason,
        // you'll notice that we eat errors below here, since we might
        // succeed for one URL and we might fail for another. We just
        // want to return a list of the successful files. Errors will
        // be logged and we'll just continue moving onward.

        .map(
            dirUrls ->
                dirUrls.flatMap(
                    dirUrl -> {
                      final var rawUrlPath = dirUrl.getPath();
                      //          Log.i(TAG, () -> "rawUrlPath " + rawUrlPath);

                      switch (dirUrl.getProtocol()) {
                        case "file":

                          // On Windows, we get URL paths like file:/C:/Users/dwallach/....
                          // On Macs, we get URL paths like file:/Users/dwallach/...

                          // With those Windows URLs, getPath() will
                          // give us /C:/Users/... which doesn't work
                          // when we try to actually open the
                          // files. The solution? Match a regular
                          // expression and then remove the leading
                          // slash.

                          final var urlPath =
                              rawUrlPath.matches("^/\\p{Upper}:/.*$")
                                  ? rawUrlPath.substring(1)
                                  : rawUrlPath;

                          // if the URLDecoder fails, for whatever
                          // reason, we'll just go with the original
                          // undecoded path
                          final var decodedPath =
                              Paths.get(
                                  tryOfNullable(
                                          () -> URLDecoder.decode(urlPath, StandardCharsets.UTF_8))
                                      .getOrElse(urlPath));

                          // Engineering note: Path and Paths are
                          // classes that come to us from the
                          // java.nio.files package. We're using
                          // these, rather than directly messing with
                          // the paths by hand because some brave
                          // Oracle engineer spent countless hours
                          // debugging and testing those classes on
                          // each platform. We don't want to deal with
                          // all the weird rules of file paths,
                          // slashes vs. backslashes, etc. Notably,
                          // the various functions that we might use
                          // to read a directory have the habit of
                          // giving us absolute paths from the root;
                          // Path.relativize() is exactly the way to
                          // get back to a nice relative path again,
                          // which is desirable when printing /
                          // debugging your code. It's also desirable
                          // because we're enforcing a bunch of rules
                          // about relative paths vs. absolute paths
                          // to ensure that student code runs
                          // correctly on grader machines. (See
                          // below.)

                          // In the 2015 and 2016 versions of Comp215,
                          // we did all this work by manipulating the
                          // path strings directly, and of course it
                          // mostly worked but not always. Starting
                          // in 2017, we did it "properly".

                          return readdirPath(decodedPath.toString())
                              .getOrElse(List.empty())
                              // just the names of the files in the given directory
                              .map(decodedPath::relativize)
                              // which we'll convert back to strings
                              .map(Path::toString)
                              // and add the directory path back on again
                              .map(path -> dirPath + "/" + path);

                        case "jar":
                          // Solution adapted from here:
                          // http://www.uofr.net/~greg/java/get-resource-listing.html

                          // strip out only the JAR file
                          var jarPath = rawUrlPath.substring(5, rawUrlPath.indexOf("!"));

                          return Try.withResources(
                                  () ->
                                      new JarFile(
                                          URLDecoder.decode(jarPath, StandardCharsets.UTF_8)))
                              .of(
                                  // This code is going to work, but could
                                  // be slow for huge JAR files. Testing &
                                  // optimization would be necessary, but
                                  // Comp215 doesn't make any use Jar files
                                  // for its resources, so we'll leave this
                                  // as "good enough" for now.
                                  jarFile ->
                                      enumerationToSeq(jarFile.entries())
                                          .map(ZipEntry::getName)
                                          .filter(name -> name.startsWith(dirPath)))
                              .onFailure(
                                  err ->
                                      Log.e(
                                          TAG,
                                          "trouble reading "
                                              + dirUrl
                                              + ", ignoring and marching onward",
                                          err))
                              .fold(failure -> Stream.empty(), success -> success);

                        default:
                          Log.e(TAG, "unknown protocol in " + dirUrl);
                          return Stream.empty();
                      }
                    }));
  }

  /**
   * Given a relative filename, read it in and return a String. This method assumes that the file is
   * encoded as a UTF-8 string. If you want the result as an array of raw bytes rather than a
   * String, use {@link #readBytes(String)} instead.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project. For Comp215, this
   * is all that you'll ever need. If you try to use an absolute file path, or a path using
   * backslashes (which only works on Windows, while forward slashes work everywhere) you'll get an
   * exception. We do this because we want to make sure that your code will run just as well when
   * it's being graded.
   *
   * @return a {@link Try#success(Object)} of the file contents as a String, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  public static Try<String> read(String filePath) {
    return readBytes(filePath).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Given a filename, read it in and return the contents as an array of bytes. If you want the
   * result as a String, use {@link #read(String)} instead.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project. For Comp215, this
   * is all that you'll ever need. If you try to use an absolute file path, or a path using
   * backslashes (which only works on Windows, while forward slashes work everywhere) you'll get an
   * exception. We do this because we want to make sure that your code will run just as well when
   * it's being graded.
   *
   * @return a {@link Try#success(Object)} of the file contents as an array of bytes, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  public static Try<byte[]> readBytes(String filePath) {
    validatePath(filePath);
    return tryOfNullable(() -> java.nio.file.Files.readAllBytes(Paths.get(filePath)))
        .onFailure(err -> Log.e(TAG, "failed to read file(" + filePath + ")", err));
  }

  /**
   * The given string of data is written to a file of the requested name, all at once.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project. For Comp215, this
   * is all that you'll ever need. If you try to use an absolute file path, or a path using
   * backslashes (which only works on Windows, while forward slashes work everywhere) you'll get an
   * exception. We do this because we want to make sure that your code will run just as well when
   * it's being graded.
   *
   * @return an empty {@link Try#success(Object)} if everything goes well, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  @CanIgnoreReturnValue
  public static Try<Void> write(String filePath, String data) {
    // We're going to use Strings.stringToUTF8 to convert 'data' to an
    // array of bytes. This is *highly* unlikely to fail, but if it
    // does, we'd get back an empty-string. Oh well.

    return writeBytes(filePath, stringToUTF8(data));
  }

  /**
   * The given byte-array of data is written to a file of the requested name, all at once.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project. For Comp215, this
   * is all that you'll ever need. If you try to use an absolute file path, or a path using
   * backslashes (which only works on Windows, while forward slashes work everywhere) you'll get an
   * exception. We do this because we want to make sure that your code will run just as well when
   * it's being graded.
   *
   * @return an empty {@link Try#success(Object)} if everything goes well, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  @CanIgnoreReturnValue
  public static Try<Void> writeBytes(String filePath, byte[] rawData) {
    validatePath(filePath);
    return Try.run(
            () ->
                java.nio.file.Files.write(
                    Paths.get(filePath),
                    rawData,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING))
        .onFailure(err -> Log.e(TAG, "failed to write file(" + filePath + ")", err));
  }

  /**
   * If the file is present, it's removed.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project. For Comp215, this
   * is all that you'll ever need. If you try to use an absolute file path, or a path using
   * backslashes (which only works on Windows, while forward slashes work everywhere) you'll get an
   * exception. We do this because we want to make sure that your code will run just as well when
   * it's being graded.
   *
   * @return an empty {@link Try#success(Object)} if everything goes well, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  @CanIgnoreReturnValue
  public static Try<Void> remove(String filePath) {
    validatePath(filePath);
    return Try.run(() -> java.nio.file.Files.delete(Paths.get(filePath)))
        .onFailure(err -> Log.e(TAG, "failed to remove file(" + filePath + ")", err));
  }

  private static Try<Seq<Path>> readdirPath(String filePath) {
    return Try.withResources(() -> java.nio.file.Files.newDirectoryStream(Paths.get(filePath)))
        // Note: using eager list. We want all the output before auto-closing the stream.
        .of(ds -> Seq.narrow(List.ofAll(ds)))
        .onFailure(err -> Log.e(TAG, "failed to read directory(" + filePath + ")", err));
  }

  /**
   * Given a relative directory path, this returns a list of all files (or subdirectories) in that
   * directory, excluding "." and "..". If nothing is actually there, then an empty list will be
   * returned.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project. For Comp215, this
   * is all that you'll ever need. If you try to use an absolute file path, or a path using
   * backslashes (which only works on Windows, while forward slashes work everywhere) you'll get an
   * exception. We do this because we want to make sure that your code will run just as well when
   * it's being graded.
   *
   * @return a {@link Try#success(Object)} of the list of resource names, or a {@link
   *     Try#failure(Throwable)} indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  public static Try<Seq<String>> readdir(String filePath) {
    validatePath(filePath);
    Path rootPath = Paths.get(filePath);

    return readdirPath(filePath)
        // gets us to just the names of the files in the given directory
        .map(
            list ->
                list.map(rootPath::relativize)
                    // which we'll convert back to strings
                    .map(Path::toString)
                    // and add the directory path back on again
                    .map(path -> filePath + "/" + path));
  }

  // Engineering note: We stated a bunch of rules for what sorts of
  // filenames / paths are appropriate for Comp215 (see the Javadoc at
  // the top of this file). Here's how we *enforce* those rules. We
  // state several regular expressions on filenames / paths and have a
  // series of error messages that go along with them. We convert
  // those error messages to exceptions, and we'll throw the
  // appropriate exception if the path matches, causing the student's
  // code to fail.

  // All of this effort is here to catch the one or two students who
  // might otherwise have code that "works for me" but doesn't work
  // for the graders.

  // In the real world, these specific constraints wouldn't
  // necessarily be what you want, but then any real world program is
  // still going to have a limited set of places that it wants to read
  // and write to the filesystem, and those places are indeed going to
  // be slightly different on Windows vs. Mac vs. whatever else, so
  // it's entirely appropriate to define rules and enforce them.

  private static final Seq<Tuple2<Predicate<String>, RuntimeException>> pathPatterns =
      List.of(
              Tuple.of(regex("\\\\"), "backslashes are not allowed in file paths"),
              Tuple.of(regex("^\\p{Alpha}:"), "Windows-style absolute paths are not allowed"),
              Tuple.of(regex("^/"), "Unix-style absolute paths are not allowed"),
              Tuple.of(regex("^\\p{Alpha}+:"), "URI-style paths are not allowed"))
          .map(
              p ->
                  Tuple.of(
                      regexToPredicate(p._1),
                      new IllegalArgumentException(p._2 + " (in Comp215, anyway)")));

  private static void validatePath(String path) {
    pathPatterns.forEach(
        p -> {
          if (p._1.test(path)) {
            throw p._2;
          }
        });
  }
}
