package model

import com.typesafe.config.ConfigFactory

import java.time.Instant
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, JsValue, Json, OFormat}
import model.ReservationStatus.ReservationStatus
import model.KafkaTopics.notifications
import model.Mail.{adminStaffEmail, roomServiceEmail}

object KafkaTopics {
  val notifications: String =
    ConfigFactory.load().getString("kafka.topic.notifications")
}

sealed trait ReservationError
case object RoomNotFound extends ReservationError
case object RoomNotAvailable extends ReservationError
case object RoomTimeConflict extends ReservationError
case object ReservationNotFound extends ReservationError
case object ReservationUpdateConflict extends ReservationError


case class Reservation(
                        reservationId: Long = 0,
                        roomId: Long,
                        bookedBy: Long,
                        employeeId: Int,
                        startTime: Instant,
                        endTime: Instant,
                        status: ReservationStatus = ReservationStatus.BOOKED,
                        specialRequirements: Option[String],
                        isOccupied: Boolean,
                        createdAt: Instant
                      )

case class ReservationCreateRequest(
                                     roomId: Long,
                                     employeeId: Int,
                                     startTime: Instant,
                                     endTime: Instant,
                                     purpose: String,
                                     specialRequirements: Option[String]
                                   )

case class ReservationUpdateRequest(
                                     startTime: Option[Instant],
                                     endTime: Option[Instant],
                                     status: ReservationStatus,
                                     specialRequirements: Option[String]
                                   )
case class AdditionalInfo(
                           roomServiceMail: Option[String] = None,
                           adminStaffMail: Option[String] = None,
                         )

case class ReservationCreatedEvent(
                                    topic: String = notifications,
                                    roomName: String,
                                    floor: Int,
                                    reservationId: Long,
                                    employeeId: Int,
                                    employeeName: String,
                                    employeeEmail: String,
                                    startTime: Instant,
                                    endTime: Instant,
                                    specialRequirements: Option[String],
                                    event: String = "ReservationCreatedEvent",
                                    additionalInfo: Option[AdditionalInfo] = Some(
                                      AdditionalInfo(
                                        roomServiceMail = Some(roomServiceEmail),
                                        adminStaffMail = Some(adminStaffEmail)
                                      )
                                    )
                                  )
case class MeetingReminderEvent(
                                 topic: String = notifications,
                                 roomName: String,
                                 floor: Int,
                                 reservationId: Long,
                                 employeeId: Int,
                                 employeeName: String,
                                 employeeEmail: String,
                                 startTime: Instant,
                                 endTime: Instant,
                                 specialRequirements: Option[String],
                                 event: String = "MeetingReminderEvent",
                                 additionalInfo: Option[AdditionalInfo] = Some(
                                   AdditionalInfo(
                                     roomServiceMail = Some(roomServiceEmail)
                                   )
                                 )
                               )

case class AutoReleaseEvent(
                             topic: String = notifications,
                             roomName: String,
                             floor: Int,
                             reservationId: Long,
                             employeeId: Int,
                             employeeName: String,
                             employeeEmail: String,
                             startTime: Instant,
                             autoReleasedAt: Instant,
                             event: String = "AutoReleaseEvent",
                             additionalInfo: Option[AdditionalInfo] = Some(
                               AdditionalInfo(
                                 roomServiceMail = Some(roomServiceEmail)
                               )
                             )
                           )




object ReservationModels {
  implicit val createFormat: OFormat[ReservationCreateRequest] =
    Json.format[ReservationCreateRequest]

  implicit val updateFormat: OFormat[ReservationUpdateRequest] =
    Json.format[ReservationUpdateRequest]
}

object Reservation {
  implicit val format: OFormat[Reservation] = Json.format[Reservation]
}

object EventFormats {
  implicit val instantFmt: Format[Instant] = new Format[Instant] {
    def writes(i: Instant) = JsString(i.toString)

    def reads(json: JsValue) = json match {
      case JsString(s) => JsSuccess(Instant.parse(s))
      case _ => JsError("Expected ISO timestamp")
    }
  }

  implicit val createdFmt: OFormat[ReservationCreatedEvent] =
    Json.format[ReservationCreatedEvent]

  implicit val reminderFmt: OFormat[MeetingReminderEvent] =
    Json.format[MeetingReminderEvent]

  implicit val autoReleaseFmt: OFormat[AutoReleaseEvent] =
    Json.format[AutoReleaseEvent]
}

object AdditionalInfo {
  implicit val format: Format[AdditionalInfo] = Json.format[AdditionalInfo]
}
