package model

import model.Role.Role
import play.api.libs.json.{Json, OFormat}

case class Employee(employeeId: Int = 0, name: String, department: String, email: String)

case class EmployeeCreateRequest(name: String, email: String, department: String)
case class UserCreateRequest(email: String, password: String, role: Role)

object EmployeeModels {
  implicit val employeeCreateRequestFormat: OFormat[EmployeeCreateRequest] = Json.format[EmployeeCreateRequest]
  implicit val userCreateRequestFormat: OFormat[UserCreateRequest] = Json.format[UserCreateRequest]

}

object Employee {
  implicit val format: OFormat[Employee] = Json.format[Employee]
}
