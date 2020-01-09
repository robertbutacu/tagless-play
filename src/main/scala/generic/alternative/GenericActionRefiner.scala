package generic.alternative

import cats.Monad
import play.api.mvc.Result
import cats.syntax.all._

import scala.language.higherKinds

trait GenericActionRefiner[F[+_], -R[_], +P[_]] extends GenericAction[F, R, P]{
  implicit def M: Monad[F]

  def refine[A](request: R[A]): F[Either[Result, P[A]]]

  override def invokeBlock[A](request: R[A], block: P[A] => F[Result]): F[Result] = {
    refine(request).flatMap(result => result.fold(err => err.pure, request => block(request)))
  }
}
