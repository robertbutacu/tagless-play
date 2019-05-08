package generic.independent.filter

import cats.~>
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait GenericActionBuilder[F[_], +R[_], B] extends GenericAction[F, Request, R] {
  self =>
  implicit def toFuture:   F      ~> Future
  implicit def fromFuture: Future ~> F

  def parser: BodyParser[B]
  implicit def ec: ExecutionContext

  def toActionBuilder: ActionBuilder[R, B] = new ActionBuilder[R, B] {
    def executionContext = self.ec
    def parser = self.parser
    def invokeBlock[A](request: Request[A], block: R[A] => Future[Result]) =
      toFuture(self.invokeBlock[A](request, p => fromFuture(block(p))))

    override protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] = bodyParser
    override protected def composeAction[A](action: Action[A]): Action[A] = action
  }
}
