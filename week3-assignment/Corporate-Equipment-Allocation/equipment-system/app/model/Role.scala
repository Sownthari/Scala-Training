package model

import play.api.libs.json._

object Role extends Enumeration {
  type Role = Value
  val SYSTEM_ADMIN, RECEPTION_STAFF, INVENTORY_STAFF, MAINTENANCE_STAFF = Value

  implicit val format: Format[Role] = Json.formatEnum(this)
}
