package msocket.impl.post

import io.bullet.borer.Encoder
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.post.HttpJsExtensions.HttpJsEncoding
import org.scalajs.dom.experimental.{Fetch, HttpMethod, RequestInit, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

object FetchHelper {
  def postRequest[Req: Encoder: ErrorProtocol](uri: String, req: Req, contentType: ContentType)(implicit
      ec: ExecutionContext
  ): Future[Response] = {

    val fetchRequest = new RequestInit {
      method = HttpMethod.POST
      body = contentType.body(req)
      headers = js.Dictionary("content-type" -> contentType.mimeType)
    }

    Fetch.fetch(uri, fetchRequest).toFuture.flatMap { response =>
      response.status match {
        case 200 => Future.successful(response)
        case 500 => contentType.responseError(response).map(throw _)
        case _   => response.text().toFuture.map(body => throw HttpError(response.status, response.statusText, body))
      }
    }
  }
}
