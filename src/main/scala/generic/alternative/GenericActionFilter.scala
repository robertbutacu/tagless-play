package generic.alternative

import play.api.mvc.Result
import scala.language.higherKinds
import cats.syntax.all._

trait GenericActionFilter[F[+_], R[_]] extends GenericActionRefiner[F, R, R]{
  def filter[A](request: R[A]): F[Option[Result]]

  override def refine[A](request: R[A]): F[Either[Result, R[A]]] = filter(request).map(_.toLeft(request))
}
