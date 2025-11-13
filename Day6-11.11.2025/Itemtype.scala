package library.items

sealed trait ItemType {
    def title: String
}

final case class Book(title: String) extends ItemType
final case class Magazine(title: String) extends ItemType
final case class DVD(title: String) extends ItemType