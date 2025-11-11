def flatteningMap(list: List[(String, List[String])]): List[String] = {
    for {
        (a,b) <- list
        b1 <- b
    } yield s"$a : $b1" 
}

def flatteningMap1(list: List[(String, List[String])]): List[String] = {
    list.flatMap { case (a, b) => 
        b.map { b1 => 
            s"$a : $b1" 
        } 
    }
}

// Example usage:
object FlatmapApp extends App {
    val departments = List( 
        ("IT", List("Ravi", "Meena")), 
        ("HR", List("Anita")), 
        ("Finance", List("Vijay", "Kiran")) 
    ) 
    val data = departments
    val result = flatteningMap1(data)
    println("Flattened Map Result:")
    result.foreach(println)
}