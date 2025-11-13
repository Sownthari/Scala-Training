trait Device {
  def turnOn(): Unit
  def turnOff(): Unit
  def status(): Unit = println("Device is operational")
}

trait Connectivity {
  def connect(): Unit = println("Device connected to network")
  def disconnect(): Unit = println("Device disconnected")
}

trait EnergySaver1 extends Device {
  def activateEnergySaver(): Unit = println("Energy saver mode activated")
  override def turnOff(): Unit = println("Device powered down to save energy")
}

trait VoiceControl {
  def turnOn(): Unit = println("Voice to turn on Device...")
  def turnOff(): Unit = println("Voice to turn off Device...")
}

class SmartLight extends Device with Connectivity with EnergySaver1 {
  override def turnOn(): Unit = {
    println("SmartLight turned on...")
  }
}

class SmartThermostat extends Device with Connectivity {
  override def turnOn(): Unit = {
    println("SmartThermostat turned on...")
  }
  override def turnOff(): Unit = {
    println("SmartThermostat turned off...")
  }
}

// Test the classes and trait composition
object SmartHomeTest extends App {
  val light = new SmartLight
  light.turnOn()
  light.turnOff()
  light.status()
  light.connect()
  light.activateEnergySaver()

  val thermostat = new SmartThermostat
  thermostat.turnOn()
  thermostat.turnOff()
  thermostat.status()
  thermostat.connect()

  val voiceLight = new SmartLight with VoiceControl {
  override def turnOn(): Unit = super[VoiceControl].turnOn()
  override def turnOff(): Unit = super[VoiceControl].turnOff() 
  //since both VoiceControl and SmartLight has its own concrete implemetation of 
  //turn off and turn on we need to explicitly define which to use
}
}
