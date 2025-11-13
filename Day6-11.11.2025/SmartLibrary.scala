import library.items._
import library.users._
import library.operations.LibraryOps._

object Library extends App {

  // Using the default implicit member
  borrow(Book("The Scala Journey"))
  borrow(Magazine("Tech Monthly"))
  borrow(DVD("Interstellar"))

  // Implicit conversion: String → Book
  borrow("Programming in Scala")

  // Explicit given Member
  val alice:Member = new Member("Alice")
  borrow(Book("AI for Everyone"))(using alice)
 
  println(itemDescription(Book("1984")))
  println(itemDescription(Magazine("National Geographic")))
  println(itemDescription(DVD("Inception")))
}
