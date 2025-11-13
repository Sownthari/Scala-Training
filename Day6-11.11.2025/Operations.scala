package library.operations

import library.items._
import library.users.Member

object LibraryOps {

  implicit def stringToBook(title: String): Book = Book(title)

  implicit val defaultMember: Member = new Member("Default Member")

  def borrow(item: ItemType)(using member: Member): Unit =
    member.borrowItem(item)

  def itemDescription(item: ItemType): String = item match {
    case Book(title)     => s"Book titled '$title'"
    case Magazine(title) => s"Magazine titled '$title'"
    case DVD(title)      => s"DVD titled '$title'"
  }
}
