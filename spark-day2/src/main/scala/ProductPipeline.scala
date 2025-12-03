import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object ProductPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("ProductPipeline2M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 2M product records
    // ---------------------------------------------------------
    val numProducts = 2000000
    val categories = Array("Electronics", "Clothes", "Books")

    val productRDD = spark.sparkContext
      .parallelize(1 to numProducts, 40)
      .map { id =>
        val cat  = categories(Random.nextInt(categories.length))
        val price = Random.nextDouble() * 2000
        val desc  = Random.alphanumeric.take(50).mkString
        (id.toLong, cat, price, desc)
      }

    val productDF = productRDD.toDF("productId", "category", "price", "description")

    // ---------------------------------------------------------
    // 2. Filter products with price > 1000 (DataFrame)
    // ---------------------------------------------------------
    val filteredDF = productDF.filter($"price" > 1000)

    println("Filtered sample:")
    filteredDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 3. Sort by price (will cause shuffle)
    // ---------------------------------------------------------
    val sortedDF = filteredDF.orderBy($"price".desc)

    println("Sorted sample:")
    sortedDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 4. Write sorted data to CSV
    // ---------------------------------------------------------
    sortedDF.write
      .mode("overwrite")
      .csv("output/products_sorted_csv")

    // ---------------------------------------------------------
    // 5. Write sorted data to Parquet
    // ---------------------------------------------------------
    sortedDF.write
      .mode("overwrite")
      .parquet("output/products_sorted_parquet")

    // Keep UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
