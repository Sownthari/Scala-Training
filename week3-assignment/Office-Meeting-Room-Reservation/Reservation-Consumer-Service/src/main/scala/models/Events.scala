package models

import play.api.libs.json._

import java.time.Instant

case class AdditionalInfo(
                           adminStaffMail: Option[String] = None,
                           roomServiceMail: Option[String] = None
                         )

object AdditionalInfo {
  implicit val format: Format[AdditionalInfo] = Json.format[AdditionalInfo]
}

case class ReservationCreatedEvent(
                                    topic: String = "reservation-notifications",
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
                                    additionalInfo: Option[AdditionalInfo] = None
                                  )

case class MeetingReminderEvent(
                                 topic: String = "reservation-notifications",
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
                                 additionalInfo: Option[AdditionalInfo] = None
                               )

case class AutoReleaseEvent(
                             topic: String = "reservation-notifications",
                             roomName: String,
                             floor: Int,
                             reservationId: Long,
                             employeeId: Int,
                             employeeName: String,
                             employeeEmail: String,
                             startTime: Instant,
                             autoReleasedAt: Instant,
                             event: String = "AutoReleaseEvent",
                             additionalInfo: Option[AdditionalInfo] = None
                           )

object EventFormats {
  implicit val instantFmt: Format[Instant] = new Format[Instant] {
    def writes(i: Instant) = JsString(i.toString)

    def reads(json: JsValue) = json match {
      case JsString(s) => JsSuccess(Instant.parse(s))
      case _ => JsError("Expected ISO timestamp")
    }
  }

  implicit val createdFmt = Json.format[ReservationCreatedEvent]
  implicit val returnedFmt = Json.format[MeetingReminderEvent]
  implicit val overdueFmt = Json.format[AutoReleaseEvent]
}
