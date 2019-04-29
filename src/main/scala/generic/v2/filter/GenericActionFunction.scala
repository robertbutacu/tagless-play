package generic.v2.filter

import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait GenericActionFunction[F[_], -R[_], +P[_]] extends ActionFunction[R, P]{
  self =>

  def invokeBlockG[A](request: R[A], block: P[A] => F[Result]): F[Result] = ???

  def andThenG[Q[_]](other: GenericActionFunction[F, P, Q]): GenericActionFunction[F, R, Q] = new GenericActionFunction[F, R, Q] {
    override def invokeBlockG[A](request: R[A], block: Q[A] => F[Result]): F[Result] = {
      self.invokeBlockG(request, other.invokeBlockG(_, block))
    }

    override def invokeBlock[A](request: R[A], block: Q[A] => Future[Result]): Future[Result] =
      self.invokeBlock[A](request, other.invokeBlock[A](_, block))

    override protected def executionContext: ExecutionContext = self.executionContext
  }
}
