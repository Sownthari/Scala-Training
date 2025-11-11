case class Order(id: Int, amount: Double, status: String) // case class definition for order

def getOrders(orders: List[Order]): List[String] = {
    for{
        order <- orders
        if order.amount > 500 && order.status == "Delivered" } yield f"Order #${order.id} -> ₹${order.amount}" // for to iterate and filter the orders
}

object Main extends App{
    val orders = List(
    Order(1, 1200.0, "Delivered"),
    Order(2, 250.0, "Pending"),
    Order(3, 980.0, "Delivered"),
    Order(4, 75.0, "Cancelled")
    )

    val result = getOrders(orders).foreach(println) //prints each order that filtered.
}

