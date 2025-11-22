package model

import play.api.libs.json._

object Status extends Enumeration {
  type Status = Value
  val AVAILABLE, ALLOCATED, UNDER_MAINTENANCE, ON_HOLD = Value

  implicit val format: Format[Status] = Json.formatEnum(this)
}
