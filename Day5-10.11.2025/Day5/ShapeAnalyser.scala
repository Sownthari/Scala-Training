case class Circle(r: Double){
    val pi = 3.14
}
case class Rectangle(w: Double, h: Double)

def area(shape: Any): Double = shape match {
    case c: Circle => c.pi * c.r * c.r
    case Rectangle(w, h) => w * h
    case _ => -1.0
}

object Shape extends App {
    var circle = Circle(3)
    var rectangle = Rectangle(2,5)

    println(f"${area(circle)}%2.2f")
    println(area(rectangle))
}