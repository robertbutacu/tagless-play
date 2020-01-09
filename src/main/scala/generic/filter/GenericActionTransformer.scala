package generic.filter

import cats.~>
import play.api.mvc.Result

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericActionTransformer[F[+_], -R[_], +P[_]] extends GenericActionRefiner[F, R, P] {
  def transformer: F ~> Future

  def genericTransform[A](request: R[A]): F[P[A]]

  def transform[A](request: R[A]): Future[P[A]] = transformer(genericTransform(request))

  override def refine[A](request: R[A]): Future[Either[Result, P[A]]] = transform(request).map(Right(_))(executionContext)
}
