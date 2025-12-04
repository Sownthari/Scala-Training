import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

object KeyspacesToS3Job {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()
    // Initialize Spark Session with Keyspaces and S3 Configuration
    val spark = SparkSession.builder()
      .appName("Keyspaces-to-S3-Parquet-Export")
      .master("local[*]")
      .config("spark.cassandra.connection.host", config.getString("spark.cassandra.connection.host"))
      .config("spark.cassandra.connection.port", config.getInt("spark.cassandra.connection.port"))

      .config("spark.cassandra.connection.localDC", config.getString("spark.cassandra.connection.localDC"))
      .config("spark.cassandra.connection.ssl.enabled", config.getBoolean("spark.cassandra.connection.ssl.enabled"))

      .config("spark.cassandra.connection.ssl.trustStore.path", config.getString("spark.cassandra.connection.ssl.trustStore.path"))
      .config("spark.cassandra.connection.ssl.trustStore.password", config.getString("spark.cassandra.connection.ssl.trustStore.password"))
      .config("spark.cassandra.auth.username", config.getString("spark.cassandra.auth.username"))
      .config("spark.cassandra.auth.password", config.getString("spark.cassandra.auth.password"))
      .config("spark.sql.catalog.cassandra", "com.datastax.spark.connector.datasource.CassandraCatalog")
      // S3 Configuration
      .config("spark.hadoop.fs.s3a.aws.credentials.provider", config.getString("spark.hadoop.fs.s3a.aws.credentials.provider"))
      .getOrCreate()

    println("=== Starting Keyspaces to S3 Export Job ===")

    // Configure S3 Access
    val hadoopConfig = spark.sparkContext.hadoopConfiguration
    hadoopConfig.set("fs.s3a.access.key", config.getString("spark.hadoop.fs.s3a.access.key"))
    hadoopConfig.set("fs.s3a.secret.key", config.getString("spark.hadoop.fs.s3a.secret.key"))
    hadoopConfig.set("fs.s3a.endpoint", config.getString("spark.hadoop.fs.s3a.endpoint"))
    hadoopConfig.set("fs.s3a.impl", config.getString("spark.hadoop.fs.s3a.impl"))

    // -------------------------------
    // Step 1: Read from Amazon Keyspaces
    // -------------------------------
    println("Step 1: Reading data from Amazon Keyspaces...")

    val salesDataDF = spark.read
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", "spark_keyspace")
      .option("table", "sales_data")
      .load()

    // Note: Avoiding .count() here as it's expensive and causes the countRows error
    println("Successfully connected to Keyspaces")
    println("Schema of sales_data table:")
    salesDataDF.printSchema()

    // -------------------------------
    // Step 2: Select Required Columns
    // -------------------------------
    println("Step 2: Selecting columns...")

    val selectedDF = salesDataDF.select(
      "customer_id",
      "order_id",
      "amount",
      "product_name",
      "quantity"
    )

    println("Sample data:")
    selectedDF.show(10, truncate = false)

    // -------------------------------
    // Step 3: Write to S3 as Partitioned Parquet
    // -------------------------------
    println("Step 3: Writing partitioned parquet files to S3...")

    val s3OutputPath = config.getString("s3.output.parquet")

    selectedDF.write
      .mode("overwrite")  // Change to "append" if you want to add to existing data
      .partitionBy("customer_id")
      .parquet(s3OutputPath)

    println(s"=== Export Completed Successfully ===")
    println(s"Data written to: $s3OutputPath")
    println(s"Partitioned by: customer_id")
    println("Note: Record count not displayed to avoid countRows limitation in Keyspaces")

    spark.stop()
  }
}