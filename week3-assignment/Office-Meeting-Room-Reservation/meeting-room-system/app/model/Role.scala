package model

import play.api.libs.json._

object Role extends Enumeration {
  type Role = Value
  val SYSTEM_ADMIN, ADMIN_STAFF, ROOM_SERVICE = Value

  implicit val format: Format[Role] = Json.formatEnum(this)
}
