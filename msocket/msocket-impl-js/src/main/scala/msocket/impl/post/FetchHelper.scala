package msocket.impl.post

import io.bullet.borer.{Encoder, Json}
import msocket.api.utils.HttpException
import org.scalajs.dom.experimental.{Fetch, HttpMethod, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

object FetchHelper {
  def postRequest[Req: Encoder](uri: String, req: Req)(implicit ec: ExecutionContext): Future[Response] = {
    val fetchRequest = new FetchRequest {
      method = HttpMethod.POST
      body = Json.encode(req).toUtf8String
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch.fetch(uri, fetchRequest).toFuture.flatMap { response =>
      response.status match {
        case 200        => Future.successful(response)
        case statusCode => response.text().toFuture.map(msg => throw HttpException(statusCode, response.statusText, msg))
      }
    }
  }
}
