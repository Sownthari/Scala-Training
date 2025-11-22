package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import play.api.libs.json.Json
import play.api.Configuration
import model.UserContext

import java.util.Date
import javax.inject.Inject

class JwtUtil @Inject() (config: Configuration) {

  private val secretKey = config.get[String]("auth.jwt.secret")
  private val issuer    = config.get[String]("auth.jwt.issuer")
  private val algorithm = Algorithm.HMAC256(secretKey)

  def generateToken(userCtx: UserContext, expirationMillis: Long = 3600000): String = {
    val now = System.currentTimeMillis()
    val userJson = Json.toJson(userCtx).toString()

    JWT.create()
      .withIssuer(issuer)
      .withClaim("user", userJson)
      .withIssuedAt(new Date(now))
      .withExpiresAt(new Date(now + expirationMillis))
      .sign(algorithm)
  }

  def validateToken(token: String): Option[UserContext] = {
    try {
      val verifier = JWT.require(algorithm).withIssuer(issuer).build()
      val decoded = verifier.verify(token)
      val userJsonString = decoded.getClaim("user").asString()
      Json.parse(userJsonString).asOpt[UserContext]
    } catch {
      case _: JWTVerificationException => None
    }
  }
}
