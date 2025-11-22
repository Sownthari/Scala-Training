package repository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import db.Tables._
import model.Role.Role
import model.User

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def createUser(employeeId: Int, hashedPassword: String, role: Role): Future[Int] = {
    val user = User(
      userId = 0,
      employeeId = employeeId,
      passwordHash = hashedPassword,
      role = role,
      isActive = true
    )

    db.run(users += user)
  }

  def findByEmployeeId(empId: Int): Future[Option[User]] =
    db.run(users.filter(_.employeeId === empId).result.headOption)
}
