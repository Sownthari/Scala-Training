package routes


import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import play.api.libs.json.Json
import actor.ProducerActor
import akka.http.scaladsl.server.Route

object ProducerRoutes {

  def apply(producer: ActorRef): Route = {
    path("produce-events") {
      post {
        entity(as[String]) { body =>
          val json = Json.parse(body)
          println(json)

          val eventName = (json \ "event").as[String]
          val topic     = (json \ "topic").as[String]

          producer ! ProducerActor.Publish(
            topic,
            eventName,
            json
          )

          complete("OK")
        }
      }
    }
  }
}
