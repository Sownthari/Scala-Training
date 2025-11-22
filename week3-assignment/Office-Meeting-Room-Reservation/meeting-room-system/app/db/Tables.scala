package db

import model.ReservationStatus.ReservationStatus
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.JdbcType
import model.Role.Role
import model.{Employee, MeetingRoom, Reservation, ReservationStatus, Role, RoomStatus, User}
import model.RoomStatus.RoomStatus

import java.sql.Timestamp
import java.time.Instant

// ----------------------------------------
// Column Mappings (Implicits)
// ----------------------------------------
object ColumnMappings {

  implicit val instantColumnType: JdbcType[Instant] =
    MappedColumnType.base[Instant, Timestamp](
      inst => Timestamp.from(inst),
      ts => ts.toInstant
    )

  implicit val statusColumnType: JdbcType[RoomStatus] =
    MappedColumnType.base[RoomStatus, String](
      _.toString,
      RoomStatus.withName
    )

  implicit val roleColumnType: JdbcType[Role] =
    MappedColumnType.base[Role, String](
      _.toString,
      Role.withName
    )

  implicit val reservationStatusColumnType : JdbcType[ReservationStatus] =
    MappedColumnType.base[ReservationStatus, String](
      _.toString,
      ReservationStatus.withName
    )
}

object Tables {

  import ColumnMappings._

  // --------------------
  // Users
  // --------------------
  class UsersTable(tag: Tag)
    extends Table[User](tag, "reservation_users") {

    def userId       = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
    def employeeId   = column[Int]("employee_id")
    def passwordHash = column[String]("passwordhash")
    def role         = column[Role]("role")
    def isActive     = column[Boolean]("is_active")

    def * =
      (userId, employeeId, passwordHash, role, isActive)
        .<>((User.apply _).tupled, User.unapply)

    def employeeFK =
      foreignKey("fk_user_employee", employeeId, employees)(_.employeeId)
  }


  // --------------------
  // Employees
  // --------------------
  class EmployeesTable(tag: Tag)
    extends Table[Employee](tag, "reservation_employees") {

    def employeeId = column[Int]("employee_id", O.PrimaryKey, O.AutoInc)
    def name       = column[String]("name")
    def department = column[String]("department")
    def email      = column[String]("email")

    def * =
      (employeeId, name, department, email)
        .<>((Employee.apply _).tupled, Employee.unapply)
  }


  // --------------------
  // Meeting Rooms
  // --------------------
  class MeetingRoomTable(tag: Tag)
    extends Table[MeetingRoom](tag, "reservation_meeting_rooms") {

    def roomId        = column[Long]("room_id", O.PrimaryKey, O.AutoInc)
    def roomName      = column[String]("room_name", O.Unique)
    def floor         = column[Option[Int]]("floor")
    def capacity      = column[Option[Int]]("capacity")
    def hasProjector  = column[Boolean]("has_projector")
    def hasWhiteboard = column[Boolean]("has_whiteboard")
    def status        = column[RoomStatus]("status")

    def * =
      (
        roomId,
        roomName,
        floor,
        capacity,
        hasProjector,
        hasWhiteboard,
        status
      ) <> ((MeetingRoom.apply _).tupled, MeetingRoom.unapply)
  }

  // --------------------
  // Reservations
  // --------------------
  class ReservationsTable(tag: Tag)
    extends Table[Reservation](tag, "reservation_reservations") {

    def reservationId       = column[Long]("reservation_id", O.PrimaryKey, O.AutoInc)
    def roomId              = column[Long]("room_id")
    def bookedBy            = column[Long]("booked_by")
    def employeeId          = column[Int]("employee_id")
    def startTime           = column[Instant]("start_time")
    def endTime             = column[Instant]("end_time")
    def status              = column[ReservationStatus]("status")
    def specialRequirements = column[Option[String]]("special_requirements")
    def isOccupied          = column[Boolean]("is_occupied")
    def createdAt           = column[Instant]("created_at")

    def * = (
        reservationId,
        roomId,
        bookedBy,
        employeeId,
        startTime,
        endTime,
        status,
        specialRequirements,
        isOccupied,
        createdAt
      ) <> ((Reservation.apply _).tupled, Reservation.unapply)
  }


  // --------------------
  // TableQuery objects
  // --------------------
  val users        = TableQuery[UsersTable]
  val employees    = TableQuery[EmployeesTable]
  val meetingRooms = TableQuery[MeetingRoomTable]
  val reservations  = TableQuery[ReservationsTable]
}
