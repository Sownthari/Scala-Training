trait Autopilot:
  def autoNavigate(): Unit = println("Default autopilot engaged...")

abstract class SpaceCraft(val name: String, private var fuelLevel: Double)
    extends Autopilot {

  def launch(): Unit
  def land(): Unit = println(s"$name performing default landing sequence...")
  def refuel(amount: Double): Unit = fuelLevel =
    Math.min(100, fuelLevel + amount)
  def status(): Unit = println(s"$name has fuel: $fuelLevel%")
}

class CargoShip(name: String, val fuel: Double) extends SpaceCraft(name, fuel) {
  override def launch(): Unit =
    println(s"CargoShip $name launching with cargo and fuel $fuel%")
  override def land(): Unit =
    println(s"CargoShip $name landing at dock...")

}

class PassengerShip(name: String, fuel: Double) extends SpaceCraft(name, fuel) {
  override def launch(): Unit =
    println(s"PassengerShip $name launching passengers with fuel $fuel%")
  final override def land(): Unit =
    println(s"PassengerShip $name executing safe docking sequence...")
}

final class LuxuryCruiser(name: String, fuel: Double)
    extends PassengerShip(name, fuel) {
  def playEntertainment(): Unit = println(
    "LuxuryCruiser: Enjoying onboard music..."
  )
}

@main def runSystem() =
  val cargo = new CargoShip("Orion", 75)
  val passenger = new PassengerShip("GalaxyOne", 90)
  val luxury = new LuxuryCruiser("StarQueen", 100)

  cargo.launch(); cargo.land(); cargo.autoNavigate()
  passenger.launch(); passenger.land()
  luxury.launch(); luxury.land(); luxury.playEntertainment()
  cargo.refuel(20);
  cargo.status();
