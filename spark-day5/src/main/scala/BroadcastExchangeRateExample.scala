import org.apache.spark.sql.{SparkSession, Row}

object BroadcastExchangeRateExample {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Broadcast Exchange Rate Example")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ----------------------------------------------------------
    // 1) Small dataset → exchange rates (best candidate to broadcast)
    // ----------------------------------------------------------
    val exchangeRates = Map(
      "USD" -> 1.0,
      "EUR" -> 1.08,
      "INR" -> 0.012,
      "JPY" -> 0.0064,
      "GBP" -> 1.25,
      "AUD" -> 0.66,
      "CAD" -> 0.74
    )

    val broadcastRates = spark.sparkContext.broadcast(exchangeRates)

    // ----------------------------------------------------------
    // 2) Large dataset → transactions
    // (assume this could be millions of records)
    // ----------------------------------------------------------
    val transactions = Seq(
      ("T001", 120.50, "USD"),
      ("T002", 8500.00, "JPY"),
      ("T003", 75.00, "EUR"),
      ("T004", 560.00, "INR"),
      ("T005", 210.99, "GBP"),
      ("T006", 1500.00, "JPY"),
      ("T007", 45.50,  "EUR"),
      ("T008", 999.00, "AUD"),
      ("T009", 3200.00,"INR"),
      ("T010", 88.00,  "CAD")
    ).toDF("transaction_id", "amount", "currency")

    println("Original Transactions:")
    transactions.show()

    // ----------------------------------------------------------
    // 3) Convert all transaction amounts to USD using broadcast
    // ----------------------------------------------------------
    val enriched = transactions.map(row => {
      val txId = row.getString(0)
      val amount = row.getDouble(1)
      val currency = row.getString(2)

      val rate = broadcastRates.value.getOrElse(currency, 0.0)
      val amountUSD = amount * rate

      (txId, amount, currency, amountUSD)
    }).toDF("transaction_id", "amount", "currency", "amount_in_usd")

    println("Transactions Converted to USD:")
    enriched.show()

    // ----------------------------------------------------------
    // 4) Count number of transactions per currency
    // (no shuffle on USD conversion step)
    // ----------------------------------------------------------
    val counts = enriched
      .groupBy("currency")
      .count()

    println("Transaction Count per Currency:")
    counts.show()

    System.in.read()
    spark.stop()
  }
}
