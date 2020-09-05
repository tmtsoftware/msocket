package msocket.impl.post

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, RequestEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.service.SourceExtension.WithSubscription
import msocket.api.models.ErrorType
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.impl.post.headers.AppNameHeader
import msocket.impl.post.streaming.FetchEvent
import msocket.impl.HttpUtils
import msocket.service.JvmTransport

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

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[FetchEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .filter(_ != FetchEvent.Heartbeat)
      .map { event =>
        val maybeErrorType = event.errorType.map(ErrorType.from)
        JsonText.decodeFull(event.data, maybeErrorType)
      }
      .withSubscription()
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    val authHeader    = tokenFactory().map(t => Authorization(OAuth2BearerToken(t)))
    val appNameHeader = appName.map(name => AppNameHeader(name))
    Marshal(request).to[RequestEntity].flatMap { requestEntity =>
      val httpRequest = HttpRequest(
        HttpMethods.POST,
        uri = uri,
        entity = requestEntity,
        headers = authHeader.toList ++ appNameHeader
      )
      new HttpUtils[Req](contentType).handleRequest(httpRequest)
    }
  }
}
