

def wordCount(lines: List[String]): Map[String, Int] = {
    val counts = scala.collection.mutable.Map[String, Int]()
    for{
        line <- lines
        word <- line.split(" ")
    } do {
        val w = word.toLowerCase
        counts(w) = counts.getOrElse(w, 0) + 1
    }
    counts.toMap
}

def wordCount1(lines: List[String]): Map[String, Int] = {
  val words = for {
    line <- lines
    word <- line.split(" ")
  } yield word.toLowerCase

  words.foldLeft(Map.empty[String, Int]) { (acc, w) =>
    acc + (w -> (acc.getOrElse(w, 0) + 1)) // acc contains the empty map at start and add the current as new key if not 
                                           //found and if key is available just update increment the value.
  } //using foldLeft
}

def wordCount2(lines: List[String]): Map[String, Int] = {
    lines.flatMap(_.split(" ")).groupBy(identity).view.mapValues(_.size).toMap

    // groupBy(identity) groups the words that matches and forms map i.e., scala -> List("scala", "scala")
    // view - creates a wrapper around Map and mapValues(._size) maps each key with count as value
} 


object Main extends App {
    val lines = List( 
    "Scala is powerful", 
    "Scala is concise", 
    "Functional programming is powerful" 
    )
    println(wordCount(lines))
}


