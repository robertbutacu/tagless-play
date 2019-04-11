package generic.action.filter

import cats.{Functor, ~>}
import play.api.mvc.{ActionFilter, Result}

import scala.language.higherKinds
import cats.syntax.all._

import scala.concurrent.Future

trait GenericActionFilter[R[_], F[_]] extends ActionFilter[R] {
  implicit def transformer: F ~> Future

  def genericFilter[A](request: R[A]): F[Option[Result]]

  override protected def filter[A](request: R[A]): Future[Option[Result]] = transformer(genericFilter(request))

  final protected def refine[A](request: R[A])(implicit FF: Functor[F]): F[Either[Result, R[A]]] =
    genericFilter(request).map(_.toLeft(request))
}
