package mscoket.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Result.{Error, Success}
import msocket.api.{RequestClient, Result}

import scala.concurrent.Future

abstract class AbstractClientJvm[Req: Encoder](uri: Uri)(implicit actorSystem: ActorSystem) extends RequestClient[Req] {
  implicit lazy val mat: Materializer = ActorMaterializer()

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
