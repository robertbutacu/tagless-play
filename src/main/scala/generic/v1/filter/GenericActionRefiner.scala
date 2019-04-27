package generic.v1.filter

import cats.~>
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericActionRefiner[F[+_], -R[_], +P[_]] extends ActionRefiner[R, P] {
  implicit def transformer: F ~> Future

  def genericRefine[A](request: R[A]): F[Either[Result, P[A]]]

  override protected def refine[A](request: R[A]): Future[Either[Result, P[A]]] = transformer(genericRefine(request))
}