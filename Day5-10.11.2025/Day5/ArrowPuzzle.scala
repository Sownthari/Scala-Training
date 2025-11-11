object Main extends App {
    var animals = Map( 
    "dog" -> "bark", 
    "cat" -> "meow", 
    "cow" -> "moo" 
    ) 
    animals = animals ++ Map("lion"-> "roar")
    println(animals)
    println(animals("cow"))
    println(animals.getOrElse("tiger", None))

}