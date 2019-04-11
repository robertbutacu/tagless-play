package generic.action.builder

import cats.~>
import play.api.mvc._

import scala.concurrent.Future
import scala.language.higherKinds

object GenericActionBuilder {
  implicit class GenericActionBuilder[+R[_], A](actionBuilder: ActionBuilder[R, A]) {
    def andThen[Q[_]](other: ActionFunction[R, Q]): ActionBuilder[Q, A] = ???

    def invokeBlock[F[_]](request: Request[A], block: Request[A] => F[Result]): F[Result] = block(request)

    def genericApply[F[_]](block: => Result): Action[AnyContent] = actionBuilder(block)

    def genericAsync[F[_]](bodyParser: BodyParser[A])
                      (block: Request[A] => F[Result])
                      (implicit transformer: F ~> Future): Action[A] =
      actionBuilder.async(bodyParser)(r => transformer(block(r)))

    def genericAsync[F[_]](block: R[A] => F[Result])
                   (implicit transformer: F ~> Future): Action[A] =
      actionBuilder.async(actionBuilder.parser)(block.andThen(g => transformer(g)))
  }
}
