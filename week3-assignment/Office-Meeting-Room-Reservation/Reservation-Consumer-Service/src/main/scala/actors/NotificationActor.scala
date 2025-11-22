package actors

import akka.actor.Actor
import mail.Mailer
import models._
import java.time._
import java.time.format.DateTimeFormatter

// -------------------------------------------
// All Notify case classes
// -------------------------------------------
case class NotifyEmployeeBooking(evt: ReservationCreatedEvent)
case class NotifyRoomServiceSetup(evt: ReservationCreatedEvent)

case class NotifyEmployeeReminder(evt: MeetingReminderEvent)
case class NotifyRoomServiceReminder(evt: MeetingReminderEvent)

case class NotifyEmployeeAutoRelease(evt: AutoReleaseEvent)
case class NotifyAdminAutoRelease(evt: AutoReleaseEvent)
case class NotifyRoomServiceReset(evt: AutoReleaseEvent) // ★ Added

// -------------------------------------------
// Notification Actor
// -------------------------------------------
class NotificationActor(mailer: Mailer) extends Actor {

  implicit val ec = context.dispatcher

  // ---------- Time Formatting ----------
  private def formatTime(instant: Instant): String = {
    val zone = ZoneId.of("Asia/Kolkata") // IST
    instant.atZone(zone).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"))
  }

  // ---------- Footer ----------
  private def footer =
    """
      |<div style="margin-top:30px; padding-top:10px; border-top:1px solid #ddd; color:#555;">
      |  <p style="font-size:14px;">Regards,<br/><b>Admin Team</b></p>
      |  <p style="font-size:12px; color:#999; margin-top:15px;">
      |    This is an automated email — please do not reply.
      |  </p>
      |</div>
      |""".stripMargin

  // ---------- Wrapper ----------
  private def wrap(content: String): String =
    s"""
    <div style="
      font-family: Arial, sans-serif;
      padding: 24px;
      max-width: 600px;
      margin: auto;
      background: #ffffff;
      border: 1px solid #eee;
      border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.05);
    ">
      $content
      $footer
    </div>
    """

  // ==========================================================
  // EMAIL HANDLERS
  // ==========================================================

  override def receive: Receive = {

    // ---------------------------------------------------------
    // Employee — Booking Confirmation
    // ---------------------------------------------------------
    case NotifyEmployeeBooking(evt) =>
      val html = wrap(s"""
        <h2 style="color:#2c3e50;">Meeting Room Booking Confirmed</h2>

        <p>Hello <b>${evt.employeeName}</b>,</p>

        <p>Your meeting room has been successfully booked:</p>

        <ul style="line-height:1.6;">
          <li><b>Room:</b> ${evt.roomName}</li>
          <li><b>Floor:</b> ${evt.floor}</li>
          <li><b>Start:</b> ${formatTime(evt.startTime)}</li>
          <li><b>End:</b> ${formatTime(evt.endTime)}</li>
          <li><b>Requirements:</b> ${evt.specialRequirements.getOrElse("None")}</li>
        </ul>
      """)

      mailer.sendHtml(evt.employeeEmail, s"Booking Confirmed – ${evt.roomName}", html)

    // ---------------------------------------------------------
    // Room Service — Setup Notification
    // ---------------------------------------------------------
    case NotifyRoomServiceSetup(evt) =>
      evt.additionalInfo.flatMap(_.roomServiceMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#2980b9;">Room Setup Required</h2>

          <p>A new meeting is scheduled:</p>

          <ul style="line-height:1.6;">
            <li><b>Room:</b> ${evt.roomName}</li>
            <li><b>Floor:</b> ${evt.floor}</li>
            <li><b>Start:</b> ${formatTime(evt.startTime)}</li>
            <li><b>Requirements:</b> ${evt.specialRequirements.getOrElse("None")}</li>
          </ul>

          <p>Please prepare the room in advance.</p>
        """)

        mailer.sendHtml(mail, s"Room Setup Required – ${evt.roomName}", html)
      }

    // ---------------------------------------------------------
    // Employee — Meeting Reminder
    // ---------------------------------------------------------
    case NotifyEmployeeReminder(evt) =>
      val html = wrap(s"""
        <h2 style="color:#8e44ad;">Meeting Reminder</h2>

        <p>Hello <b>${evt.employeeName}</b>,</p>

        <p>Your meeting will begin at <b>${formatTime(evt.startTime)}</b>.</p>

        <p><b>Room:</b> ${evt.roomName} (Floor ${evt.floor})</p>
      """)

      mailer.sendHtml(evt.employeeEmail, "Upcoming Meeting Reminder", html)

    // ---------------------------------------------------------
    // Room Service — Reminder
    // ---------------------------------------------------------
    case NotifyRoomServiceReminder(evt) =>
      evt.additionalInfo.flatMap(_.roomServiceMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#16a085;">Room Service Reminder</h2>

          <p>Reminder for an upcoming meeting:</p>

          <ul style="line-height:1.6;">
            <li><b>Room:</b> ${evt.roomName}</li>
            <li><b>Start:</b> ${formatTime(evt.startTime)}</li>
            <li><b>Requirements:</b> ${evt.specialRequirements.getOrElse("None")}</li>
          </ul>

          <p>Please ensure the room is ready on time.</p>
        """)

        mailer.sendHtml(mail, s"Room Prep Reminder – ${evt.roomName}", html)
      }

    // ---------------------------------------------------------
    // Employee — Auto Release
    // ---------------------------------------------------------
    case NotifyEmployeeAutoRelease(evt) =>
      val html = wrap(s"""
        <h2 style="color:#c0392b;">Reservation Auto-Released</h2>

        <p>Hello <b>${evt.employeeName}</b>,</p>

        <p>Your reservation for <b>${evt.roomName}</b> was auto-released
        because the room was not occupied within 15 minutes.</p>

        <p><b>Released At:</b> ${formatTime(evt.autoReleasedAt)}</p>
      """)

      mailer.sendHtml(evt.employeeEmail, s"Reservation Auto-Released – ${evt.roomName}", html)

    // ---------------------------------------------------------
    // Admin — Auto Release Alert
    // ---------------------------------------------------------
    case NotifyAdminAutoRelease(evt) =>
      evt.additionalInfo.flatMap(_.adminStaffMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#d35400;">Auto-Release Alert</h2>

          <p>A reservation has been auto-released:</p>

          <ul style="line-height:1.6;">
            <li><b>Room:</b> ${evt.roomName}</li>
            <li><b>Employee:</b> ${evt.employeeName} (${evt.employeeEmail})</li>
            <li><b>Released At:</b> ${formatTime(evt.autoReleasedAt)}</li>
          </ul>
        """)

        mailer.sendHtml(mail, s"Reservation Auto-Released – ${evt.roomName}", html)
      }

    // ---------------------------------------------------------
    // NEW: Room Service — Reset Notification
    // ---------------------------------------------------------
    case NotifyRoomServiceReset(evt) =>
      evt.additionalInfo.flatMap(_.roomServiceMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#27ae60;">Room Reset Required</h2>

          <p>The meeting in <b>${evt.roomName}</b> has ended automatically.</p>

          <ul style="line-height:1.6;">
            <li><b>Room:</b> ${evt.roomName}</li>
            <li><b>Released At:</b> ${formatTime(evt.autoReleasedAt)}</li>
            <li><b>Floor:</b> ${evt.floor}</li>
          </ul>

          <p>Please reset the room for the next meeting.</p>
        """)

        mailer.sendHtml(mail, s"Room Reset Required – ${evt.roomName}", html)
      }
  }
}
