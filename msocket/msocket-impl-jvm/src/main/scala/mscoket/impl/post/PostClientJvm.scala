package mscoket.impl.post

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import mscoket.impl.HttpCodecs
import mscoket.impl.StreamSplitter._
import msocket.api.{RequestClient, Result}

import scala.concurrent.{ExecutionContext, Future}

class PostClientJvm[Req: Encoder](uri: Uri)(implicit actorSystem: ActorSystem) extends RequestClient[Req] with HttpCodecs {

  implicit lazy val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = actorSystem.dispatcher

  override def requestResponse[Res: Decoder](request: Req): Future[Res] = {
    getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[Res, NotUsed]])
    Source.fromFutureSource(futureSource).mapMaterializedValue(_ => NotUsed)
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    requestStream[Result[Res, Err]](request).split
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    Marshal(request).to[RequestEntity].flatMap { requestEntity =>
      val httpRequest = HttpRequest(HttpMethods.POST, uri = uri, entity = requestEntity)
      Http().singleRequest(httpRequest)
    }
  }
}
