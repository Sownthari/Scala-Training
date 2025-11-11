def digitSum(n: Int): Int = {
    if (n == 0) then 0 // base case
    else (n % 10) + digitSum(n / 10) // recursive case
}

// Example usage:
object DigitSumApp extends App {
    val number = 12345
    val sum = digitSum(number)
    println(s"The sum of digits in $number is $sum") // Output: The sum of digits in 12345 is 15
}