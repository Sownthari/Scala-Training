import java.time.LocalDateTime

object TrafficMain:
  def main(args: Array[String]): Unit =
    Database.database()
    var exit = false
    while !exit do
      println("""
          |=== Traffic Management System ===
          |1. Add Vehicle
          |2. Add Traffic Signal
          |3. Record Violation
          |4. Update Signal Status
          |5. View Vehicles
          |6. View Traffic Signals
          |7. View Violations
          |8. Delete Vehicle
          |9. Delete Violation
          |10. Exit
          |Select option:
          |""".stripMargin)

      val choice = scala.io.StdIn.readInt()
      choice match
        case 1 =>
          println("Enter license plate: ");
          val plate = scala.io.StdIn.readLine()
          println("Select vehicle type:")
          println("1. Car")
          println("2. Bike")
          println("3. Truck")

          val choice = scala.io.StdIn.readInt()
          val vtype = choice match
            case 1 => "car"
            case 2 => "bike"
            case 3 => "truck"
            case _ => {
              println("Invalid choice! Defaulting to 'car'."); "car"
            }

          println("Enter owner name: "); val owner = scala.io.StdIn.readLine()
          TrafficFunctions.addVehicle(
            Vehicle(
              licensePlate = plate,
              vehicleType = vtype,
              ownerName = owner
            )
          )
        case 2 =>
          println("Enter signal location: ");
          val loc = scala.io.StdIn.readLine()
          println("Enter signal status: ");
          println("1. Green")
          println("2. Yellow")
          println("3. Red")

          val choice = scala.io.StdIn.readInt()
          val status = choice match
            case 1 => "green"
            case 2 => "yellow"
            case 3 => "red"
            case _ => {
              println("Invalid choice! Defaulting to 'green'."); "green"
            }
          TrafficFunctions.addTrafficSignal(
            TrafficSignal(location = loc, status = status)
          )
        case 3 =>
          println("Enter vehicle ID: "); val vid = scala.io.StdIn.readInt()
          println("Enter signal ID: "); val sid = scala.io.StdIn.readInt()
          println("Enter violation type: ");
          println("1. Speeding")
          println("2. Signal")
          println("3. Jump")

          val choice = scala.io.StdIn.readInt()
          val vtype = choice match
            case 1 => "speeding"
            case 2 => "signal"
            case 3 => "jump"
            case _ => {
              println("Invalid choice! Defaulting to 'speeding'."); "jump"
            }
          TrafficFunctions.recordViolation(
            Violation(
              vehicleId = vid,
              signalId = sid,
              violationType = vtype,
              timestamp = LocalDateTime.now
            )
          )
        case 4 =>
          println("Enter signal ID: "); val sid = scala.io.StdIn.readInt()
          println("Enter new status: "); val status = scala.io.StdIn.readLine()
          TrafficFunctions.updateSignalStatus(sid, status)
        case 5 => TrafficFunctions.viewVehicles()
        case 6 => TrafficFunctions.viewSignals()
        case 7 => TrafficFunctions.viewViolations()
        case 8 =>
          println("Enter vehicle ID to delete: ");
          val vid = scala.io.StdIn.readInt()
          TrafficFunctions.deleteVehicle(vid)
        case 9 =>
          println("Enter violation ID to delete: ");
          val vid = scala.io.StdIn.readInt()
          TrafficFunctions.deleteViolation(vid)
        case 10 => exit = true
        case _  => println("Invalid choice.")
