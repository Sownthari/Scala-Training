package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import repository.OverdueLogRepository
import model.{Role, OverdueLog}
import security.RBAC

import play.api.libs.json._

@Singleton
class OverdueLogController @Inject()(cc: ControllerComponents, repo: OverdueLogRepository, secured: RBAC)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getAll: Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.RECEPTION_STAFF, Role.INVENTORY_STAFF)).async { implicit request =>
      repo.findAll().map(list => Ok(Json.toJson(list)))
    }
}
