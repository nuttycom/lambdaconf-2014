# Complexity and Reasoning in Functional Programming

[Kris Nuttycombe](http://github.com/nuttycom)

April 19, 2014

#

> "Let me take you on an adventure which will give you superpowers."

[Chris Allen](https://twitter.com/bitemyapp/status/455464035987623936)

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

#

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

#

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

#

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

#

Let's get a little more involved. How many states does this program represent?

~~~{.haskell}

tuple :: (Bool, Int32)
  
~~~

<div class="fragment">
* 2^32^ states if the value on the left is True
* 2^32^ states if it's False

* 2 * 2^32^ = 2^33^ possible states in total
</div>

#

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

#

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

#

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



</div>
