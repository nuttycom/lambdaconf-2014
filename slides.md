# How To Write A Perfect Program

[Kris Nuttycombe](http://github.com/nuttycom) -- [`@nuttycom`](http://twitter.com/nuttycom)

April 19, 2014

---------

<img src="./img/ras_anvil.jpg" width="400"/>

> "Let me take you on an adventure which will give you superpowers." --[`@bitemyapp`](https://twitter.com/bitemyapp/status/455464035987623936)

<https://github.com/nuttycom/lambdaconf-2014-workshop>

<div class="notes">

Trail of Awesome

Perfect Programs

Not on my board

Haskell and Scala

* Mention utility of types. - Piper
* Going to start with a very simple program.

</div>

-------

# Start Very Simple

~~~{.haskell }

True
  
~~~

This program doesn't have any bugs.

<div class="notes">

This program has only a single possible result.

</div>

--------

## Learn To Count

~~~{.haskell}

data Bool = True | False

bool :: Bool
-- "::" is pronounced, "has type"
  
~~~

<div class="fragment">

1 + 1 = 2 

> applause

</div>

<div class="fragment">

~~~{.haskell}

i :: Int32
  
~~~

</div>

<div class="fragment">

This one is just awful...

~~~{.haskell}

s :: String
  
~~~

</div>

--------

## Learn to Add

~~~{.haskell}

data Maybe a = Just a | Nothing

inhabitants (Maybe a) = inhabitants a + 1


data Either a b = Left a | Right b

inhabitants (Either a b) = inhabitants a + inhabitants b
  
~~~

Bool, Maybe and Either are called sum types for the obvious reason.

--------

~~~{.haskell}

tuple :: (Bool, Int32)
  
~~~

* 2^32^ inhabitants if the value on the left is True
* 2^32^ inhabitants if it's False

* 2^32^ + 2^32^ = 2 * 2^32 = 2^33^

--------

## Learn to Multiply

~~~{.haskell}

-- if haskell had unsafe type-level pattern matching... 
inhabitants :: Type -> Nat

inhabitants Bool = 2
inhabitants Int32 = 2^32

inhabitants (a, b) = inhabitants a * inhabitants b
inhabitants (Int32, Int32) = 2^32 * 2^32 = 2^64

inhabitants (a, b, c) = inhabitants a * inhabitants b * inhabitants c
inhabitants (Bool, Bool, Int32) = 2 * 2 * 2^32 = 2^34
  
~~~

With tuples, we always multiply.

We call these "product" types.

--------

2 + 2^32^ = 2 * 2^32 = 2^33^

~~~{.haskell}

either :: Either Int32 Int32
-- 2^33 inhabitants

tuple :: (Bool, Int32)
-- 2^33 inhabitants
  
~~~

These types are isomorphic.

Choose whichever one is most convenient.

---------

## Add if you can, multiply if you must

~~~{.haskell}

inhabitants (Maybe Int32) = 2^32 + 1
  
~~~

Most languages emphasize products.
<br/><br/>

Bad ones don't let you define a type <br/>with 2^32^ + 1 inhabitants easily.

<div class="notes">

Sapir-Whorf

Most mainstream languages today make products easy, sums hard.

Too easy to define types with too many inhabitants.

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

# The Vampire Policy

![](./img/bela-lugosi.jpg)

> "Bug fixing strategy: forbid yourself to fix the bug. Instead, render 
> the bug impossible by construction." 
> --[Paul Phillips](https://twitter.com/extempore2/status/417366903209091073)

<div class="notes">

To do this, we need to minimize the state space of our program.

What types should we not let in?

</div>

--------

# Strings (Ugh.)

> The type `String` should only ever appear in your program when a value is being shown to a human being.

## Two common offenders

* Strings as dictionary keys
* Strings as serialized form

<div class="notes">

Strings are worse than any other potentially infinite data structure
because they're convenient.

A very common misfeature is to use strings as a serialization mechanism.

Appealing because human readable.

Introduce bugs so that you can have a debugging tool?

Good for being read by humans. But not for machines. Don't turn your value
into a string until it's about to be turned into photons headed at eyeballs.

</div>

--------

## Garlic

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

## Holy Water

Hide your newtype constructor behind validation.

~~~{.scala}

case class IPAddr private (addr: String) extends AnyVal

object IPAddr {
  val pattern = """(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})""".r

  def parse(s: String): Option[IPAddr] = for {
    xs <- pattern.unapplySeq(s) if xs.forall(_.toInt <= 255)
  } yield IPAddr(s)
}
  
~~~

We've shrunk down an infinite number of states to (256^4^ + 1).
Given the inputs, that's the best we can do.

<div class="notes">

This approach applies to virtually every primitive type. 

</div>

--------

# Stake

## import scalaz._

Three very useful types:

* `A \/ B`

* `Validation[A, B]`

* `EitherT[M[_], A, B]`

<div class="notes">

Sum types where the error type conventionally comes on the left.

* Going to quickly look at each of them.

</div>

--------

**`MyError \/ B`**

* `\/` (Disjunction) has a Monad biased to the right

* We can *sequentially* compose operations that might fail 

* `for` comprehension syntax is useful for this

~~~{.scala}

import scalaz.std.syntax.option._

def parseJson(s: String): ParseError \/ JValue = ???
def ipField(jv: JValue): ParseError \/ String = ???

def parseIP(addrStr: String): ParseError \/ IPAddr = 
  IPAddr.parse(addrStr) toRightDisjunction {
    ParseError(s"$addrStr is not a valid IP address")
  }

val ipV: ParseError \/ IPAddr = for {
  jv <- parseJson("""{"hostname": "nuttyland", "ipAddr": "127.0.0.1"}""")
  addrStr <- ipField(jv)
  ipAddr <- parseIP(addrStr)
} yield ipAddr
  
~~~

--------

**`Validation[NonEmptyList[MyError], B]`**

* Validation **does not** have a Monad instance.

* Composition uses Applicative: conceptually parallel!

* if you need sequencing, `.disjunction`

~~~{.scala}

type VPE[B] = Validation[NonEmptyList[ParseError], B]

def hostname(jv: JValue): VPE[String] = ???

def ipField(jv: JValue): ParseError \/ String = ???
def parseIP(addrStr: String): ParseError \/ IPAddr = ???

def host(jv: JValue) = ^[VPE, String, IPAddr, Host](
  hostname(jv), 
  (ipField(jv) >>= parseIP).validation.leftMap(nels(_)) 
) { Host.apply _ }
  
~~~

<div class="notes">

There is a functor isomorphism between `\/` and validation.

Types have the same information content, but compose differently.

</div>

--------

**`EitherT[M[_], MyErrorType, B]`**

* EitherT layers together two effects:

    - The "outer" monadic effect of the type constructor M[_]

    - The disjunctive effect of `\/`

~~~{.scala}

// EitherT[M, A, _] <~> M[A \/ _]

def findDocument(key: DocKey): EitherT[IO, DBErr, Document] = ???
def storeWordCount(key: DocKey, wordCount: Long): EitherT[IO, DBErr, Unit] = ???

val program: EitherT[IO, DBErr, Unit] = for {
  doc <- findDocument(myDocKey)
  words = wordCount(doc)
  _ <- storeWordCount(myDocKey, words)
} yield ()
  
~~~

<div class="notes">

"Real world" interaction.

</div>

--------

# Side Effects

![](./img/trapped.png)

> in other words, what was that IO thingy?

<div class="notes">

What is IO?

A value of type IO[String] represents some action that can be performed to return a String.

IO is kind of like if you had only a single sensory organ.

It tells you that there's something going on, but it's not very specific about what.

You couldn't, for example, determine whether it was your hair or your hand that 
was on fire. That sort of thing.

We want to be more specific. Sum types give us a way to do that.

</div>

--------

## Managing Effects

<div class="notes">

Minimizing the number of inhabitants of our program includes minimizing side effects.

We want to be specific.

Sounds like a job for a sum type!

</div>

~~~{.scala}

// def findDocument(key: DocKey): EitherT[IO, DBErr, Document] = ???
// def storeWordCount(key: DocKey, wordCount: Long): EitherT[IO, DBErr, Unit] = ???

sealed trait DocAction
case class FindDocument(key: DocKey) extends DocAction
case class StoreWordCount(key: DocKey, wordCount: Long) extends DocAction
  
~~~

How can we build a program out of these actions?

--------

## This?

~~~{.scala}
object DocAction {
  type Program = List[DocAction]

  def runProgram(actions: Program): IO[Unit] = ???
}
~~~

* Obviously, this doesn't work.
    - can't interleave pure computations
    - no way to represent return values 
    - can't produce a new DocAction from a Program 
    - can't make later action a function of an earlier one
<br/><br/>
* But, it *is* conceptually related to what we want.
    - a data structure an ordered sequence of actions
    - an interpreter that evaluates this data structure 

-------

## Sequencing

Let's, see, what is good for sequencing effects?

~~~{.scala}

trait Monad[M[_]] {
  def pure[A](a: => A): M[A]
  def bind[A, B](ma: M[A])(f: A => M[B]): M[B]
}
  
~~~

* State threads state through a computation...
* Reader gives the same inputs to all...
* Writer keeps a log...
* Not going to be Option, List, ...

--------

## Requirements

* **restrict** the client to only permit actions in our sum

* produce later actions as a function of earlier ones

* interleave pure computations with effectful 

<div class="notes">

Restate relationship of minimization of states to correctness.

</div>

--------

# Free

~~~{.scala}

sealed trait Free[F[_], A]
case class Pure[F[_], A](a: A) extends Free[F, A]
case class Bind[F[_], A, B](s: Free[F, A], f: A => Free[F, B]) extends Free[F, A]
case class Suspend[F[_], A](s: F[Free[F, A]]) extends Free[F, A]
  
~~~

> for a complete derivation of this type, see [Functional Programming In Scala](http://manning.com/bjarnason)

**Exercise 1: Write the Monad instance for Free.**

<div class="notes">

This data structure will take the place of List in our broken Program type.

Instead of walking through the derivation, I'm going to show you how to
use it, and I think that through the process of using it it'll become obvious
why it is useful for our purpose.

</div>

--------

## Solution

~~~{.scala}

object Free {
  def monad[F[_]] = new Monad[({ type λ[α] = Free[F, α] })#λ] {
    def pure[A](a: => A): Free[F, A] = Pure(a)

    // def bind[A, B](ma: Free[F, A])(f: A => Free[F, B]): Free[F, B] = Bind(ma, f)

    def bind[A, B](ma: Free[F, A])(f: A => Free[F, B]): Free[F, B] = ma match {
      case Bind(ma0, f0) => bind(ma0) { a => Bind(f0(a), f) }
      case other => Bind(other, f)
    }
  }
}

~~~

<div class="notes">

Back to our requirements.

Need to chain together 

</div>

--------

## Capture The Rest of the Program

Here's a little trick: store the rest of the program in each action.

~~~{.scala}

// sealed trait DocAction
// case class FindDocument(key: DocKey) extends DocAction
// case class StoreWordCount(key: DocKey, wordCount: Long) extends DocAction
  
sealed trait DocAction[A]
case class FindDocument(key: DocKey, cont: Option[Document] => A) extends DocAction[A]
case class StoreWordCount(key: DocKey, wordCount: Long, cont: () => A) extends DocAction[A]
  
~~~

<div class="notes">

This is an implementation detail, really, but it's a critical one. 

Remember, a program is a *value* representing a sequence of operations for an
interpreter to follow. So, capturing the rest of the program is just capturing
that description - there's not a lot of overhead to it.

Earlier, we used a list to represent this sequence, and a couple of slides back
I introduced the Free data structure (which is itself a sum type) as the one
we'd use to capture the sequence of operations. Let's do that now.

</div>

--------

## Write a Little Language

~~~{.scala}

sealed trait DocAction[A]

object DocAction {
  case class FindDocument[A] private[DocAction] (
    key: DocKey, 
    cont: Option[Document] => A
    ) extends DocAction[A]

  case class StoreWordCount[A] private[DocAction] (
    key: DocKey, wordCount: Long, 
    cont: () => A
    ) extends DocAction[A]

  type Program[A] = Free[DocAction, A]

  def findDocument(key: DocKey): Program[Option[Document]] = 
    Suspend(FindDocument(key, Pure(_)))

  def storeWordCount(key: DocKey, wordCount: Long): Program[Unit] = 
    Suspend(StoreWordCount(key, wordCount, () => Pure(())))
}
  
~~~

<div class="notes">

Private constructors, no need to expose them.

We now have a way to create programs, and we know in the abstract that 
because Free has a Monad, that we can chain those programs together.

So let's see what that looks like.

</div>

--------

# Write a Perfect Program!

~~~{.scala}

import DocAction._

val program: Program[Unit] = for {
  document <- findDocument(docKey)
  _ <- document match {
    case Some(d) => storeWordCount(docKey, countWords(d)) 
    case None => Free.monad[DocAction].pure(())
  }
} yield ()
  
~~~~

<div class="notes">

While this looks like an effectful program, it's just chaining together calls to
the Program constructors that we defined previously. So, all that we've done is built
up a data structure, like our original List, but much more powerful!

The next thing to do is interpret this data structure.

</div>

--------

## A Pure Interpreter

~~~{.scala}

type Memory = Map[DocKey, (Document, Option[Long])]

@tailrec def run[A](program: Program[A], memory: Memory): A = {
  program match {
    case Pure(a) => a

    case Suspend(FindDocument(key, cont)) => ???

    case Suspend(StoreWordCount(key, n, cont)) => ???

    case Bind(s, f) => ???
  }
}

~~~

--------

## A Pure Interpreter #2

~~~{.scala}

type Memory = Map[DocKey, (Document, Option[Long])]

@tailrec def run[A](program: Program[A], memory: Memory): A = {
  program match {
    case Pure(a) => a

    case Suspend(FindDocument(key, cont)) =>
      run(cont(memory.get(key).map(_._1)), memory)

    case Suspend(StoreWordCount(key, n, cont)) => ???

    case Bind(s, f) => ???
  }
}

~~~

--------

## A Pure Interpreter #3

~~~{.scala}

type Memory = Map[DocKey, (Document, Option[Long])]

@tailrec def run[A](program: Program[A], memory: Memory): A = {
  program match {
    case Pure(a) => a

    case Suspend(FindDocument(key, cont)) =>
      run(cont(memory.get(key).map(_._1)), memory)

    case Suspend(StoreWordCount(key, n, cont)) => ???
      val newValue = memory.get(key).map(_.copy(_2 = Some(n)))
      val newMemory = memory ++ newValue.map(key -> _)
      run(cont(), newMemory)

    case Bind(s, f) => ???
  }
}

~~~

--------

## A Pure Interpreter #4

~~~{.scala}

type Memory = Map[DocKey, (Document, Option[Long])]

@tailrec def run[A](program: Program[A], memory: Memory): A = {
  program match {
    case Pure(a) => a
    case Suspend(FindDocument(key, cont)) => // implemented 
    case Suspend(StoreWordCount(key, n, cont)) => //implemented

    case Bind(s, f) => 
      s match {
        case Pure(a) => run(f(a), memory)

        case Suspend(FindDocument(key, cont)) =>
          run(Bind(cont(memory.get(key).map(_._1)), f), memory)

        case Suspend(StoreWordCount(key, n, cont)) =>
          val newValue = memory.get(key).map(_.copy(_2 = Some(n)))
          val newMemory = memory ++ newValue.map(key -> _)
          run(Bind(cont(), f), newMemory)

        case Bind(s0, f0) =>
          run(Bind(s0, (a: Any) => Bind(f0(a), f)), memory)
      }
  }
}

~~~

**Exercise 2: Implement a pure interpreter for ASDF**

<div class="notes">

After this, we'll work on side-effecting interpreter.

</div>

--------

## Stretch Goals

**Exercise 3: Refactor the tests to allow reuse against other interpreters.**

**Exercise 4: Implement an effectful interpreter for ASDF**

--------

# Thank You
