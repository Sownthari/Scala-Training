package actors

import akka.actor.{Actor, ActorRef}
import models._

class RouterActor(
                              employeeActor: ActorRef,
                              roomServiceActor: ActorRef,
                              adminStaffActor: ActorRef,
                            ) extends Actor {

  override def receive: Receive = {

    // ---------------------------------------------------------
    // Reservation Created
    // ---------------------------------------------------------
    case evt: ReservationCreatedEvent =>
      // Domain actors decide if/what to mail
      employeeActor ! evt
      roomServiceActor ! evt



    // ---------------------------------------------------------
    // Meeting Reminder
    // ---------------------------------------------------------
    case evt: MeetingReminderEvent =>
      employeeActor ! evt
      roomServiceActor ! evt



    // ---------------------------------------------------------
    // Auto Release
    // ---------------------------------------------------------
    case evt: AutoReleaseEvent =>
      employeeActor ! evt
      roomServiceActor ! evt
      adminStaffActor ! evt



    // ---------------------------------------------------------
    // Unknown
    // ---------------------------------------------------------
    case _ =>
    // ignore
  }
}
