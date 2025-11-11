error id: file://<WORKSPACE>/AdaptiveDiscount.scala:[259..259) in Input.VirtualFile("file://<WORKSPACE>/AdaptiveDiscount.scala", "def discountStrategy(memberType: String): Double => Double = {
    memberType match {
        case "gold" => amount => amount - (amount * 20/100)
        case "silver" => amount => amount - (amount * 10/100)
        case _ => amount => amount
    }
}

object ")
file://<WORKSPACE>/file:<WORKSPACE>/AdaptiveDiscount.scala
file://<WORKSPACE>/AdaptiveDiscount.scala:9: error: expected identifier; obtained eof
object 
       ^
#### Short summary: 

expected identifier; obtained eof