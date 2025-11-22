package actors

import akka.actor.{Actor, ActorRef}
import models._

class InventoryActor(
                      notificationActor: ActorRef,
                      maintenanceActor: ActorRef
                    ) extends Actor {

  def receive: Receive = {

    case evt: AllocationCreatedEvent =>
      println(s"[InventoryActor] Allocation created: $evt")
      notificationActor ! NotifyInventoryAllocation(evt)

    case evt: AllocationReturnedEvent if evt.condition.equalsIgnoreCase("GOOD") =>
      println(s"[InventoryActor] Returned GOOD: $evt")
      notificationActor ! NotifyInventoryReturnedGood(evt)

    case evt: AllocationReturnedEvent if evt.condition.equalsIgnoreCase("DAMAGED") =>
      println(s"[InventoryActor] Returned DAMAGED: $evt")
      // NEW: send maintenance notification
      maintenanceActor ! NotifyMaintenanceDamaged(evt)

    case evt: AllocationOverdueEvent =>
      println(s"[InventoryActor] Overdue: $evt")
      notificationActor ! NotifyInventoryOverdue(evt)
  }
}
