package services

import javax.inject._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import scala.concurrent.{ExecutionContext, Future}
import java.nio.file.{Files, Paths, StandardCopyOption}

@Singleton
class  FileUploadService @Inject()()(implicit ec: ExecutionContext) {

  private val uploadDir = "/tmp/uploads"

  def saveFile(filePart: FilePart[TemporaryFile]): Future[String] = Future {
    val fileName  = filePart.filename
    val tmp       = filePart.ref.path.toFile

    val targetDir = Paths.get(uploadDir)
    if (!Files.exists(targetDir)) {
      Files.createDirectories(targetDir)
    }

    val targetPath = targetDir.resolve(fileName)

    Files.copy(tmp.toPath, targetPath, StandardCopyOption.REPLACE_EXISTING)

    targetPath.toString
  }
}
