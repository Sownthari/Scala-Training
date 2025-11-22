package scheduler

import org.apache.pekko.actor.ActorSystem
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import java.time.Instant

import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import model._
import repository._
import model.EventFormats._

@Singleton
class MeetingSchedulers @Inject()(
                                   actorSystem: ActorSystem,
                                   reservationRepo: ReservationRepository,
                                   employeeRepo: EmployeeRepository,
                                   meetingRoomRepo: MeetingRoomRepository,
                                   ws: WSClient
                                 )(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  // -----------------------------------------------------------
  // Scheduler : send reminders every minute
  // -----------------------------------------------------------
  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero,
    interval = 1.minute
  )(() => sendMeetingReminders())

  // -----------------------------------------------------------
  // Scheduler : auto-release rooms every 2 mins
  // -----------------------------------------------------------
  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero,
    interval = 2.minutes
  )(() => autoReleaseRooms())


  // ===========================================================
  // 1) MEETING REMINDER JOB
  // ===========================================================
  private def sendMeetingReminders(): Unit = {
    val now = Instant.now()
    logger.info(s"[ReminderJob] Checking upcoming meetings at $now")

    reservationRepo.findUpcomingMeetings(now).foreach { meetings =>
      logger.info(s"[ReminderJob] Found ${meetings.size} meeting(s)")

      meetings.foreach { res =>
        val employeeF  = employeeRepo.findById(res.employeeId)
        val roomF      = meetingRoomRepo.findById(res.roomId)

        val combined: Future[Unit] = for {
          employeeOpt <- employeeF
          roomOpt     <- roomF
        } yield {

          (employeeOpt, roomOpt) match {

            case (Some(employee), Some(room)) =>
              logger.info(
                s"[ReminderJob] Sending reminder for reservationId=${res.reservationId}"
              )

              val event = MeetingReminderEvent(
                roomName       = room.roomName,
                floor          = room.floor.getOrElse(0),
                reservationId  = res.reservationId,
                employeeId     = employee.employeeId,
                employeeName   = employee.name,
                employeeEmail  = employee.email,
                startTime      = res.startTime,
                endTime        = res.endTime,
                specialRequirements = res.specialRequirements
              )

              ws.url("http://localhost:8082/produce-events")
                .post(Json.toJson(event))
                .map(r => logger.info(
                  s"[ReminderJob] Event sent, reservationId=${res.reservationId}, status=${r.status}"
                ))
                .recover {
                  case ex =>
                    logger.error(
                      s"[ReminderJob] WS error for reservationId=${res.reservationId}",
                      ex
                    )
                }

            case _ =>
              logger.warn(
                s"[ReminderJob] Skipping reservationId=${res.reservationId} → missing employee or room"
              )
          }
        }

        combined.recover {
          case ex => logger.error(s"[ReminderJob] Failure in reminder flow", ex)
        }
      }
    }
  }


  // ===========================================================
  // 2) AUTO-RELEASE ROOMS JOB
  // ===========================================================
  private def autoReleaseRooms(): Unit = {
    val now = Instant.now()
    logger.info(s"[AutoReleaseJob] Checking unoccupied reservations at $now")

    reservationRepo.findUnoccupiedAfterStart(now).foreach { reservations =>
      reservations.foreach { res =>

        val employeeF = employeeRepo.findById(res.employeeId)
        val roomF     = meetingRoomRepo.findById(res.roomId)

        val combined: Future[Unit] = for {
          employeeOpt <- employeeF
          roomOpt     <- roomF
        } yield {

          (employeeOpt, roomOpt) match {

            case (Some(employee), Some(room)) =>
              logger.info(
                s"[AutoReleaseJob] Auto-releasing reservationId=${res.reservationId}"
              )

              reservationRepo
                .updateStatus(res.reservationId, ReservationStatus.AUTO_RELEASED)
                .flatMap { _ =>
                  val event = AutoReleaseEvent(
                    roomName       = room.roomName,
                    floor          = room.floor.getOrElse(0),
                    reservationId  = res.reservationId,
                    employeeId     = employee.employeeId,
                    employeeName   = employee.name,
                    employeeEmail  = employee.email,
                    startTime      = res.startTime,
                    autoReleasedAt = now
                  )

                  ws.url("http://localhost:8082/produce-events")
                    .post(Json.toJson(event))
                }
                .map(_ =>
                  logger.info(
                    s"[AutoReleaseJob] Auto-release event sent for reservation ${res.reservationId}"
                  )
                )
                .recover {
                  case ex =>
                    logger.error(
                      s"[AutoReleaseJob] Failed for reservationId=${res.reservationId}",
                      ex
                    )
                }

            case _ =>
              logger.warn(
                s"[AutoReleaseJob] Skipping reservationId=${res.reservationId} → missing employee or room"
              )
          }
        }

        combined.recover {
          case ex => logger.error(s"[AutoReleaseJob] Failure in auto-release flow", ex)
        }
      }
    }
  }
}
