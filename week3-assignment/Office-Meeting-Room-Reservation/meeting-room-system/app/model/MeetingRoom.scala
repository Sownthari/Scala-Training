package model

import model.RoomStatus.RoomStatus
import play.api.libs.json.{Json, OFormat}

case class MeetingRoom(
                        roomId: Long = 0,
                        roomName: String,
                        floor: Option[Int],
                        capacity: Option[Int],
                        hasProjector: Boolean = false,
                        hasWhiteboard: Boolean = false,
                        status: RoomStatus = RoomStatus.AVAILABLE
                      )

case class MeetingRoomCreateRequest(
                                     roomName: String,
                                     floor: Option[Int],
                                     capacity: Option[Int],
                                     hasProjector: Boolean,
                                     hasWhiteboard: Boolean,
                                     status: RoomStatus
                                   )

case class MeetingRoomUpdateRequest(
                                     floor: Option[Int],
                                     capacity: Option[Int],
                                     hasProjector: Boolean,
                                     hasWhiteboard: Boolean,
                                     status: RoomStatus
                                   )

object MeetingRoomModels {
  implicit val meetingRoomCreateFormat: OFormat[MeetingRoomCreateRequest] =
    Json.format[MeetingRoomCreateRequest]

  implicit val meetingRoomUpdateFormat: OFormat[MeetingRoomUpdateRequest] =
    Json.format[MeetingRoomUpdateRequest]
}

object MeetingRoom {
  implicit val format: OFormat[MeetingRoom] = Json.format[MeetingRoom]
}
