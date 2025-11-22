package model

import model.Condition._

import java.time.Instant
import play.api.libs.json._
import model.Mail._
import com.typesafe.config.ConfigFactory
import model.KafkaTopics.notifications

object KafkaTopics {
  val notifications: String =
    ConfigFactory.load().getString("kafka.topic.notifications")
}

case class Allocation(
                       allocationId: Int = 0,
                       equipmentId: Int,
                       employeeId: Int,
                       allocatedAt: Instant,
                       expectedReturnAt: Instant,
                       returnedAt: Option[Instant],
                       conditionOnReturn: Option[Condition]
                     )

case class AllocationCreateRequest(
                                    equipmentId: Int,
                                    employeeId: Int,
                                    expectedReturnAt: Instant
                                  )

case class AllocationReturnRequest(
                                    conditionOnReturn: Condition,
                                    notes: Option[String] = None,
                                  )

case class AdditionalInfo(
                           inventoryMail: Option[String] = None,
                           maintenanceMail: Option[String] = None
                         )

case class AllocationCreatedEvent(
                                   topic: String = notifications,
                                   equipmentType: String,
                                   equipmentSerial: String,
                                   employeeId: Int,
                                   employeeName: String,
                                   employeeEmail: String,
                                   allocatedAt: Instant,
                                   expectedReturnAt: Instant,
                                   event: String = "AllocationCreatedEvent",
                                   additionalInfo: Option[AdditionalInfo] = Some(
                                     AdditionalInfo(
                                       inventoryMail = Some(inventoryEmail),
                                       maintenanceMail = None
                                     )
                                   )
                                 )

case class AllocationReturnedEvent(
                                    topic: String = notifications,
                                    equipmentType: String,
                                    equipmentSerial: String,
                                    employeeId: Int,
                                    employeeName: String,
                                    employeeEmail: String,
                                    returnedAt: Instant,
                                    condition: String,
                                    notes: Option[String],
                                    event: String = "AllocationReturnedEvent",
                                    additionalInfo: Option[AdditionalInfo] = Some(
                                      AdditionalInfo(
                                        inventoryMail = Some(inventoryEmail),
                                        maintenanceMail = Some(maintenanceEmail)
                                      )
                                    )
                                  )

case class AllocationOverdueEvent(
                                   topic: String = notifications,
                                   equipmentType: String,
                                   equipmentSerial: String,
                                   employeeId: Int,
                                   employeeName: String,
                                   employeeEmail: String,
                                   overdueByDays: Long,
                                   overdueAt: Instant,
                                   event: String = "AllocationOverdueEvent",
                                   additionalInfo: Option[AdditionalInfo] = Some(
                                     AdditionalInfo(
                                       inventoryMail = Some(inventoryEmail),
                                       maintenanceMail = None
                                     )
                                   )
                                 )


object EventFormats {
  implicit val instantFmt: Format[Instant] = new Format[Instant] {
    def writes(i: Instant) = JsString(i.toString)

    def reads(json: JsValue): JsResult[Instant] = json match {
      case JsString(s) => JsSuccess(Instant.parse(s))
      case _ => JsError("Expected ISO timestamp")
    }
  }

  implicit val createdFmt: OFormat[AllocationCreatedEvent] =
    Json.format[AllocationCreatedEvent]

  implicit val returnedFmt: OFormat[AllocationReturnedEvent] =
    Json.format[AllocationReturnedEvent]

  implicit val overdueFmt: OFormat[AllocationOverdueEvent] =
    Json.format[AllocationOverdueEvent]
}

object Allocation {
  implicit val format: OFormat[Allocation] = Json.format[Allocation]
}

object AllocationModels {
  implicit val createFormat: OFormat[AllocationCreateRequest] =
    Json.format[AllocationCreateRequest]

  implicit val returnFormat: OFormat[AllocationReturnRequest] =
    Json.format[AllocationReturnRequest]
}

object AdditionalInfo {
  implicit val format: Format[AdditionalInfo] = Json.format[AdditionalInfo]
}
