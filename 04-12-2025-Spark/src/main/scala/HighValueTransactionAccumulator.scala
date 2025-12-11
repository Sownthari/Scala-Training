import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD

object HighValueTransactionAccumulator {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("HighValueTransactionAccumulator")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext

    // ----------------------------------------------------------
    // Threshold for high-value transactions
    // ----------------------------------------------------------
    val threshold = 500.0

    // ----------------------------------------------------------
    // Accumulator to count high-value transactions
    // ----------------------------------------------------------
    val highValueCount = sc.longAccumulator("HighValueCounter")

    // ----------------------------------------------------------
    // Sample dataset of transaction amounts
    // (In real case, this could be millions of records)
    // ----------------------------------------------------------
    val transactions: RDD[Double] = sc.parallelize(
      Seq(120.0, 999.0, 450.0, 800.0, 520.0, 50.0, 1300.0, 499.9, 600.0, 75.5),
      numSlices = 4
    )

    // ----------------------------------------------------------
    // Process the dataset in parallel and update accumulator
    // ----------------------------------------------------------
    transactions.foreach(amount => {
      if (amount > threshold) {
        highValueCount.add(1)
      }
    })

    // ----------------------------------------------------------
    // Read accumulator result on driver
    // ----------------------------------------------------------
    println(s"Number of high-value transactions (> $threshold): ${highValueCount.value}")

    spark.stop()
  }
}
