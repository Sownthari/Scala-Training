file://<WORKSPACE>/PersonalizedCalculator.scala
### java.lang.NullPointerException: Cannot invoke "scala.meta.internal.pc.CompilerWrapper.compiler()" because "access" is null

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
uri: file://<WORKSPACE>/PersonalizedCalculator.scala
text:
```scala
def calculate(op: String)(x: Int, y:Int): Int = {
    op match {
        case "add" x+y
        case "sub" x-
    }
}
```



#### Error stacktrace:

```
dotty.tools.pc.ScalaPresentationCompiler.semanticdbTextDocument$$anonfun$1(ScalaPresentationCompiler.scala:240)
```
#### Short summary: 

java.lang.NullPointerException: Cannot invoke "scala.meta.internal.pc.CompilerWrapper.compiler()" because "access" is null