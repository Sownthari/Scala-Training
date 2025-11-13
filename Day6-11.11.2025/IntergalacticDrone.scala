trait Drone {
  def activate(): Unit
  def deactivate(): Unit
  def status(): Unit = println("Drone status")
}

trait NavigationModule extends Drone {
  def flyTo(destination: String): Unit = println(
    s"Flying to destination $destination"
  )
  abstract override def deactivate(): Unit = println(
    "Navigation systems shutting down"
  )
}

trait DefenseModule extends Drone {
  def activateShields(): Unit = println("Shields activated")
  abstract override def deactivate(): Unit = println(
    "Defense systems deactivated"
  )
}

trait CommunicationModule extends Drone {
  def sendMessage(message: String): Unit = println(s"message sent: $message")
  abstract override def deactivate(): Unit = {
    super.deactivate()
    println("Communication module shutting down")
  }
}

class BasicDrone extends Drone {
  override def activate(): Unit = println(" Basic Drone Activated")
  override def deactivate(): Unit = println("Basic Drone Deactivated")
}

object DroneInstance extends App {
  val basic = new BasicDrone;
  basic.activate()
  basic.status()
  basic.deactivate()

  val basic1 = new BasicDrone
    with NavigationModule
    with DefenseModule
    with CommunicationModule;
  basic1.deactivate()
}
