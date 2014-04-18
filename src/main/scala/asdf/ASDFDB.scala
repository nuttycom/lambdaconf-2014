package asdf

import ASDF._
import scalaz._
import scalaz.Free._
import scalaz.effect._
import scalaz.syntax.monad._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scala.annotation.tailrec

object ASDFDB {
  sealed trait Action[A]
  case class Insert[A]   private[ASDFDB] (path: Path, value: ASDF, cont: () => A) extends Action[A]
  case class FindOne[A]  private[ASDFDB] (path: Path, cont: Option[ASDF] => A) extends Action[A]
  //case class Delete[A]   private[ASDFDB] (path: Path, cont: () => A) extends Action[A]

  type Program[A] = Free[Action, A]

  def insert(path: Path, value: ASDF): Program[Unit] = Suspend(Insert(path, value, () => Return(())))
  def findOne(path: Path): Program[Option[ASDF]] = Suspend(FindOne(path, Return(_)))
  //def delete(path: Path): Program[Unit] = Suspend(Delete(path, () => Return(())))
}

/** 
 * Newtype wrapper around a list of path elements.
 */
case class Path(elements: List[Path.Element]) extends AnyVal
object Path {
  sealed abstract class Element(val atype: AType)
  case class Field(name: ADict.Key) extends Element(AType.Dict)
  case class Index(i: Int) extends Element(AType.Seq)
}

/**
 * This type exists just to help provide meaningful error messages.
 */
sealed trait AType
object AType {
  case object Dict extends AType
  case object Seq extends AType
  case object Value extends AType
  case object Missing extends AType 

  def forValue(v: ASDF) = v match {
    case ABool(_, _) | ANum(_, _) | AStr(_, _)  => Value
    case ASeq(_, _) => Seq
    case ADict(_, _) => Dict
  }
}

/**
 * Sum type for error reporting.
 */
sealed trait Err
object Err {
  case class PathMismatch private[Err] (at: Path, expected: AType, found: AType) extends Err
  case class Exists private[Err] (path: Path) extends Err

  def pathMismatch(at: Path, expected: AType, found: AType): Err = PathMismatch(at, expected, found)
  def exists(path: Path): Err = Exists(path)
}
