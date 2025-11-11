case class Money(amount: Double) {

  // Addition with implicit rounding
  def +(other: Money)(using precision: Double): Money = {
    Money(round(amount + other.amount, precision))
  }

  // Subtraction with implicit rounding
  def -(other: Money)(using precision: Double): Money = {
    Money(round(amount - other.amount, precision))
  }

  // Helper function to round to nearest precision
  private def round(value: Double, precision: Double): Double = {
    (Math.round(value / precision) * precision * 100).round / 100.0
  }

  override def toString: String = f"Money($amount%.2f)"
}

object Main extends App {

  // Default rounding precision
  given defaultPrecision: Double = 0.05

  val m1 = Money(10.23)
  val m2 = Money(5.19)

  println(m1 + m2) // Money(15.20)
  println(m1 - m2) // Money(5.05)

  // Override precision locally using 'using' in the method call
  val localPrecision: Double = 0.1
  println(m1.+(m2)(using localPrecision)) // Money(15.2)
  println(m1.-(m2)(using localPrecision)) // Money(5.1)
}
