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

import static edu.rice.json.Scanner.JsonPatterns;
import static edu.rice.json.Scanner.JsonPatterns.CLOSECURLY;
import static edu.rice.json.Scanner.JsonPatterns.CLOSESQUARE;
import static edu.rice.json.Scanner.JsonPatterns.COLON;
import static edu.rice.json.Scanner.JsonPatterns.COMMA;
import static edu.rice.json.Scanner.JsonPatterns.FAIL;
import static edu.rice.json.Scanner.JsonPatterns.FALSE;
import static edu.rice.json.Scanner.JsonPatterns.NULL;
import static edu.rice.json.Scanner.JsonPatterns.NUMBER;
import static edu.rice.json.Scanner.JsonPatterns.OPENCURLY;
import static edu.rice.json.Scanner.JsonPatterns.OPENSQUARE;
import static edu.rice.json.Scanner.JsonPatterns.STRING;
import static edu.rice.json.Scanner.JsonPatterns.TRUE;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

import edu.rice.regex.Token;
import io.vavr.control.Option;

// Both javac's warning system and ErrorProne complain about
// the generated code, but it's perfectly fine.
@SuppressWarnings({"FallThrough", "fallthrough"})
%%

// Engineering note: You're looking at a tokenizer / scanner written
// using JFlex (http://www.jflex.de/).  You'll notice that there are
// three sections here. Up above are a selection of Java imports, then
// after the first %%, you'll see some weird directives beginning with
// % and then some definitions that are just regular expressions.
// Lastly, after the second %%, you'll see a series of "patterns"
// followed by Java code that returns something.

// As part of how we configured Gradle, it runs a "jflex" task
// whenever necessary, translating everything here to a new Java file:
// build/generated-src/jflex/edu/rice/json/FlexScanner.java which
// implements the actual scanner. The generated Java code wouldn't
// pass all of our style checks on its own, so you'll notice we
// suppress some warnings, up above, and we've also had to configure
// CheckStyle to ignore it. Still, go read it to have an idea of
// what JFlex creates and why we'd never want to have to write
// code like that by hand.

// Why are we using JFlex? JFlex takes the regular expressions and
// converts them into a pre-compiled DFA state machine which, unlike
// our original java.util.regex version, runs very fast and doesn't
// crash on long inputs. This makes our JSON parser more robust.

// Here's the hard part: Consider an input like "nullnullnull" or
// "falsetruefalse". We need to reject those.

// We accomplished the same task with Java's regular expression
// library by using the "\b" ("word boundary") regular
// expression. Unfortunately, JFlex doesn't support \b.

// The solution? We have two "states" for the tokenizer. In the
// initial state (called "YYINITIAL"), we'll accept any of the tokens
// that JSON knows about. However, if we get a "wordy" token (null,
// true, false, etc.), then we'll both return that *and* switch to a
// new state, "NEEDSPACE", which has its own set of rules. If a wordy
// token shows up in the NEEDSPACE state, that will be treated as an
// error, and the failToken is returned. Otherwise, if one of the
// "spacey" tokens shows up, we can switch back to the YYINITIAL state.

// The general idea then is that the caller will keep invoking the
// function yylex(), which will ultimately hit one of the return
// statements below, while advancing its internal state to the next
// token. Note also how {WhiteSpace} doesn't return anything. JFlex
// will just continue on to the next token, thus quietly filtering out
// whitespace, while still using it to change states.

// Note that we're using Option as part of the type we're
// returning. We use Option.some() to signal that we've got another
// successful token except for the failToken, which signals that the
// tokenizer got invalid input. When we hit the end of the input, we
// return Option.none().

// Historical note: the original "lex" tool dates back to 1975, and
// did pretty much the same thing as what we see here, only it
// generated C code, not Java code. One of the authors of "lex" was
// Eric Schmidt, who worked on it as a summer intern at Bell
// Labs. Much later, he was the CEO of Google.

// https://en.wikipedia.org/wiki/Lex_(software)
// https://en.wikipedia.org/wiki/Eric_Schmidt

%public
%unicode
%type Option<Token<JsonPatterns>>
%class FlexScanner
%eofval{
  return none();
%eofval}

%{
  // We're using singleton instances for all of these tokens, avoiding
  // unnecessary memory allocation and thus speeding up the tokenizer.
  // For JSON strings and numbers, which will be different, we'll make
  // new Token objects.

  public final static Option<Token<JsonPatterns>> failToken = some(new Token<>(FAIL, ""));
  public final static Option<Token<JsonPatterns>> nullToken = some(new Token<>(NULL, "null"));
  public final static Option<Token<JsonPatterns>> trueToken = some(new Token<>(TRUE, "true"));
  public final static Option<Token<JsonPatterns>> falseToken = some(new Token<>(FALSE, "false"));
  public final static Option<Token<JsonPatterns>> openCurlyToken = some(new Token<>(OPENCURLY, "{"));
  public final static Option<Token<JsonPatterns>> closeCurlyToken = some(new Token<>(CLOSECURLY, "}"));
  public final static Option<Token<JsonPatterns>> openSquareToken = some(new Token<>(OPENSQUARE, "["));
  public final static Option<Token<JsonPatterns>> closeSquareToken = some(new Token<>(CLOSESQUARE, "]"));
  public final static Option<Token<JsonPatterns>> commaToken = some(new Token<>(COMMA, ","));
  public final static Option<Token<JsonPatterns>> colonToken = some(new Token<>(COLON, ":"));
%}

WhiteSpace = [\s\r\n]+
Number     = (-)?(0|([1-9][0-9]*))(\.[0-9]+)?([eE][+-]?[0-9]+)?
String     = \"{Char}*\"
Char       = [^\\\"\p{Control}]|\\[\"\\bfnrt\/]|\\u[0-9A-Fa-f]{4}

%states NEEDSPACE

%%
<YYINITIAL> {
    "{"       { return openCurlyToken; }
    "}"       { return closeCurlyToken; }
    "\["      { return openSquareToken; }
    "\]"      { return closeSquareToken; }
    ","       { return commaToken; }
    ":"       { return colonToken; }

    {WhiteSpace} { /* continue to next token */ }

    "null"    { yybegin(NEEDSPACE); return nullToken; }
    "true"    { yybegin(NEEDSPACE); return trueToken; }
    "false"   { yybegin(NEEDSPACE); return falseToken; }
    {Number}  { yybegin(NEEDSPACE); return some(new Token<>(NUMBER, yytext())); }
    {String}  { yybegin(NEEDSPACE); return some(new Token<>(STRING, yytext())); }

    .         { return failToken; }
}

<NEEDSPACE> {
    "{"       { yybegin(YYINITIAL); return openCurlyToken; }
    "}"       { yybegin(YYINITIAL); return closeCurlyToken; }
    "\["      { yybegin(YYINITIAL); return openSquareToken; }
    "\]"      { yybegin(YYINITIAL); return closeSquareToken; }
    ","       { yybegin(YYINITIAL); return commaToken; }
    ":"       { yybegin(YYINITIAL); return colonToken; }

    {WhiteSpace} { yybegin(YYINITIAL); /* continue to next token */ }

    .         { return failToken; }
}
