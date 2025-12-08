import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

object SalesCacheExample {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SalesCacheExample")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext

    // ----------------------------------------------------------
    // 1) Large sales dataset (customerId, productId, quantity, amount)
    // ----------------------------------------------------------
    val sales: RDD[(Int, Int, Int, Double)] = sc.parallelize(
      Seq(
        (1, 101, 2, 50.0),
        (1, 102, 1, 25.0),
        (2, 101, 3, 75.0),
        (2, 103, 1, 45.0),
        (3, 102, 4, 100.0),
        (3, 104, 2, 60.0)
      ),
      numSlices = 4
    )

    // ----------------------------------------------------------
    // Helper function to measure execution time
    // ----------------------------------------------------------
    def time[R](block: => R): (R, Long) = {
      val start = System.nanoTime()
      val result = block
      val end = System.nanoTime()
      (result, (end - start) / 1000000) // ms
    }

    // ----------------------------------------------------------
    // Define shared dataset (this is the dataset we will cache)
    // ----------------------------------------------------------
    val shared = sales
      .map(s => (s._1, s._2, s._3, s._4)) // identity transform, placeholder
    // This dataset will be reused by two branches:
    // - Total amount per customer
    // - Total quantity per product

    // ----------------------------------------------------------
    // WITHOUT CACHE – measure time
    // ----------------------------------------------------------
    val (_, time1) = time {
      val totalAmountPerCustomer = shared           // reuse raw RDD
        .map { case (cId, _, _, amount) => (cId, amount) }
        .reduceByKey(_ + _)
        .collect()

      val totalQtyPerProduct = shared               // reuse raw RDD
        .map { case (_, pId, qty, _) => (pId, qty) }
        .reduceByKey(_ + _)
        .collect()
    }

    println(s"Execution time WITHOUT cache: $time1 ms")

    // ----------------------------------------------------------
    // WITH CACHE
    // ----------------------------------------------------------
    shared.persist(StorageLevel.MEMORY_ONLY)

    // first action forces caching
    shared.count()

    // Run the same operations again
    val (_, time2) = time {
      val totalAmountPerCustomer = shared
        .map { case (cId, _, _, amount) => (cId, amount) }
        .reduceByKey(_ + _)
        .collect()

      val totalQtyPerProduct = shared
        .map { case (_, pId, qty, _) => (pId, qty) }
        .reduceByKey(_ + _)
        .collect()
    }

    println(s"Execution time WITH cache: $time2 ms")

    // ----------------------------------------------------------
    // Unpersist to free memory
    // ----------------------------------------------------------
    shared.unpersist()

    spark.stop()
  }
}
