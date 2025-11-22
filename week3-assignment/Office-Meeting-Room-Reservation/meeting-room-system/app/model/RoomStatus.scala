package model

import play.api.libs.json.{Format, Json}

object RoomStatus extends Enumeration {
  type RoomStatus = Value
  val AVAILABLE, UNAVAILABLE = Value

  implicit val roomStatusFormat: Format[RoomStatus] =
    Json.formatEnum(RoomStatus)
}
