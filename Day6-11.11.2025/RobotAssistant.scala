trait Robot {
  def start(): Unit
  def shutDown(): Unit
  def status(): Unit = println("Robot is operational")
}

trait SpeechModule {
  def speak(message: String) = println(s"Robot says: $message")
}

trait MovementModule {
  def moveForward(): Unit = println("Moving forward")
  def moveBackward(): Unit = println("Moving backward")
}

trait EnergySaver {
  def activateEnergySaver(): Unit = println("Energy saver mode activated")
  def shutDown(): Unit = println("Robot shutting down to save energy")
}

class BasicRobot extends Robot {
  override def start(): Unit = println("Basic robot started running")
  override def shutDown(): Unit = println("Basic robot shutDown.... Bye Bye")
}

object RobotTest extends App {

  // Robot with speech and movement capabilities
  val robot1 = new BasicRobot with SpeechModule with MovementModule
  robot1.start()
  robot1.status()
  robot1.speak("Hello!")
  robot1.moveForward()
  robot1.shutDown()

  // Robot with energy saver and movement capabilities
  val robot2 = new BasicRobot with EnergySaver with MovementModule {
    override def shutDown(): Unit = super[EnergySaver].shutDown()
  }
  robot2.start()
  robot2.moveBackward()
  robot2.activateEnergySaver()
  robot2.shutDown()
}
