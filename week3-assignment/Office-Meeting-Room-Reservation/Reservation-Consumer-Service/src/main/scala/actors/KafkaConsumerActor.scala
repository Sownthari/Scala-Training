package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json._

import models._
import models.EventFormats._

class KafkaConsumerActor(
                                     system: ActorSystem,
                                     bootstrap: String,
                                     groupId: String,
                                     topic: String,
                                     routerActor: ActorRef
                                   ) {

  implicit val sys: ActorSystem = system
  implicit val ec = sys.dispatcher

  private val consumerSettings =
    ConsumerSettings(sys, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrap)
      .withGroupId(groupId)
      .withProperty("auto.offset.reset", "latest")

  def start(): Unit = {

    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .map { msg =>
        val key     = msg.key()
        val jsonStr = msg.value()

        key match {

          // --------------------------------------------------
          // Reservation Created
          // --------------------------------------------------
          case "ReservationCreatedEvent" =>
            Json.parse(jsonStr).validate[ReservationCreatedEvent] match {
              case JsSuccess(evt, _) => Some(evt)
              case JsError(_) =>
                println(s"[Kafka ERROR] Invalid ReservationCreatedEvent: $jsonStr")
                None
            }

          // --------------------------------------------------
          // Meeting Reminder
          // --------------------------------------------------
          case "MeetingReminderEvent" =>
            Json.parse(jsonStr).validate[MeetingReminderEvent] match {
              case JsSuccess(evt, _) => Some(evt)
              case JsError(_) =>
                println(s"[Kafka ERROR] Invalid MeetingReminderEvent: $jsonStr")
                None
            }

          // --------------------------------------------------
          // Auto Release
          // --------------------------------------------------
          case "AutoReleaseEvent" =>
            Json.parse(jsonStr).validate[AutoReleaseEvent] match {
              case JsSuccess(evt, _) => Some(evt)
              case JsError(_) =>
                println(s"[Kafka ERROR] Invalid AutoReleaseEvent: $jsonStr")
                None
            }

          // --------------------------------------------------
          // Unknown Event Key
          // --------------------------------------------------
          case other =>
            println(s"[Kafka ERROR] Unknown key '$other', skipping")
            None
        }
      }
      .collect { case Some(evt) => evt }
      .runWith(
        Sink.foreach { event =>
          println(s"[Kafka] Event received: $event")
          routerActor ! event
        }
      )
  }
}
