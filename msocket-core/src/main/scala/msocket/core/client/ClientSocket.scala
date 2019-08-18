package msocket.core.client

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.Result
import msocket.core.api.Result.{Error, Success}

import scala.concurrent.Future

trait ClientSocket[Req] {
  def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, NotUsed]

  def requestStreamWithError[Res: Decoder: Encoder, Err: Decoder: Encoder](
      request: Req
  )(implicit mat: Materializer): Source[Res, Future[Option[Err]]] = {
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

  def requestResponse[Res: Decoder: Encoder](request: Req)(implicit mat: Materializer): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }
}
