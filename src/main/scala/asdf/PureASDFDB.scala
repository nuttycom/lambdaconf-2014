package asdf

import scalaz.{Free, \/, \/-, -\/}
import scalaz.Free._
import scalaz.syntax.monad._
import scalaz.syntax.std.boolean._
import scalaz.syntax.std.option._
import scala.annotation.tailrec

import ASDF._
import ASDFDB._
import Path._
import Err._

object PureASDFDB {
  @tailrec 
  def runAction[A](db: ASDF)(action: Program[A]): Err \/ A = {
    action match {
      case Return(a) => \/.right(a)

      case Suspend(FindOne(path, cont)) =>
        runAction(db)(cont(findOne0(path, db)))

      case Suspend(Create(path, value, cont)) => 
        // this ugly pattern match is just because Scala can't see through \/.fold
        create0(Nil, path, db, value) match {
          case \/-(updated) => runAction(updated)(cont())
          case -\/(error) => \/.left(error) 
        }
        
      //case Suspend(Delete(Path(elems), cont)) => ???

      case Gosub(fa, cont) => 
        // we have to fully evaluate fa before continuing
        fa() match {
          case Return(a) => runAction(db)(cont(a))
          case Suspend(Create(path, value, cont0)) => 
            // this ugly pattern match is just because Scala can't see through \/.fold
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
                case Some(Field(_)) => create0(fld :: trace, Path(xs), ADict.empty(), value)
                case Some(Index(_)) => create0(fld :: trace, Path(xs), ASeq.empty(), value)
                case None => \/.right(value)
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
                  case Some(Field(_)) => create0(idx :: trace, Path(xs), ADict.empty(), value)
                  case Some(Index(_)) => create0(idx :: trace, Path(xs), ASeq.empty(), value)
                  case None => \/.right(value)
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


