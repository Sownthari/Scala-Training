package scheduler

import org.apache.pekko.actor.ActorSystem

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import model._
import repository._
import model.EventFormats._

import java.time.{Duration => JDuration}
import scala.concurrent.duration._


@Singleton
class OverdueScheduler @Inject()(
                                  actorSystem: ActorSystem,
                                  repo: AllocationRepository,
                                  employeeRepo: EmployeeRepository,
                                  equipmentRepo: EquipmentRepository,
                                  ws: WSClient,
                                  overdueRepo: OverdueLogRepository,
                                )(implicit ec: ExecutionContext) {

  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero,
    interval = 5.minutes
  ) { () =>
    checkOverdue()
  }

  // ======================================================
  // MAIN SCHEDULER LOGIC
  // ======================================================
  private def checkOverdue(): Unit = {
    val now = Instant.now()
    val reminderGap = JDuration.ofHours(24)

    repo.findOverdue(now).flatMap { overdueList =>
      println(overdueList)

      val tasks: Seq[Future[Unit]] = overdueList.map { alloc =>
        overdueRepo.findLog(alloc.allocationId).flatMap {

          // ---------- FIRST TIME ----------
          case None =>
            println(s"First reminder for ${alloc.allocationId}")

            for {
              _ <- overdueRepo.createInitial(alloc.allocationId, now)
              _ <- sendOverdueEvent(alloc, now)
            } yield ()

          // ---------- ALREADY EXISTS ----------
          case Some(log) =>
            val last = log.lastReminderAt.getOrElse(log.notifiedAt)
            val shouldRemind =
              JDuration.between(last, now).compareTo(reminderGap) >= 0

            if (shouldRemind) {
              println(s"Sending reminder again for ${alloc.allocationId}")

              for {
                _ <- overdueRepo.updateReminder(alloc.allocationId, now)
                _ <- sendOverdueEvent(alloc, now)
              } yield ()
            } else {
              Future.successful(())
            }
        }
      }

      Future.sequence(tasks)
    }.recover { case ex =>
      println("Error in checkOverdue: " + ex.getMessage)
    }
  }


  // ======================================================
  // SEND EVENT (FIXED: MUST RETURN FUTURE)
  // ======================================================
  private def sendOverdueEvent(allocation: Allocation, now: Instant): Future[Unit] = {

    val overdueDays =
      java.time.Duration.between(allocation.expectedReturnAt, now).toDays

    for {
      employee <- employeeRepo.findById(allocation.employeeId)
      equipment <- equipmentRepo.findById(allocation.equipmentId)
      equipmentType <- equipmentRepo.getEquipmentTypeById(equipment.get.typeId)
      _ <- {
        val event = AllocationOverdueEvent(
          equipmentType = equipmentType.get.typeName,
          equipmentSerial = equipment.get.serialNumber,
          employeeId = employee.get.employeeId,
          employeeName = employee.get.name,
          employeeEmail = employee.get.email,
          overdueByDays = overdueDays,
          overdueAt = allocation.expectedReturnAt,
        )

        ws.url("http://localhost:8082/produce-events")
          .post(Json.toJson(event))
          .map(_ => ())
      }
    } yield ()
  }
}
