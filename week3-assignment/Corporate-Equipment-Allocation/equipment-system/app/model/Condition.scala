package model

import play.api.libs.json._

object Condition extends Enumeration {
  type Condition = Value
  val GOOD, DAMAGED = Value

  implicit val format: Format[Condition] = Json.formatEnum(this)
}
