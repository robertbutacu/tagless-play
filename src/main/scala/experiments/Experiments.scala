package experiments

import cats.{Monad, ~>}
import generic.action.filter.{GenericActionFilter, GenericActionRefiner}
import play.api.mvc.{Request, Result, WrappedRequest}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object Experiments {

  case class RequestWithProviderId[A](providerId: String, request: Request[A]) extends WrappedRequest[A](request)

  class RequestFiltered[F[_] : Monad](implicit val executionContext: ExecutionContext,
                                      val transformer: F ~> Future) extends GenericActionFilter[RequestWithProviderId, F] {
    override def genericFilter[A](request: RequestWithProviderId[A]): F[Option[Result]] =
      implicitly[Monad[F]].pure(None)
  }

  class ExtraRequest[F[_]](implicit val executionContext: ExecutionContext, val transformer: F ~> Future, M: Monad[F])
    extends GenericActionRefiner[Request, RequestWithProviderId, F] {
    override def genericRefine[A](request: Request[A]): F[Either[Result, RequestWithProviderId[A]]] = {
      M.pure(Right(RequestWithProviderId("some-provider-id", request)))
    }
  }

}
