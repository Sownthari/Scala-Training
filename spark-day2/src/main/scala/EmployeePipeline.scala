import org.apache.spark.sql.SparkSession
import scala.util.Random
import org.apache.spark.sql.functions._

object EmployeePipeline {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("EmployeePipeline1M")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // ---------------------------------------------------------
    // 1. Generate 1M employee records
    // ---------------------------------------------------------
    val numEmp = 1000000
    val deptList: Array[String] = Array("HR", "IT", "Sales", "Finance")

    val empRDD = spark.sparkContext
      .parallelize(1 to numEmp, 20)
      .map { id =>
        val name  = Random.alphanumeric.take(7).mkString
        val dept  = deptList(Random.nextInt(deptList.length))
        val salary = 30000 + Random.nextInt(70000)
        (id, name, dept, salary)
      }

    val empDF = empRDD.toDF("empId", "name", "department", "salary")

    // ---------------------------------------------------------
    // 2. Average salary per department
    // ---------------------------------------------------------
    val avgSalDF = empDF
      .groupBy("department")
      .agg(avg("salary").as("avgSalary"))

    avgSalDF.show(truncate = false)

    // ---------------------------------------------------------
    // 3. Write result to CSV
    // ---------------------------------------------------------
    avgSalDF.write
      .mode("overwrite")
      .option("header", "true")
      .csv("output/avg_salary_csv")

    // ---------------------------------------------------------
    // 4. Read CSV back & compare schema
    // ---------------------------------------------------------
    val readBackDF = spark.read
      .option("header", "true")
      .csv("output/avg_salary_csv")

    println("Schema after reading CSV:")
    readBackDF.printSchema()

    // Keep UI open for inspection
    Thread.sleep(5000000)

    spark.stop()
  }
}
