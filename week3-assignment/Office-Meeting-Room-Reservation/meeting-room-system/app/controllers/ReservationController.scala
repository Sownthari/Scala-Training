package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import model._
import model.ReservationModels._
import repository.{EmployeeRepository, MeetingRoomRepository, ReservationRepository}
import security.RBAC
import model.EventFormats._
import play.api.libs.ws._

@Singleton
class ReservationController @Inject()(
                                       secured: RBAC,
                                       cc: ControllerComponents,
                                       repo: ReservationRepository,
                                       meetingRoomRepository: MeetingRoomRepository,
                                       employeeRepository: EmployeeRepository,
                                       ws: WSClient
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // -------------------------------------
  // Create Reservation
  // -------------------------------------
  def createReservation: Action[JsValue] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async(parse.json) { implicit request =>
      val user = request.attrs(UserAttr.Key)
      request.body.validate[ReservationCreateRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),

        req =>
          repo.create(req, user.userId).flatMap {
            case Left(RoomNotFound) =>
              Future.successful(BadRequest("Meeting room does not exist"))

            case Left(RoomNotAvailable) =>
              Future.successful(BadRequest("Meeting room is not available"))

            case Left(RoomTimeConflict) =>
              Future.successful(BadRequest("Meeting room is already booked for the selected time"))

            case Right(newId) =>
              for {
                employee <- employeeRepository.findById(req.employeeId) // already exists, no need Option
                room <- meetingRoomRepository.findById(req.roomId) // get room information

                _ <- ws.url("http://localhost:8082/produce-events")
                  .post(
                    Json.toJson(
                      ReservationCreatedEvent(
                        roomName = room.get.roomName,
                        floor = room.get.floor.getOrElse(0),
                        reservationId = newId,
                        employeeId = employee.get.employeeId,
                        employeeName = employee.get.name,
                        employeeEmail = employee.get.email,
                        startTime = req.startTime,
                        endTime = req.endTime,
                        specialRequirements = req.specialRequirements
                      )
                    )
                  )
              } yield Created(Json.obj("reservationId" -> newId))
          }
      )
    }


  // -------------------------------------
  // Get by ID
  // -------------------------------------
  def getReservation(id: Long): Action[AnyContent] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async {
      repo.findById(id).map {
        case Some(r) => Ok(Json.toJson(r))
        case None => NotFound(Json.obj("error" -> "Reservation not found"))
      }
    }

  // -------------------------------------
  // List All
  // -------------------------------------
  def listReservations: Action[AnyContent] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async {
      repo.listAll().map(res => Ok(Json.toJson(res)))
    }

  // -------------------------------------
  // Update Reservation (start/end/status)
  // -------------------------------------
  def updateReservation(id: Long): Action[JsValue] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async(parse.json) { implicit request =>
      request.body.validate[ReservationUpdateRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),

        req =>
          repo.update(id, req).map {
            case Left(ReservationNotFound) =>
              NotFound(Json.obj("error" -> "Reservation not found"))

            case Left(RoomNotFound) =>
              BadRequest(Json.obj("error" -> "Meeting room does not exist"))

            case Left(RoomNotAvailable) =>
              BadRequest(Json.obj("error" -> "Room is not available for update"))

            case Left(ReservationUpdateConflict) =>
              BadRequest(Json.obj("error" -> "Update would conflict with another reservation"))

            case Right(updatedRows) =>
              Ok(Json.obj("status" -> "updated"))
          }
      )
    }


  // -------------------------------------
  // Cancel (status change only)
  // -------------------------------------
  def cancelReservation(id: Long): Action[AnyContent] =
    secured.WithRole(List(Role.ADMIN_STAFF, Role.SYSTEM_ADMIN)).async {
      repo.updateStatus(id, ReservationStatus.CANCELLED).map { rows =>
        if (rows == 0)
          NotFound(Json.obj("error" -> "Reservation not found"))
        else
          Ok(Json.obj("status" -> "cancelled"))
      }
    }
}
