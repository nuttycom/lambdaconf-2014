package fperfect

import scalaz._
import org.specs2.mutable.Specification

class ASDFDBSpec extends Specification {
  import ASDF._
  import ASDFDB._
  import Path._

  val a = ADict.key("a").get
  val b = ADict.key("b").get

  val simpleObjectPath = Path(Field(a) :: Field(b) :: Nil)
  val objectCompositePath = Path(Field(a) :: Field(b) :: Index(0) :: Nil)

  val simpleSeqPath0 = Path(Index(0) :: Field(a) :: Nil)
  val simpleSeqPath1 = Path(Index(1) :: Field(a) :: Nil)

  "pure transformations on a value" should {
    import PureASDFDB._

    "create values at nested paths" in {
      val action = for {
        _ <- create(simpleObjectPath, ASeq.empty())
        _ <- create(objectCompositePath, aBool(true))
        v <- findOne(objectCompositePath) 
      } yield v

      runAction(ADict.empty())(action) must beLike {
        case \/-(Some(ABool(v, _))) => v must beTrue
      }
    }
  }
}

