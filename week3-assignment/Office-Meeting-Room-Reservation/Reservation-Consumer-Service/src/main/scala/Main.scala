import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import actors.Supervisor
import mail.Mailer

object Main extends App {

  val conf = ConfigFactory.load()
  val mailConf = conf.getConfig("mail")
  implicit val system: ActorSystem = ActorSystem("KafkaConsumerStreamSystem")

  //FIX: Add the execution context
  implicit val ec = system.dispatcher

  val mailer = new Mailer(
    host = mailConf.getString("host"),
    port = mailConf.getInt("port"),
    user = mailConf.getString("user"),
    pass = mailConf.getString("password")
  )

  new Supervisor(conf, system, mailer)
}
