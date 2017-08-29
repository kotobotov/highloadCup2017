import com.google.inject.Inject
import java.util.UUID
import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.http.Status.{ INTERNAL_SERVER_ERROR, NOT_FOUND, UNPROCESSABLE_ENTITY }
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Results.{ InternalServerError, NotImplemented, NotFound, BadRequest, Status }
import play.api.mvc.{ RequestHeader, Result }
import scala.concurrent.Future
import scala.util.control.NonFatal

class ErrorHandler @Inject()(val messagesApi: MessagesApi) extends HttpErrorHandler with I18nSupport {

  // 4xx response
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val id = UUID.randomUUID
    Logger.error(s"$id - Client error occurred: $message. Returning $statusCode for ${request.uri}")

    Future.successful(Status(statusCode)(Json.obj(
      "error" -> Json.obj(
        "id" -> id,
        "statusCode" -> 400,
        "message" -> message
      )
    )))
  }

  // 5xx response
  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      //case ModelNotFoundException => onClientError(request, NOT_FOUND, Messages(s"exceptions.$NOT_FOUND"))
      //case e: ModelFormatException => onClientError(request, UNPROCESSABLE_ENTITY, Messages(e.getI18nKey))
      //case NonFatal(e) => {
      case _ => {
        val id = UUID.randomUUID
        Logger.error(s"$id - Error for ${request.uri} - Returning $INTERNAL_SERVER_ERROR ")

        Future.successful(NotFound(""))
      }
    }
  }

}