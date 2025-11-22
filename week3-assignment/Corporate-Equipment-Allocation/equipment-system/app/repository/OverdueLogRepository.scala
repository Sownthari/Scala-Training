package repository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider

import db.Tables._
import model.OverdueLog
import java.time.Instant

@Singleton
class OverdueLogRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def findLog(allocationId: Int): Future[Option[OverdueLog]] = {
    db.run(
      overdueLogs.filter(_.allocationId === allocationId).result.headOption
    ).map { res =>
      res
    }
  }

  def findAll(): Future[Seq[OverdueLog]] =
    db.run(overdueLogs.result)

  def createInitial(allocationId: Int, now: Instant): Future[Int] = {
    db.run(overdueLogs.map(l => (l.allocationId, l.notifiedAt, l.lastReminderAt))
      .returning(overdueLogs.map(_.overdueId)) += (allocationId, now, Some(now)))
  }

  def create(allocationId: Int): Future[Int] = {
    val log = OverdueLog(
      overdueId = 0,
      allocationId = allocationId,
      notifiedAt = Instant.now(),
      lastReminderAt = Some(Instant.now())
    )

    db.run((overdueLogs returning overdueLogs.map(_.overdueId)) += log)
  }

  def updateReminder(allocationId: Int, now: Instant): Future[Int] =
    db.run(
      overdueLogs
        .filter(_.allocationId === allocationId)
        .map(_.lastReminderAt)
        .update(Some(now))
    )

}
