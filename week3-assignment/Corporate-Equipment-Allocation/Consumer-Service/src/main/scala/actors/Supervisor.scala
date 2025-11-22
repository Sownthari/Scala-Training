package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.Config
import mail.Mailer

class Supervisor(conf: Config, system: ActorSystem, mailer: Mailer) {

  private val bootstrap = conf.getString("kafka-consumer-service.kafka-bootstrap")
  private val baseGroupId = conf.getString("kafka-consumer-service.group-id")
  private val topic = conf.getString("kafka-consumer-service.topic")

  // NotificationActor for emails
  private val notificationActor: ActorRef =
    system.actorOf(Props(new NotificationActor(mailer)), "notificationActor")

  // MaintenanceActor receives events only from InventoryActor
  private val maintenanceActor =
    system.actorOf(Props(new MaintenanceActor(notificationActor)), "maintenanceActor")

  // InventoryActor does GOOD/DAMAGED/overdue + maintenance routing
  private val inventoryActor =
    system.actorOf(Props(new InventoryActor(notificationActor, maintenanceActor)), "inventoryActor")

  // Router: only routes to inventory + employee notifications
  private val routerActor =
    system.actorOf(Props(new RouterActor(inventoryActor, notificationActor)), "routerActor")

  // Kafka Consumer
  private val streamConsumer = new KafkaConsumerActor(
    system,
    bootstrap,
    baseGroupId,
    topic,
    routerActor
  )

  streamConsumer.start()
}
