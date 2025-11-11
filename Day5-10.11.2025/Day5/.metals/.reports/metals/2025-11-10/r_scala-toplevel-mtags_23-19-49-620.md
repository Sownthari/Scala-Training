error id: file://<WORKSPACE>/ImplicitOrdering.scala:[241..242) in Input.VirtualFile("file://<WORKSPACE>/ImplicitOrdering.scala", "class Person(name: String, age:Int)

implicit class AgeOrder(val a: Person) extends AnyVal {
    def >(b: Person): boolean = a.age > b.age
    def >=(b: Person): boolean = a.age >= b.age
    def <(b: Person): boolean = a.age < b.age
    def (b: Person): boolean = a.age > b.age
}")
file://<WORKSPACE>/file:<WORKSPACE>/ImplicitOrdering.scala
file://<WORKSPACE>/ImplicitOrdering.scala:7: error: expected identifier; obtained lparen
    def (b: Person): boolean = a.age > b.age
        ^
#### Short summary: 

expected identifier; obtained lparen