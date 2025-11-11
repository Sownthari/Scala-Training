case class Rational(n: Int, d: Int) {

  require(d != 0, "Denominator cannot be zero")

  // Overload division: Rational / Rational
  def /(that: Rational): Rational = Rational(this.n * that.d, this.d * that.n)

  override def toString: String = s"Rational($n,$d)"
}

// 1️⃣ Implicit conversion from Int to Rational
import scala.language.implicitConversions

implicit def intToRational(n: Int): Rational = Rational(n, 1)

object Main extends App {
  val r1 = Rational(2, 3)
  val r2 = 1 / r1       // Implicit conversion: 1 -> Rational(1,1), then divide
  println(r2)           // Rational(3,2)

  val r3 = 4 / Rational(5,2)  // Rational(4,1) / Rational(5,2) = Rational(8,5)
  println(r3)                   // Rational(8,5)

  // Normal Int / Int still works
  val x = 6 / 3
  println(x)                   // 2
}
