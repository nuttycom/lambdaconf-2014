package xample

import free._

trait DocKey
trait Document

object docProgram1 {
  import scalaz.effect._

  sealed trait DocAction
  case class FindDocument(key: DocKey) extends DocAction
  case class StoreWordCount(key: DocKey, wordCount: Long) extends DocAction

  object DocAction {
    type Program = List[DocAction]

    def runProgram(actions: Program): IO[Unit] = ???
  }
}

object docProgram2 {
  sealed trait DocAction[A]
  case class FindDocument[A](key: DocKey, cont: Option[Document] => A) extends DocAction[A]
  case class StoreWordCount[A](key: DocKey, wordCount: Long, cont: () => A) extends DocAction[A]
}

object docProgram3 {
  sealed trait DocAction[A]
  object DocAction {
    case class FindDocument[A] private[DocAction] (key: DocKey, cont: Option[Document] => A) extends DocAction[A]
    case class StoreWordCount[A] private[DocAction] (key: DocKey, wordCount: Long, cont: () => A) extends DocAction[A]

    type Program[A] = Free[DocAction, A]

    def findDocument(key: DocKey): Program[Option[Document]] = Suspend(FindDocument(key, Pure(_)))

    def storeWordCount(key: DocKey, wordCount: Long): Program[Unit] = Suspend(StoreWordCount(key, wordCount, () => Pure(())))
  }

  val docKey: DocKey = ???
  def countWords(document: Document): Long = ???

  import DocAction._
  for {
    document <- findDocument(docKey)
    _ <- document match {
      case Some(d) => storeWordCount(docKey, countWords(d)) 
      case None => Free.monad[DocAction].pure(())
    }
  } yield ()
}
