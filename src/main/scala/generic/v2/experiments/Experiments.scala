package generic.v2.experiments

import cats.{Monad, ~>}
import generic.v2.builder.AbstractGenericController
import generic.v2.filter.{GenericActionFilter, GenericActionRefiner}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.language.higherKinds


object Experiments extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  case class RequestWithProviderId[A](providerId: String, request: Request[A]) extends WrappedRequest[A](request)

  class RequestFiltered[F[_] : Monad]()(implicit val transformer: F ~> Future) extends GenericActionFilter[F, Request] {

    override def genericFilter[A](request: Request[A]): F[Option[Result]] =
      implicitly[Monad[F]].pure(None)

    override protected def executionContext: ExecutionContext = ec
  }

  class ExtraRequest[F[+_]]()(implicit val transformer: F ~> Future, M: Monad[F])
    extends GenericActionRefiner[F, Request, RequestWithProviderId] {

    override def genericRefine[A](request: Request[A]): F[Either[Result, RequestWithProviderId[A]]] = {
      M.pure(Right(RequestWithProviderId("some-provider-id", request)))
    }

    override protected def executionContext: ExecutionContext = ec
  }

  class ComposedActions[F[+_]](
                                requestFiltered: RequestFiltered[F],
                                extraRequest: ExtraRequest[F],
                                cc: ControllerComponents
                              )(implicit M: Monad[F], transformer: F ~> Future) extends AbstractGenericController(cc) {
    def filtered: ActionBuilder[RequestWithProviderId, AnyContent] = Action andThen requestFiltered andThen extraRequest
  }

  class Controller[F[+_]](composedActions: ComposedActions[F],
                          someService: SomeService[F],
                          cc: ControllerComponents)(implicit M: Monad[F], transformer: F ~> Future) extends AbstractGenericController(cc) {

    def someAction: Action[AnyContent] = ??? //composedActions.filtered.genericAsync(implicit request => M.pure(Ok(Json.obj())))

    def otherAction: Action[AnyContent] = GenericAction.async(implicit request => M.pure(Ok(Json.obj())))
  }

  class SomeService[F[_]]()
}
