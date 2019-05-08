package generic.independent.filter.experiments

import cats.{Monad, ~>}
import generic.builder.AbstractGenericController
import generic.builder.GenericActionBuilder.GenericActionBuilderAsync
import generic.independent.filter.{GenericActionBuilder, GenericActionFilter, GenericActionRefiner}
import play.api.libs.json.Json
import play.api.mvc._
import generic.independent.filter.ActionBuilderExtensionMethod.GenericActionBuilderExtension
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.language.higherKinds


private[experiments] object Experiments extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  case class RequestWithProviderId[A](providerId: String, request: Request[A]) extends WrappedRequest[A](request)

  class RequestFiltered[F[+ _]]()(implicit val toFuture: F ~> Future,
                                  val fromFuture: Future ~> F,
                                  val M: Monad[F],
                                  val ec: ExecutionContext) extends GenericActionFilter[F, Request] {
    override def filter[A](request: Request[A]): F[Option[Result]] =
      implicitly[Monad[F]].pure(None)
  }

  class ExtraRequest[F[+ _]]()(implicit val toFuture: F ~> Future,
                               val fromFuture: Future ~> F,
                               val M: Monad[F],
                               val ec: ExecutionContext)
    extends GenericActionRefiner[F, Request, RequestWithProviderId] {
    override def refine[A](request: Request[A]): F[Either[Result, RequestWithProviderId[A]]] = {
      M.pure(Right(RequestWithProviderId("some-provider-id", request)))
    }
  }

  class ComposedActions[F[+ _]](
                                 requestFiltered: RequestFiltered[F],
                                 extraRequest: ExtraRequest[F],
                                 cc: ControllerComponents
                               )(implicit val toFuture: F ~> Future,
                                 val fromFuture: Future ~> F,
                                 val M: Monad[F]) extends AbstractGenericController(cc) {
    def filtered: GenericActionBuilder[F, Request, AnyContent] = Action.toGenericAction andThen requestFiltered andThen extraRequest
  }

  class Controller[F[+ _]](composedActions: ComposedActions[F],
                           someService: SomeService[F],
                           cc: ControllerComponents)(implicit M: Monad[F], transformer: F ~> Future) extends AbstractGenericController(cc) {

    def someAction: Action[AnyContent] = composedActions.filtered.toActionBuilder.genericAsync(implicit request => M.pure(Ok(Json.obj())))

    def otherAction: Action[AnyContent] = GenericAction.async(implicit request => M.pure(Ok(Json.obj())))
  }

  class SomeService[F[_]]()

}
