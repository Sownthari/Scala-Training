package model

import java.time.Instant
import play.api.libs.json.{Json, OFormat}

case class OverdueLog(
                       overdueId: Int = 0,
                       allocationId: Int,
                       notifiedAt: Instant,
                       lastReminderAt: Option[Instant]
                     )

object OverdueLog {
  implicit val format: OFormat[OverdueLog] = Json.format[OverdueLog]
}
