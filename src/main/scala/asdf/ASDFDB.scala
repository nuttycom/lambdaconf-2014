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
  case class Create[A]   private[ASDFDB] (path: Path, value: ASDF, cont: () => A) extends Action[A]
  case class FindOne[A]  private[ASDFDB] (path: Path, cont: Option[ASDF] => A) extends Action[A]
  //case class Delete[A]   private[ASDFDB] (path: Path, cont: () => A) extends Action[A]
  //case class FoldL[A, B] private[ASDFDB] (path: Path, init: B, combine: (B, ASDF) => B, cont: B => A) extends Action[A]

  type ActionIO[A] = Free[Action, A]

  private val done: () => ActionIO[Unit] = () => Return(())
  def create(path: Path, value: ASDF): ActionIO[Unit] = Suspend(Create(path, value, done))
  def findOne(path: Path): ActionIO[Option[ASDF]] = Suspend(FindOne(path, Return(_)))
  //def delete(path: Path): ActionIO[Unit] = Suspend(Delete(path, done))

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
   * Semantic newtype wrapper around a list of path elements.
   */
  case class Path(elements: List[Path.Element]) extends AnyVal
  object Path {
    sealed abstract class Element(val atype: AType)
    case class Field(name: ADict.Key) extends Element(AType.Dict)
    case class Index(i: Int) extends Element(AType.Seq)
  }

  sealed trait Err
  object Err {
    case class PathMismatch private[ASDFDB] (at: Path, expected: AType, found: AType) extends Err
    case class Exists private[ASDFDB] (path: Path) extends Err
    def pathMismatch(at: Path, expected: AType, found: AType): Err = PathMismatch(at, expected, found)
    def exists(path: Path): Err = Exists(path)
  }
}

object PureASDFDB {
  import ASDFDB._
  import Path._
  import Err._

  @tailrec 
  def runAction[A](db: ASDF)(action: ActionIO[A]): Err \/ A = {
    action match {
      case Return(a) => \/.right(a)

      case Suspend(FindOne(path, cont)) =>
        runAction(db)(cont(findOne0(path, db)))

      case Suspend(Create(path, value, cont)) => 
        create0(Nil, path, db, value) match {
          case \/-(updated) => runAction(updated)(cont())
          case -\/(error) => \/.left(error) 
        }
        
      case Gosub(fa, cont) => 
        // we have to fully evaluate fa before continuing
        fa() match {
          case Return(a) => runAction(db)(cont(a))
          case Suspend(Create(path, value, cont0)) => 
            create0(Nil, path, db, value) match {
              case \/-(updated) => runAction(updated)(Gosub(cont0, cont))
              case -\/(error) => \/.left(error) 
            }

          case Suspend(FindOne(path, cont0)) =>
            runAction(db)(Gosub(() => cont0(findOne0(path, db)), cont))

          case Gosub(fa0, cont0) => 
            runAction(db)(fa0() flatMap { a => cont0(a) flatMap cont })
        }
    }
  }

  private def create0(trace: List[Path.Element], path: Path, db: ASDF, value: ASDF): Err \/ ASDF = {
    (path, db) match {
      case (Path(Nil), v) => \/.right(v)

      case (Path((fld @ Field(n)) :: xs), ADict(d, meta)) => 
        d.get(n) match {
          case Some(child) =>
            create0(fld :: trace, Path(xs), child, value) map { v =>
              aDict(d + (n -> v), meta)
            }

          case None => 
            for {
              v <- xs.headOption match {
                case Some(Field(_)) => 
                  create0(fld :: trace, Path(xs), ADict.empty(), value)

                case Some(Index(_)) =>
                  create0(fld :: trace, Path(xs), ASeq.empty(), value)

                case None =>
                  \/.right(value)
              }
            } yield {
              aDict(d + (n -> v), meta)
            }
        }

      case (Path((idx @ Index(i)) :: xs), ASeq(vx, meta)) => 
        val (prefix, suffix) = vx.splitAt(i)
        if (prefix.size < i) {
          \/.left(pathMismatch(Path((idx :: trace).reverse), AType.Value, AType.Missing))
        } else {
          suffix.headOption match {
            case Some(child) =>
              create0(idx :: trace, Path(xs), child, value) map { v =>
                aSeq(prefix ++: v +: suffix.drop(1), meta)
              }

            case None => 
              for {
                v <- xs.headOption match {
                  case Some(Index(_)) =>
                    create0(idx :: trace, Path(xs), ASeq.empty(), value)

                  case Some(Field(_)) =>
                    create0(idx :: trace, Path(xs), ADict.empty(), value)

                  case None => 
                    \/.right(value)
                }
              } yield {
                aSeq(prefix ++: v +: suffix.drop(1), meta)
              }
          }
        }

      case (Path(x :: xs), v) => 
        \/.left(pathMismatch(Path((x :: trace).reverse), x.atype, AType.forValue(v)))
    }
  }

  private def findOne0(path: Path, db: ASDF): Option[ASDF] = {
    (path, db) match {
      case (Path(Nil), v) => Some(v)
      case (Path(Index(i) :: xs), ASeq(v, _)) => v.lift(i) flatMap { findOne0(Path(xs), _) }
      case (Path(Field(n) :: xs), ADict(m, _)) => m.get(n) flatMap { v => findOne0(Path(xs), v) }
      case _ => None
    }
  }
}

class IOASDFDB {
  import ASDFDB._

  // Use a mutable variable to simulate the outside world.
  private var state: Option[ASDF] = None

  type EitherM[M[_]] = {
    type ErrOr[A] = EitherT[M, Err, A]
  }

  def runAction[M[_]: Monad]: ActionIO ~> EitherM[M]#ErrOr = new (ActionIO ~> EitherM[M]#ErrOr) {
    def apply[A](actionIO: ActionIO[A]): EitherM[M]#ErrOr[A] = {
      actionIO match {
        case Return(a) => a.point[EitherM[M]#ErrOr]
        case Suspend(FindOne(path, cont)) => ???
        case Suspend(Create(path, value, cont)) => ???
        //case Suspend(Delete(Path(elems), cont)) => ???
        case Gosub(fa, cont0) => ???
      }
    }
  }
}
