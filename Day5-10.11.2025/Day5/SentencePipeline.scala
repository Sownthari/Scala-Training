val trimSpaces: String => String = _.trim 
val toLower: String => String = _.toLowerCase 
val capitalizeFirst: String => String = s => s.head.toUpper + s.tail 

object Main extends App {
    val messy = " HeLLo WOrld"
    def result: String => String = trimSpaces andThen toLower andThen capitalizeFirst
    println(result(messy))
}