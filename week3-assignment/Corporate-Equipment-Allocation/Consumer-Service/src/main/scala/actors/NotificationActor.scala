package actors

import akka.actor.Actor
import mail.Mailer
import models._
import java.time._
import java.time.format.DateTimeFormatter

case class NotifyEmployeeCreated(evt: AllocationCreatedEvent)
case class NotifyEmployeeOverdue(evt: AllocationOverdueEvent)

case class NotifyInventoryAllocation(evt: AllocationCreatedEvent)
case class NotifyInventoryReturnedGood(evt: AllocationReturnedEvent)
case class NotifyInventoryReturnedDamaged(evt: AllocationReturnedEvent)
case class NotifyInventoryOverdue(evt: AllocationOverdueEvent)

case class NotifyMaintenanceDamaged(evt: NotifyMaintenanceDamaged)

class NotificationActor(mailer: Mailer) extends Actor {

  implicit val ec = context.dispatcher

  // ---------- Time Formatting ----------
  private def formatTime(instant: Instant): String = {
    val zone = ZoneId.of("Asia/Kolkata")  // IST
    val dt = instant.atZone(zone)
    dt.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"))
  }

  // ---------- Footer ----------
  private def footer =
    """
      |<div style="margin-top:30px; padding-top:10px; border-top:1px solid #ddd; color:#555;">
      |  <p style="font-size:14px;">Regards,<br/>
      |  <b style="color:#2c3e50;">Admin Team</b></p>
      |
      |  <p style="font-size:12px; color:#999; margin-top:20px;">
      |    This is an automated message. Please do not reply.
      |  </p>
      |</div>
      |""".stripMargin

  // ---------- Wrapper for all mails ----------
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

  // ---------- Actor Messages ----------
  def receive: Receive = {

    // EMPLOYEE: Allocation Created
    case NotifyEmployeeCreated(evt) =>
      val html = wrap(s"""
        <h2 style="color:#2c3e50;">Equipment Allocation Created</h2>

        <p>Hello <b>${evt.employeeName}</b>,</p>

        <p>Your equipment has been allocated with the following details:</p>
        <ul style="line-height:1.6;">
          <li><b>Type:</b> ${evt.equipmentType}</li>
          <li><b>Serial:</b> ${evt.equipmentSerial}</li>
          <li><b>Allocated At:</b> ${formatTime(evt.allocatedAt)}</li>
        </ul>
      """)

      mailer.sendHtml(evt.employeeEmail, s"Allocation Created - ${evt.equipmentType}", html)

    // EMPLOYEE: Overdue
    case NotifyEmployeeOverdue(evt) =>
      val html = wrap(s"""
        <h2 style="color:#c0392b;">Equipment Overdue</h2>

        <p>Hello <b>${evt.employeeName}</b>,</p>

        <p>Your equipment <b>${evt.equipmentSerial}</b> is overdue by
        <b>${evt.overdueByDays} days</b>.</p>

        <p><b>Due Date:</b> ${formatTime(evt.overdueAt)}</p>
        <p>Please return it as soon as possible.</p>
      """)

      mailer.sendHtml(evt.employeeEmail, "Equipment Overdue", html)

    // INVENTORY: Allocation Notification
    case NotifyInventoryAllocation(evt) =>
      evt.additionalInfo.flatMap(_.inventoryMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#2c3e50;">New Allocation</h2>

          <p>Equipment has been allocated:</p>
          <ul style="line-height:1.6;">
            <li><b>Type:</b> ${evt.equipmentType}</li>
            <li><b>Serial:</b> ${evt.equipmentSerial}</li>
            <li><b>Employee:</b> ${evt.employeeName} (ID: ${evt.employeeId})</li>
            <li><b>Allocated At:</b> ${formatTime(evt.allocatedAt)}</li>
          </ul>
        """)

        mailer.sendHtml(mail, s"Inventory Allocation - ${evt.equipmentSerial}", html)
      }

    // INVENTORY: Returned GOOD
    case NotifyInventoryReturnedGood(evt) =>
      evt.additionalInfo.flatMap(_.inventoryMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#27ae60;">Returned - GOOD Condition</h2>

          <p>The equipment <b>${evt.equipmentSerial}</b> was returned in good condition.</p>
          <p><b>Returned At:</b> ${formatTime(evt.returnedAt)}</p>
        """)

        mailer.sendHtml(mail, s"Returned GOOD - ${evt.equipmentSerial}", html)
      }

    // INVENTORY: Returned DAMAGED
    case NotifyInventoryReturnedDamaged(evt) =>
      evt.additionalInfo.flatMap(_.inventoryMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#e67e22;">Returned - DAMAGED</h2>

          <p>The equipment <b>${evt.equipmentSerial}</b> was returned damaged.</p>
          <p><b>Notes:</b> ${evt.notes.getOrElse("No details")}</p>
          <p><b>Returned At:</b> ${formatTime(evt.returnedAt)}</p>
        """)

        mailer.sendHtml(mail, s"Returned DAMAGED - ${evt.equipmentSerial}", html)
      }

    // INVENTORY: Overdue
    case NotifyInventoryOverdue(evt) =>
      evt.additionalInfo.flatMap(_.inventoryMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#c0392b;">Inventory Overdue</h2>

          <p>Equipment <b>${evt.equipmentSerial}</b> is overdue.</p>
          <p><b>Overdue by:</b> ${evt.overdueByDays} days</p>
          <p><b>Due Date:</b> ${formatTime(evt.overdueAt)}</p>
        """)

        mailer.sendHtml(mail, s"Inventory OVERDUE - ${evt.equipmentSerial}", html)
      }

    // MAINTENANCE
    case NotifyMaintenanceDamaged(evt) =>
      evt.additionalInfo.flatMap(_.maintenanceMail).foreach { mail =>
        val html = wrap(s"""
          <h2 style="color:#d35400;">Maintenance Required</h2>

          <p>The equipment <b>${evt.equipmentSerial}</b> requires maintenance.</p>
          <p><b>Notes:</b> ${evt.notes.getOrElse("None")}</p>
          <p><b>Returned At:</b> ${formatTime(evt.returnedAt)}</p>
        """)

        mailer.sendHtml(mail, s"Maintenance Required – ${evt.equipmentSerial}", html)
      }
  }
}
