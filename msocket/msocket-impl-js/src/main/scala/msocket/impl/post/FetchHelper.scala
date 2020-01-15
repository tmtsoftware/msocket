package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.ErrorProtocol
import msocket.api.models.HttpError
import org.scalajs.dom.experimental.{Fetch, HttpMethod, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.control.NonFatal

object FetchHelper {
  def postRequest[Req: Encoder: ErrorProtocol, En](uri: String, req: Req)(
      implicit encoders: HttpJsEncoders[En],
      ec: ExecutionContext
  ): Future[Response] = {
    val fetchRequest = new FetchRequest {
      method = HttpMethod.POST
      body = encoders.body(req)
      headers = js.Dictionary("content-type" -> encoders.mimeType)
    }

    def handleError(response: Response): Future[Throwable] = encoders.responseError(response).recover {
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
