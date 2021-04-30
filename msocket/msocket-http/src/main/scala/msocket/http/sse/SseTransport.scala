package msocket.http.sse

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.{NotUsed, actor}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.http.HttpUtils
import msocket.jvm.JvmTransport
import msocket.jvm.SourceExtension.RichSource

import scala.concurrent.{ExecutionContext, Future}

class SseTransport[Req: Encoder: ErrorProtocol](
    uri: String,
    contentType: ContentType,
    tokenFactory: () => Option[String],
    appName: Option[String] = None
)(implicit actorSystem: ActorSystem[_])
    extends JvmTransport[Req] {

  implicit val ec: ExecutionContext               = actorSystem.executionContext
  implicit val system: actor.ActorSystem          = actorSystem.toClassic
  private implicit val materializer: Materializer = Materializer(actorSystem)

  val httpUtils = new HttpUtils[Req](contentType, uri, tokenFactory, appName)

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val futureSource = httpUtils.getResponse(request).flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .map(event => JsonText.decodeWithError[Res, Req](event.data))
      .withSubscription()
  }
}
