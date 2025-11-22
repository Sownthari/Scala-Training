package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.libs.json._
import repository.AuthRepository
import model.{ErrorResponse, LoginRequest, LoginResponse, UserContext}
import model.AuthModels._
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import play.api.Configuration

@Singleton
class AuthController @Inject()(cc: ControllerComponents, authRepo: AuthRepository, config: Configuration)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  private val secret            = config.get[String]("auth.jwt.secret")
  private val issuer            = config.get[String]("auth.jwt.issuer")
  private val expirationMinutes = config.get[Int]("auth.jwt.expirationMinutes")

  // Login Endpoint
  def login: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[LoginRequest].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      req =>
        authRepo.authenticate(req.email, req.password).map {
          case Some(userCtx) =>
            val token = createJWT(userCtx)
            Ok(Json.toJson(LoginResponse(token)))
          case None =>
            Unauthorized(Json.toJson(ErrorResponse("Invalid email or password")))
        }
    )
  }

  private val algorithm = Algorithm.HMAC256(secret)

  private def createJWT(user: UserContext): String = {
    val claimJson = Json.toJson(user).toString()

    JWT.create()
      .withIssuer(issuer)
      .withClaim("user", claimJson)
      .withExpiresAt(new java.util.Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000))
      .sign(algorithm)
  }
}
