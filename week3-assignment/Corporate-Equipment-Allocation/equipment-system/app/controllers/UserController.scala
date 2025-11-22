package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import model.{ErrorResponse, Role, UserCreateRequest}
import repository.{AuthRepository, EmployeeRepository, UserRepository}
import model.EmployeeModels._
import model.AuthModels._
import security.RBAC

@Singleton
class UserController @Inject()(secured: RBAC, cc : ControllerComponents, userRepo: UserRepository, empRepo: EmployeeRepository, authRepo: AuthRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def createUser: Action[JsValue] =
    secured.WithRole(List(Role.SYSTEM_ADMIN)).async(parse.json) { implicit request =>
    request.body.validate[UserCreateRequest].fold(
      errors =>
        Future.successful(BadRequest(JsError.toJson(errors))),

      req =>
        empRepo.findByEmail(req.email).flatMap {
          case None =>
            Future.successful(NotFound(Json.toJson(ErrorResponse("Employee not found"))))

          case Some(emp) =>
            val hashed = authRepo.hashPassword(req.password)

            userRepo.createUser(emp.employeeId, hashed, req.role).map { _ =>
              Created(Json.obj(
                "status" -> "success",
                "employeeId" -> emp.employeeId,
                "role" -> req.role.toString
              ))
            }
        }
    )
  }
}
