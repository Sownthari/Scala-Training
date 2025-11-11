def moodChanger(prefix: String): String => String = {
    word => s"$prefix-$word-$prefix"
}

object Main extends App {
    val happyMood = moodChanger("happy")
    println(happyMood("night"))

    val sadMood = moodChanger("sad")
    println(sadMood("day"))
    println(moodChanger("hello")("sownthari"))
}