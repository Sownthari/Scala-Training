package mail

import jakarta.mail._
import jakarta.mail.internet._
import java.util.Properties
import scala.concurrent.{ExecutionContext, Future}

class Mailer(host: String, port: Int, user: String, pass: String)(implicit ec: ExecutionContext) {

  private val props = new Properties()
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.starttls.enable", "true")
  props.put("mail.smtp.host", host)
  props.put("mail.smtp.port", port.toString)

  private val session = Session.getInstance(props, new Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication =
      new PasswordAuthentication(user, pass)
  })

  def send(to: String, subject: String, body: String): Future[Unit] = Future {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(user))
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setText(body)

    Transport.send(message)
  }

  def sendHtml(to: String, subject: String, html: String): Future[Unit] = Future {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(user))
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)

    val multipart = new MimeMultipart("alternative")
    val htmlPart = new MimeBodyPart()
    htmlPart.setContent(html, "text/html; charset=utf-8")

    multipart.addBodyPart(htmlPart)
    message.setContent(multipart)

    Transport.send(message)
  }

}
