import java.sql.{Connection, DriverManager, Statement}
import java.time.LocalDateTime
import java.sql.Timestamp

case class Vehicle(
    licensePlate: String,
    vehicleType: String,
    ownerName: String
)

case class TrafficSignal(
    location: String,
    status: String
)

case class Violation(
    vehicleId: Int,
    signalId: Int,
    violationType: String,
    timestamp: LocalDateTime
)

object Database {

  Class.forName("com.mysql.cj.jdbc.Driver")

  // Database credentials
  private val url =
    "jdbc:mysql://azuremysql8823.mysql.database.azure.com:3306/sownthari"
  private val username = "mysqladmin"
  private val password = "Password@12345"

  def getConnection(): Connection =
    DriverManager.getConnection(url, username, password)

  private def createTables(connection: Connection): Unit =
    val statement: Statement = connection.createStatement()

    val createVehiclesTable =
      """
        CREATE TABLE IF NOT EXISTS Vehicles (
          vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
          license_plate VARCHAR(20) NOT NULL,
          vehicle_type VARCHAR(20) NOT NULL,
          owner_name VARCHAR(100) NOT NULL
        );
      """

    val createTrafficSignalsTable =
      """
        CREATE TABLE IF NOT EXISTS TrafficSignals (
          signal_id INT PRIMARY KEY AUTO_INCREMENT,
          location VARCHAR(100) NOT NULL,
          status VARCHAR(10) NOT NULL
        );
      """

    val createViolationsTable =
      """
        CREATE TABLE IF NOT EXISTS Violations (
          violation_id INT PRIMARY KEY AUTO_INCREMENT,
          vehicle_id INT,
          signal_id INT,
          violation_type VARCHAR(50) NOT NULL,
          timestamp DATETIME NOT NULL,
          FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id),
          FOREIGN KEY (signal_id) REFERENCES TrafficSignals(signal_id)
        );
      """

    statement.execute(createVehiclesTable)
    statement.execute(createTrafficSignalsTable)
    statement.execute(createViolationsTable)

    println("All tables created successfully.")

  def closeConnection(connection: Connection): Unit =
    if connection != null then
      connection.close()
      println("Database connection closed.")

  def database(): Unit = {
    val connection = getConnection();
    try {

      createTables(connection);
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      closeConnection(connection)
    }
  }

}

object TrafficFunctions {
  // Insert Vehicle
  def addVehicle(vehicle: Vehicle): Unit =
    val conn = Database.getConnection()
    val sql =
      "INSERT INTO Vehicles (license_plate, vehicle_type, owner_name) VALUES (?, ?, ?)"
    val ps = conn.prepareStatement(sql)
    ps.setString(1, vehicle.licensePlate)
    ps.setString(2, vehicle.vehicleType)
    ps.setString(3, vehicle.ownerName)
    ps.executeUpdate()
    println(s"Vehicle '${vehicle.licensePlate}' added successfully.")
    Database.closeConnection(conn)

  // Insert Traffic Signal
  def addTrafficSignal(signal: TrafficSignal): Unit =
    val conn = Database.getConnection()
    val sql = "INSERT INTO TrafficSignals (location, status) VALUES (?, ?)"
    val ps = conn.prepareStatement(sql)
    ps.setString(1, signal.location)
    ps.setString(2, signal.status)
    ps.executeUpdate()
    println(s"Signal at '${signal.location}' added successfully.")
    conn.close()

  // Record Violation (check if vehicle exists first)
  def recordViolation(violation: Violation): Unit =
    val conn = Database.getConnection()
    val checkSql = "SELECT COUNT(*) FROM Vehicles WHERE vehicle_id = ?"
    val checkPs = conn.prepareStatement(checkSql)
    checkPs.setInt(1, violation.vehicleId)
    val rs = checkPs.executeQuery()
    rs.next()
    if rs.getInt(1) > 0 then
      val sql =
        "INSERT INTO Violations (vehicle_id, signal_id, violation_type, timestamp) VALUES (?, ?, ?, ?)"
      val ps = conn.prepareStatement(sql)
      ps.setInt(1, violation.vehicleId)
      ps.setInt(2, violation.signalId)
      ps.setString(3, violation.violationType)
      ps.setTimestamp(4, Timestamp.valueOf(violation.timestamp))
      ps.executeUpdate()
      println(s"Violation recorded for Vehicle ID ${violation.vehicleId}.")
    else
      println(
        s"Vehicle ID ${violation.vehicleId} does not exist — cannot record violation."
      )
    conn.close()

  // Update Traffic Signal Status
  def updateSignalStatus(signalId: Int, newStatus: String): Unit =
    val conn = Database.getConnection()
    val sql = "UPDATE TrafficSignals SET status = ? WHERE signal_id = ?"
    val ps = conn.prepareStatement(sql)
    ps.setString(1, newStatus)
    ps.setInt(2, signalId)
    val rows = ps.executeUpdate()
    if rows > 0 then println(s"Signal $signalId updated to '$newStatus'.")
    else println(s"No signal found with ID $signalId.")
    conn.close()

  // Delete Vehicle
  def deleteVehicle(vehicleId: Int): Unit =
    val conn = Database.getConnection()
    val sql = "DELETE FROM Vehicles WHERE vehicle_id = ?"
    val ps = conn.prepareStatement(sql)
    ps.setInt(1, vehicleId)
    val rows = ps.executeUpdate()
    if rows > 0 then println(s"Vehicle $vehicleId deleted.")
    else println(s"Vehicle $vehicleId not found.")
    conn.close()

  // Delete Violation
  def deleteViolation(violationId: Int): Unit =
    val conn = Database.getConnection()
    val sql = "DELETE FROM Violations WHERE violation_id = ?"
    val ps = conn.prepareStatement(sql)
    ps.setInt(1, violationId)
    val rows = ps.executeUpdate()
    if rows > 0 then println(s"Violation $violationId deleted.")
    else println(s"Violation $violationId not found.")
    conn.close()

  // View all Vehicles
  def viewVehicles(): Unit =
    val conn = Database.getConnection()
    val rs = conn.createStatement().executeQuery("SELECT * FROM Vehicles")
    println("🚗 Vehicles:")
    while rs.next() do
      println(
        s"ID: ${rs.getInt("vehicle_id")}, Plate: ${rs.getString("license_plate")}, Type: ${rs
            .getString("vehicle_type")}, Owner: ${rs.getString("owner_name")}"
      )
    conn.close()

  // View all Signals
  def viewSignals(): Unit =
    val conn = Database.getConnection()
    val rs = conn.createStatement().executeQuery("SELECT * FROM TrafficSignals")
    println("🚦 Traffic Signals:")
    while rs.next() do
      println(s"ID: ${rs.getInt("signal_id")}, Location: ${rs
          .getString("location")}, Status: ${rs.getString("status")}")
    conn.close()

  // View all Violations
  def viewViolations(): Unit =
    val conn = Database.getConnection()
    val rs = conn.createStatement().executeQuery("SELECT * FROM Violations")
    println("Violations:")
    while rs.next() do
      println(s"ID: ${rs.getInt("violation_id")}, Vehicle: ${rs
          .getInt("vehicle_id")}, Signal: ${rs.getInt("signal_id")}, Type: ${rs
          .getString("violation_type")}, Time: ${rs.getTimestamp("timestamp")}")
    conn.close()
}
