package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.Encoding.JsonText
import msocket.api.models.{HttpError, ServiceError}
import org.scalablytyped.runtime.StringDictionary
import typings.std.{RequestInit, Response, fetch}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object FetchHelper {
  def postRequest[Req: Encoder](uri: String, req: Req)(implicit ec: ExecutionContext): Future[Response] = {

    val fetchRequest: RequestInit = RequestInit(
      method = "POST",
      body = JsonText.encode(req),
      headers = StringDictionary("content-type" -> "application/json")
    )

    fetch(uri, fetchRequest).toFuture.flatMap { response =>
      response.status match {
        case 200 => Future.successful(response)
        case statusCode =>
          response.text().toFuture.map { msg =>
            try throw JsonText.decode[ServiceError](msg)
            catch {
              case NonFatal(ex) => throw HttpError(statusCode.toInt, response.statusText, msg)
            }
          }
      }
    }
  }
}
