import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object LogPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("LogPipeline5M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 5M random logs
    // ---------------------------------------------------------
    val levels = Array("INFO", "WARN", "ERROR")
    val numLogs = 5000000

    val logsRDD = spark.sparkContext
      .parallelize(1 to numLogs, 40)
      .map { _ =>
        val ts = System.currentTimeMillis() - Random.nextInt(10000000)
        val level = levels(Random.nextInt(levels.length))
        val msg = Random.alphanumeric.take(15).mkString
        val user = Random.nextInt(10000)
        s"$ts|$level|$msg|$user"
      }

    // ---------------------------------------------------------
    // 2. Convert to DataFrame
    // ---------------------------------------------------------
    val logsDF = logsRDD
      .map(_.split("\\|"))
      .map(a => (a(0), a(1), a(2), a(3)))
      .toDF("timestamp", "level", "message", "userId")

    // ---------------------------------------------------------
    // 3. RDD filter → Count ERROR logs
    // ---------------------------------------------------------
    val errorRDD = logsRDD.filter(_.contains("|ERROR|"))
    val errorCountRDD = errorRDD.count()

    println(s"RDD ERROR Count: $errorCountRDD")

    // ---------------------------------------------------------
    // 4. DataFrame filter → Count ERROR logs
    // ---------------------------------------------------------
    val errorDF = logsDF.filter($"level" === "ERROR")
    val errorCountDF = errorDF.count()

    println(s"DataFrame ERROR Count: $errorCountDF")

    // ---------------------------------------------------------
    // 5. Write ERROR logs to plain text
    // ---------------------------------------------------------
    errorRDD.saveAsTextFile("output/error_logs_text")

    // ---------------------------------------------------------
    // 6. Write full dataset to JSON
    // ---------------------------------------------------------
    logsDF.write
      .mode("overwrite")
      .json("output/full_logs_json")

    // Keep Spark UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
