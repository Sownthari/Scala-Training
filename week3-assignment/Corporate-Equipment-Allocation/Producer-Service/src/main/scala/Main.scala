import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory

import actor.ProducerActor
import kafka.KafkaProducerWrapper
import routes.ProducerRoutes

object Main extends App {

  // CLASSIC AKKA ACTOR SYSTEM
  implicit val system: ActorSystem = ActorSystem("producer-system")
  implicit val ec = system.dispatcher

  // Read config
  val config = ConfigFactory.load().getConfig("producer-service")
  val bootstrap = config.getString("kafka-bootstrap")

  // Kafka producer wrapper
  val producerWrapper = new KafkaProducerWrapper(bootstrap)

  sys.addShutdownHook {
    println("Shutting down producer…")
    producerWrapper.close()
  }

  // Classic Actor
  val producerActor = system.actorOf(Props(new ProducerActor(producerWrapper)), "ProducerActor")

  // Routes
  val route = ProducerRoutes(producerActor)

  // Start HTTP Server
  Http().newServerAt("0.0.0.0", 8082).bind(route)

  println("Producer Service running at http://localhost:8082")
}
