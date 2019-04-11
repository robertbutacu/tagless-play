package experiments

import cats.{Monad, ~>}
import generic.action.AbstractGenericController
import generic.action.filter.{GenericActionFilter, GenericActionRefiner}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import generic.action.builder.GenericActionBuilder.GenericActionBuilder
object Experiments {

  case class RequestWithProviderId[A](providerId: String, request: Request[A]) extends WrappedRequest[A](request)

  class RequestFiltered[F[_] : Monad](implicit val executionContext: ExecutionContext,
                                      val transformer: F ~> Future) extends GenericActionFilter[Request, F] {
    override def genericFilter[A](request: Request[A]): F[Option[Result]] =
      implicitly[Monad[F]].pure(None)
  }

  class ExtraRequest[F[_]](implicit val executionContext: ExecutionContext, val transformer: F ~> Future, M: Monad[F])
    extends GenericActionRefiner[Request, RequestWithProviderId, F] {
    override def genericRefine[A](request: Request[A]): F[Either[Result, RequestWithProviderId[A]]] = {
      M.pure(Right(RequestWithProviderId("some-provider-id", request)))
    }
  }

  class ComposedActions[F[_]](
                       requestFiltered: RequestFiltered[F],
                       extraRequest: ExtraRequest[F],
                       cc: ControllerComponents
                       ) extends AbstractGenericController(cc) {
    def filtered: ActionBuilder[RequestWithProviderId, AnyContent] = Action andThen requestFiltered andThen extraRequest
  }
}
