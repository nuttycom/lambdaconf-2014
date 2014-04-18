package asdf

import scalaz._
import org.specs2.mutable.Specification

class PureASDFDBSpec extends Specification {
  import ASDF._
  import ASDFDB._
  import Path._
  import PureASDFDB.runPure

  val a = ADict.key("a").get
  val b = ADict.key("b").get

  val simpleObjectPath = Path(Field(a) :: Field(b) :: Nil)
  val objectCompositePath = Path(Field(a) :: Field(b) :: Index(0) :: Nil)

  val simpleSeqPath0 = Path(Index(0) :: Field(a) :: Nil)
  val simpleSeqPath1 = Path(Index(1) :: Field(a) :: Nil)

  "pure transformations on a value" should {
    "insert values at nested paths" in {
      val action = for {
        _ <- insert(simpleObjectPath, ASeq.empty())
        _ <- insert(objectCompositePath, aBool(true))
        v <- findOne(objectCompositePath) 
        root <- findOne(Path(Nil))
      } yield (root, v)

      runPure(ADict.empty())(action) must beLike {
        case \/-((Some(root), Some(v))) => 
          v must_== aBool(true)
          root must_== aDict(Map(a -> aDict(Map(b -> aSeq(Vector(v))))))
      }
    }
  }
}

