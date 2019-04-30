package generic.builder

import play.api.mvc.{ActionBuilder, AnyContent, Request}

import scala.language.higherKinds

trait GenericBaseController extends GenericBaseControllerHelpers {
  def Action       : ActionBuilder[Request, AnyContent]           = controllerComponents.actionBuilder
}

