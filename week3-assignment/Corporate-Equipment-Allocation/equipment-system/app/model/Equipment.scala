package model

import model.Status.Status
import play.api.libs.json.{Json, OFormat}
import java.time.Instant

case class Equipment(
  equipmentId: Int = 0,
  serialNumber: String,
  typeId: Int,
  status: Status,
  createdAt: Instant
)

case class EquipmentCreateRequest(
  serialNumber: String,
  typeId: Int,
  status: Status
)

case class EquipmentUpdateRequest( status: Status)

object EquipmentModels {
  implicit val equipmentCreateRequestFormat: OFormat[EquipmentCreateRequest] =
    Json.format[EquipmentCreateRequest]
  implicit val equipmentUpdateRequestFormat: OFormat[EquipmentUpdateRequest] = Json.format[EquipmentUpdateRequest]
}

object Equipment {
  implicit val format: OFormat[Equipment] = Json.format[Equipment]
}
