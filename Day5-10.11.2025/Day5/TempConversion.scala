def convertTemp(value: Double, scale: String) = {
    scale match {
        case "C" => (value - 32) * 5 / 9
        case "F" => (value * 9 / 5) + 32
        case _ => value
    }
}

// using if else

def convertTemp1(value: Double, scale: String): Double = {
    if scale.equalsIgnoreCase("C") then
        (value - 32) * 5 / 9
    else if scale.equalsIgnoreCase("F") then
        (value * 9 / 5) + 32
    else
        value
} // if else if else as expression

// Example usage:
object Main extends App {
    println(convertTemp(100, "C")) // Output: 37.77777777777778
    println(convertTemp(37.7778, "F")) // Output: 100.0
    println(convertTemp(50, "K")) // Output: 50.0

    println(convertTemp1(104, "C")) // Output: 40.0
}