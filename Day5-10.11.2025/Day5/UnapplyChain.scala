case class Address(city: String, pincode: Int)
case class Person(name: String, address: Address)

object Main extends App {
  val p = Person("Ravi", Address("Chennai", 600001))

  p match {
    case Person(name, Address(city, pin)) =>
      if city.startsWith("C") then println(s"$city - $pin")
    case _ => println("No match")
  }

}
