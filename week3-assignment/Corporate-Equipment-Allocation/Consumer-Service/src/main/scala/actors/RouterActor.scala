package actors

import akka.actor.{Actor, ActorRef}
import models._

class RouterActor(
                   inventory: ActorRef,
                   notification: ActorRef // Only for employee notifications
                 ) extends Actor {

  override def receive: Receive = {

    case evt: AllocationCreatedEvent =>
      // Inventory will send inventory mail
      inventory ! evt

      // Employee mail
      notification ! NotifyEmployeeCreated(evt)

    case evt: AllocationReturnedEvent =>
      // Inventory handles GOOD/DAMAGED + maintenance decisions
      inventory ! evt

    case evt: AllocationOverdueEvent =>
      // Inventory sends inventory overdue mail
      inventory ! evt

      // Employee overdue mail
      notification ! NotifyEmployeeOverdue(evt)
  }
}
