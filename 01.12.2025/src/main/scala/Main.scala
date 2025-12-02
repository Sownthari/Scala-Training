import java.io._
import scala.util.Random
import java.time._

object Main extends App {

  val file = new PrintWriter(new File("urbanmove_trips.csv"))

  val areas = Array(
    "MG Road", "Indira Nagar", "Koramangala", "Whitefield",
    "Marathahalli", "HSR Layout", "BTM", "Jayanagar"
  )

  val vehicleTypes = Array("AUTO", "TAXI", "BIKE")
  val paymentMethods = Array("CASH", "UPI", "CARD")

  val rand = new Random()

  def randomDateTime(): LocalDateTime = {
    val now = LocalDateTime.now()
    now.minusMinutes(rand.nextInt(100000))
  }

  file.write(
    "tripId,driverId,vehicleType,startTime,endTime,startLocation,endLocation," +
      "distanceKm,fareAmount,paymentMethod,customerRating\n"
  )

  for (i <- 1 to 1000000) {

    val start = randomDateTime()
    val duration = rand.nextInt(50) + 5
    val end = start.plusMinutes(duration)

    val distance = math.round((rand.nextDouble() * 15 + 1) * 100) / 100.0
    val fare = math.round((distance * (rand.nextInt(10) + 10)) * 100) / 100.0
    val rating = math.round((rand.nextDouble() * 4 + 1) * 100) / 100.0

    file.write(
      s"$i,${rand.nextInt(5000)},${vehicleTypes(rand.nextInt(3))}," +
        s"$start,$end," +
        s"${areas(rand.nextInt(areas.length))}," +
        s"${areas(rand.nextInt(areas.length))}," +
        s"$distance,$fare," +
        s"${paymentMethods(rand.nextInt(3))},$rating\n"
    )
  }

  file.close()
}
