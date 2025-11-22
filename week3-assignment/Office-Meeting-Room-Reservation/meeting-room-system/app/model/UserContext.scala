package model

import model.Role.Role
import play.api.libs.json.{Json, OFormat}
import play.api.libs.typedmap.TypedKey

case class UserContext(userId: Long, employeeId: Int, name: String, email: String, role: Role)

object UserContext {
  implicit val userContextFormat: OFormat[UserContext] = Json.format[UserContext]
}

object UserAttr {
  val Key: TypedKey[UserContext] = TypedKey[UserContext]("user")
}
