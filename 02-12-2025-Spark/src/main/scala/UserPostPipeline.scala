import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object UserPostPipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("UserPostPipeline")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate Users (1M)
    // ---------------------------------------------------------
    val userCount = 1000000

    val userRDD = spark.sparkContext
      .parallelize(1 to userCount, 30)
      .map { id =>
        val name = Random.alphanumeric.take(8).mkString
        val age  = 15 + Random.nextInt(60)
        (id, name, age)
      }

    val userDF = userRDD.toDF("userId", "name", "age")

    // ---------------------------------------------------------
    // 2. Generate Posts (2M)
    // ---------------------------------------------------------
    val postCount = 2000000

    val postRDD = spark.sparkContext
      .parallelize(1 to postCount, 40)
      .map { pid =>
        val user = Random.nextInt(userCount) + 1
        val txt  = Random.alphanumeric.take(20).mkString
        (pid, user, txt)
      }

    val postDF = postRDD.toDF("postId", "userId", "text")

    // ---------------------------------------------------------
    // 3. Join users and posts on userId
    // ---------------------------------------------------------
    val joinedDF = postDF
      .join(userDF, "userId")

    println("Joined sample:")
    joinedDF.show(5, truncate = false)

    // ---------------------------------------------------------
    // 4. Count posts per age group
    // ---------------------------------------------------------
    val postsByAgeDF = joinedDF
      .groupBy("age")
      .count()

    println("Posts per age group:")
    postsByAgeDF.show(10, truncate = false)

    // ---------------------------------------------------------
    // 5. Write result to JSON
    // ---------------------------------------------------------
    postsByAgeDF.write
      .mode("overwrite")
      .json("output/posts_by_age_json")

    // Keep UI open
    Thread.sleep(5000000)

    spark.stop()
  }
}
