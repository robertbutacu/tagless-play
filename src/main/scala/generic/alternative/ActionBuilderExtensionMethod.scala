package generic.alternative

import cats.{InjectK, Monad}
import play.api.mvc.{ActionBuilder, BodyParser, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object ActionBuilderExtensionMethod {
  implicit class GenericActionBuilderExtension[F[_], +R[_], B](actionBuilder: ActionBuilder[R, B]) {
    def toGenericAction(implicit injct: InjectK[F, Future],
                        M:                 Monad[F],
                        executionContext:  ExecutionContext
                       ): GenericActionBuilder[F, R, B] = new GenericActionBuilder[F, R, B] {
      override def injector: InjectK[F, Future] = injct
      override def parser:   BodyParser[B]      = actionBuilder.parser

      override implicit def ec: ExecutionContext = executionContext

      override def invokeBlock[A](request: Request[A], block: R[A] => F[Result]): F[Result] = {
        injector.prj(actionBuilder.invokeBlock(request, {p: R[A] => injector.inj(block(p))})).get
      }
    }
  }
}
