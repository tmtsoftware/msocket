package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.models.HttpError
import msocket.api.{Encoding, ErrorProtocol}
import msocket.impl.post.HttpJsExtensions._
import org.scalajs.dom.experimental.{Fetch, HttpMethod, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.control.NonFatal

object FetchHelper {
  def postRequest[Req: Encoder: ErrorProtocol](uri: String, req: Req, encoding: Encoding[_])(
      implicit ec: ExecutionContext
  ): Future[Response] = {
    val fetchRequest = new FetchRequest {
      method = HttpMethod.POST
      body = encoding.body(req)
      headers = js.Dictionary("content-type" -> encoding.mimeType)
    }

    def handleError(response: Response): Future[Throwable] = encoding.responseError(response).recover {
      case NonFatal(ex) => HttpError(response.status, response.statusText, ex.getMessage)
    }

    Fetch.fetch(uri, fetchRequest).toFuture.flatMap { response =>
      response.status match {
        case 200 => Future.successful(response)
        case _   => handleError(response).map(throw _)
      }
    }
  }
}
