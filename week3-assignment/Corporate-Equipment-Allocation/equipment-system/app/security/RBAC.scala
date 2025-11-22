package security

import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import model.Role.Role

class RBAC @Inject()(val parser: BodyParsers.Default)(implicit val ec: ExecutionContext) {

  def WithRole(requiredRoles: List[Role]): ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent] {

      override def parser: BodyParser[AnyContent] = RBAC.this.parser
      override protected def executionContext: ExecutionContext = RBAC.this.ec

      override def invokeBlock[A](
                                   request: Request[A],
                                   block: Request[A] => Future[Result]
                                 ): Future[Result] = {

        request.attrs.get(UserAttr.Key) match {

          case Some(user) if requiredRoles.contains(user.role) =>
            block(request)

          case Some(_) =>
            Future.successful(Results.Forbidden("Access denied: insufficient role"))

          case None =>
            Future.successful(Results.Unauthorized("Missing or invalid token"))
        }
      }
    }
}
