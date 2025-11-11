error id: file://<WORKSPACE>/IntentionalCrasher.scala:[8..11) in Input.VirtualFile("file://<WORKSPACE>/IntentionalCrasher.scala", "object 
def safeDivide(n: Int, s: String): PartialFunction[Int, String] = {
    if n != 0 then f"Result ${100/x}"
    else "not defined"
}

val safe = safeDivide.lift 
println(safe(10)) // Some("Result: 10") 
println(safe(0)) // None
")
file://<WORKSPACE>/file:<WORKSPACE>/IntentionalCrasher.scala
file://<WORKSPACE>/IntentionalCrasher.scala:2: error: expected identifier; obtained def
def safeDivide(n: Int, s: String): PartialFunction[Int, String] = {
^
#### Short summary: 

expected identifier; obtained def