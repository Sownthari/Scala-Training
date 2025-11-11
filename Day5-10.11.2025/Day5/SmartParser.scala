def parseAndDivide(input: String): Either[String, Int] = {
    input.toIntOption match
    case Some(value) => safeDivide(100, value)
    case None        => Left("Invalid Number")
}

def safeDivide(x: Int, y:Int) : Either[String, Int] = {
    if y != 0 then  Right(x/y)
    else Left("Division By Zero")
}

object ParseAndDivide extends App {
    var result = parseAndDivide("25")
    println(result)
}