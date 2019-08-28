package mscoket.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Result.{Error, Success}
import msocket.api.{EitherCodecs, RequestClient, Result}

import scala.concurrent.Future

class PostClientJvm[Req: Encoder](uri: Uri)(implicit actorSystem: ActorSystem)
    extends RequestClient[Req]
    with HttpCodecs
    with EitherCodecs {
  import actorSystem.dispatcher
  implicit lazy val mat: Materializer = ActorMaterializer()

  override def requestResponse[Res: Decoder](req: Req): Future[Res] = {
    Marshal(req).to[RequestEntity].flatMap { requestEntity =>
      val request = HttpRequest(HttpMethods.POST, uri = uri, entity = requestEntity)
      Http().singleRequest(request).flatMap { response =>
        //todo: make generic status checks and then test if required
        Unmarshal(response.entity).to[Res]
      }
    }
  }

  override def requestStream[Res: Decoder](req: Req): Source[Res, NotUsed] = {
    val futureSource = Marshal(req).to[RequestEntity].flatMap { requestEntity =>
      val request = HttpRequest(HttpMethods.POST, uri = uri, entity = requestEntity)
      Http().singleRequest(request).flatMap { response =>
        //todo: make generic status checks and then test if required
        Unmarshal(response.entity).to[Source[Res, NotUsed]]
      }
    }
    Source.fromFutureSource(futureSource).mapMaterializedValue(_ => NotUsed)
  }

  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = {
    val streamOfStreams = requestStream[Result[Res, Err]](request).prefixAndTail(1).map {
      case (xs, stream) =>
        xs.toList match {
          case Error(e) :: _   => Source.empty.mapMaterializedValue(_ => Some(e))
          case Success(r) :: _ => Source.single(r).concat(stream.collect { case Success(r) => r }).mapMaterializedValue(_ => None)
          case Nil             => Source.empty.mapMaterializedValue(_ => None)
        }
    }
    Source.fromFutureSource(streamOfStreams.runWith(Sink.head))
  }
}
