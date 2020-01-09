package generic.builder

import cats.~>
import play.api.mvc._

import scala.concurrent.Future
import scala.language.higherKinds

object GenericActionBuilder {
  implicit class GenericActionBuilderAsync[F[_], +R[_], A](actionBuilder: ActionBuilder[R, A]) {
    def genericAsync(bodyParser: BodyParser[A])
             (block: R[A] => F[Result])
             (implicit transformer: F ~> Future): Action[A] =
      actionBuilder.async(bodyParser)(r => transformer(block(r)))

    def genericAsync(block: R[A] => F[Result])
             (implicit transformer: F ~> Future): Action[A] =
      actionBuilder.async(actionBuilder.parser)(block.andThen(g => transformer(g)))
  }
}
