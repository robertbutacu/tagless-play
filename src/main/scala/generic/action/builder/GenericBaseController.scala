package generic.action.builder

import play.api.mvc.{ActionBuilder, AnyContent, Request}

trait GenericBaseController extends GenericBaseControllerHelpers {
  def Action: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder
}

