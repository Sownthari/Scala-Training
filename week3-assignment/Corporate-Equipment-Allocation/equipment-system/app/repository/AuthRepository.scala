package repository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import db.Tables
import model.UserContext

import com.github.t3hnar.bcrypt._

@Singleton
class AuthRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  import Tables._

  /** Validate email & password, return enriched UserContext */
  def authenticate(email: String, plainPassword: String): Future[Option[UserContext]] = {

    val joinQuery =
      users
        .join(employees).on(_.employeeId === _.employeeId)
        .filter { case (_, emp) => emp.email === email }

    db.run(joinQuery.result.headOption).map {
      case Some((user, emp)) if user.isActive && plainPassword.isBcryptedBounded(user.passwordHash) =>
        Some(
          UserContext(
            userId = user.userId,
            employeeId = emp.employeeId,
            name = emp.name,
            email = emp.email,
            role = user.role
          )
        )

      case _ =>
        None
    }
  }
  def hashPassword(plain: String): String =
    plain.boundedBcrypt

}
