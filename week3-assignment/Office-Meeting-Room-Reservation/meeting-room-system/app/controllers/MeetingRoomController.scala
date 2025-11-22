package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

import model.{Role, MeetingRoomCreateRequest, MeetingRoomUpdateRequest}
import model.MeetingRoomModels._
import repository.MeetingRoomRepository
import security.RBAC

@Singleton
class MeetingRoomController @Inject()(
                                       secured: RBAC,
                                       cc: ControllerComponents,
                                       repo: MeetingRoomRepository
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // -------------------------------------
  // Create Meeting Room
  // -------------------------------------
  def createRoom: Action[JsValue] =
    secured.WithRole(List(Role.SYSTEM_ADMIN)).async(parse.json) { implicit request =>
      request.body.validate[MeetingRoomCreateRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        req =>
          repo.create(
            req.roomName,
            req.floor,
            req.capacity,
            req.hasProjector,
            req.hasWhiteboard,
            req.status
          ).map { newId =>
            Created(Json.obj(
              "status" -> "success",
              "roomId" -> newId
            ))
          }.recover { case ex =>
            InternalServerError(Json.obj("error" -> ex.getMessage))
          }
      )
    }

  // -------------------------------------
  // Get Room
  // -------------------------------------
  def getRoom(id: Long): Action[AnyContent] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async {
      repo.findById(id).map {
        case Some(room) => Ok(Json.toJson(room))
        case None       => NotFound(Json.obj("error" -> "Room not found"))
      }
    }

  // -------------------------------------
  // List Rooms
  // -------------------------------------
  def listRooms: Action[AnyContent] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async {
      repo.listAll().map(rooms => Ok(Json.toJson(rooms)))
    }

  // -------------------------------------
  // Update Status Only
  // -------------------------------------
  def updateRoom(id: Long): Action[JsValue] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async(parse.json) { implicit request =>
      request.body.validate[MeetingRoomUpdateRequest].fold(
        errors =>
          Future.successful(BadRequest(JsError.toJson(errors))),

        req =>
          repo.update(
            id,
            req.floor,
            req.capacity,
            req.hasProjector,
            req.hasWhiteboard,
            req.status
          ).map { rows =>
            if (rows == 0)
              NotFound(Json.obj("error" -> "Room not found"))
            else
              Ok(Json.obj("status" -> "updated"))
          }.recover { case ex =>
            InternalServerError(Json.obj("error" -> ex.getMessage))
          }
      )
    }

}
