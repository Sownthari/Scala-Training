// cartesian product of two lists

def cartesianProduct(list1: List[String], list2: List[String]): List[(String, String)] = {
    for {
        a <- list1
        b <- list2
        if a.length >= b.length
    } yield(a, b) // for yield with filter condition that takes 2 lists and returns their cartesian product as list of tuples
}

//with flatMap and map
def cartesianProduct1(list1: List[String], list2: List[String]): List[(String, String)] = {
    list1.flatMap { a =>
        list2.map { b =>
            (a, b)
        }
    }.filter { case (a, b) => a.length >= b.length }
}   

def cartesianProduct2(list1: List[String], list2: List[String]): List[(String, String)] = {
    list1.flatMap { a => 
        list2.withFilter { b => 
            a.length >= b.length 
        }.map { b => 
            (a, b) 
        }
    }
}
// Example usage:
object CartesianApp extends App {
    val listA = List("Asha", "Bala", "Chitra")
    val listB = List("Math", "Physics")
    val product = cartesianProduct(listA, listB)
    println("Cartesian Product (with length condition):")
    product.foreach { case (a, b) => println(s"($a, $b)") }
}