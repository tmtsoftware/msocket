package msocket.impl.post

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Source}
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.models._
import msocket.api.{Encoding, ErrorProtocol, Subscription}
import msocket.impl.{HttpUtils, JvmTransport}

import scala.concurrent.{ExecutionContext, Future}

class HttpPostTransport[Req: Encoder](uri: String, messageEncoding: Encoding[_], tokenFactory: () => Option[String])(
    implicit actorSystem: ActorSystem[_],
    ep: ErrorProtocol[Req]
) extends JvmTransport[Req]
    with ClientHttpCodecs {

  override def encoding: Encoding[_] = messageEncoding

  implicit val ec: ExecutionContext = actorSystem.executionContext

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    getResponse(request).flatMap(Unmarshal(_).to[Res])
  }

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val futureSource = getResponse(request).flatMap(Unmarshal(_).to[Source[FetchEvent, NotUsed]])
    Source
      .futureSource(futureSource)
      .filter(_ != FetchEvent.Heartbeat)
      .map(event => JsonText.decodeWithError[Res, Req](event.data))
      .viaMat(KillSwitches.single)(Keep.right)
      .mapMaterializedValue(switch => () => switch.shutdown())
  }

  private def getResponse(request: Req): Future[HttpResponse] = {
    Marshal(request).to[RequestEntity].flatMap { requestEntity =>
      val httpRequest = HttpRequest(
        HttpMethods.POST,
        uri = uri,
        entity = requestEntity,
        headers = tokenFactory() match {
          case Some(token) => Seq(Authorization(OAuth2BearerToken(token)))
          case None        => Nil
        }
      )
      new HttpUtils[Req](encoding).handleRequest(httpRequest)
    }
  }
}
