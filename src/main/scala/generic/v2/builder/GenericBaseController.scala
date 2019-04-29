package generic.v2.builder

import play.api.mvc.{ActionBuilder, AnyContent, Request}

trait GenericBaseController extends GenericBaseControllerHelpers {
  def Action: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder
}

