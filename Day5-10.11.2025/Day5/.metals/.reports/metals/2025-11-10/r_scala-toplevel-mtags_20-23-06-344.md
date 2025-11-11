error id: file://<WORKSPACE>/ShapeAnalyser.scala:[213..213) in Input.VirtualFile("file://<WORKSPACE>/ShapeAnalyser.scala", "case class Circle(r: Double)
case class Rectangle(w: Double, h: Double)

def area(shape: Any): Double = shape match {
    case Circle(r) => r * r
    case Rectangle(w, h) => 2 * w * h
    case _ => -1.0
}

object ")
file://<WORKSPACE>/file:<WORKSPACE>/ShapeAnalyser.scala
file://<WORKSPACE>/ShapeAnalyser.scala:10: error: expected identifier; obtained eof
object 
       ^
#### Short summary: 

expected identifier; obtained eof