import org.apache.spark.sql.SparkSession
import scala.util.Random

object customerData {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("CustomerCityCount")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate sample data
    // ---------------------------------------------------------
    val numRecords = 5 * 1000 * 1000 // 5 million
    val cities = (1 to 50).map(i => s"City_$i").toArray

    val customersRDD = spark.sparkContext
      .parallelize(1 to numRecords, 50)
      .map { id =>
        val name = Random.alphanumeric.take(10).mkString
        val age  = 18 + Random.nextInt(53)
        val city = cities(Random.nextInt(cities.length))
        (id.toLong, name, age, city)
      }

    val customersDF = customersRDD.toDF("customerId", "name", "age", "city")

    // ---------------------------------------------------------
    // 2. RDD — Count of customers in each city
    // ---------------------------------------------------------
    val cityCountsRDD = customersRDD
      .map { case (_, _, _, city) => (city, 1) }
      .reduceByKey(_ + _)

    println("RDD Result (first 10):")
    cityCountsRDD.take(10).foreach(println)

    // ---------------------------------------------------------
    // 3. DataFrame — Count of customers in each city
    // ---------------------------------------------------------
    val cityCountsDF = customersDF
      .groupBy("city")
      .count()

    println("DataFrame Result:")
    cityCountsDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 4. Write results as CSV, JSON, Parquet
    // ---------------------------------------------------------
    cityCountsDF.write
      .mode("overwrite")
      .csv("output/city_counts_csv")

    cityCountsDF.write
      .mode("overwrite")
      .json("output/city_counts_json")

    cityCountsDF.write
      .mode("overwrite")
      .parquet("output/city_counts_parquet")


    Thread.sleep(5000000)

    spark.stop()
  }
}
