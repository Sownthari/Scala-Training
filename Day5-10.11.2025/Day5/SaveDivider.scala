def safeDivide(x: Int, y:Int) : Option[Int] = {
    if y != 0 then Some(x/y)
    else None
}

object SafeDivide extends App {
    var result = safeDivide(10,0).getOrElse(-1)
    println(result)
}