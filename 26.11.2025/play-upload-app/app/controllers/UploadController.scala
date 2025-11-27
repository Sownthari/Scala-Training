package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import services.FileUploadService
import play.api.libs.Files.TemporaryFile

@Singleton
class UploadController @Inject()(
                                  cc: ControllerComponents,
                                  uploadService: FileUploadService
                                )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def upload: Action[MultipartFormData[TemporaryFile]] =
    Action(parse.multipartFormData).async { request =>
      request.body.file("file") match {

        case Some(filePart) =>
          uploadService.saveFile(filePart).map { savedPath =>
            Ok(s"File uploaded to: $savedPath")
          }

        case None =>
          Future.successful(BadRequest("Missing file field 'file'"))
      }
    }
}
