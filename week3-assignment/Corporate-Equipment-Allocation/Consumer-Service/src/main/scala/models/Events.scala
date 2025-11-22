package models

import play.api.libs.json._
import java.time.Instant

case class AdditionalInfo(
                           inventoryMail: Option[String] = None,
                           maintenanceMail: Option[String] = None
                         )

object AdditionalInfo {
  implicit val format: Format[AdditionalInfo] = Json.format[AdditionalInfo]
}

case class AllocationCreatedEvent(
                                   equipmentType: String,
                                   equipmentSerial: String,
                                   employeeId: Int,
                                   employeeName: String,
                                   employeeEmail: String,
                                   allocatedAt: Instant,
                                   expectedReturnAt: Instant,
                                   event: String,
                                   additionalInfo: Option[AdditionalInfo] = None
                                 )

case class AllocationReturnedEvent(
                                    equipmentType: String,
                                    equipmentSerial: String,
                                    employeeId: Int,
                                    employeeName: String,
                                    employeeEmail: String,
                                    returnedAt: Instant,
                                    condition: String,
                                    notes: Option[String] = None,
                                    event: String,
                                    additionalInfo: Option[AdditionalInfo] = None
                                  )

case class AllocationOverdueEvent(
                                 equipmentType: String,
                                 equipmentSerial: String,
                                 employeeId: Int,
                                 employeeName: String,
                                 employeeEmail: String,
                                 overdueByDays: Long,
                                 overdueAt: Instant,
                                 event: String,
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

  implicit val createdFmt = Json.format[AllocationCreatedEvent]
  implicit val returnedFmt = Json.format[AllocationReturnedEvent]
  implicit val overdueFmt = Json.format[AllocationOverdueEvent]
}
