package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.Encoding.JsonText
import msocket.api.models.{HttpError, ServiceError}
import org.scalajs.dom.experimental.{Fetch, HttpMethod, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.control.NonFatal

object FetchHelper {
  def postRequest[Req: Encoder](uri: String, req: Req)(implicit ec: ExecutionContext): Future[Response] = {
    val fetchRequest = new FetchRequest {
      method = HttpMethod.POST
      body = JsonText.encode(req)
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch.fetch(uri, fetchRequest).toFuture.flatMap { response =>
      response.status match {
        case 200 => Future.successful(response)
        case statusCode =>
          response.text().toFuture.map { msg =>
            try throw JsonText.decode[ServiceError](msg)
            catch {
              case NonFatal(ex) => throw HttpError(statusCode, response.statusText, msg)
            }
          }
      }
    }
  }
}
