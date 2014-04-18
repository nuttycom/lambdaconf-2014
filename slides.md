# Complexity and Reasoning in Functional Programming

[Kris Nuttycombe](http://github.com/nuttycom)

April 19, 2014

---------

<img src="./img/ras_anvil.jpg" width="400"/>

> "Let me take you on an adventure which will give you superpowers." --[`@bitemyapp`](https://twitter.com/bitemyapp/status/455464035987623936)

<div class="notes">

I believe that it is possible to write perfect software. By "perfect", I mean
something very specific: that every state that is representable in source code
is a valid state, and that no invalid states may be obtained by the process of
assembly of the program's components. 

Now, this is a fine sentiment, but it obviously depends very much upon what is
considered to be invalid. A reasonable first estimation of an invalid state is
one that is unexpected; naturally, such a state definitionally is one that the
programmer has not prepared the program to operate correctly from.

</div>

# Complexity 

<div class="notes">

When we talk about software programs, we use the word complexity to refer to a
lot of different things; some, like big-O complexity of algorithms and
Kolmogorov complexity, are well-defined; much more frequently, we throw the
word complexity around to refer and vague and handwavey notions of
comprehensibility, maintainability, and so forth. Throughout this talk though,
I'm going to strive to focus on a single definition when I refer to complexity:
how many different possible states can a program represent?

</div>

---------

## What is a program?

![](./img/moviecode.jpg)

<div class="notes">

A program serves a single purpose: to describe the transformation of
information from one form to another. Most of the time, this transformation
involves many intermediate steps, and may involve various parties along the way
responsible for providing or interpreting the data being transformed. 

You'll note that I say "describes the transformation" rather than "transforms."
Even if a program is never evaluated, it describes a fact about the universe:
that some input data can be combined or reduced to yield some output data. 

Now, I'm going to write the simplest program possible.

By the way. I'm going to jump confusingly back and forth between Haskell and
Scala in this talk, mostly because I don't know Haskell very well. For most
things, it's just flat out better at expressing them than Scala is, but I think
that for some other things, until you get used to Haskell syntax, Scala is
better. This is an opinion some of you will probably throw tomatoes at me for
holding; that's fine.

</div>

---------

## A very simple program

~~~{.haskell }

True
  
~~~

<div class="fragment">

This program represents a single state.

</div>

#

~~~{.haskell}

data Bool = True | False

bool :: Bool
  
~~~

<div class="fragment">
`::` is pronounced, "has type". <br/>

So the program `bool` has type Bool. <br/>

How many states can the program `bool` represent?
</div>

<div class="notes">

Of course, 1 + 1 = 2

Right, there are two states that a boolean program can occupy.  Since this is a
pure program and it has no inputs, it'll always return the same one, of course,
but we can't tell which one from the type, so we'll treat it as occupying both.

</div>

---------

The following program represents an application with 2^32^ possible states.

~~~{.haskell}

i :: Int32
  
~~~

<div class="fragment">

And this one is just awful...

~~~{.haskell}

s :: String
  
~~~

</div>

---------

Let's get a little more involved. How many states does this program represent?

~~~{.haskell}

tuple :: (Bool, Int32)
  
~~~

<div class="fragment">
* 2^32^ states if the value on the left is True
* 2^32^ states if it's False

* 2 * 2^32^ = 2^33^ possible states in total
</div>

---------

## Products

~~~{.haskell}

tuple :: (Int32, Int32)
  
~~~

<div class="fragment">

2^32^ * 2^32^ = 2^64^

For tuples, we always multiply. 

These are called "product" types

The number of possible states is the product of the member's possible states.

</div>

---------

## Sums

~~~{.haskell}

data Bool = True | False

data Maybe a = Just a | Nothing

data Either a b = Left a | Right b
  
~~~

Bool, Maybe and Either are called sum types. Can you see why?

<div class="fragment">
~~~{.scala}

sealed trait Maybe[+A]

case class Some[A](a: A) extends Maybe[A]

case object None extends Maybe[Nothing]
  
~~~
</div>

---------

## Isomorphisms

~~~{.haskell}

tuple :: (Bool, Int32)

either :: Either Int32 Int32
  
~~~

These types are isomorphic.

# Product Bias 

~~~{.haskell}

maybe :: Maybe Int32

-- (Just) 2^32 + (Nothing) 1
  
~~~

Most languages emphasize products.

Many don't allow you to define a type representing 2^32^ + 1 states well.

<div class="notes">

Sapir-whorf says that language defines what thoughts we can have; in
programming, languages influence the kinds of data structures that we end up
using by making different things easy. Unfortunately, most mainstream languages
today make it really easy to define product types, but at very least a bit more
challenging or verbose to define sum types. The effect of this is that most
languages encourage coders to try to define their programs principally in terms
of product types (objects!) and in doing so encourage them to choose
representations that literally multiply the complexity of their programs. I'm 
going to pick on Java here because it's an easy target, but I'm sure that you 
can see the parallels to most other mainstream languages.

</div>

---------

~~~{.java}
interface EitherVisitor<A, B, C> {
  public C visitLeft(Left<A, B> left);
  public C visitRight(Right<A, B> right);
}

interface Either<A, B> {
  public <C> C accept(EitherVisitor<A, B, C> visitor);
}

public final class Left<A, B> implements Either<A, B> {
  public final A value;
  public Left(A value) {
    this.value = value;
  }

  public <C> C accept(EitherVisitor<A, B, C> visitor) {
    return visitor.visitLeft(this);
  }
}

public final class Right<A, B> implements Either<A, B> {
  public final B value;
  public Right(B value) {
    this.value = value;
  }

  public <C> C accept(EitherVisitor<A, B, C> visitor) {
    return visitor.visitRight(this);
  }
}
~~~

--------

## Error Handling

The best way to handle errors in a program is not to handle them at all.
Instead, we want to make error states *unrepresentable.*

<div class="notes">

Primitive types are, in general, pretty terrible for this purpose.

Int32 is probably not be a very good type for your domain. What if negative
values should be regarded as invalid? What if the maximum value you should ever
expect to encounter is 42? 

And of course, strings are just awful.

</div>

--------

# Strings

<img src="./img/no_strings.png" width="600"/>

_782,000? Not nearly enough._

--------

> The type `String` should only ever appear in your program when a value
> is being shown to a human being.

--------

## Two egregious offenders

* Strings as dictionary keys
* Strings as serialized form

_so seductive_

<div class="notes">

Why are strings any worse than any other (potentially infinite) data structure?
It's that they're convenient. A very common misfeature is to use strings as a
serialization mechanism. Serializing values as strings is really appealing,
because you can just read the serialized form directly for debugging and so
forth. The value of this in badly written systems isn't to be understated, but
the goal is to write perfect systems.

I'm not saying don't ever use strings. But I am saying that String is a data
type of last result, and that if you're ever going to be encoding any semantic
information into strings you'd better have bulletproof validation on your
serialization and deserialization.

</div>

--------

# Mitigation

Use newtypes liberally.

~~~{.haskell}

newtype Name = Name { strValue :: String }
-- don't export strValue unless you really, really need it
  
~~~

~~~{.scala}

case class Name(strValue: String) extends AnyVal
  
~~~

**Never, ever pass bare String values<br/>unless it's to `putStrLn` or equivalent.**

**Never, ever return bare String values <br/>except from `readLn` or equivalent**

<div class="notes">

If a string is serving any purpose other than display in your system, then it
has semantic meaning that should be tracked by the type system. 

</div>

--------

## Mitigation

Hide your newtype constructor behind validation.

~~~{.scala}

case class IPAddr private (addr: String) extends AnyVal

object IPAddr {
  val pattern = """(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})""".r

  def addr(s: String): Option[IPAddr] = for {
    xs <- pattern.unapplySeq(s) if xs.forall(_.toInt <= 255)
  } yield IPAddr(s)
}
  
~~~

We've shrunk down an infinite number of states to (256^4^ + 1).
Given the inputs, that's the best we can do.

<div class="notes">

This applies to virtually every primitive type. Of course, you need to be cautious of
boxing in languages where this is relevant and code is performance-sensitive, but 
even in high-performance applications it's rare that you're passing large numbers of
values around individually; newtype entire collection types if necessary.

Make the compiler work for you.

</div>

--------

# Errors



_If it can go wrong, it needs to have a type._

--------

# Side Effects

![](./img/trapped.png)

--------

## Managing Effects


