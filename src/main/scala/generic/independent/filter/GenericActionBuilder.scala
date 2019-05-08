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
    override implicit def toFuture:   F      ~> Future = self.toFuture
    override implicit def fromFuture: Future ~> F      = self.fromFuture

    override def parser:      BodyParser[B]    = self.parser
    override implicit def ec: ExecutionContext = self.ec

    override def invokeBlock[A](request: Request[A], block: Q[A] => F[Result]): F[Result] = {
      self.invokeBlock(request, { p: R[A] => other.invokeBlock(p, block) })
    }
  }

  final def async(bodyParser: BodyParser[B])(block: R[B] => F[Result]): Action[B] = {
    val actionBuilder = toActionBuilder

    actionBuilder.async(actionBuilder.parser)(r => toFuture(block(r)))
  }

  final def async(block: R[B] => F[Result]): Action[B] = async(parser)(block)

  private def toActionBuilder: ActionBuilder[R, B] = new ActionBuilder[R, B] {
    def executionContext: ExecutionContext = self.ec
    def parser:           BodyParser[B]    = self.parser

    def invokeBlock[A](request: Request[A], block: R[A] => Future[Result]) =
      toFuture(self.invokeBlock[A](request, p => fromFuture(block(p))))

    override protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] = bodyParser
    override protected def composeAction[A](action: Action[A]): Action[A] = action
  }
}
