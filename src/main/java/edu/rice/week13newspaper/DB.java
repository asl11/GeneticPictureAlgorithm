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

package edu.rice.week13newspaper;

import static edu.rice.json.Builders.jarray;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.json.Operations.getPathOptionArray;
import static edu.rice.json.Operations.getPathOptionString;
import static edu.rice.json.Parser.parseJsonObject;
import static edu.rice.vavr.Maps.mapFromSeq;
import static edu.rice.vavr.Options.optionLift;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.json.Value;
import edu.rice.util.Log;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

/**
 * A super-basic newspaper article database, validates that the JSON is well-formed as part of
 * building up an author database (i.e., a mapping from strings to Author records) and a list of
 * Articles (which have a list of authors, a title, and a body).
 *
 * <p>Note that if you were doing this for real, there are sophisticated JSON rules enforcement
 * engines where you describe all the data requirements up front. <a
 * href="http://json-schema.org/">JSON-Schema</a> is a standard for this sort of thing, and there
 * are several validators out there that can use it.
 *
 * <p>This database demonstrates a variety of ways of doing computations on Option values, dealing
 * with errors when they occur by logging an appropriate error message and returning Option.none().
 * This sort of coding style is *much* cleaner than the old-school Java style of throwing
 * exceptions. Note also that this database knows how to read itself in from JSON and write itself
 * back out to JSON again. If we wanted to be fancier, we might imagine having some sort of public
 * "add article" method that reporters or editors might use.
 */
public interface DB {
  /**
   * This is the main entry point: given a raw string of JSON data, parseJsonObject it as JSON, then
   * load up the authors and articles.
   */
  static Option<Tuple2<Map<String, Author>, Seq<Article>>> load(String contents) {
    // Engineering note: In this Java class, you'll see several
    // variants on how flatMap(), map(), and match() can be combined
    // together. In the example below, you can read the steps from top
    // to bottom:

    // 1) parse the JSON
    // 2) use that to make the author database
    // 3) use that to make the article list
    // 4) use that as part of the pair of things we're returning.

    // But you'll notice that the lambdas are *nested*. Why do it this
    // way? In this case, you'll notice that some of the intermediate
    // values like jValue or authorDB are used more than once. That
    // means we need to give them names, and hang onto them. We're
    // taking advantage of the *lexical scope* rules that allow
    // lambdas to capture values from their environment.

    // If we didn't need to reuse values like this, we might prefer
    // the *pipeline* style that you see in, for example, makeAuthor
    // (below).

    // The payoff here is that there's no explicit error
    // handling. It's all hidden behind the Option processing with
    // flatMap() and map(). If any of those do return Option.none(),
    // then that value will be the ultimate result of this load()
    // call.

    // Also, you'll notice that we're trying to be somewhat consistent
    // in our variable naming.  Anything that's a JSON value of
    // whatever sort begins with a lower-case j. Anything that's an
    // Option of whatever sort begins with a lower-case o. This style
    // of coding is often named "Hungarian Notation"
    // (https://en.wikipedia.org/wiki/Hungarian_notation), and some
    // organizations use it ubiquitously.  In Comp215, we use it where
    // it seems helpful, but not everywhere.

    return parseJsonObject(contents)
        .flatMap(
            jValue ->
                makeAuthorDB(jValue)
                    .flatMap(
                        authorDB ->
                            makeArticleList(jValue, authorDB)
                                .map(articles -> Tuple.of(authorDB, articles))));
  }

  /** Given the root of the JSON database, extracts the authors and returns the author database. */
  static Option<Map<String, Author>> makeAuthorDB(Value input) {
    final var TAG = "DB.makeAuthorDB";

    // we require an authors field which has an array of authors inside

    var jAuthorList = getPathOptionArray(input, "authors").getOrElse(List.empty());

    var authors = jAuthorList.flatMap(DB::makeAuthor);

    // if any of the authors failed, then that's an error for us
    if (authors.length() != jAuthorList.length()) {
      Log.eformat(
          TAG,
          "failed to read authorDB: only %d of %d authors were complete",
          authors.length(),
          jAuthorList.length());
      return none();
    }

    // we require that there are authors!
    if (authors.isEmpty()) {
      Log.e(TAG, "failed to read authorDB: no authors found");
      return none();
    }

    var authorDB =
        mapFromSeq(
            authors,
            author -> author.email,
            author -> author,
            (author1, author2) -> {
              Log.eformat(TAG, "same author appears more than once: %s vs. %s", author1, author2);
              return author1;
            });

    // what if the same email happened twice? then the authorDB would
    // only have it once, and that's an error
    if (authorDB.toList().length() != authors.length()) {
      // errors already logged above, so we're done
      return none();
    }

    Log.i(TAG, "Author database: " + authorDB);

    return some(authorDB);
  }

  /**
   * Given the root of the JSON database and the author database, extracts a list of articles,
   * ensuring that each article has a valid author.
   */
  static Option<Seq<Article>> makeArticleList(Value input, Map<String, Author> authorDB) {
    final var TAG = "DB.makeArticleList";

    // we require an articles field which has an array of articles inside
    return getPathOptionArray(input, "articles")
        .flatMap(
            jArticleList -> {
              Seq<Article> articles = jArticleList.flatMap(val -> makeArticle(val, authorDB));

              // if any of the articles failed, then that's an error for us
              if (articles.length() != jArticleList.length()) {
                Log.eformat(
                    TAG,
                    "failed to read articles: only %d of %d were complete",
                    articles.length(),
                    jArticleList.length());
                return none();
              }

              return some(articles);
            });
  }

  /**
   * Given a JSON Value, which should be a JSON object with the name and email fields defined,
   * return an Optional Author. If the building process fails, an Option.None is returned.
   */
  static Option<Author> makeAuthor(Value input) {
    final var TAG = "DB.makeAuthor";

    return optionLift(Author::new)
        .apply(
            getPathOptionString(input, "email")
                .onEmpty(() -> Log.e(TAG, "no author email: " + input)),
            getPathOptionString(input, "name")
                .onEmpty(() -> Log.e(TAG, "no author name: " + input)));

    // Engineering note: The above version uses a "lifted" version of
    // the Author::new constructor, where all the arguments are
    // replaced with options. If they're all some(), then the values
    // inside are extracted, the real constructor is run, and some()
    // of the new object is returned.  If any of the inputs are
    // none(), then the constructor never runs.

    // You can see that the top three lines of code are much easier to
    // read than the bottom commented code, with nested lambdas and so
    // forth. Also, the above code, with its embedded logIfNone calls,
    // gives better error feedback.

    /* ****************************************************************
    Option<Author> result =
        getPathOptionString(input, "name")
            .flatMap(name -> getPathOptionString(input, "email")
                .flatMap(email -> some(new Author(email, name))));

    if (result.isEmpty()) {
      Log.e(TAG, () -> "failed to read valid author: " + input);
    }

    return result;
    ***************************************************************** */
  }

  /**
   * Given a JSON Value, which should be a JSON object with the title, body, and authors fields
   * defined, return an Optional Article. If the building process fails, an Option.None returned.
   */
  static Option<Article> makeArticle(Value input, Map<String, Author> authorDB) {
    final var TAG = "DB.makeArticle";

    // We need a bunch of custom logic to deal with the authors: we
    // require at least one author, and every author in the list needs
    // to be in our author database.

    final var oAuthorVals =
        getPathOptionArray(input, "authors")
            .onEmpty(() -> Log.e(TAG, "article missing authors: " + input));

    // We'll look up each author in our database and return them, if
    // they're there, or just filter them out, via the flatMap, and
    // move on otherwise.

    final var oAuthors =
        oAuthorVals.map(
            vals ->
                vals.flatMap(
                    jval ->
                        jval.asJStringOption()
                            .flatMap(jstr -> authorDB.get(jstr.toUnescapedString()))));

    if (oAuthorVals.isEmpty() || oAuthorVals.get().isEmpty()) {
      Log.e(TAG, "article must have at least one author: " + input);
      return none();
    }

    if (oAuthors.isEmpty() || oAuthors.get().length() != oAuthorVals.get().length()) {
      Log.e(TAG, "one or more authors malformed: " + input);
      return none();
    }

    return optionLift(Article::new)
        .apply(
            oAuthors,
            getPathOptionString(input, "title")
                .onEmpty(() -> Log.e(TAG, "article missing title: " + input)),
            getPathOptionString(input, "body")
                .onEmpty(() -> Log.e(TAG, "article missing body: " + input)));
  }

  class Author {
    public final String email;
    public final String name;

    private Author(String email, String name) {
      this.email = email;
      this.name = name;
      // if we were doing this "for real", we'd have author bio
      // information and other such stuff in here, but the email
      // address is the "key" to all this data
    }

    /** Given an Author, this returns a JSON string corresponding to that Author. */
    @Override
    public String toString() {
      return toJson().toString();
    }

    /** Given an Author, this returns a JSON data structure corresponding to that Author. */
    public Value toJson() {
      return jobject(jpair("email", email), jpair("name", name));
    }
  }

  class Article {
    public final Seq<Author> authors;
    public final String title;
    public final String body;

    // if we were doing this "for real", we'd have a whole separate
    // concept of articles having "sections", "dates", and various
    // other metadata. We'd most importantly have unique article IDs,
    // to act as primary keys for the articles, letting us declare
    // that an "issue" is just a list of articles, or that articles
    // can cross-reference one another. Never mind that the "body"
    // would become a lot more complex. Would we do HTML markup inside
    // the JSON structure? Would we do something else?

    private Article(Seq<Author> authors, String title, String body) {
      this.authors = authors;
      this.title = title;
      this.body = body;
    }

    /** Given an Article, this returns a JSON data-structure representation of that Article. */
    public Value toJson() {
      return jobject(
          jpair("title", title),
          jpair("body", body),
          jpair("authors", jarray(authors.map(Author::toJson))));
    }

    /** Given an Article, this returns a JSON string of that Article. */
    @Override
    public String toString() {
      return toJson().toString();
    }
  }
}
