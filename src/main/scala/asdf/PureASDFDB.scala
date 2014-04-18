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
  @tailrec def runPure[A](db: ASDF)(program: Program[A]): Err \/ A = {
    program match {
      case Return(a) => \/.right(a)

      case Suspend(FindOne(path, cont)) =>
        runPure(db)(cont(findOne(path, db)))

      case Suspend(Insert(path, value, cont)) => 
        // this ugly pattern match is just because Scala can't see through \/.fold
        insert(db, path, value) match {
          case \/-(updated) => runPure(updated)(cont())
          case -\/(error) => \/.left(error) 
        }
        
      //case Suspend(Delete(Path(elems), cont)) => ???

      case Gosub(fa, cont) => 
        // we have to fully evaluate fa before continuing
        fa() match {
          case Return(a) => runPure(db)(cont(a))
          case Suspend(Insert(path, value, cont0)) => 
            // this ugly pattern match is just because Scala can't see through \/.fold
            insert(db, path, value) match {
              case \/-(updated) => runPure(updated)(Gosub(cont0, cont))
              case -\/(error) => \/.left(error) 
            }

          case Suspend(FindOne(path, cont0)) =>
            runPure(db)(Gosub(() => cont0(findOne(path, db)), cont))

          case Gosub(fa0, cont0) => 
            runPure(db)(fa0() flatMap { a => cont0(a) flatMap cont })
        }
    }
  }

  //
  // The following methods are provided for your convenience in the implementation
  // of the ??? portions above. 
  //

  /**
   * T
   */
  private def insert(into: ASDF, at: Path, value: ASDF): Err \/ ASDF = {
    def rec(trace: List[Path.Element], path: Path, db: ASDF): Err \/ ASDF = {
      (path, db) match {
        case (Path(Nil), v) => \/.right(v)

        case (Path((fld @ Field(n)) :: xs), ADict(d, meta)) => 
          d.get(n) match {
            case Some(child) =>
              rec(fld :: trace, Path(xs), child) map { v =>
                aDict(d + (n -> v), meta)
              }

            case None => 
              for {
                v <- xs.headOption match {
                  case Some(Field(_)) => rec(fld :: trace, Path(xs), ADict.empty())
                  case Some(Index(_)) => rec(fld :: trace, Path(xs), ASeq.empty())
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
                rec(idx :: trace, Path(xs), child) map { v =>
                  aSeq(prefix ++: v +: suffix.drop(1), meta)
                }

              case None => 
                for {
                  v <- xs.headOption match {
                    case Some(Field(_)) => rec(idx :: trace, Path(xs), ADict.empty())
                    case Some(Index(_)) => rec(idx :: trace, Path(xs), ASeq.empty())
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

    rec(Nil, at, into)
  }

  private def findOne(at: Path, in: ASDF): Option[ASDF] = (at, in) match {
    case (Path(Nil), v) => Some(v)
    case (Path(Index(i) :: xs), ASeq(v, _)) => v.lift(i) flatMap { findOne(Path(xs), _) }
    case (Path(Field(n) :: xs), ADict(m, _)) => m.get(n) flatMap { v => findOne(Path(xs), v) }
    case _ => None
  }
}


