import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object SensorPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SensorPipeline3M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 3M sensor readings
    // ---------------------------------------------------------
    val numSensors = 3000000

    val sensorRDD = spark.sparkContext
      .parallelize(1 to numSensors, 40)
      .map { _ =>
        val dev  = "DEV_" + Random.nextInt(5000)
        val temp = 20 + Random.nextDouble() * 15
        val hum  = 40 + Random.nextDouble() * 20
        val hour = Random.nextInt(24)
        (dev, temp, hum, hour)
      }

    val sensorDF = sensorRDD
      .toDF("deviceId", "temperature", "humidity", "hour")

    // ---------------------------------------------------------
    // 2. DF: Average temperature per hour
    // ---------------------------------------------------------
    val avgTempDF = sensorDF
      .groupBy("hour")
      .agg(avg("temperature").as("avgTemperature"))

    println("Average temperature per hour:")
    avgTempDF.show(24, truncate = false)

    // ---------------------------------------------------------
    // 3. Write result to Parquet partitioned by hour
    // ---------------------------------------------------------
    avgTempDF.write
      .mode("overwrite")
      .partitionBy("hour")
      .parquet("output/sensor_avg_temp_parquet")

    // Keep UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
