package generic.v2.builder

import generic.v2.builder.GenericActionBuilder.GenericActionBuilder
import play.api.mvc.{ActionBuilder, AnyContent, Request}

trait GenericBaseController extends GenericBaseControllerHelpers {
  def Action: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder
  //def ActionG: GenericActionBuilder[Request, AnyContent] =
}

