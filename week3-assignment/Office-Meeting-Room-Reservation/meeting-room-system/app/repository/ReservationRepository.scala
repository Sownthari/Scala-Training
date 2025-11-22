package repository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import db.Tables._
import db.ColumnMappings._
import model.{Reservation, ReservationCreateRequest, ReservationError, ReservationNotFound, ReservationStatus, ReservationUpdateConflict, ReservationUpdateRequest, RoomNotAvailable, RoomNotFound, RoomStatus, RoomTimeConflict}
import model.ReservationStatus.ReservationStatus

import java.time.Instant

class ReservationRepository @Inject()(
                                       dbConfigProvider: DatabaseConfigProvider, meetingRoomRepository: MeetingRoomRepository
                                     )(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  // -----------------------------------
  // List All Reservations
  // -----------------------------------
  def listAll(): Future[Seq[Reservation]] =
    db.run(reservations.result)

  // -----------------------------------
  // Find by ID
  // -----------------------------------
  def findById(id: Long): Future[Option[Reservation]] =
    db.run(reservations.filter(_.reservationId === id).result.headOption)

  def isRoomFree(roomId: Long, start: Instant, end: Instant): Future[Boolean] =
    db.run(
      reservations
        .filter(r => r.roomId === roomId)
        .filter(r => r.status === ReservationStatus.BOOKED)
        .filter(r =>
          r.startTime < end && r.endTime > start
        )
        .exists
        .result
        .map(exists => !exists)
    )


  // -----------------------------------
  // Create Reservation
  // -----------------------------------
  def create(req: ReservationCreateRequest, userId: Long): Future[Either[ReservationError, Long]] = {
    val roomId = req.roomId

    // Entire flow must run inside a SINGLE transaction
    val action: DBIO[Either[ReservationError, Long]] = for {
      // Lock room row
      roomOpt <- meetingRooms
        .filter(_.roomId === roomId)
        .forUpdate
        .result
        .headOption

      existsCheck = if (roomOpt.isEmpty) Left(RoomNotFound) else Right(())

      result <- existsCheck match {
        case Left(err) => DBIO.successful(Left(err))

        case Right(_) =>
          for {
            //Check room available
            available <- meetingRooms
              .filter(r => r.roomId === roomId && r.status === RoomStatus.AVAILABLE)
              .exists
              .result

            availableCheck = if (!available) Left(RoomNotAvailable) else Right(())

            result2 <- availableCheck match {
              case Left(err) => DBIO.successful(Left(err))

              case Right(_) =>
                for {
                  // Check time conflict (existing BOOKED reservations)
                  conflict <- reservations
                    .filter(_.roomId === roomId)
                    .filter(_.status === ReservationStatus.BOOKED)
                    .filter(r => r.startTime < req.endTime && r.endTime > req.startTime)
                    .exists
                    .result

                  freeCheck = if (conflict) Left(RoomTimeConflict) else Right(())

                  finalResult <- freeCheck match {
                    case Left(err) => DBIO.successful(Left(err))

                    case Right(_) =>
                      (
                        (reservations returning reservations.map(_.reservationId)) +=
                          Reservation(
                            reservationId = 0,
                            roomId = req.roomId,
                            bookedBy = userId,
                            employeeId = req.employeeId,
                            startTime = req.startTime,
                            endTime = req.endTime,
                            status = ReservationStatus.BOOKED,
                            specialRequirements = req.specialRequirements,
                            isOccupied = false,
                            createdAt = Instant.now()
                          )
                        ).map(id => Right(id))
                  }
                } yield finalResult
            }
          } yield result2
      }
    } yield result

    // Run entire flow as one transaction
    db.run(action.transactionally)
  }


  // -----------------------------------
  // Update Reservation (Start, End, Status)
  // -----------------------------------
  def update(id: Long, req: ReservationUpdateRequest): Future[Either[ReservationError, Int]] = {

    val action = (for {

      // Lock the reservation row
      existingOpt <- reservations
        .filter(_.reservationId === id)
        .forUpdate
        .result
        .headOption

      // Reservation must exist
      _ <- existingOpt match {
        case None => DBIO.successful(Left(ReservationNotFound))
        case Some(_) => DBIO.successful(Right(()))
      }

      existing = existingOpt.get

      roomId = existing.roomId

      // Lock the room row as well
      roomOpt <- meetingRooms
        .filter(_.roomId === roomId)
        .forUpdate
        .result
        .headOption

      _ <- roomOpt match {
        case None => DBIO.successful(Left(RoomNotFound))
        case Some(_) => DBIO.successful(Right(()))
      }

      // Use updated values or retain old ones
      newStart = req.startTime.getOrElse(existing.startTime)
      newEnd   = req.endTime.getOrElse(existing.endTime)
      newStatus = req.status

      // Ensure room is available (unless this reservation is BOOKED already)
      available <- meetingRooms
        .filter(r => r.roomId === roomId && r.status === RoomStatus.AVAILABLE)
        .exists
        .result

      _ <- if (!available && existing.status != ReservationStatus.BOOKED)
        DBIO.successful(Left(RoomNotAvailable))
      else DBIO.successful(Right(()))

      // Check overlapping reservations, EXCLUDING this reservation
      conflict <- reservations
        .filter(_.roomId === roomId)
        .filter(_.reservationId =!= id)
        .filter(_.status === ReservationStatus.BOOKED)
        .filter(r => r.startTime < newEnd && r.endTime > newStart)
        .exists
        .result

      _ <- if (conflict)
        DBIO.successful(Left(ReservationUpdateConflict))
      else DBIO.successful(Right(()))

      // Perform update
      updatedRows <- reservations
        .filter(_.reservationId === id)
        .map(r => (r.startTime, r.endTime, r.status, r.specialRequirements))
        .update(
          (
            req.startTime.getOrElse(existing.startTime),
            req.endTime.getOrElse(existing.endTime),
            req.status,
            req.specialRequirements.orElse(existing.specialRequirements)
          )
        )
        .map(rows => Right(rows))

    } yield updatedRows).transactionally

    db.run(action)
  }


  // -----------------------------------
  // Update Status Only
  // -----------------------------------
  def updateStatus(id: Long, newStatus: ReservationStatus): Future[Int] =
    db.run(
      reservations
        .filter(_.reservationId === id)
        .map(_.status)
        .update(newStatus)
    )

  // -----------------------------------
  // Delete Reservation
  // -----------------------------------
  def delete(id: Long): Future[Int] =
    db.run(reservations.filter(_.reservationId === id).delete)

  def findUpcomingMeetings(now: Instant): Future[Seq[Reservation]] = {
    println("inside upcoming meetings")
    db.run(
      reservations
        .filter(_.status === ReservationStatus.BOOKED)
        .filter(_.startTime.between(now.plusSeconds(15 * 60), now.plusSeconds(16 * 60)))
        .result
    )
  }

  def findUnoccupiedAfterStart(now: Instant): Future[Seq[Reservation]] =
    db.run(
      reservations
        .filter(_.status === ReservationStatus.BOOKED)
        .filter(_.startTime < now.minusSeconds(15 * 60))
        .filter(_.isOccupied === false)
        .result
    )


}
