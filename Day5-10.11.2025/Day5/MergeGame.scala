object Main extends App {
    val a = List(1, 2)
    val b = List(3, 4)
    println(a :: b)
    println(a ++ b) // works across multiple collections
    println(a ::: b) // it works only for list and maintains order
}