package msocket.impl

import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink, Source}
import msocket.api.models.{Result, StreamError, StreamStarted, StreamStatus}

import scala.concurrent.Future

object StreamSplitter {
  implicit class ResultStream[S](stream: Source[Result[S, StreamError], _]) {
    def split(implicit actorSystem: ActorSystem[_]): Source[S, Future[StreamStatus]] = {
      val streamOfStreams = stream.prefixAndTail(1).map {
        case (xs, stream) =>
          xs.toList match {
            case Result.Error(e) :: _ =>
              Source.empty.mapMaterializedValue(_ => e)
            case Result.Success(r) :: _ =>
              Source
                .single(r)
                .concat(stream.collect { case Result.Success(r) => r })
                .viaMat(KillSwitches.single)(Keep.right)
                .mapMaterializedValue(switch => StreamStarted(() => switch.shutdown()))
            case Nil =>
              Source.empty.mapMaterializedValue(_ => StreamStarted(() => ()))
          }
      }
      Source.futureSource(streamOfStreams.runWith(Sink.head))
    }
  }
}
