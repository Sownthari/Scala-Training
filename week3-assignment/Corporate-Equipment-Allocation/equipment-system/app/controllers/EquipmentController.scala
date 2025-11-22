package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

import repository.EquipmentRepository
import model.{Equipment, Role, EquipmentCreateRequest, EquipmentUpdateRequest}
import security.RBAC
import model.EquipmentModels._

import play.api.libs.json._

@Singleton
class EquipmentController @Inject() (cc: ControllerComponents, repo: EquipmentRepository, secured: RBAC)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getAllEquipmentTypes: Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF, Role.RECEPTION_STAFF)).async { implicit request =>
      repo.getAllEquipmentTypes().map { list =>
        Ok(Json.toJson(list))
      }
    }
  def getEquipmentTypeById(id: Int): Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF)).async { implicit request =>
      repo.getEquipmentTypeById(id).map {
        case Some(e) => Ok(Json.toJson(e))
        case None    => NotFound(Json.obj("error" -> "Not found"))
      }
    }
  def getAll: Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF, Role.RECEPTION_STAFF)).async { implicit request =>
      repo.findAll().map { list =>
        Ok(Json.toJson(list))
      }
    }

  def getById(id: Int): Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF)).async { implicit request =>
      repo.findById(id).map {
        case Some(e) => Ok(Json.toJson(e))
        case None    => NotFound(Json.obj("error" -> "Not found"))
      }
    }

  def getBySerialNo(id: String): Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF)).async { implicit request =>
      repo.findBySerialNo(id).map {
        case Some(e) => Ok(Json.toJson(e))
        case None    => NotFound(Json.obj("error" -> "Not found"))
      }
    }

  def create: Action[JsValue] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF)).async(parse.json) { implicit request =>
      request.body.validate[EquipmentCreateRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),

        req => {
          val now = java.time.Instant.now()
          val eq = Equipment(
            equipmentId = 0,
            serialNumber = req.serialNumber,
            typeId = req.typeId,
            status = req.status,
            createdAt = now
          )

          repo.create(eq).map { id =>
            Created(Json.obj("equipmentId" -> id))
          }
        }
      )
    }

  def updateStatus(id: Int): Action[JsValue] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF)).async(parse.json) { implicit request =>
      request.body.validate[EquipmentUpdateRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),

        req =>
          repo.updateStatus(id, req.status).map {
            case 0 => NotFound(Json.obj("error" -> "Equipment not found"))
            case _ => Ok(Json.obj("status" -> "updated"))
          }
      )
    }

  def delete(id: Int): Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.INVENTORY_STAFF)).async { implicit request =>
      repo.delete(id).map {
        case 0 => NotFound(Json.obj("error" -> "Not found"))
        case _ => Ok(Json.obj("status" -> "deleted"))
      }
    }
}
