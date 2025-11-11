class Counter(val value:Int){
    def +(that: Counter): Int = {
        this.value + that.value
    }

    def +(that: Int): Int = {
        this.value + that
    }
}



object Main extends App {
    val a = Counter(10)
    val b = Counter(5)
    println(a + b)
    println(a + 10)
}