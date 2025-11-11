implicit class RichString(val s: String) extends AnyVal {
  // Repeat string t times
  def *(t: Int): String = List.fill(t)(s).mkString
  
  // Concatenate with a space
  def ~(t: String): String = s + " " + t
}

object Main extends App {
  println("Hi" * 3)       // HiHiHi
  println("Hi" ~ "Hello") // Hi Hello
}
