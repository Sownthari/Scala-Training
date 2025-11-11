def mirrorArray(arr: Array[Int]): Array[Int] = {
    val reverse = for (i <- arr.length - 1 to 0 by -1) yield arr(i)
    val mirrored: Array[Int] = for (i <- 0 until arr.length*2 by 1) yield {
        if (i < arr.length) then arr(i)
        else reverse(i - arr.length)
    }
    mirrored.toArray
} // used for yield to create mirrored array, until loop with if else without mutable variables

def mirrorArray1(arr: Array[Int]): Array[Int] = {
    arr.clone() ++ arr.reverse
} // simpler version using built-in reverse method that concatenates original and reversed arrays

object ArrayMirrorPuzzle extends App {
    val originalArray = Array(1, 2, 3, 4, 5)
    val mirroredArray = mirrorArray(originalArray)
    println(mirroredArray.mkString(", ")) // Output: 1, 2, 3, 4, 5, 5, 4, 3, 2, 1
}

