package experiments

import cats.{Monad, ~>}
import generic.action.filter.GenericActionFilter
import play.api.mvc.{Request, Result, WrappedRequest}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object Experiments {
  case class RequestWithProviderId[A](providerId: String, request: Request[A]) extends WrappedRequest[A](request)

  class RequestFiltered[F[_]: Monad](implicit val executionContext: ExecutionContext) extends GenericActionFilter[RequestWithProviderId, F] {
    override def genericFilter[A](request: RequestWithProviderId[A]): F[Option[Result]] =
      implicitly[Monad[F]].pure(None)

    override implicit def transformer: F ~> Future = implicitly[F ~> Future]
  }
}
