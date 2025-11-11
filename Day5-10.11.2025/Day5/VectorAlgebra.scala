class Vec2D(val a: Int, val b: Int) {
  def +(that: Vec2D): Vec2D = new Vec2D(this.a + that.a, this.b + that.b)
  def -(that: Vec2D): Vec2D = new Vec2D(this.a - that.a, this.b - that.b)
  def *(scalar: Int): Vec2D = new Vec2D(this.a * scalar, this.b * scalar)
  
  override def toString: String = s"Vec2D($a, $b)"
}

// Extension method to enable 3 * v1
extension (scalar: Int)
  def *(v: Vec2D): Vec2D = v * scalar

object Main extends App {
  val v1 = new Vec2D(2, 3)
  val v2 = new Vec2D(4, 5)
  
  println(v1 + v2)   // Vec2D(6, 8)
  println(v1 - v2)   // Vec2D(-2, -2)
  println(v1 * 3)    // Vec2D(6, 9)
  println(3 * v1)    // Vec2D(6, 9)
}
