error id: file://<WORKSPACE>/DualNature.scala:[258..258) in Input.VirtualFile("file://<WORKSPACE>/DualNature.scala", "object Email {
  def apply(user: String, domain: String): String =
    s"$user@$domain"
  def unapply(email: String): Option[(String, String)] = {
    val parts = email.split("@")
    if (parts.length == 2) Some((parts(0), parts(1))) else None
  }
}

object ")
file://<WORKSPACE>/file:<WORKSPACE>/DualNature.scala
file://<WORKSPACE>/DualNature.scala:10: error: expected identifier; obtained eof
object 
       ^
#### Short summary: 

expected identifier; obtained eof