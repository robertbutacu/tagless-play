package generic.action.builder

import play.api.mvc.ControllerComponents

abstract class AbstractGenericController(protected val controllerComponents: ControllerComponents) extends GenericBaseController

