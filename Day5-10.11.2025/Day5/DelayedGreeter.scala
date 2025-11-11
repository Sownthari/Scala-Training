def delayedMessage(delayMs: Int)(message: String): Unit = { 
 Thread.sleep(delayMs) 
 println(message) 
} 

object Main extends App {
    val onSecondDelay = delayedMessage(1000)
    onSecondDelay("hello")
    onSecondDelay("Scala")
}       