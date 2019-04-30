package generic.v2.filter

import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait GenericActionFunction[F[_], -R[_], +P[_]] extends ActionFunction[R, P]{
  self =>
}
