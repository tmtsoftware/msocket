package msocket.impl.post

import io.bullet.borer.{Encoder, Json}
import org.scalajs.dom.experimental.{Fetch, HttpMethod, Response}

import scala.concurrent.Future
import scala.scalajs.js

object FetchHelper {
  def postRequest[Req: Encoder](uri: String, req: Req): Future[Response] = {
    val fetchRequest = new FetchRequest {
      method = HttpMethod.POST
      body = Json.encode(req).toUtf8String
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch.fetch(uri, fetchRequest).toFuture
  }
}
