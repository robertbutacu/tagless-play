package generic.independent.filter

import play.api.mvc.Result
import cats.syntax.all._
import scala.language.higherKinds

trait GenericActionTransformer[F[+_], -R[_], +P[_]] extends GenericActionRefiner[F, R, P] {
  def transform[A](request: R[A]): F[P[A]]

  override def refine[A](request: R[A]): F[Either[Result, P[A]]] = transform(request).map(Right(_))
}
