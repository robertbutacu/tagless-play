package generic.action

import scala.concurrent.Future
import play.api.mvc._
import scala.language.higherKinds
import cats.~>

trait GenericBaseControllerHelpers extends BaseControllerHelpers {
  object GenericAction {
    def async[F[_], A](bodyParser: BodyParser[A])
                        (block: Request[A] => F[Result])
                        (implicit transformer: F ~> Future): Action[A] =
      controllerComponents.actionBuilder.async(bodyParser)(r => transformer(block(r)))

    def async[F[_]](block: Request[AnyContent] => F[Result])
                          (implicit transformer: F ~> Future): Action[AnyContent] =
      controllerComponents.actionBuilder.async(controllerComponents.actionBuilder.parser)(block.andThen(g => transformer(g)))
  }
}