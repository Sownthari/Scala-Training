package actors

import akka.actor.{Actor, ActorRef}
import models._

class RoomServiceActor(notificationActor: ActorRef) extends Actor {

  override def receive: Receive = {

    // 1) Room preparation instruction (chairs, cleaning, setup)
    case evt: ReservationCreatedEvent =>
      println(s"[RoomServiceActor] Setup event: $evt")
      notificationActor ! NotifyRoomServiceSetup(evt)

    // 2) 15-minute reminder for room preparation
    case evt: MeetingReminderEvent =>
      println(s"[RoomServiceActor] Preparation reminder: $evt")
      notificationActor ! NotifyRoomServiceReminder(evt)

    case evt: AutoReleaseEvent =>
      println(s"[RoomServiceActor] Reset reminder: $evt")
      notificationActor ! NotifyRoomServiceReset(evt)
  }
}
