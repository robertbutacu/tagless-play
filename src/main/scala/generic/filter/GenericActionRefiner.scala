package generic.filter

import cats.~>
import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericActionRefiner[F[+_], -R[_], +P[_]] extends ActionFunction[R, P] {
  implicit def transformer: F ~> Future

  def genericRefine[A](request: R[A]): F[Either[Result, P[A]]]

  override def invokeBlock[A](request: R[A], block: P[A] => Future[Result]): Future[Result] =
    refine(request).flatMap(_.fold(Future.successful, block))(executionContext)

  def refine[A](request: R[A]): Future[Either[Result, P[A]]] = transformer(genericRefine(request))
}
