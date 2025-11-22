package repository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import db.Tables._
import model.Employee

@Singleton
class EmployeeRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  // Create new employee
  def create(name: String, department: String, email: String): Future[Int] = {
    val emp = Employee(0, name, department, email)
    val insert = (employees returning employees.map(_.employeeId)) += emp
    db.run(insert)
  }


  // Get by ID
  def findById(id: Int): Future[Option[Employee]] =
    db.run(employees.filter(_.employeeId === id).result.headOption)

  //Get by Email
  def findByEmail(email: String): Future[Option[Employee]] =
    db.run(employees.filter(_.email === email).result.headOption)

  // List employees
  def list(): Future[Seq[Employee]] =
    db.run(employees.result)

  // Update
  def update(emp: Employee): Future[Int] =
    db.run(employees.filter(_.employeeId === emp.employeeId).update(emp))

  // Delete
  def delete(id: Int): Future[Int] =
    db.run(employees.filter(_.employeeId === id).delete)
}
