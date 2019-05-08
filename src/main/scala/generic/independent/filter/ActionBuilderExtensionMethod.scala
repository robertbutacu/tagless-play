package generic.independent.filter

import cats.{Monad, ~>}
import play.api.mvc.{ActionBuilder, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object ActionBuilderExtensionMethod {

  implicit class GenericActionBuilder[F[_], +R[_], B](actionBuilder: ActionBuilder[R, B]) {
    def toGenericAction(implicit toFuture: F ~> Future,
                        fromFuture: Future ~> F,
                        M: Monad[F],
                        ec: ExecutionContext
                       ): GenericAction[F, Request, R] = new GenericAction[F, Request, R] {
      override def toFuture: F ~> Future = toFuture

      override def fromFuture: Future ~> F = fromFuture

      override def invokeBlock[A](request: Request[A], block: R[A] => F[Result]): F[Result] = {
        super.invokeBlock(request, block)
      }

      override implicit def ec: ExecutionContext = ec
    }
  }
}
