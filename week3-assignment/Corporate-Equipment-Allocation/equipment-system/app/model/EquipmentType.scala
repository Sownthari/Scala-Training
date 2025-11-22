package model

case class EquipmentType(typeId: Int = 0, typeName: String)

object EquipmentType {
  import play.api.libs.json.{Json, OFormat}
  implicit val equipmentTypeFormat: OFormat[EquipmentType] = Json.format[EquipmentType]
}


