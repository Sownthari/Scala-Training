package model

import model.Role.Role
import play.api.libs.json.{Json, OFormat}

case class User(userId: Long=0, employeeId: Int, passwordHash: String, role: Role, isActive: Boolean)

case class LoginRequest(email: String, password: String)

case class LoginResponse(token: String)

case class ErrorResponse(error: String)

object AuthModels {
  implicit val loginRequestFormat: OFormat[LoginRequest] = Json.format[LoginRequest]
  implicit val loginResponseFormat: OFormat[LoginResponse] = Json.format[LoginResponse]
  implicit val ErrorResponseFormat:  OFormat[ErrorResponse] = Json.format[ErrorResponse]
}

object User {
  implicit val format: OFormat[User] = Json.format[User]
}