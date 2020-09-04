package msocket.impl.ws

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.SourceExtension.WithSubscription
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.impl.post.headers.AppNameHeader
import msocket.impl.ws.WebsocketExtensions.WebsocketEncoding
import msocket.impl.{CborByteString, JvmTransport}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class WebsocketTransport[Req: Encoder: ErrorProtocol](
    uri: String,
    contentType: ContentType,
    tokenFactory: () => Option[String] = () => None,
    appName: Option[String] = None
)(implicit actorSystem: ActorSystem[_])
    extends JvmTransport[Req] {

  implicit val ec: ExecutionContext = actorSystem.executionContext

  def setup(): WebsocketTransportSetup = {
    val appNameParam  = appName.map(name => AppNameHeader.name -> name)
    val tokenParam    = tokenFactory().map(token => Authorization.name -> token)
    val params        = (appNameParam ++ tokenParam).toMap
    val uriWithParams = Uri(uri).withQuery(Uri.Query(params))
    new WebsocketTransportSetup(WebSocketRequest(uriWithParams))
  }

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    Future.failed(new RuntimeException("requestResponse protocol without timeout is not supported for this transport"))
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] =
    setup()
      .request(contentType.strictMessage(request))
      .mapAsync(16) {
        case msg: TextMessage   => msg.toStrict(100.millis).map(m => JsonText.decodeWithError[Res, Req](m.text))
        case msg: BinaryMessage => msg.toStrict(100.millis).map(m => CborByteString.decodeWithError[Res, Req](m.data))
      }
      .withSubscription()
}
