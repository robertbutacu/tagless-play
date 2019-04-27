package generic.v1.builder

import play.api.mvc.{ActionBuilder, AnyContent, Request}

trait GenericBaseController extends GenericBaseControllerHelpers {
  def Action: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder
}

