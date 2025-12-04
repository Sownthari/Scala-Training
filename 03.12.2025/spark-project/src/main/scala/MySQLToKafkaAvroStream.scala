import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

import java.io.ByteArrayOutputStream
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.io.EncoderFactory

object MySQLToKafkaAvroStream {
  val config = ConfigFactory.load()
  var lastMaxOrderId: Int = 0

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("MySQL-To-Kafka-Avro-Streaming")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    println("=== Starting MySQL to Kafka Avro Streaming Pipeline ===")

    //-----------------------------
    // Load Avro Schema
    //-----------------------------
    val schemaStream = getClass.getResourceAsStream("/orders.avsc")
    val schemaString = scala.io.Source.fromInputStream(schemaStream).mkString
    schemaStream.close()
    val avroSchema = new Schema.Parser().parse(schemaString)
    println(s"Loaded Avro schema: ${avroSchema.getName}")

    //-----------------------------
    // Fake stream to trigger batch every 5s
    //-----------------------------
    val triggerStream = spark.readStream
      .format("rate")
      .option("rowsPerSecond", 1)
      .load()

    //-----------------------------
    // foreachBatch: poll MySQL and send new rows to Kafka
    //-----------------------------
    val query = triggerStream.writeStream
      .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("5 seconds"))
      .foreachBatch { (_: DataFrame, batchId: Long) =>

        println(s"\n=== Batch $batchId - Checking for new orders ===")

        try {
          // 1. Read ONLY new rows
          val jdbcUrl = config.getString("mysql.url")
          val props = new java.util.Properties()
          props.setProperty("user", config.getString("mysql.user"))
          props.setProperty("password", config.getString("mysql.password"))
          props.setProperty("driver", config.getString("mysql.driver"))

          val newRowsDF = spark.read
            .jdbc(jdbcUrl, "new_orders", props)
            .filter(col("order_id") > lastMaxOrderId)

          val rowCount = newRowsDF.count()

          if (rowCount == 0) {
            println(s"No new records (lastMaxOrderId = $lastMaxOrderId)")
          } else {

            println(s"Found $rowCount new records")

            // Update high-watermark
            val maxOrderIdValue = newRowsDF.agg(max("order_id")).first().get(0)
            if (maxOrderIdValue != null) {
              lastMaxOrderId = maxOrderIdValue.asInstanceOf[Number].intValue()
              println(s"Updated lastMaxOrderId = $lastMaxOrderId")
            }

            // Serialize each row to Avro
            val serializeToAvro = udf { (orderId: Int, customerId: Int, amount: Double, createdAt: String) =>
              val record: GenericRecord = new GenericData.Record(avroSchema)
              record.put("order_id", orderId)
              record.put("customer_id", customerId)
              record.put("amount", amount)
              record.put("created_at", createdAt)

              val out = new ByteArrayOutputStream()
              val writer = new GenericDatumWriter[GenericRecord](avroSchema)
              val encoder = EncoderFactory.get().binaryEncoder(out, null)
              writer.write(record, encoder)
              encoder.flush()
              val bytes = out.toByteArray
              out.close()
              bytes
            }

            val avroDF = newRowsDF
              .withColumn(
                "value",
                serializeToAvro(
                  col("order_id"),
                  col("customer_id"),
                  col("amount"),
                  col("created_at").cast("string")
                )
              ).select("value")

            // Send to Kafka
            avroDF.write
              .format("kafka")
              .option("kafka.bootstrap.servers", "localhost:9092")
              .option("topic", "orders_avro_topic")
              .save()

            println(s"Successfully sent $rowCount records to Kafka topic: orders_avro_topic")
          }

        } catch {
          case e: Exception =>
            println(s"Error in batch $batchId: ${e.getMessage}")
            e.printStackTrace()
        }
      }
      .start()

    println("Streaming query started. Press Ctrl+C to stop.")
    query.awaitTermination()
  }
}