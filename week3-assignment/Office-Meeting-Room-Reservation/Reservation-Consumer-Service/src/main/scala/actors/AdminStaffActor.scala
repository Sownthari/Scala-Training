package actors

import akka.actor.{Actor, ActorRef}
import models._

class AdminStaffActor(notificationActor: ActorRef) extends Actor {

  override def receive: Receive = {

    // Auto-release escalation email
    case evt: AutoReleaseEvent =>
      println(s"[AdminStaffActor] Auto release escalation: $evt")
      notificationActor ! NotifyAdminAutoRelease(evt)
  }
}
