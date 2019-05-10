package generic.alternative

import cats.{Monad, ~>}
import play.api.mvc.{ActionBuilder, BodyParser, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object ActionBuilderExtensionMethod {
  implicit class GenericActionBuilderExtension[F[_], +R[_], B](actionBuilder: ActionBuilder[R, B]) {
    def toGenericAction(implicit toF: F ~> Future,
                        fromF: Future ~> F,
                        M: Monad[F],
                        executionContext: ExecutionContext
                       ): GenericActionBuilder[F, R, B] = new GenericActionBuilder[F, R, B] {
      override implicit def toFuture: F ~> Future = toF

      override implicit def fromFuture: Future ~> F = fromF

      override def parser: BodyParser[B] = actionBuilder.parser

      override implicit def ec: ExecutionContext = executionContext

      override def invokeBlock[A](request: Request[A], block: R[A] => F[Result]): F[Result] = {
        fromFuture(actionBuilder.invokeBlock(request, {p: R[A] => toFuture(block(p))}))
      }
    }
  }
}
