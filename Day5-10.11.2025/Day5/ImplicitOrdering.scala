class Person(val name: String, val age: Int)

implicit class AgeOrder(val a: Person) extends AnyVal {
  def >(b: Person): Boolean = a.age > b.age
  def >=(b: Person): Boolean = a.age >= b.age
  def <(b: Person): Boolean = a.age < b.age
  def <=(b: Person): Boolean = a.age <= b.age
}

object Main extends App {
  val p1 = new Person("Ravi", 25)
  val p2 = new Person("Meena", 30)

  println(p1 < p2)   // true
  println(p1 >= p2)  // false
}
