import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object StudentPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("StudentPipeline1_5M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 1.5M Student Records
    // ---------------------------------------------------------
    val numStudents = 1500000

    val studentRDD = spark.sparkContext
      .parallelize(1 to numStudents, 20)
      .map { id =>
        val name  = Random.alphanumeric.take(6).mkString
        val score = Random.nextInt(100)
        (id, name, score)
      }

    val studentDF = studentRDD.toDF("studentId", "name", "score")

    // ---------------------------------------------------------
    // 2. Sort students by score descending
    // ---------------------------------------------------------
    val sortedDF = studentDF.orderBy($"score".desc)

    println("Top students:")
    sortedDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 3. Write sorted output to JSON
    // ---------------------------------------------------------
    sortedDF.write
      .mode("overwrite")
      .json("output/students_sorted_json")

    // Keep Spark UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
