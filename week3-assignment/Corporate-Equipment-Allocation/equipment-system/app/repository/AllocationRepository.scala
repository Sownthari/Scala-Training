package repository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider

import db.Tables._
import db.ColumnMappings._
import model.{Allocation, AllocationCreateRequest, AllocationReturnRequest}
import model.Status
import model.errors._
import model.Condition

import java.time.Instant

@Singleton
class AllocationRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, employeeRepo: EmployeeRepository, equipmentRepo: EquipmentRepository)
                                    (implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def findAll(): Future[Seq[Allocation]] =
    db.run(allocations.result)

  def findById(id: Int): Future[Option[Allocation]] =
    db.run(allocations.filter(_.allocationId === id).result.headOption)

  def create(req: AllocationCreateRequest): Future[Either[AllocationError, Int]] = {
    val employeeF  = employeeRepo.findById(req.employeeId)
    val equipmentF = equipmentRepo.findById(req.equipmentId)

    for {
      employeeOpt  <- employeeF
      equipmentOpt <- equipmentF
      result <- (employeeOpt, equipmentOpt) match {

        case (None, _) =>
          Future.successful(Left(EmployeeNotFound))

        case (_, None) =>
          Future.successful(Left(EquipmentNotFound))

        case (_, Some(eq)) if eq.status != Status.AVAILABLE =>
          Future.successful(Left(EquipmentNotAvailable(eq.equipmentId)))

        case (Some(emp), Some(eq)) =>
          val record = Allocation(
            allocationId = 0,
            equipmentId = eq.equipmentId,
            employeeId = emp.employeeId,
            allocatedAt = Instant.now(),
            expectedReturnAt = req.expectedReturnAt,
            returnedAt = None,
            conditionOnReturn = None
          )

          val action = for {
            id <- (allocations returning allocations.map(_.allocationId)) += record
            _  <- equipment.filter(_.equipmentId === req.equipmentId)
              .map(_.status).update(Status.ALLOCATED)
          } yield id

          db.run(action.transactionally).map(Right(_))
      }
    } yield result
  }
  def markReturned(
                    id: Int,
                    req: AllocationReturnRequest,
                    returnedAt: Instant
                  ): Future[Either[AllocationError, Allocation]] = {

    findById(id).flatMap {
      case None =>
        Future.successful(Left(AllocationNotFound))

      case Some(allocation) =>

        if (allocation.returnedAt.isDefined)
          Future.successful(Left(AlreadyReturned))
        else {

          val equipmentId = allocation.equipmentId

          val isDamaged = req.conditionOnReturn == Condition.DAMAGED
          val isGood    = req.conditionOnReturn == Condition.GOOD

          val allocationQ = allocations.filter(_.allocationId === id)
          val equipmentQ  = equipment.filter(_.equipmentId === equipmentId)

          val statusUpdate =
            if (isDamaged)
              equipmentQ.map(_.status).update(Status.ON_HOLD)
            else if (isGood)
              equipmentQ.map(_.status).update(Status.AVAILABLE)
            else
              DBIO.successful(1)

          val action = (for {
            _ <- allocationQ.map(_.returnedAt).update(Some(returnedAt))
            _ <- allocationQ.map(_.conditionOnReturn).update(Some(req.conditionOnReturn))
            _ <- statusUpdate
          } yield ()).transactionally

          db.run(action).map(_ => Right(allocation))
        }
    }
  }

  def findOverdue(now: Instant): Future[Seq[Allocation]] = {
    println("hello")
    db.run(
      allocations
        .filter(a => a.returnedAt.isEmpty && a.expectedReturnAt < now)
        .result
    )
  }



  def delete(id: Int): Future[Int] =
    db.run(allocations.filter(_.allocationId === id).delete)
}
