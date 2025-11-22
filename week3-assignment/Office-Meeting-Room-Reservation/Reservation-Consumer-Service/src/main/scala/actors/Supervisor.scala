package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.Config
import mail.Mailer

class Supervisor(conf: Config, system: ActorSystem, mailer: Mailer) {

  private val bootstrap = conf.getString("kafka-consumer-service.kafka-bootstrap")
  private val baseGroupId = conf.getString("kafka-consumer-service.group-id")
  private val topic = conf.getString("kafka-consumer-service.topic")



  // NotificationActor → ONLY sends mails (same role as your equipment NotificationActor)
  private val notificationActor: ActorRef =
    system.actorOf(Props(new NotificationActor(mailer)), "reservationNotificationActor")

  // ----------------------------------
  // Domain-specific actors
  // ----------------------------------

  // Employee actor → handles employee-related reservation logic
  private val employeeActor: ActorRef =
    system.actorOf(Props(new EmployeeActor(notificationActor)), "employeeActor")

  // Room service actor → chairs, cleaning, special requirements
  private val roomServiceActor: ActorRef =
    system.actorOf(Props(new RoomServiceActor(notificationActor)), "roomServiceActor")

  // Admin staff actor → handles admin alerts (auto-release etc.)
  private val adminStaffActor: ActorRef =
    system.actorOf(Props(new AdminStaffActor(notificationActor)), "adminStaffActor")

  // ----------------------------------
  // Router (same pattern as your sample)
  // ----------------------------------
  private val routerActor: ActorRef =
    system.actorOf(
      Props(
        new RouterActor(
          employeeActor,
          roomServiceActor,
          adminStaffActor
        )
      ),
      "reservationRouterActor"
    )

  // ----------------------------------
  // Kafka Consumer
  // ----------------------------------
  private val streamConsumer = new KafkaConsumerActor(
    system,
    bootstrap,
    baseGroupId,
    topic,
    routerActor
  )

  streamConsumer.start()
}
