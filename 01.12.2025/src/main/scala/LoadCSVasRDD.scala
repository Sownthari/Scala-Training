import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._

import scala.util.Try

// ------------------------------------------
// CASE CLASS
// ------------------------------------------
case class Trip(
                 tripId: Long,
                 driverId: Int,
                 vehicleType: String,
                 startTime: String,
                 endTime: String,
                 startLocation: String,
                 endLocation: String,
                 distanceKm: Double,
                 fareAmount: Double,
                 paymentMethod: String,
                 customerRating: Double
               )

object LoadCSVasRDD {
  def main(args: Array[String]): Unit = {

    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("Simple RDD Example")
      .master("local[*]") // Local mode
      .getOrCreate()

    import spark.implicits._

    // ============================================================
    // PIPELINE 1 — Load CSV using RDD
    // ============================================================

    val trips = spark.sparkContext.textFile("urbanmove_trips.csv", minPartitions = 8)

    val header = trips.first()
    val data = trips.filter(line => line != header)

    // Filter trips with distance > 10 km
    val filtered = data.filter { line =>
      val cols = line.split(",")
      cols(7).toDouble > 10
    }

    // Map to (vehicleType, distance)
    val mapped = filtered.map { line =>
      val cols = line.split(",")
      val vehicleType = cols(2)
      val distance = cols(7).toDouble
      (vehicleType, distance)
    }

    // Save output as text file
    mapped.saveAsTextFile("output/output_vehicle_distance")

    // ============================================================
    // PIPELINE 2 — Convert RDD → DataFrame using Case Class
    // ============================================================

    val tripRDD = data.map { line =>
      val c = line.split(",")
      Trip(
        c(0).toLong, c(1).toInt, c(2), c(3), c(4),
        c(5), c(6), c(7).toDouble, c(8).toDouble,
        c(9), c(10).toDouble
      )
    }

    // 👇 REQUIRED METHOD
    val df = spark.createDataFrame(tripRDD)

    df.show(5)

    // ============================================================
    // PIPELINE 3 — Clean DataFrame
    // ============================================================

    val dfClean = df
      .filter($"distanceKm" > 0)
      .filter($"fareAmount" >= 0)
      .filter($"startTime" < $"endTime")

    // ============================================================
    // PIPELINE 4 — Add Trip Duration
    // ============================================================

    val df2 = dfClean.withColumn(
      "tripDurationMinutes",
      (unix_timestamp($"endTime") - unix_timestamp($"startTime")) / 60
    )

    // ============================================================
    // PIPELINE 5 — Aggregations
    // ============================================================

    val avgDistance = df2.groupBy("vehicleType")
      .avg("distanceKm")

    val df3 = df2.withColumn("tripDate", to_date($"startTime"))

    val revenuePerDay = df3.groupBy("tripDate")
      .sum("fareAmount")
      .withColumnRenamed("sum(fareAmount)", "totalRevenue")

    val topRoutes = df3.groupBy("startLocation", "endLocation")
      .count()
      .orderBy(desc("count"))
      .limit(5)

    // ============================================================
    // PIPELINE 6 — SQL QUERIES
    // ============================================================

    df3.createOrReplaceTempView("trips")

    val sqlVehicleCount = spark.sql(
      "SELECT vehicleType, COUNT(*) FROM trips GROUP BY vehicleType"
    )

    val sqlAvgFareRoute = spark.sql(
      "SELECT startLocation, endLocation, AVG(fareAmount) AS avgFare FROM trips GROUP BY startLocation, endLocation"
    )

    val sqlPaymentCount = spark.sql(
      "SELECT paymentMethod, COUNT(*) FROM trips GROUP BY paymentMethod"
    )

    // ============================================================
    // PIPELINE 7 — Write to Parquet
    // ============================================================

    df3.write
      .mode("overwrite")
      .parquet("parquet/trips_clean.parquet")

    // ============================================================
    // PIPELINE 8 — Read Parquet, Re-Aggregate
    // ============================================================

    val pq = spark.read.parquet("parquet/trips_clean.parquet")

    val fareByVehicle = pq.groupBy("vehicleType")
      .avg("fareAmount")

    // ============================================================
    // PIPELINE 9 — Write to MySQL
    // ============================================================

//    df3.write
//      .format("jdbc")
//      .option("url", "jdbc:mysql://azuremysql8823.mysql.database.azure.com:3306/sownthari?useSSL=false&serverTimezone=UTC&connectionTimeZone=UTC")
//      .option("dbtable", "trip_summary")
//      .option("user", "mysqladmin")
//      .option("password", "Password@12345")
//      .mode("append")
//      .save()

    Try {
      df3
        .write
        .format("jdbc")
        .option("url", "jdbc:mysql://azuremysql8823.mysql.database.azure.com:3306/sownthari")
        .option("dbtable", "trip_summary")
        .option("user", "mysqladmin")
        .option("password", "Password@12345")
        .option("driver", "com.mysql.cj.jdbc.Driver")
        .option("rewriteBatchedStatements", "true")
        .option("sslMode", "DISABLED")
        .mode(SaveMode.Append)
        .save()
      println(s"Pipeline9 done: data appended to MySQL")
    }.recover { case ex =>
      println(s"Pipeline9 skipped (jdbc write failed): ${ex.getMessage}")
    }



    // ============================================================
    // PIPELINE 10 — Export Report to Text
    // ============================================================

    revenuePerDay.rdd.saveAsTextFile("reports/revenue_per_day")

    // Stop the Spark session
    spark.stop()
  }
}