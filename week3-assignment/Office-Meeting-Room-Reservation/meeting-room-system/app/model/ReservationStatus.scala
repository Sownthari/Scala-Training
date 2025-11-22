package model

import play.api.libs.json.{Format, Json}

object ReservationStatus extends Enumeration {
  type ReservationStatus = Value
  val BOOKED, CANCELLED, COMPLETED, AUTO_RELEASED = Value

  implicit val roomStatusFormat: Format[ReservationStatus] =
    Json.formatEnum(ReservationStatus)
}
