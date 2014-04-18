package asdf

import scalaz._
import scalaz.std.option._
import scalaz.syntax.apply._
import org.specs2.mutable.Specification

class PureASDFDBSpec extends Specification {
  import ASDF._
  import ASDFDB._
  import Path._
  import PureASDFDB.runPure

  val a = ADict.key("a").get
  val b = ADict.key("b").get
  val c = ADict.key("c").get

  val simpleObjectPath = Path(Field(a) :: Field(b) :: Nil)
  val objectCompositePath = Path(Field(a) :: Field(b) :: Index(0) :: Nil)
  val caPath = Path(Field(c) :: Field(a) :: Nil)
  val cbPath = Path(Field(c) :: Field(b) :: Nil)

  val simpleSeqPath0 = Path(Index(0) :: Field(a) :: Nil)
  val simpleSeqPath1 = Path(Index(1) :: Field(a) :: Nil)

  val foundIt = aStr("found it!")

  "pure transformations on a value" should {
    "return a pure value" in {
      val program = for {
        _ <- insert(simpleObjectPath, ASeq.empty())
      } yield "hello"

      runPure(ADict.empty())(program) must_== \/.right("hello")
    }

    "return a value unmodified" in {
      val program = for {
        v <- findOne(simpleObjectPath)
      } yield v

      val dict = aDict(Map(a -> aDict(Map(b -> foundIt))))

      runPure(dict)(program) must_== \/.right(Some(foundIt))
    }

    "return a value and its container" in {
      val program = for {
        container <- findOne(simpleObjectPath)
        value <- findOne(objectCompositePath)
      } yield container tuple value

      val dict = aDict(Map(a -> aDict(Map(b -> aSeq(Vector(foundIt))))))

      runPure(dict)(program) must beLike {
        case \/-(Some((container, value))) =>
          value must_== foundIt
          container must_== aSeq(Vector(foundIt))
      }
    }

    "return a value conditionally depending upon the value of another value" in {
      val program = for {
        value <- findOne(simpleObjectPath)
        result <- value match {
          case Some(ABool(true, _)) => findOne(caPath)
          case _ => findOne(cbPath)
        }
      } yield result

      val cDict = aDict(Map(a -> aStr("found c-a!"), b -> aStr("found c-b")))
      val trueCase  = aDict(Map(a -> aDict(Map(b -> aBool(true))), c -> cDict))
      val falseCase = aDict(Map(a -> aStr("nada"), c -> cDict))

      runPure(trueCase)(program) must beLike {
        case \/-(Some(v)) => v must_== aStr("found c-a!")
      }

      runPure(falseCase)(program) must beLike {
        case \/-(Some(v)) => v must_== aStr("found c-b")
      }
    }

    "insert values at nested paths" in {
      val program = for {
        _ <- insert(simpleObjectPath, ASeq.empty())
        _ <- insert(objectCompositePath, aBool(true))
        v <- findOne(objectCompositePath) 
        root <- findOne(Path(Nil))
      } yield (root, v)

      runPure(ADict.empty())(program) must beLike {
        case \/-((Some(root), Some(v))) => 
          v must_== aBool(true)
          root must_== aDict(Map(a -> aDict(Map(b -> aSeq(Vector(v))))))
      }
    }
  }
}

