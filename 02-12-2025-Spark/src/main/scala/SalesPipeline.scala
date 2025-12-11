import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object SalesPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SalesPipeline10M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 10M sales records
    // ---------------------------------------------------------
    val numSales = 10000000
    val stores = (1 to 100).map(i => s"Store_$i").toArray

    val salesRDD = spark.sparkContext
      .parallelize(1 to numSales, 50)
      .map { id =>
        val store = stores(Random.nextInt(stores.length))
        val amt   = Random.nextDouble() * 500
        (store, amt)
      }

    val salesDF = salesRDD.toDF("storeId", "amount")

    // ---------------------------------------------------------
    // 2. RDD — groupByKey (slow)
    // ---------------------------------------------------------
    val gbkRDD = salesRDD
      .groupByKey()
      .mapValues(values => values.sum)

    // Action to trigger
    println("groupByKey sample:")
    gbkRDD.take(5).foreach(println)

    // ---------------------------------------------------------
    // 3. RDD — reduceByKey (fast)
    // ---------------------------------------------------------
    val rbkRDD = salesRDD
      .reduceByKey(_ + _)

    // Action to trigger
    println("reduceByKey sample:")
    rbkRDD.take(5).foreach(println)

    // ---------------------------------------------------------
    // 4. DataFrame — store-wise total sales
    // ---------------------------------------------------------
    val storeSalesDF = salesDF
      .groupBy("storeId")
      .agg(sum("amount").as("totalAmount"))

    println("DataFrame Result:")
    storeSalesDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 5. Save result as Parquet
    // ---------------------------------------------------------
    storeSalesDF.write
      .mode("overwrite")
      .parquet("output/store_sales_parquet")

    // Keep UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
