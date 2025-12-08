import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.hadoop.fs.{FileSystem, Path}

object CoalesceExample {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("CoalesceExample")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ----------------------------------------------------------
    // Simulate a large dataset of logs
    // ----------------------------------------------------------
    val logs = spark.range(1, 10000000)
      .map(id => s"log-entry-$id")
      .toDF("log")

    println(s"Original partition count: ${logs.rdd.getNumPartitions}")

    // ----------------------------------------------------------
    // Filter logs heavily - results are small
    // ----------------------------------------------------------
    val filteredLogs = logs.filter($"log".contains("999"))
    println(s"Filtered record count: ${filteredLogs.count()}")
    println(s"Filtered partitions: ${filteredLogs.rdd.getNumPartitions}")

    // ----------------------------------------------------------
    // Coalesce to reduce partitions (NO shuffle)
    // ----------------------------------------------------------
    val reduced = filteredLogs.coalesce(2)
    println(s"After coalesce partitions: ${reduced.rdd.getNumPartitions}")

    // ----------------------------------------------------------
    // Write results and measure performance
    // ----------------------------------------------------------
    def time[R](block: => R): (R, Long) = {
      val start = System.nanoTime()
      val result = block
      val end = System.nanoTime()
      (result, (end - start) / 1000000) // ms
    }

    val outputPath = "filtered_logs_output"

    // Cleanup old output
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val outDir = new Path(outputPath)
    if (fs.exists(outDir)) fs.delete(outDir, true)

    // Measure write time
    val (_, writeTime) = time {
      reduced.write.mode("overwrite").text(outputPath)
    }

    println(s"Write completed in $writeTime ms")

    // ----------------------------------------------------------
    // Count files written
    // ----------------------------------------------------------
    val fileCount = fs.listStatus(new Path(outputPath))
      .count(_.getPath.getName.startsWith("part-"))

    println(s"Files written: $fileCount")

    spark.stop()
  }
}
