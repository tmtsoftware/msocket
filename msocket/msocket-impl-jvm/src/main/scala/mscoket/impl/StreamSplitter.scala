package mscoket.impl

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import msocket.api.utils.Result

import scala.concurrent.Future

object StreamSplitter {
  implicit class ResultStream[S, E](stream: Source[Result[S, E], _]) {
    def split(implicit mat: Materializer): Source[S, Future[Option[E]]] = {
      val streamOfStreams = stream.prefixAndTail(1).map {
        case (xs, stream) =>
          xs.toList match {
            case Result.Error(e) :: _ =>
              Source.empty.mapMaterializedValue(_ => Some(e))
            case Result.Success(r) :: _ =>
              Source
                .single(r)
                .concat(stream.collect { case Result.Success(r) => r })
                .mapMaterializedValue(_ => None)
            case Nil =>
              Source.empty.mapMaterializedValue(_ => None)
          }
      }
      Source.fromFutureSource(streamOfStreams.runWith(Sink.head))
    }
  }
}
