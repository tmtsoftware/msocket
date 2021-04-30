package msocket.http.post

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.models.ErrorType
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.http.HttpUtils
import msocket.http.post.streaming.FetchEvent
import msocket.jvm.JvmTransport
import msocket.jvm.SourceExtension.RichSource

import scala.concurrent.{ExecutionContext, Future}

class HttpPostTransport[Req: Encoder](
    uri: String,
    contentType: ContentType,
    tokenFactory: () => Option[String],
    appName: Option[String] = None
)(implicit
    actorSystem: ActorSystem[_],
    ep: ErrorProtocol[Req]
) extends JvmTransport[Req]
    with ClientHttpCodecs {

  override def clientContentType: ContentType = contentType

  implicit val ec: ExecutionContext = actorSystem.executionContext
  val httpUtils                     = new HttpUtils[Req](contentType, uri, tokenFactory, appName)

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    httpUtils.getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val futureSource = httpUtils.getResponse(request).flatMap(Unmarshal(_).to[Source[FetchEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .filter(_ != FetchEvent.Heartbeat)
      .map { event =>
        val maybeErrorType = event.errorType.map(ErrorType.from)
        JsonText.decodeFull(event.data, maybeErrorType)
      }
      .withSubscription()
  }
}
