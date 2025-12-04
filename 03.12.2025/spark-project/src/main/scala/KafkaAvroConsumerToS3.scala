import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.DecoderFactory

object KafkaAvroConsumerToS3 {

  def main(args: Array[String]): Unit = {

    // Configuration
    val config = ConfigFactory.load()
    val checkpointLocation = "s3a://sparkdemo-bucket-1/checkpoints/kafka-stream/"

    val spark = SparkSession.builder()
      .appName("Kafka-Avro-Consumer-to-S3-JSON")
      .master("local[*]")
      .config("spark.hadoop.fs.s3a.aws.credentials.provider",
        "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Configure S3 Access
    val hadoopConfig = spark.sparkContext.hadoopConfiguration
    hadoopConfig.set("fs.s3a.access.key", config.getString("spark.hadoop.fs.s3a.access.key"))
    hadoopConfig.set("fs.s3a.secret.key", config.getString("spark.hadoop.fs.s3a.secret.key"))
    hadoopConfig.set("fs.s3a.endpoint", config.getString("spark.hadoop.fs.s3a.endpoint"))
    hadoopConfig.set("fs.s3a.impl", config.getString("spark.hadoop.fs.s3a.impl"))

    println("=== Starting Kafka Avro Consumer to S3 JSON Streaming Pipeline ===")

    //-----------------------------
    // Load Avro Schema
    //-----------------------------
    val schemaStream = getClass.getResourceAsStream("/orders.avsc")
    val schemaString = scala.io.Source.fromInputStream(schemaStream).mkString
    schemaStream.close()
    val avroSchema = new Schema.Parser().parse(schemaString)
    println(s"Loaded Avro schema: ${avroSchema.getName}")

    //-----------------------------
    // Step 1: Read from Kafka
    //-----------------------------
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", config.getString("kafka.bootstrapServer"))
      .option("subscribe", config.getString("kafka.topic"))
      .option("startingOffsets", "earliest")
      .load()

    println("Connected to Kafka topic: " + config.getString("kafka.topic"))

    //-----------------------------
    // Step 2: Decode Avro Binary to Columns
    //-----------------------------
    val decodeAvro = udf { (avroBytes: Array[Byte]) =>
      try {
        val reader = new GenericDatumReader[GenericRecord](avroSchema)
        val decoder = DecoderFactory.get().binaryDecoder(avroBytes, null)
        val record = reader.read(null, decoder)

        // Extract fields and return as comma-separated string
        val orderId = record.get("order_id")
        val customerId = record.get("customer_id")
        val amount = record.get("amount")
        val createdAt = record.get("created_at")

        s"$orderId,$customerId,$amount,$createdAt"
      } catch {
        case e: Exception =>
          println(s"Error decoding Avro: ${e.getMessage}")
          null
      }
    }

    // Decode the Avro binary value
    val decodedDF = kafkaDF
      .select(
        col("value").as("avro_bytes"),
        col("timestamp").as("kafka_timestamp")
      )
      .withColumn("decoded", decodeAvro(col("avro_bytes")))
      .filter(col("decoded").isNotNull)

    // Split the decoded string into separate columns
    val parsedDF = decodedDF
      .withColumn("order_id", split(col("decoded"), ",").getItem(0).cast(IntegerType))
      .withColumn("customer_id", split(col("decoded"), ",").getItem(1).cast(IntegerType))
      .withColumn("amount", split(col("decoded"), ",").getItem(2).cast(DoubleType))
      .withColumn("created_at", split(col("decoded"), ",").getItem(3))
      .select(
        col("order_id"),
        col("customer_id"),
        col("amount"),
        col("created_at"),
        col("kafka_timestamp")
      )

    println("Avro decoding configured")

    //-----------------------------
    // Step 3 & 4: Convert to JSON and Write to S3
    //-----------------------------
    val query = parsedDF.writeStream
      .format("json")
      .option("path", config.getString("s3.output.streamJson"))
      .option("checkpointLocation", checkpointLocation)
      .outputMode("append")
      .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("10 seconds"))
      .start()

    println(s"Streaming to S3: ${config.getString("s3.output.streamJson")}")
    println("Streaming query started. Press Ctrl+C to stop.")

    query.awaitTermination()
  }
}