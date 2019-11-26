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

package edu.rice.json;

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.lens.MonoLens.monoLens;
import static edu.rice.util.Strings.stringToOptionInteger;
import static edu.rice.vavr.Sequences.seqMatch;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.autograder.annotations.GradeCoverage;
import edu.rice.json.Value.JArray;
import edu.rice.json.Value.JBoolean;
import edu.rice.json.Value.JNumber;
import edu.rice.json.Value.JString;
import edu.rice.lens.Lens;
import edu.rice.lens.MonoLens;
import edu.rice.util.Strings;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Helpful utility operations for querying and updating JSON Values. */
@GradeCoverage(project = "Week09")
public interface Operations {
  /**
   * Given a list of strings, fetch the Value corresponding to this path. For a JSON object, the
   * values are interpreted as the keys of a key-value tuple in the JSON object. For a JSON array,
   * the values are interpreted as decimal (base 10) integers. When the JSON data structure matches
   * up with the path, the Value at the end will be returned.
   *
   * @return Option.some of the query result, if it's there, or Option.none if it's absent.
   */
  static Option<Value> getPathOption(Value value, Seq<String> pathList) {
    // TODO (Thursday): recursively walk into the Value, based on the path, and return
    //   the value if it's there.

    // TODO (Sunday):   get a lens for the path, then use it to get the value within.

    //    throw new UnsupportedOperationException("not implemented yet");

    return value.lensGet(lensPath(pathList));
  }

  /**
   * Given a forward-slash-separated path, fetch the Value corresponding to this path. For a JSON
   * object, the values between the slashes are interpreted as the keys of a key-value tuple in the
   * JSON object. For a JSON array, the values are interpreted as decimal (base 10) integers. When
   * the JSON data structure matches up with the path, the Value at the end will be returned.
   *
   * <p>Note: if you have a JSON object with a key that includes a forward slash, you won't be able
   * to get to it with this API call. Instead, you should use {@link #getPathOption(Value, Seq)},
   * which take a list of strings rather than a slash-separated string.
   *
   * @return Option.some of the query result, if it's there, or Option.none if it's absent.
   */
  static Option<Value> getPathOption(Value value, String path) {
    // TODO (Thursday): adapt the path here to fit the pathlList argument of the above
    // getPathOption().

    //    throw new UnsupportedOperationException("not implemented yet");
    return getPathOption(value, List.of(path.split("/")));
  }

  /**
   * Given a forward-slash-separated path, fetch the Value corresponding to this path. If it's
   * present and it's a string, convert it to a regular Java string and return {@link
   * Option#some(Object)} of it. Otherwise return {@link Option#none()}.
   *
   * <p>Useful when you're expecting to find a string at a particular location in a JSON object.
   */
  static Option<String> getPathOptionString(Value value, String path) {
    return getPathOption(value, path)
        .flatMap(Value::asJStringOption)
        .map(JString::toUnescapedString);
  }

  /**
   * Given a forward-slash-separated path, fetch the Value corresponding to this path. If it's
   * present and it's a number, convert it to a regular Java double and return {@link
   * Option#some(Object)} of it. Otherwise return {@link Option#none()}.
   *
   * <p>Useful when you're expecting to find a number at a particular location in a JSON object.
   */
  static Option<Double> getPathOptionNumber(Value value, String path) {
    return getPathOption(value, path).flatMap(Value::asJNumberOption).map(JNumber::get);
  }

  /**
   * Given a forward-slash-separated path, fetch the Value corresponding to this path. If it's
   * present and it's a boolean, convert it to a regular Java boolean and return {@link
   * Option#some(Object)} of it. Otherwise return {@link Option#none()}.
   *
   * <p>Useful when you're expecting to find a boolean at a particular location in a JSON object.
   */
  static Option<Boolean> getPathOptionBoolean(Value value, String path) {
    return getPathOption(value, path).flatMap(Value::asJBooleanOption).map(JBoolean::get);
  }

  /**
   * Given a forward-slash-separated path, fetch the Value corresponding to this path. If it's
   * present and it's a JSON array, convert it to a regular Java list of {@link Value} and return
   * {@link Option#some(Object)} of it. Otherwise return {@link Option#none()}.
   *
   * <p>Useful when you're expecting to find a JSON array at a particular location in a JSON object.
   */
  static Option<Seq<Value>> getPathOptionArray(Value value, String path) {
    return getPathOption(value, path).flatMap(Value::asJArrayOption).map(JArray::getSeq);
  }

  /**
   * Given a list of regular expressions, fetch a list of Values which match this path. Each regex
   * matches the key in a JSON object's key/value tuples. For JSON arrays, the "key" is treated as a
   * decimal (base 10) integer, then converted to a string for the regex match.
   */
  static Seq<Value> getValuesMatchingPathRegex(Value value, Seq<String> pathRegexList) {
    // TODO: use getValuesMatchingPathPredicates(), but adapt the
    //   regexes here to fit the predicates there.  Helpful hint: take a
    //   look at edu.rice.Strings.regexToPredicate

    //    throw new UnsupportedOperationException("not implemented yet");
    return getValuesMatchingPathPredicates(value, pathRegexList.map(Strings::regexToPredicate));
  }

  /**
   * Given a list of predicates over strings, fetch list of Values which match these predicates.
   * Each predicate matches the key in a JSON object's key/value tuples. For JSON arrays, the "key"
   * is treated as a decimal (base 10) integer, then converted to a string for the predicate.
   */
  static Seq<Value> getValuesMatchingPathPredicates(
      Value value, Seq<Predicate<String>> pathPredicateList) {
    // TODO: you're converting from path predicates to internal
    //   values. Use getLensesMatching() and lensGet()

    //    throw new UnsupportedOperationException("not implemented yet");
    return getLensesMatching(value, pathPredicateList).flatMap(value::lensGet);
  }

  /**
   * Given a list of predicates over strings, fetch a list of {@link Lens} objects corresponding to
   * any matching paths into the given value. See {@link #getPathsMatching(Value, Seq)} for how the
   * matching works.
   */
  static Seq<MonoLens<Option<Value>>> getLensesMatching(
      Value value, Seq<Predicate<String>> pathPredicateList) {
    // TODO: you're converting from path predicates to lenses. Use
    //   getPathsMatching() and lensPath()

    //    throw new UnsupportedOperationException("not implemented yet");

    // Engineering note: It's hugely valuable, from a bug detection
    // and remediation perspective, to have getLensesMatching() built
    // in terms of getPathsMatching(), because getPathsMatching() is
    // much easier to test. Once that's working, then
    // getLensesMatching() becomes a one-liner.

    return getPathsMatching(value, pathPredicateList).map(Operations::lensPath);
  }

  /**
   * Given a list of predicates over strings, returns a list of paths (where each path is just a
   * list of strings) which match the predicates over a given value. For JSON objects, each
   * predicate matches the key in a JSON object's key/value tuples. For JSON arrays, the "key" is
   * treated as a decimal (base 10 integer), then converted to a string for the predicate. For other
   * JSON types, no matches are returned. (We're looking for paths, not for end values.)
   */
  static Seq<Seq<String>> getPathsMatching(Value value, Seq<Predicate<String>> pathPredicateList) {
    // TODO: You'll have to deal with several different cases here,
    //   and your solution will be recursive.

    //    throw new UnsupportedOperationException("not implemented yet");

    Seq<Tuple2<String, Value>> emptyResultsList = List.empty();

    return seqMatch(
        pathPredicateList,
        // Fun bugfix: initially, I wrote emptyList ->
        // List.empty(), which type-checks just fine, but it
        // caused the resulting recursion to bottom out and return an
        // empty-list, which then caused the map(), after the
        // recursive call, to have nothing to which it could prepend
        // the front of the path. Debugging this took some effort --
        // logging everything with some test cases to realize that the
        // lambda inside the map() was never getting called.

        emptyList -> List.of(List.empty()),

        // Each of these cases will return a list of key-value
        // tuples. If use a path predicate of ".*", which will match
        // anything, the resulting list will have all the keys in the
        // given JSON object or all the array indices in the given
        // JSON array. The values of the key-value tuples will be
        // whatever the object or array entries are for that key.

        (pathHead, pathTail) ->
            value
                .match(
                    jObject -> jObject.getMatching(pathHead),
                    jArray ->
                        jArray
                            .getKVSeq()
                            .map(kv -> Tuple.of(kv._1.toString(), kv._2))
                            .filter(kv -> pathHead.test(kv._1)),
                    jString -> emptyResultsList,
                    jNumber -> emptyResultsList,
                    jBoolean -> emptyResultsList,
                    jNull -> emptyResultsList)
                .flatMap(
                    // Here's the recursive part: we go deeper
                    // into the JSON structure, using the tail of
                    // the list of predicates. We take the
                    // resulting list and prepend the current key
                    // to the front of each.
                    kv -> getPathsMatching(kv._2, pathTail).map(path -> path.prepend(kv._1))));
  }

  /**
   * Given a slash-separated path and a starting Value, this will replace the ultimate value that
   * this path leads to with the result of the update-function (updateFunc) applied to the previous
   * value. If there's no value already there, an option.none will be passed as the argument to
   * updateFunc. If an option.none is returned, that will be treated as an instruction to remove the
   * value.
   *
   * @return Option.some of the updated JSON value, or option.none in the case that the update
   *     removed the whole value
   * @see Value.JObject#updateKeyValue(String, UnaryOperator)
   */
  static Option<Value> updatePath(
      Value value, String path, UnaryOperator<Option<Value>> updateFunc) {
    // TODO: use one of the subsequent versions of updatePath(), just
    //   adapt your arguments to fit

    //    throw new UnsupportedOperationException("not implemented yet");

    return updatePath(value, List.of(path.split("/")), updateFunc);
  }

  /**
   * Given a list of strings as a path and a starting Value, this will replace the ultimate value
   * that this path leads to with the result of the update-function (updateFunc) applied to the
   * previous value. If there's no value already there, an option.none will be passed as the
   * argument to updateFunc. If an option.none is returned, that will be treated as an instruction
   * to remove the value.
   *
   * @return Option.some of the updated JSON value, or option.none in the case that the update
   *     removed the whole value
   * @see Value.JObject#updateKeyValue(String, UnaryOperator)
   */
  static Option<Value> updatePath(
      Value value, Seq<String> pathList, UnaryOperator<Option<Value>> updateFunc) {
    // TODO: use the subsequent version of updatePath(), just adapt your arguments to fit

    //    throw new UnsupportedOperationException("not implemented yet");

    return updatePath(some(value), pathList, updateFunc);
  }

  /**
   * Given a list of strings as a path and an optional starting Value, this will replace the
   * ultimate value that this path leads to with the result of the update-function (updateFunc)
   * applied to the previous value. If there's no value already there, an option.none will be passed
   * as the argument to updateFunc. If an option.none is returned, that will be treated as an
   * instruction to remove the value.
   *
   * @return Option.some of the updated JSON value, or option.none in the case that the update
   *     removed the whole value
   * @see Value.JObject#updateKeyValue(String, UnaryOperator)
   */
  static Option<Value> updatePath(
      Option<Value> oValue, Seq<String> pathList, UnaryOperator<Option<Value>> updateFunc) {

    // TODO: get a lens for the given path, then use it to update the JSON value

    //    throw new UnsupportedOperationException("not implemented yet");

    return lensPath(pathList).update(oValue, updateFunc);
  }

  /**
   * Given a list of regular expressions, find all Values which match this path, then apply the
   * function to each one, returning a new JSON object. The matching process works the same as
   * getValuesMatchingPathRegex, and the overall effect is analogous to mapping a function on a
   * list, returning a new list. Any contents, unmatched by the expressions, will be unchanged.
   *
   * @return Option.some of the updated JSON value, or option.none in the case that the update
   *     removed the whole value
   * @see #getValuesMatchingPathRegex(Value, Seq)
   */
  static Option<Value> updateValuesMatchingPathRegex(
      Value value, Seq<String> pathRegexList, UnaryOperator<Option<Value>> updateFunc) {

    // TODO: use the subsequent version of
    //   updateValuesMatchingPathPredicate(), adapting the list of
    //   regular expressions given as arguments here to fit the
    //   predicates used below.

    //    throw new UnsupportedOperationException("not implemented yet");

    return updateValuesMatchingPathPredicate(
        value, pathRegexList.map(Strings::regexToPredicate), updateFunc);
  }

  /**
   * Given a list of predicates over strings, find all Values which match these predicates, then
   * apply the function to each one, returning a new JSON object. The matching process works the
   * same as getValuesMatchingPathPredicates, and the overall effect is analogous to mapping a
   * function to a list, returning a new list. Any contents, unmatched by the predicates, will be
   * unchanged.
   *
   * @return Option.some of the updated JSON value, or option.none in the case that the update
   *     removed the whole value
   * @see #getValuesMatchingPathPredicates(Value, Seq)
   */
  static Option<Value> updateValuesMatchingPathPredicate(
      Value value,
      Seq<Predicate<String>> pathPredicateList,
      UnaryOperator<Option<Value>> updateFunc) {

    // TODO: use getLensesMatching() to get a series of lenses that
    //   let you look into this particular JSON value, then use each
    //   lens, in sequence, to do all the updates.

    //    throw new UnsupportedOperationException("not implemented yet");

    // Check this out! We're folding a list of lenses, each of which
    // takes Option<Value> in and gives Option<Value> out. Each lens
    // looks at a different path into the JSON value, and we can
    // modify each of those values with the updateFunc. Of course,
    // this is purely functional, so the original JSON Value is
    // unchanged.
    return getLensesMatching(value, pathPredicateList)
        .foldLeft(some(value), (ov, lens) -> lens.update(ov, updateFunc));
  }

  /**
   * Given a slash-separated path into any JSON object, this function returns an {@link Lens}
   * suitable for reading or editing the values at that path. Of course, it's purely functional, so
   * the original JSON object is never mutated. You might later use this with the convenience {@link
   * Value#lensGet(MonoLens)} or {@link Value#lensSet(MonoLens, Option)} methods, or you may call it
   * directly.
   *
   * @param path List of keys for searching into a JSON object
   */
  static MonoLens<Option<Value>> lensPath(String path) {
    // TODO: use the lensPath() version below. The difference is that
    //   here the path is a single string separated by slashes.

    //    throw new UnsupportedOperationException("not implemented yet");

    return lensPath(List.of(path.split("/")));
  }

  /**
   * Given a path into a JSON object, this function returns an {@link Lens} suitable for reading or
   * editing the values at that path. Of course, it's purely functional, so the original JSON object
   * is never mutated.
   *
   * @param path List of keys for searching into a JSON object
   */
  static MonoLens<Option<Value>> lensPath(Seq<String> path) {
    // TODO: given a list of strings describing a path into any JSON
    //   object, return a mono-lens over option-values.  Remember:
    //   you're not actually operating on a JSON object here. You're
    //   returning a *lens* that will later on do those operations.

    // You'll have to deal with several different cases here, and your
    // solution can be recursive, or it can do a fold. Either way,
    // you're creating and composing lenses.

    //    throw new UnsupportedOperationException("not implemented yet");

    // Engineering note: Here's a really long description of Lenses
    // and JavaScript objects, describing how this sort of thing can
    // be written in JavaScript itself. Honestly, it's going to be
    // less helpful for a Comp215 student because it brings in a bunch
    // of concepts that we don't teach in Comp215, but you'll notice
    // that they're ultimately doing the same things that we are:
    // dealing distinctly with JSON objects vs. arrays, getters and
    // setters. JavaScript makes it a bit easier to blur the
    // difference between an integer and a string, so we have to do a
    // bit more work here than they do.  Also, our version, using the
    // Option<Value> structures, is more powerful, because we can use
    // our lens to delete things and/or create things that weren't
    // already there to be updated.

    // https://medium.com/@dtipson/functional-lenses-d1aba9e52254

    // See also:

    // https://github.com/DrBoolean/lenses/blob/master/src/lenses.js

    return path.foldLeft(
        MonoLens.identity(),
        (prevLens, pathEntry) ->
            prevLens.andThenMono(
                monoLens(
                    // getter
                    ov ->
                        ov.flatMap(
                            v ->
                                v.match(
                                    jObject -> jObject.get(pathEntry),
                                    jArray -> stringToOptionInteger(pathEntry).flatMap(jArray::get),
                                    x -> none(),
                                    x -> none(),
                                    x -> none(),
                                    x -> none())),

                    // setter
                    (ov, oresult) ->
                        ov.fold(
                            // case 1: the original value was
                            // Option.none() and we're being asked to
                            // update it to the new result (which might or
                            // might not have a value, thus the call to
                            // map).
                            () -> oresult.map(result -> jobject(jpair(pathEntry, result))),

                            // case 2: there's something there already, so
                            // depending on what kind of value it is, we
                            // need to treat it differently.
                            v ->
                                v.match(
                                    // case 2a: the value that was already
                                    // there was a JSON object, so we need
                                    // to update it, using a method
                                    // already provided to us by the
                                    // JObject class.
                                    jObject ->
                                        some(
                                            jObject.updateKeyValue(
                                                pathEntry, origValue -> oresult)),

                                    // case 2b: the value that was already
                                    // there was a JSON array, so we need
                                    // to deal with the string->integer
                                    // issue, which could potentially
                                    // fail.  If we get a good integer,
                                    // then we use the appropriate method
                                    // from JArray.
                                    jArray ->
                                        some(
                                            stringToOptionInteger(pathEntry)
                                                .fold(
                                                    () -> jArray,
                                                    number ->
                                                        jArray.update(
                                                            number, origValue -> oresult))),

                                    // case 2c: if we get down to anything
                                    // other than a JSON array or object,
                                    // then there's no meaningful way to
                                    // update it as if it were a key. We
                                    // could just throw an exception, but
                                    // it's probably easier to just return
                                    // things as-is, so doing a "set"
                                    // operation in these cases becomes a
                                    // no-op.
                                    Option::some,
                                    Option::some,
                                    Option::some,
                                    Option::some)))));
  }
}
