package model

import model.Role.Role
import play.api.libs.json.{Json, OFormat}

case class UserContext(userId: Long, employeeId: Int, name: String, email: String, role: Role)

object UserContext {
  implicit val userContextFormat: OFormat[UserContext] = Json.format[UserContext]
}
