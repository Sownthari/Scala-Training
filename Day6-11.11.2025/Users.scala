package library.users

import library.items.ItemType

class Member(val name: String) {
  def borrowItem(item: ItemType): Unit =
    println(s"$name borrowed '${item.title}'.")
}
