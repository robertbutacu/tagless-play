package generic.independent.filter

import cats.~>
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait GenericAction[F[_], -R[_], +P[_]] {
  self =>

  def toFuture: F ~> Future

  def fromFuture: Future ~> F

  implicit def ec: ExecutionContext

  def invokeBlock[A](request: R[A], block: P[A] => F[Result]): F[Result]

  def andThen[Q[_]](other: GenericAction[F, P, Q]): GenericAction[F, R, Q] = new GenericAction[F, R, Q] {
    def toFuture:   F      ~> Future = toFuture
    def fromFuture: Future ~> F      = fromFuture

    override implicit def ec: ExecutionContext = ec

    override def invokeBlock[A](request: R[A], block: Q[A] => F[Result]): F[Result] = {
      self.invokeBlock(request, other.invokeBlock(_, block))
    }
  }
}
