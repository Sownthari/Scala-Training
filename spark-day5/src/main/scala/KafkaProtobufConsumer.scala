import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.protobuf.functions.from_protobuf
import org.apache.spark.sql.streaming.Trigger

object KafkaProtobufConsumer {

  def main(args: Array[String]): Unit = {

    // Create Spark Session with Kafka + Protobuf
    val spark = SparkSession.builder()
      .appName("KafkaProtobufConsumer")
      .master("local[*]")
      .config(
        "spark.jars.packages",
        "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.0," +
          "org.apache.spark:spark-protobuf_2.12:3.5.0"
      )
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    println("\n" + "=" * 80)
    println("Kafka Protobuf Streaming Consumer (Scala)")
    println("=" * 80)

    processStreaming(spark)
  }

  /** Streaming version — runs forever */
  def processStreaming(spark: SparkSession): Unit = {
    import spark.implicits._

    val kafkaBootstrapServers = "localhost:9092"
    val kafkaTopic = "user-events"
    val descriptorFile = "user_event.desc"
    val messageName = "UserEvent"

    println("\n[Streaming Mode] Listening for messages...\n")

    // 1. Read from Kafka (streaming)
    val kafkaStreamDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", kafkaTopic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss", "false")
      .load()

    // 2. Deserialize Protobuf messages
    val deserializedStreamDF = kafkaStreamDF
      .select(
        from_protobuf(col("value"), messageName, descriptorFile).alias("event")
      )
      .select(
        col("event.userId").alias("userId"),
        col("event.action").alias("action"),
        col("event.value").alias("value")
      )

    // 3. Clean invalid data
    val validStreamDF = deserializedStreamDF
      .filter(col("userId").isNotNull && col("action").isNotNull)
      .na.fill(0.0, Seq("value"))

    // 4. Count events per action
    val actionCountStreamDF = validStreamDF
      .groupBy("action")
      .agg(count("*").alias("event_count"))

    // Print action counts every 10 seconds
    val query1 = actionCountStreamDF.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", false)
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .start()

    // 5. Top users by value
    val topUsersStreamDF = validStreamDF
      .groupBy("userId")
      .agg(sum("value").alias("total_value"))
      .orderBy(desc("total_value"))
      .limit(5)

    val query2 = topUsersStreamDF.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", false)
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .start()

    // Keep the stream alive forever
    query1.awaitTermination()
    query2.awaitTermination()
  }
}
