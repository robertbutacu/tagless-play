package generic.v2.filter

import cats.~>
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericActionFilter[F[_], R[_]] extends ActionFilter[R] {
  implicit def transformer: F ~> Future

  def genericFilter[A](request: R[A]): F[Option[Result]]

  override protected def filter[A](request: R[A]): Future[Option[Result]] = transformer(genericFilter(request))
}
