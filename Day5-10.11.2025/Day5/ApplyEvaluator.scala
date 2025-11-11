object Evaluator {
    def apply(block: =>Any): Unit = {
        println(s"Evaluating block...")
        val result = block
        println(s"Result = $result")
    }
}

object Main extends App {
    val result = Evaluator{
        val x = 5
        val y = 6
        x + y * 2
    }
}