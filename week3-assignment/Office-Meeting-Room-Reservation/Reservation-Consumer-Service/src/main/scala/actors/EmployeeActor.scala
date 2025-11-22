package actors

import akka.actor.{Actor, ActorRef}
import models._

class EmployeeActor(notificationActor: ActorRef) extends Actor {

  override def receive: Receive = {

    // 1) Booking confirmation email to employee
    case evt: ReservationCreatedEvent =>
      println(s"[EmployeeActor] Booking event: $evt")
      notificationActor ! NotifyEmployeeBooking(evt)

    // 2) Reminder email before meeting
    case evt: MeetingReminderEvent =>
      println(s"[EmployeeActor] Reminder event: $evt")
      notificationActor ! NotifyEmployeeReminder(evt)

    // 3) Auto-release email
    case evt: AutoReleaseEvent =>
      println(s"[EmployeeActor] Auto release: $evt")
      notificationActor ! NotifyEmployeeAutoRelease(evt)
  }
}
