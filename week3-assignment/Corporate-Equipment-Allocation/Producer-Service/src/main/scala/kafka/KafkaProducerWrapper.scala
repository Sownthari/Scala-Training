package kafka

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.concurrent.{Future, Promise}
import play.api.libs.json.JsValue

class KafkaProducerWrapper(bootstrap: String) {

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrap)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put("acks", "all")

  private val producer = new KafkaProducer[String, String](props)

  def send(topic: String, key: String, json: JsValue): Future[Unit] = {
    val p = Promise[Unit]()
    val record = new ProducerRecord[String, String](topic, key, json.toString())

    producer.send(record, (meta, ex) => {
      if (ex != null) p.failure(ex)
      else p.success(())
    })

    p.future
  }

  def close(): Unit = {
    producer.flush()
    producer.close()
  }
}
