package mscoket.impl

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import msocket.api.utils.{Result, StreamStatus, StreamSuccess}

import scala.concurrent.Future

object StreamSplitter {
  implicit class ResultStream[S](stream: Source[Result[S, StreamStatus], _]) {
    def split(implicit mat: Materializer): Source[S, Future[StreamStatus]] = {
      val streamOfStreams = stream.prefixAndTail(1).map {
        case (xs, stream) =>
          xs.toList match {
            case Result.Error(e) :: _ =>
              Source.empty.mapMaterializedValue(_ => e)
            case Result.Success(r) :: _ =>
              Source
                .single(r)
                .concat(stream.collect { case Result.Success(r) => r })
                .mapMaterializedValue(_ => StreamSuccess)
            case Nil =>
              Source.empty.mapMaterializedValue(_ => StreamSuccess)
          }
      }
      Source.fromFutureSource(streamOfStreams.runWith(Sink.head))
    }
  }
}
