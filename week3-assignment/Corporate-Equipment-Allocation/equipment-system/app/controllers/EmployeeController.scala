package controllers

import model.{Employee, EmployeeCreateRequest, Role}

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import repository.EmployeeRepository
import model.EmployeeModels._
import security.RBAC

@Singleton
class EmployeeController @Inject()(secured: RBAC, cc: ControllerComponents, repo: EmployeeRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def createEmployee: Action[JsValue] =
    secured.WithRole(List(Role.SYSTEM_ADMIN)).async(parse.json) { implicit request =>
      request.body.validate[EmployeeCreateRequest].fold(
        errors =>
          Future.successful(BadRequest(JsError.toJson(errors))),

        req =>
          repo.create(req.name, req.department, req.email).map { newId =>
            Created(Json.obj(
              "status"      -> "success",
              "employeeId"  -> newId,
              "email"       -> req.email
            ))
          }.recover {
            case ex =>
              InternalServerError(Json.obj("error" -> ex.getMessage))
          }
      )
    }
}
