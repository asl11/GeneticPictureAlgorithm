<img align=right src="https://avatars3.githubusercontent.com/u/5786132?s=200&v=4" width=200 height=200 alt="vavr.io logo">

# Notes for Comp215 Alumni on VAVR

*Current Comp215 students*: this file isn't particularly useful for you, but feel free to show
it to any former Comp215 student, who may have taken the class prior to Fall 2019.

*Prior Comp215 students*: Starting in Fall 2019, Comp215 has left behind many of its core classes (`IList`, `IMap`, and so forth)
and adopted a library called VAVR. A current student may ask you for help with their work. This
document tells you want you need to know.

## Table of contents
- [VAVR vs. "Old" Comp215](#vavr-vs-old-comp215)
- [Structured pattern matching](#structured-pattern-matching)
- [Folding and lifting](#folding-and-lifting)
- [Other helper methods](#other-helper-methods)
- [Honor Code notes](#honor-code-notes)
- [VAVR's evolution and the Comp215 subset of VAVR](#vavrs-evolution-and-the-comp215-subset-of-vavr)
- [Fun reading on the future of Java](#fun-reading-on-the-future-of-java)

## VAVR vs. "Old" Comp215

Here's a table that shows something of how "old" Comp215 classes line up with VAVR classes:

| "Old" Comp215 | VAVR |
| ---- | ---- |
`edu.rice.list.IList` | `io.vavr.collection.Seq` |
`edu.rice.list.List` | `io.vavr.collection.List` |
`edu.rice.list.LazyList` | `io.vavr.collection.Stream` |
`edu.rice.tree.IMap` | `io.vavr.collection.Map` |
`edu.rice.tree.ISet` | `io.vavr.collection.Set` |
`edu.rice.util.Memo` | `io.vavr.Lazy` |
`edu.rice.util.Option` | `io.vavr.control.Option` |
`edu.rice.util.Try` | `io.vavr.control.Try` |

The concepts are all the same, and many of the classes have the same names. Because
VAVR uses names that collide with names in `java.util` like `List` and `Stream`, probably the #1 source of
confusion will occur if students accidentally import "the wrong List" or "the wrong Stream".
Inevitably, when a student is complaining about not seeing the right methods or is getting
weird compiler errors, the first thing to do is to will be to check their `import` statements.

Other core classes, like `edu.rice.json.Value`, are more-or-less the same as beforehand,
but many of the method names have changed to be more consistent with how VAVR names its methods.
For example, in `edu.rice.json.Value.JObject`:


| "Old" Comp215 | New Comp215 |
| ---- | ---- |
| `public Option<Value> oget(String key)` | `public Option<Value> get(String key)` |
| `public Value get(String key)` | `public Value apply(String key)` |

In this case, the new Comp215's `Value` class is implementing a specific interface,
VAVR's [PartialFunction](https://static.javadoc.io/io.vavr/vavr/0.10.2/io/vavr/PartialFunction.html),
which is also used by VAVR's various map classes. 

More generally, all the methods in "old" Comp215 that had an "o" in front of their name now
have "Option" at the end. For example, the JSON operation `ogetPath` is now called `getPathOption`.
Again, we're following the naming conventions generally used throughout VAVR (and, incidentally,
throughout the Scala standard libraries as well).

The "old" names vs. new names are all entirely comprehensible, but they are different. For example:

| **`edu.rice.list.IList`** | **`io.vavr.collection.Seq`** |
| ---- | ---- |
| `IList<T> add(T t)` | `Seq<T> prepend(T element)` |
| `T head()` | `T head()` |
| `Option<T> ohead()` | `Option<T> headOption()` |
| `boolean empty()` | `boolean isEmpty()` |
| `IList<T> append(T t)` | `Seq<T> append(T element)` |
| `int length()` | `int length()` |
| `IList<T> filter(Predicate<? super T> predicate)` | `Seq<T> filter(Predicate<? super T> predicate)` |
| `<R> IList<R> flatmap(Function<? super T, ? extends IList<? extends R>> f)` | `<U> Seq<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper)` |
| `<R> IList<R> oflatmap(Function<? super T, Option<? extends R>> f)` | *not needed, regular `flatMap` works here!* |
| `IList<T> concat(IList<? extends T> afterTail)` | `Seq<T> appendAll(Iterable<? extends T> elements)` |
| `IList<T> limit(int n)` | `Seq<T> take(int n)` |
| `T nth(int n)` | `T apply(Integer index)` and also `T get(int index)` |
| `Option<T> oNth(int n)` | *missing!* |
| `IList<T> skipN(int n)` | `Seq<T> drop(int n)` |
| `<U> U foldl(U zero, BiFunction<? super U, ? super T, ? extends U> f)` | `<U> U foldLeft(U zero, BiFunction<? super U, ? super T, ? extends U> f)` |
| `String join(String mergeStr)` | `String mkString(CharSequence delimiter)` |

| **`edu.rice.list.List`** | **`io.vavr.collection.List`** |
| ---- | ---- |
| `static <T> IList<T> makeEmpty()` | `static <T> List<T> empty()` |
| `static <T> IList<T> make(T headVal, IList<? extends T> tailVal)` | *missing for List, available for Stream as* `static <T> Stream<T> cons(T head, Supplier<? extends Stream<? extends T>> tailSupplier)` |
| `static <T> IList<T> of(T... source)` | `static <T> List<T> of(T... elements)` |

None of these differences are insurmountable, but they may cause confusion when
an "old" Comp215 student is looking at "new" Comp215 code. Conceptually, the only
really big change here is that VAVR's `flatMap` is much more powerful than the
"old" Comp215 version, because it takes any `Iterable` and will then flatten
those results into the resulting sequence. All sorts of things are iterable,
including `Option`, so `oflatmap` was no longer necessary.

Also potentially confusing, while the "old" Comp215 library had one list-like interface, `IList`, which
was implemented by both `List` and `LazyList`, VAVR has a whole hierarchy of list-like interfaces
and list-like classes that implement them:

<img src="https://www.vavr.io/vavr-docs/images/collections-seq.png">

The general idea is that just about *anything* can be traversed, so that's the
most general interface in VAVR for collections. And, of course, everything
that can be traversed is also compatible with the old-school Java `Iterable`, so
it can be easily passed to and adapted to work with legacy Java code.

Broadly, the "indexed" sequences promise a sub-linear way of getting the nth
element in the sequence, while the "linear" sequences promise a sub-linear way
of working with the "front" of the list.

Anyway, where we used `IList` ubiquitously throughout the "old" Comp215 codebase,
and could then use regular or lazy lists, as appropriate, we now use `Seq` ubiquitously,
and mostly only ever use the `List` and `Stream` instances of it.

## Structured pattern matching

"Old" Comp215 had a `match` method in all of its major data structures for deconstructing / visiting
the different cases. For example, here's an old `IList.match` method:

```java
interface IList {
  /**
   * General-purpose structural pattern matching on a list with deconstruction as well.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param nonEmptyFunc
   *     called if the list has at least one value within; the head of the list is the first argument,
   *     then a list with the remainder
   * @param <R>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of invoking whichever function matches
   */
  default <R> R match(Function<? super IList<T>, ? extends R> emptyFunc,
                      BiFunction<? super T, ? super IList<T>, ? extends R> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }
}
```

VAVR has a [sophisticated matching system](https://www.vavr.io/vavr-docs/#_the_basics_of_match_for_java), but
we're not using it in Comp215 because it's [going away in a future version of VAVR](https://blog.vavr.io/vavr-one-log-02/),
driven in no small part by [future features coming to Java](#fun-reading-on-the-future-of-java).
So what are we supposed to do in Comp215? We don't want to make invasive changes to VAVR, nor do
we want to code with VAVR features that won't be around for much longer. Our solution is to
have static helper methods in a new `edu.rice.vavr` package. So here's the equivalent
to the `match` method, above, inside `edu.rice.vavr.Sequences`:

```java
interface Sequences {
  /**
   * General-purpose pattern matching on a {@link Seq} (i.e., a {@link io.vavr.collection.List} or
   * {@link io.vavr.collection.Stream}) with two lambdas: one for the empty-list case, one for a
   * list with one or more entries.
   */
  static <T, R> R seqMatch(
      Seq<T> input,
      Function<Seq<T>, ? extends R> emptyF,
      BiFunction<T, Seq<T>, ? extends R> oneOrMoreElemF) {
  
    if (input.isEmpty()) {
      return emptyF.apply(input);
    } else {
      return oneOrMoreElemF.apply(input.head(), input.tail());
    }
  }
}
```

This static method, in `edu.rice.vavr.Sequences`, operates exactly the same way as the old
default method in `edu.rice.list.IList`, except since it's static, the sequence that it's operating
on has to be a parameter to the `seqMatch` method. In other words, it's "just a function"
rather than being an instance method. 

## Folding and lifting

VAVR's `Option` class doesn't have a `match` method either, but it does have a `fold` method,
which works exactly the way the "old" Comp215 Option's `match` method worked. So why is it called `fold`? VAVR
is generalizing the concept of folding as the process of going from `Something<T>`
to just `T`. Comp215's codebase uses these VAVR fold methods in all the places
it used to use `match` methods.

We only briefly touched on "lifting" in the "old" Comp215, where we had a method that took
some arbitrary function on inputs and generated a new function that takes optional inputs;
if any of them were `none()`, then the lifted function would return `none()` as well.
That lives on as a helper method, `optionLift` inside of `edu.rice.vavr.Options`.

VAVR also has a broader and more general concept of lifting. For example, you can
treat any `Seq<T>` as a `PartialFunction<Integer, T>` (i.e., a mapping from an
index in the sequence to the corresponding value, except it's not defined for
all possible indices). `Seq` has a method `Function1<Integer, Option<T>> lift()`
which converts that partial function to a "total" function, defined over all
possible indices, but returning `some()` vs. `none()`  as appropriate.

Anyway, if you [browse the VAVR 0.10.2 codebase](https://github.com/vavr-io/vavr/tree/v0.10.2), 
you'll find all sorts of "lifting" going on. You'll also see that
many lifting methods are labeled `@Deprecated` as part of the broader transition
to VAVR 1.0. We won't be using those in Comp215.

## Other helper methods

We've made a variety of other static helper methods in the classes within `edu.rice.vavr`. For example:

| Helper method | Purpose |
| --- | --- |
| `Maps.updateMap` | Given a VAVR `Map` instance and a key, uses a general purpose updating function to either add a new value or update the existing value. |
| `Maps.mapFromSeq` | Given a sequence of values, of any type, and two functions to extract keys and values from the sequence, constructs a new Map, while also having a dedicated merge function on values if the same key occurs more than once. |
| `Sequences.seqIsSorted` | Given a sequence, determines if it's in sorted order. |

Perhaps unsurprisingly, VAVR has a ton of new features, but it doesn't include every
single feature of the "old" Comp215 library. We didn't want to make changes to VAVR
itself, so we made helper methods instead.

## Honor Code notes

As a former Comp215 student, you may be asked by a current student to help. 
Feel free to offer spoken advice to a current student and even
to look over their shoulder at their code. However, don't pull up your original
work when there's a student who might see it, and certainly don't share your 
original work or our reference code with a current student.

A good rule of thumb is to keep your hands in your pockets. If they're doing all
the typing and you're just talking, you'll avoid any Honor Code issues. If you're
doodling on a whiteboard, don't just freehand a solution to the student's problems.
Instead, focus on identifying the concept where the student is stuck and try to
nudge them in the correct general direction.

## VAVR's evolution and the Comp215 subset of VAVR

Daniel Dietrich, VAVR's main author, wrote a series of [blog posts](https://blog.vavr.io/) about the future of VAVR. He intends significant changes
from the "zero dot x" versions (we're using "0.10.2") to the upcoming "1.0" release, partly in response
to new features promised in upcoming versions of the Java language, itself. For example, VAVR 1.0
is removing [VAVR's matching features](https://www.vavr.io/vavr-docs/#_the_basics_of_match_for_java), 
since future Java versions will have something like this built-in 
(see below on [the future of Java](#fun-reading-on-the-future-of-java)).

VAVR 1.0, today, is only available in an early and incomplete alpha release, and those future Java language
features aren't here yet; Dietrich makes
no promises for when VAVR 1.0 will be available as a final, stable, production release. Since
the Comp215 show must go on, Comp215 is using the last stable release of the original VAVR.

VAVR is really a series of separate modules: 
- VAVR itself, a library with essential data and control structures for functional programming in Java
- VAVR-Match, which allows the programmer to write code with structured pattern matching (behind the scenes, there's even a _code generator_ to support this)
- VAVR-Test, a property-based testing library for unit tests
- Support for Jackson and GSON (popular input/output serialization libraries)

We're only using the core of VAVR in Comp215. We're not using VAVR-Match, since it's going away
in the next major release of VAVR, nor are we using Jackson or GSON since we build
our own JSON input and output. 

[VAVR-Test](https://www.vavr.io/vavr-docs/#_property_checking) is an example of a _property-based testing_ library. We definitely
like this style of software testing, but we're doing it with 
[QuickTheories](https://github.com/quicktheories/QuickTheories). Both VAVR-Test and QuickTheories have similar designs,
but QuickTheories has a simpler API. Once you know QuickTheories, you'll be able to pick
up comparable libraries, in Java and other languages, with only minor effort. (For example,
the [Hypothesis](https://hypothesis.readthedocs.io/en/latest/) library for Python does
all the same things.)

Since VAVR doesn't have every possible feature we might want, especially without VAVR-Match,
we're providing a series of "helper methods" in the `edu.rice.vavr` package:
- [edu.rice.vavr.Maps](src/main/java/edu/rice/vavr/Maps.java)
- [edu.rice.vavr.Options](src/main/java/edu/rice/vavr/Options.java)
- [edu.rice.vavr.Sequences](src/main/java/edu/rice/vavr/Sequences.java)
- [edu.rice.vavr.Tries](src/main/java/edu/rice/vavr/Tries.java)

Since QuickTheories doesn't know anything about VAVR, we also provide a QuickTheories
[generator for VAVR sequences](src/test/java/edu/rice/qt/SequenceGenerators.java), which we'll use when testing our code.

### VAVR customizations for Comp215

*(Advanced stuff: students don't need to understand this, but after the semester is over, it might be helpful.)*

We've made a small but significant customization to VAVR itself: We added [ErrorProne](https://errorprone.info/) annotations
to the VAVR code; our custom build of VAVR 0.10.2 is included in the "libs" directory of a Comp215 code repository.
Our annotations make sure that student code will be flagged by ErrorProne if it fails to check
a return value from VAVR or if it tries to pass a `null` somewhere it isn't expected.

In short, VAVR is perfectly happy to have `null` inside lists, inside `Option` or `Try`, and 
in many other places, while Comp215 has a strong _no nulls_ rule. The ErrorProne
annotations that we added to VAVR will have it reject `null` values in places where
VAVR is perfectly happy having them. For example, it's perfectly meaningful in VAVR
to say `Option.some(null)` or `Try.success(null)`, but our version will reject these.

We also added `@CheckReturnValue` annotations to most VAVR methods. This means if a student
forgets to save the value they get back from these methods, ErrorProne will generate a 
warning. This will help students find bugs in their code.

*What's this mean?*
- Every program that works in the Comp215 VAVR subset will also work identically with regular VAVR.
  But not every program written with VAVR will work in the Comp215 subset.
  
- In the future, if a student is working with the regular VAVR, they'll have to worry about _null_ values
  in lists, options, and so forth, which will never be an issue in Comp215.
  
### Other alternative libraries

VAVR isn't the only library for functional programming in Java. Some other popular alternatives
are [Cyclops](https://github.com/aol/cyclops), [FunctionalJava](https://www.functionaljava.org/) and [Derive4J](https://github.com/derive4j/derive4j).
We selected VAVR because it's well documented, widely used,
and has the features we need. So when might anybody want to go beyond VAVR?
 
- If you're defining a bunch of algebraic data types, and you want to have the
  equals, hashCode, and even lenses automatically generated for you.
  
- If you're trying to port a Haskell program to Java and you needed more
  advanced monadic features or perhaps even
  [higher-kinded types](https://www.stephanboyer.com/post/115/higher-rank-and-higher-kinded-types). 
  
If you read the documentation for these and other functional programming libraries for Java,
a common theme is how they're inspired by libraries from
[Scala](https://www.scala-lang.org/), which in turn borrowed its core ideas from
other programming languages. A student who has learned VAVR should find that it's straightforward to move back and forth
once they know the basic concepts.

## Fun reading on the future of Java

A lot of what we do in Comp215 is all about finding ways in Java to express
_algebraic data types_. That means that, for example, two lists built
with the same contents should be `equals()` to each other and have the same `hashCode()`. 
This should also hold true for their heads and tails.

Future versions of Java are adding a ton of features to make ADTs cleaner to
express in Java. Under the banner of
[Java's Project Amber](https://cr.openjdk.java.net/~briangoetz/amber/datum.html),
several very interesting projects are under way:

- [Sealed types](https://bugs.openjdk.java.net/browse/JDK-8227043)
- [Records](https://bugs.openjdk.java.net/browse/JDK-8222777)
- [Pattern matching](https://cr.openjdk.java.net/~briangoetz/amber/pattern-match.html)

Once all these features land in some future version of Java, everything we teach
in Comp215 will be redone to use them. This is also what's driving many of the
changes in VAVR, since VAVR is also currently built with a style similar to
the Comp215 standard style.
