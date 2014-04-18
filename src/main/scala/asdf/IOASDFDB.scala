package asdf

import scalaz.{Monad, Free, EitherT, ~>}
import scalaz.Free._
import scalaz.effect._
import scalaz.syntax.monad._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scala.annotation.tailrec

import ASDF._
import ASDFDB._
import Path._
import Err._

class IOASDFDB {
  import ASDFDB._

  // Use a mutable variable to simulate the outside world.
  private var state: Option[ASDF] = None

  // this type alias is just to allow partial application of the EitherT type to
  // the type constructor M
  type FA[M[_]] = {
    type ErrOr[A] = EitherT[M, Err, A]
  }

  def runAction[M[_]: Monad]: Program ~> FA[M]#ErrOr = new (Program ~> FA[M]#ErrOr) {
    def apply[A](actionIO: Program[A]): FA[M]#ErrOr[A] = {
      actionIO match {
        case Return(a) => a.point[FA[M]#ErrOr]
        case Suspend(FindOne(path, cont)) => ???
        case Suspend(Insert(path, value, cont)) => ???
        //case Suspend(Delete(Path(elems), cont)) => ???
        case Gosub(fa, cont0) => ???
      }
    }
  }
}

