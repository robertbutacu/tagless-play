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


  override def andThen[Q[_]](other: GenericAction[F, R, Q]): GenericActionBuilder[F, Q, B] = new GenericActionBuilder[F, Q, B] {
    override implicit def toFuture: F ~> Future = toFuture

    override implicit def fromFuture: Future ~> F = fromFuture

    override def parser: BodyParser[B] = parser

    override implicit def ec: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: Q[A] => F[Result]): F[Result] = {
      self.invokeBlock(request, {p: R[A] => other.invokeBlock(p, block) })
    }
  }

  def toActionBuilder: ActionBuilder[R, B] = new ActionBuilder[R, B] {
    def executionContext = self.ec
    def parser = self.parser
    def invokeBlock[A](request: Request[A], block: R[A] => Future[Result]) =
      toFuture(self.invokeBlock[A](request, p => fromFuture(block(p))))

    override protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] = bodyParser
    override protected def composeAction[A](action: Action[A]): Action[A] = action
  }
}
