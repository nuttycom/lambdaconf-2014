package fperfect

import ASDF._
import scalaz._
import scalaz.Free._

object ASDFDB {
  sealed trait Action[A]
  case class Create[A] private[ASDFDB] (path: Path, value: ASDF, cont: () => A) extends Action[A]
  case class Read[A] private[ASDFDB] (path: Path, value: ASDF, cont: Option[ASDF] => A) extends Action[A]
  case class Update[A] private[ASDFDB] (path: Path, value: ASDF, cont: () => A) extends Action[A]
  case class Delete[A] private[ASDFDB] (path: Path, cont: () => A) extends Action[A]

  type ActionIO[A] = Free[Action, A]

  private val done: () => ActionIO[Unit] = () => Return(())
  def create(path: Path, value: ASDF): ActionIO[Unit] = Suspend(Create(path, value, done))
  def read(path: Path, value: ASDF): ActionIO[Option[ASDF]] = Suspend(Read(path, value, Return(_)))
  def update(path: Path, value: ASDF): ActionIO[Unit] = Suspend(Update(path, value, done))
  def delete(path: Path): ActionIO[Unit] = Suspend(Delete(path, done))
}
