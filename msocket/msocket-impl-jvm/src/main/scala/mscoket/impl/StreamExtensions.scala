package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.{Encoder, Json}
import mscoket.impl.Encoding.JsonText
import msocket.api.Result
import msocket.api.Result.{Error, Success}

import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

trait StreamExtensions[M] {
  def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[M, Mat]

  def stream[T](input: Future[T])(implicit encoder: Encoder[T]): Source[M, NotUsed] = {
    stream(Source.fromFuture(input))
  }

  def streamWithError[S, E](input: Source[S, Future[Option[E]]])(
      implicit encS: Encoder[S],
      encE: Encoder[E],
      mat: Materializer
  ): Source[M, NotUsed] = {
    val (matF, source) = input.preMaterialize()
    val resultStream: Source[Result[S, E], NotUsed] = Source.fromFuture(matF).flatMapConcat {
      case Some(value) => Source.single(Error(value))
      case None        => source.map(Success(_))
    }
    stream(resultStream)
  }
}

trait SseStreamExtensions extends StreamExtensions[ServerSentEvent] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[ServerSentEvent, Mat] = {
    input
      .map(x => ServerSentEvent(Json.encode(x).toUtf8String))
      .keepAlive(30.seconds, () => ServerSentEvent.heartbeat)
  }
}

trait WebsocketStreamExtensions extends StreamExtensions[Message] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[Message, Mat] = {
    input.map(JsonText.strictMessage[T])
  }
}
