import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object CustomerTransactionPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("CustomerTransactionPipeline")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate Customers (2M)
    // ---------------------------------------------------------
    val custCount = 2000000

    val custRDD = spark.sparkContext
      .parallelize(1 to custCount, 50)
      .map { id =>
        val name = Random.alphanumeric.take(8).mkString
        (id, name)
      }

    val custDF = custRDD.toDF("customerId", "name")

    // ---------------------------------------------------------
    // 2. Generate Transactions (5M)
    // ---------------------------------------------------------
    val txnCount = 5000000

    val txnRDD2 = spark.sparkContext
      .parallelize(1 to txnCount, 80)
      .map { tid =>
        val cust = Random.nextInt(custCount) + 1
        val amt  = Random.nextDouble() * 1000
        (tid, cust, amt)
      }

    val txnDF2 = txnRDD2.toDF("txnId", "customerId", "amount")

    // ---------------------------------------------------------
    // 3. Join customers and transactions on customerId
    // ---------------------------------------------------------
    val joinedDF = txnDF2.join(custDF, "customerId")

    println("Join sample:")
    joinedDF.show(5, truncate = false)

    // ---------------------------------------------------------
    // 4. Total spend per customer
    // ---------------------------------------------------------
    val totalSpendDF = joinedDF
      .groupBy("customerId")
      .agg(sum("amount").as("totalSpent"))

    println("Total spend per customer:")
    totalSpendDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 5. Save results to Parquet
    // ---------------------------------------------------------
    totalSpendDF.write
      .mode("overwrite")
      .parquet("output/customer_total_spend_parquet")

    // Keep UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
