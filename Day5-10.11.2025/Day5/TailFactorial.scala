def factorial(n: Int): Int = {
    @annotation.tailrec // tail-recursive helper function and @annotation.tailrec to ensure optimization, compiler checks whether the function is tail-recursive
    def loop(n: Int, acc: Int): Int = {
        if (n <= 1) acc
        else loop(n - 1, n * acc) // recursive call with decremented n and updated accumulator at the end of the function
    }
    loop(n, 1) // initial call to helper function with accumulator set to 1
}

// with the help of accumulator to store intermediate results, making it more efficient for large n using the same stack frame

// Example usage:
object TailFactorialApp extends App {
    val number = 5
    val result = factorial(number)
    println(s"The factorial of $number is $result") // Output: The factorial of 5 is 120
}