def multiplicationTable(n: Int): List[String] = {
  (1 to n).flatMap { i =>
    (1 to n).map { j =>
      f"$i x $j = ${i * j}%2d"
    }
  }.toList
}

// generates multiplication table up to n using nested for-yield loops, formatted output
// Example usage:
object MultiplicationTableApp extends App {
    val n = 5
    val table = multiplicationTable(n)
    table.foreach(row => println(row))
}