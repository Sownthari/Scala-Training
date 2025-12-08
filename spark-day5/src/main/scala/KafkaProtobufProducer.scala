import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.protobuf.functions.to_protobuf

object KafkaProtobufProducer {

  def main(args: Array[String]): Unit = {

    // Kafka topic (default: user-events)
    val topic = if (args.length > 0) args(0) else "user-events"

    // Create Spark session with Kafka + Protobuf support
    val spark = SparkSession.builder()
      .appName("Spark Protobuf Kafka Producer")
      .master("local[*]")
      .config(
        "spark.jars.packages",
        "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.0," +
          "org.apache.spark:spark-protobuf_2.12:3.5.0"
      )
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    println("\n=== Starting Spark Protobuf Kafka Producer ===\n")

    // Path to descriptor file generated from .proto
    val descriptorFile = "user_event.desc"

    // 1. Streaming source — generates rows continuously
    val sourceDF = spark.readStream
      .format("rate")
      .option("rowsPerSecond", 1)
      .load()
      .withColumn("userId", $"value".cast("int"))
      .withColumn("action",
        when($"value" % 3 === 0, "click")
          .when($"value" % 3 === 1, "purchase")
          .otherwise("view")
      )
      .withColumn("value", ($"value" * 5).cast("double"))

    // 2. Convert each row into Protobuf binary using to_protobuf
    val protobufDF = sourceDF.select(
      col("userId").as("key"),
      to_protobuf(
        struct(
          col("userId"),
          col("action"),
          col("value")
        ),
        "UserEvent",
        descriptorFile
      ).alias("value")
    )

    // 3. Publish messages to Kafka
    val query = protobufDF
      .selectExpr(
        "CAST(key AS STRING) AS key",
        "CAST(value AS BINARY) AS value"
      )
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", topic)
      .option("checkpointLocation", "checkpoint/protobuf-producer")
      .start()

    query.awaitTermination()
  }
}
