package xample

import scalaz.effect._

trait DocKey

object docProgram1 {
  sealed trait DocAction
  case class FindDocument(key: DocKey) extends DocAction
  case class StoreWordCount(key: DocKey, wordCount: Long) extends DocAction

  object DocAction {
    type Program = List[DocAction]

    def runProgram(actions: Program): IO[Unit] = ???
  }
}

object docProgram2 {
  sealed trait DocAction
  case class FindDocument(key: DocKey) extends DocAction
  case class StoreWordCount(key: DocKey, wordCount: Long) extends DocAction
}
