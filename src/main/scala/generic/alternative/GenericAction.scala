package generic.alternative

import cats.{InjectK, ~>}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait GenericAction[F[_], -R[_], +P[_]] {
  self =>
  implicit def injector: InjectK[F, Future]
  implicit def ec:       ExecutionContext

  def invokeBlock[A](request: R[A], block: P[A] => F[Result]): F[Result]

  def andThen[Q[_]](other: GenericAction[F, P, Q]): GenericAction[F, R, Q] = new GenericAction[F, R, Q] {
    implicit def injector:   InjectK[F, Future] = self.injector
    implicit def ec:         ExecutionContext   = self.ec

    override def invokeBlock[A](request: R[A], block: Q[A] => F[Result]): F[Result] = {
      self.invokeBlock(request, {p: P[A] => other.invokeBlock(p, block)})
    }
  }
}
