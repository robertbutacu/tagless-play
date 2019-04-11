package generic.action

import play.api.mvc.ControllerComponents

abstract class AbstractGenericController(protected val controllerComponents: ControllerComponents) extends GenericBaseController

