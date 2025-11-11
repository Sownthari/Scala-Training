def discountStrategy(memberType: String): Double => Double = {
    memberType match {
        case "gold" => amount => amount - (amount * 20/100)
        case "silver" => amount => amount - (amount * 10/100)
        case _ => amount => amount
    }
}

object Main extends App {
    val goldDiscount = discountStrategy("gold")
    println(goldDiscount(1000))
    println(discountStrategy("silver")(1000))
    println(discountStrategy("platinum")(800))
}