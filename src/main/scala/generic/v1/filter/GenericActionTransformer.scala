package generic.v1.filter

import cats.~>
import play.api.mvc.ActionTransformer

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericActionTransformer[F[+_], -R[_], +P[_]] extends ActionTransformer[R, P] {
  def transformer: F ~> Future

  def genericTransform[A](request: R[A]): F[P[A]]

  override protected def transform[A](request: R[A]): Future[P[A]] = transformer(genericTransform(request))

}
