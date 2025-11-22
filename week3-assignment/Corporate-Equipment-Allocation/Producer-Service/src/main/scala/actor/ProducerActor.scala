package actor

import akka.actor.Actor
import kafka.KafkaProducerWrapper
import play.api.libs.json.JsValue

object ProducerActor {
  case class Publish(topic: String, key: String, payload: JsValue)
}

class ProducerActor(producer: KafkaProducerWrapper) extends Actor {
  import ProducerActor._
  import context.dispatcher

  def receive: Receive = {
    case Publish(topic, key, payload) =>
      producer.send(topic, key, payload).foreach { _ =>
        println(s"[Producer] Published event to '$topic' (key=$key)")
      }
  }
}
