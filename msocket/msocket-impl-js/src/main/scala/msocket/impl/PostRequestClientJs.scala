package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.RequestClient
import org.scalajs.dom.experimental.{Fetch, HttpMethod}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

class PostRequestClientJs[Req: Encoder](uri: String)(implicit ec: ExecutionContext) extends RequestClient[Req] {
  def requestResponse[Res: Decoder](req: Req): Future[Res] = {
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
  }

  override def requestStream[Res: Decoder](req: Req): Source[Res, NotUsed] = ???

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = ???
}
