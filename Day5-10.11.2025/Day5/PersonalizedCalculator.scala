def calculate(op: String)(x: Int, y:Int): Int = {
    op match {
        case "add" => x+y
        case "sub" => x-y
        case "mul" => x*y
        case "div" => x/y
    }
}

object Calculator extends App {
    val sum = calculate("add")
    val diff: (Int,Int) => Int = calculate("sub")
    val multiply: (Int, Int) => Int = calculate("mul")
    val divide: (Int,Int) => Int = calculate("div")

    println(sum(10,20))
    println(diff(20,10))
    println(multiply(10,20))
    println(divide(20,10))
}