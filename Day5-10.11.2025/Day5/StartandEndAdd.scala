object Main extends App {
    var nums = List(2, 4, 6)
    nums = nums :+ 8
    nums = 0 +: nums
    println(nums)
}