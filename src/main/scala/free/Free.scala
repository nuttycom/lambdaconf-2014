package free 

trait Monad[M[_]] {
  def pure[A](a: => A): M[A]
  def bind[A, B](ma: M[A])(f: A => M[B]): M[B]
}

sealed trait Free[F[_], A] {
  private val M = Free.monad[F]
  def map[B](f: A => B): Free[F, B] = M.bind(this)(a => M.pure(f(a)))
  def flatMap[B](f: A => Free[F, B]): Free[F, B] = M.bind(this)(f)
}

case class Pure[F[_], A](a: A) extends Free[F, A]
case class Bind[F[_], A, B](s: Free[F, A], f: A => Free[F, B]) extends Free[F, B]
case class Suspend[F[_], A](s: F[Free[F, A]]) extends Free[F, A]

object Free {
  def monad[F[_]] = new Monad[({ type λ[α] = Free[F, α] })#λ] {
    def pure[A](a: => A): Free[F, A] = Pure(a)

    // def bind[A, B](ma: Free[F, A])(f: A => Free[F, B]): Free[F, B] = Bind(ma, f)

    def bind[A, B](ma: Free[F, A])(f: A => Free[F, B]): Free[F, B] = ma match {
      case Bind(ma0, f0) => bind(ma0) { a => Bind(f0(a), f) }
      case other => Bind(other, f)
    }
  }
}

