package generic.v2.filter

import cats.~>
import play.api.mvc._

import scala.concurrent.Future
import scala.language.higherKinds

trait GenericActionBuilder[F[_], +R[_], B] extends GenericActionFunction[F, Request, R] with ActionBuilder[R, B]{
  self =>
  def genericAsync[A](bodyParser: BodyParser[A])
                  (block: R[A] => F[Result])
                  (implicit transformer: F ~> Future): Action[A] =
    self.async(bodyParser)(r => transformer(block(r)))

  def genericAsync(block: R[B] => F[Result])
                  (implicit transformer: F ~> Future): Action[B] =
    self.async(self.parser)(block.andThen(g => transformer(g)))
}
