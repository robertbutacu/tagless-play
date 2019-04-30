package generic.v2.builder

import cats.~>
import play.api.mvc._

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericBaseControllerHelpers extends BaseControllerHelpers {
  object GenericAction {
    def async[F[_], A](bodyParser          : BodyParser[A])
                      (block               : Request[A] => F[Result])
                      (implicit transformer: F ~> Future): Action[A] =
      controllerComponents.actionBuilder.async(bodyParser)(r => transformer(block(r)))

    def async[F[_]](block               : Request[AnyContent] => F[Result])
                   (implicit transformer: F ~> Future): Action[AnyContent] =
      controllerComponents.actionBuilder.async(controllerComponents.actionBuilder.parser)(block.andThen(g => transformer(g)))
  }
}
