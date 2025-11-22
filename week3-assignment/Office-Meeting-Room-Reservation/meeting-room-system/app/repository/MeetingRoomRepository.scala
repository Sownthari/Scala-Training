package repository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import db.Tables._
import db.ColumnMappings._
import model.{MeetingRoom, RoomStatus}
import model.RoomStatus.RoomStatus

class MeetingRoomRepository @Inject() (
                                        dbConfigProvider: DatabaseConfigProvider
                                      )(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  // -----------------------------------
  // Find All
  // -----------------------------------
  def listAll(): Future[Seq[MeetingRoom]] =
    db.run(meetingRooms.result)

  // -----------------------------------
  // Find By ID
  // -----------------------------------
  def findById(id: Long): Future[Option[MeetingRoom]] =
    db.run(meetingRooms.filter(_.roomId === id).result.headOption)

  // -----------------------------------
  // Find By Unique room name
  // -----------------------------------
  def findByName(name: String): Future[Option[MeetingRoom]] =
    db.run(meetingRooms.filter(_.roomName === name).result.headOption)

  def roomExists(roomId: Long): Future[Boolean] =
    db.run(meetingRooms.filter(_.roomId === roomId).exists.result)

  def isRoomAvailable(roomId: Long): Future[Boolean] =
    db.run(
      meetingRooms
        .filter(r => r.roomId === roomId && r.status === RoomStatus.AVAILABLE)
        .exists
        .result
    )


  // -----------------------------------
  // Create
  // -----------------------------------
  def create(
              roomName: String,
              floor: Option[Int],
              capacity: Option[Int],
              hasProjector: Boolean,
              hasWhiteboard: Boolean,
              status: RoomStatus
            ): Future[Long] = {
    val room = MeetingRoom(
      0,
      roomName,
      floor,
      capacity,
      hasProjector,
      hasWhiteboard,
      status
    )

    db.run((meetingRooms returning meetingRooms.map(_.roomId)) += room)
  }

  // -----------------------------------
  // Update
  // -----------------------------------
  def update(
              id: Long,
              floor: Option[Int],
              capacity: Option[Int],
              hasProjector: Boolean,
              hasWhiteboard: Boolean,
              status: RoomStatus
            ): Future[Int] =
    db.run(
      meetingRooms
        .filter(_.roomId === id)
        .map(r => (r.floor, r.capacity, r.hasProjector, r.hasWhiteboard, r.status))
        .update((floor, capacity, hasProjector, hasWhiteboard, status))
    )

  // -----------------------------------
  // Delete
  // -----------------------------------
  def delete(id: Long): Future[Int] =
    db.run(meetingRooms.filter(_.roomId === id).delete)
}
