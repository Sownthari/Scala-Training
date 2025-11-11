def maxInArray(arr: Array[Int]): Int = {
    if(arr.isEmpty) then 0
    else if(arr.length == 1) then arr(0)
    else {
        @annotation.tailrec
        def loop(index: Int, currentMax: Int): Int = {
            if(index == arr.length) then currentMax
            else{
                val newMax = if(arr(index) > currentMax) arr(index) else currentMax
                loop(index + 1, newMax)
            }
        }
        loop(1, 0)
    }
} // tail-recursive function to find maximum in an array without mutable variables

// Example usage:
object TailMaxInArrayApp extends App {
    val array = Array(3, 5, 2, 9, 14)
    val maxVal = maxInArray(array)
    println(s"The maximum value in the array is: $maxVal") // Output: The maximum value in the array is: 14
}