import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object S3AggregationJob {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()
    // Initialize Spark Session with S3 Configuration
    val spark = SparkSession.builder()
      .appName("S3-Parquet-Aggregation-to-JSON")
      .master("local[*]")
      // S3 Configuration
      .config("spark.hadoop.fs.s3a.aws.credentials.provider", config.getString("spark.hadoop.fs.s3a.aws.credentials.provider"))
      .getOrCreate()

    println("=== Starting S3 Aggregation Pipeline ===")

    // Configure S3 Access
    val hadoopConfig = spark.sparkContext.hadoopConfiguration
    hadoopConfig.set("fs.s3a.access.key", config.getString("spark.hadoop.fs.s3a.access.key"))
    hadoopConfig.set("fs.s3a.secret.key", config.getString("spark.hadoop.fs.s3a.secret.key"))
    hadoopConfig.set("fs.s3a.endpoint", config.getString("spark.hadoop.fs.s3a.endpoint"))
    hadoopConfig.set("fs.s3a.impl", config.getString("spark.hadoop.fs.s3a.impl"))

    // -------------------------------
    // Step 1: Read Parquet from S3
    // -------------------------------
    println("Step 1: Reading parquet files from S3...")

    val s3InputPath = config.getString("s3.output.parquet")

    val salesDF = spark.read
      .parquet(s3InputPath)

    println(s"Total records read: ${salesDF.count()}")
    println("Schema of input data:")
    salesDF.printSchema()

    println("Sample input data:")
    salesDF.show(10, truncate = false)

    // -------------------------------
    // Step 2: Compute Aggregations per Product
    // -------------------------------
    println("Step 2: Computing aggregations per product...")

    val aggregatedDF = salesDF
      .groupBy("product_name")
      .agg(
        sum("quantity").alias("total_quantity"),
        sum("amount").alias("total_revenue")
      )
      .select(
        col("product_name"),
        col("total_quantity"),
        col("total_revenue")
      )
      .orderBy(desc("total_revenue"))

    println("Aggregated results:")
    aggregatedDF.show(20, truncate = false)

    println(s"Total unique products: ${aggregatedDF.count()}")

    // -------------------------------
    // Step 3: Write to S3 as JSON
    // -------------------------------
    println("Step 3: Writing aggregated data to S3 as JSON...")

    val s3OutputPath = config.getString("s3.output.json")

    aggregatedDF
      .coalesce(1)  // Single JSON file for easy consumption
      .write
      .mode("overwrite")
      .json(s3OutputPath)

    println(s"=== Aggregation Pipeline Completed Successfully ===")
    println(s"Output written to: $s3OutputPath")
    println(s"Total products aggregated: ${aggregatedDF.count()}")

    // Optional: Show summary statistics
    println("\nTop 10 Products by Revenue:")
    aggregatedDF.show(10, truncate = false)

    spark.stop()
  }
}