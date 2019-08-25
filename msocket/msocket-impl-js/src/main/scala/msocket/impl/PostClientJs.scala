package msocket.impl

import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.PostClient
import org.scalajs.dom.experimental.{Fetch, HttpMethod}
import org.scalajs.dom.ext.{Ajax, AjaxException}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.control.NonFatal

class PostClientJs(uri: String)(implicit ec: ExecutionContext) extends PostClient {
  override def requestResponse[Req: Encoder, Res: Decoder](req: Req): Future[Res] = {
    Ajax
      .post(
        url = uri,
        data = Json.encode(req).toUtf8String,
        headers = Map("content-type" -> "application/json")
      )
      .map { xhr =>
        xhr.getResponseHeader("content-type") match {
          case "application/json" => Json.decode(xhr.responseText.getBytes()).to[Res].value
          case _                  => Json.decode(s""""${xhr.responseText}"""".getBytes()).to[Res].value
        }
      }
      .recover {
        case NonFatal(AjaxException(req)) => throw new RuntimeException(req.responseText)
      }
  }

  def requestResponse2[Req: Encoder, Res: Decoder](req: Req): Future[Res] = {
    val request = new FetchRequest {
      method = HttpMethod.POST
      body = Json.encode(req).toUtf8String
      headers = js.Dictionary("content-type" -> "application/json")
    }
    Fetch
      .fetch(uri, request)
      .toFuture
      .flatMap { x =>
        x.text().toFuture.map { y =>
          Json.decode(y.getBytes()).to[Res].value
        }
      }
      .recover {
        case NonFatal(AjaxException(req)) => throw new RuntimeException(req.responseText)
      }
  }
}
