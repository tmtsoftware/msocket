package msocket.impl.post

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.RequestClient
import msocket.impl.streaming.{ConnectedSourceWithErr, ConnectionFactory, SimpleConnectedSource}
import org.scalajs.dom.experimental.{Fetch, HttpMethod}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

class PostClientJs[Req: Encoder](uri: String, connectionFactory: ConnectionFactory[Req])(implicit ec: ExecutionContext)
    extends RequestClient[Req] {
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

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    connectionFactory.connect(request, new SimpleConnectedSource)
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    connectionFactory.connect(request, new ConnectedSourceWithErr)
  }

}
