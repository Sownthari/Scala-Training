import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession

object SalesETLJob {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()

    val spark = SparkSession.builder()
      .appName("Sales-ETL-Job")
      .master("local[*]")
      .config("spark.cassandra.connection.host", config.getString("spark.cassandra.connection.host"))
      .config("spark.cassandra.connection.port", config.getInt("spark.cassandra.connection.port"))

      .config("spark.cassandra.connection.localDC", config.getString("spark.cassandra.connection.localDC"))
      .config("spark.cassandra.connection.ssl.enabled", config.getBoolean("spark.cassandra.connection.ssl.enabled"))

      .config("spark.cassandra.connection.ssl.trustStore.path", config.getString("spark.cassandra.connection.ssl.trustStore.path"))
      .config("spark.cassandra.connection.ssl.trustStore.password", config.getString("spark.cassandra.connection.ssl.trustStore.password"))
      .config("spark.cassandra.auth.username", config.getString("spark.cassandra.auth.username"))
      .config("spark.cassandra.auth.password", config.getString("spark.cassandra.auth.password"))
      .getOrCreate()

    // -------------------------------
    // 1. Read MySQL Tables via JDBC
    // -------------------------------

    val mysqlUrl = config.getString("mysql.url")
    val mysqlProps = new java.util.Properties()
    mysqlProps.setProperty("user", config.getString("mysql.user"))
    mysqlProps.setProperty("password", config.getString("mysql.password"))
    mysqlProps.setProperty("driver", config.getString("mysql.driver"))

    val customersDF = spark.read.jdbc(mysqlUrl, "customers", mysqlProps)
    val ordersDF = spark.read.jdbc(mysqlUrl, "orders", mysqlProps)
    val orderItemsDF = spark.read.jdbc(mysqlUrl, "order_items", mysqlProps)

    // -----------------------------------
    // 2. Perform Two-Level Join
    // customers → orders → order_items
    // -----------------------------------

    val joinedDF =
      customersDF
        .join(ordersDF, customersDF("customer_id") === ordersDF("customer_id"))
        .join(orderItemsDF, ordersDF("order_id") === orderItemsDF("order_id"))

    // ------------------------------------------------
    // 3. Select Columns in Required Denormalized Shape
    // ------------------------------------------------

    val finalDF =
      joinedDF.select(
        customersDF("customer_id"),
        customersDF("name"),
        customersDF("email"),
        customersDF("city"),
        ordersDF("order_id"),
        ordersDF("order_date"),
        ordersDF("amount"),
        orderItemsDF("item_id"),
        orderItemsDF("product_name"),
        orderItemsDF("quantity")
      )

    // -----------------------
    // 4. Write to Keyspaces
    // -----------------------


    finalDF.write
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", config.getString("keyspaces.keyspace"))
      .option("table", config.getString("keyspaces.table"))
      .mode("append")
      .save()


    Thread.sleep(5000000)

    spark.stop()
  }
}

