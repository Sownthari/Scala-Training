package model.errors

sealed trait AllocationError

case object EmployeeNotFound extends AllocationError
case object EquipmentNotFound extends AllocationError
case class EquipmentNotAvailable(id: Int) extends AllocationError
case object AllocationNotFound extends AllocationError
case object AlreadyReturned extends AllocationError
