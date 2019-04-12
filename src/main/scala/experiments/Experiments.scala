package experiments

import cats.{Monad, ~>}
import generic.action.filter.{GenericActionFilter, GenericActionRefiner}
import play.api.mvc._
import cats.instances.all._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import generic.action.builder.GenericActionBuilder.GenericActionBuilder
import play.api.libs.json.Json

object Experiments {

  case class RequestWithProviderId[A](providerId: String, request: Request[A]) extends WrappedRequest[A](request)

  class RequestFiltered[F[_] : Monad](implicit val executionContext: ExecutionContext,
                                      val transformer: F ~> Future) extends GenericActionFilter[F, Request] {
    override def genericFilter[A](request: Request[A]): F[Option[Result]] =
      implicitly[Monad[F]].pure(None)
  }

  class ExtraRequest[F[+_]](implicit val executionContext: ExecutionContext, val transformer: F ~> Future, M: Monad[F])
    extends GenericActionRefiner[F, Request, RequestWithProviderId] {
    override def genericRefine[A](request: Request[A]): F[Either[Result, RequestWithProviderId[A]]] = {
      M.pure(Right(RequestWithProviderId("some-provider-id", request)))
    }
  }

  class ComposedActions[F[+_]](
                       requestFiltered: RequestFiltered[F],
                       extraRequest: ExtraRequest[F],
                       someService: SomeService[F],
                       cc: ControllerComponents
                       )(implicit M: Monad[F], transformer: F ~> Future) extends AbstractController(cc) {
    def filtered: ActionBuilder[RequestWithProviderId, AnyContent] = Action andThen requestFiltered andThen extraRequest

    filtered.genericAsync(implicit request => M.pure(Ok(Json.obj())))
  }

  class SomeService[F[_]] {

  }
}
