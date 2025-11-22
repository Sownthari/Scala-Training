package security

import org.apache.pekko.stream.Materializer
import play.api.http.HttpFilters
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import model.{UserAttr, UserContext}

class JwtAuthFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext, jwtUtil: JwtUtil) extends Filter {

  private val publicRoutes = Seq("/api/auth/login")

  override def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {

    // Allow public endpoints
    if (publicRoutes.exists(request.path.startsWith)) {
      nextFilter(request)
    } else {

      val tokenOpt = request.headers.get("Authorization")
        .map(_.replace("Bearer ", "").trim)

      val maybeUserCtx: Option[UserContext] = tokenOpt.flatMap(jwtUtil.validateToken)

      maybeUserCtx match {
        case Some(userCtx) =>
          // attach UserContext to request attributes
          val updatedReq = request.addAttr(UserAttr.Key, userCtx)
          nextFilter(updatedReq)

        case None =>
          Future.successful(Results.Unauthorized("Invalid or missing token"))
      }
    }
  }
}

class Filters @Inject()(jwtAuthFilter: JwtAuthFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(jwtAuthFilter)
}
