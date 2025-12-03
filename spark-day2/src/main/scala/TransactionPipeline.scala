import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object TransactionPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("TransactionPipeline3M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 3M transactions
    // ---------------------------------------------------------
    val numTxns = 3000000

    val txnRDD = spark.sparkContext
      .parallelize(1 to numTxns, 40)
      .map { id =>
        val acc = "ACC_" + Random.nextInt(100000)
        val amt = Random.nextDouble() * 10000
        (acc, amt)
      }

    val txnDF = txnRDD.toDF("accountId", "amount")

    // ---------------------------------------------------------
    // 2. RDD approach: reduceByKey → sortBy → take 10
    // ---------------------------------------------------------
    val top10RDD = txnRDD
      .reduceByKey(_ + _)        // aggregate per account (shuffle)
      .sortBy(_._2, ascending = false) // global sort (shuffle)
      .take(10)

    println("Top 10 accounts (RDD):")
    top10RDD.foreach(println)

    // ---------------------------------------------------------
    // 3. DataFrame approach: groupBy → sum → orderBy → limit 10
    // ---------------------------------------------------------
    val top10DF = txnDF
      .groupBy("accountId")
      .agg(sum("amount").as("totalSpent"))
      .orderBy($"totalSpent".desc)   // shuffle
      .limit(10)

    println("Top 10 accounts (DF):")
    top10DF.show(truncate = false)

    // Keep UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
