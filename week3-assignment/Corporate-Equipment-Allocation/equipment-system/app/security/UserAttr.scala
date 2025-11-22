package security

import play.api.libs.typedmap.TypedKey
import model.UserContext

object UserAttr {
  val Key: TypedKey[UserContext] = TypedKey[UserContext]("user")
}
