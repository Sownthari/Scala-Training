package db

import slick.jdbc.MySQLProfile.api._
import slick.jdbc.JdbcType

import java.sql.Timestamp
import java.time.Instant

import model._
import model.Role.Role
import model.Status.Status
import model.Condition.Condition


// ----------------------------------------
// Column Mappings (Implicits)
// ----------------------------------------
object ColumnMappings {

  implicit val instantColumnType: JdbcType[Instant] =
    MappedColumnType.base[Instant, Timestamp](
      inst => Timestamp.from(inst),
      ts => ts.toInstant
    )

  implicit val statusColumnType: JdbcType[Status] =
    MappedColumnType.base[Status, String](
      _.toString,
      Status.withName
    )

  implicit val roleColumnType: JdbcType[Role] =
    MappedColumnType.base[Role, String](
      _.toString,
      Role.withName
    )

  implicit val conditionColumnType: JdbcType[Condition] =
    MappedColumnType.base[Condition, String](
      _.toString,
      Condition.withName
    )
}


// ----------------------------------------
// Tables
// ----------------------------------------
object Tables {

  // import all implicit column types for all tables
  import ColumnMappings._


  // --------------------
  // Users
  // --------------------
  class UsersTable(tag: Tag)
    extends Table[User](tag, "users") {

    def userId       = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
    def employeeId   = column[Int]("employee_id")
    def passwordHash = column[String]("passwordhash")
    def role         = column[Role]("role")
    def isActive     = column[Boolean]("is_active")

    def * =
      (userId, employeeId, passwordHash, role, isActive)
        .<>((User.apply _).tupled, User.unapply)
  }


  // --------------------
  // Employees
  // --------------------
  class EmployeesTable(tag: Tag)
    extends Table[Employee](tag, "employees") {

    def employeeId = column[Int]("employee_id", O.PrimaryKey, O.AutoInc)
    def name       = column[String]("name")
    def department = column[String]("department")
    def email      = column[String]("email")

    def * =
      (employeeId, name, department, email)
        .<>((Employee.apply _).tupled, Employee.unapply)
  }


  // --------------------
  // Equipment Types
  // --------------------
  class EquipmentTypeTable(tag: Tag)
    extends Table[EquipmentType](tag, "equipment_types") {

    def typeId   = column[Int]("type_id", O.PrimaryKey, O.AutoInc)
    def typeName = column[String]("type_name", O.Unique)

    def * =
      (typeId, typeName)
        .<>((EquipmentType.apply _).tupled, EquipmentType.unapply)
  }


  // --------------------
  // Equipment
  // --------------------
  class EquipmentTable(tag: Tag)
    extends Table[Equipment](tag, "equipment_items") {

    def equipmentId  = column[Int]("equipment_id", O.PrimaryKey, O.AutoInc)
    def serialNumber = column[String]("serial_number")
    def typeId       = column[Int]("type_id")
    def status       = column[Status]("status")
    def createdAt    = column[Instant]("created_at")

    def equipmentTypeFK =
      foreignKey("fk_type_id", typeId, equipmentTypes)(_.typeId)

    def * =
      (equipmentId, serialNumber, typeId, status, createdAt)
        .<>((Equipment.apply _).tupled, Equipment.unapply)
  }


  // --------------------
  // Equipment Allocations
  // --------------------
  class AllocationTable(tag: Tag)
    extends Table[Allocation](tag, "equipment_allocations") {

    def allocationId      = column[Int]("allocation_id", O.PrimaryKey, O.AutoInc)
    def equipmentId       = column[Int]("equipment_id")
    def employeeId        = column[Int]("employee_id")
    def allocatedAt       = column[Instant]("allocated_at")
    def expectedReturnAt  = column[Instant]("expected_return_at")
    def returnedAt        = column[Option[Instant]]("returned_at")
    def conditionOnReturn = column[Option[Condition]]("condition_on_return")

    def equipmentFK =
      foreignKey("fk_eq_alloc_eq", equipmentId, equipment)(_.equipmentId)

    def employeeFK =
      foreignKey("fk_eq_alloc_emp", employeeId, employees)(_.employeeId)

    def * =
      (
        allocationId,
        equipmentId,
        employeeId,
        allocatedAt,
        expectedReturnAt,
        returnedAt,
        conditionOnReturn
      ) <> ((Allocation.apply _).tupled, Allocation.unapply)
  }


  // --------------------
  // Overdue Logs
  // --------------------
  class OverdueLogsTable(tag: Tag)
    extends Table[OverdueLog](tag, "overdue_logs") {

    def overdueId    = column[Int]("overdue_id", O.PrimaryKey, O.AutoInc)
    def allocationId = column[Int]("allocation_id")
    def notifiedAt   = column[Instant]("notified_at")
    def lastReminderAt = column[Option[Instant]]("last_reminder_at")

    def allocationFK =
      foreignKey("fk_overdue_allocation", allocationId, allocations)(_.allocationId)

    def * =
      (overdueId, allocationId, notifiedAt, lastReminderAt)
        .<>((OverdueLog.apply _).tupled, OverdueLog.unapply)
  }

  // ----------------------------------------
  // TableQuery objects
  // ----------------------------------------
  val users              = TableQuery[UsersTable]
  val employees          = TableQuery[EmployeesTable]
  val equipmentTypes     = TableQuery[EquipmentTypeTable]
  val equipment          = TableQuery[EquipmentTable]
  val allocations        = TableQuery[AllocationTable]
  val overdueLogs        = TableQuery[OverdueLogsTable]
}
