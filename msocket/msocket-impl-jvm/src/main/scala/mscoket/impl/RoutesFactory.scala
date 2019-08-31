package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route, StandardRoute}
import akka.stream.scaladsl.Source
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import io.bullet.borer.{Decoder, Json}
import mscoket.impl.sse.QueryHeader
import mscoket.impl.ws.WsServerFlow
import msocket.api.RequestHandler

class RoutesFactory[Req: Decoder](
    postHandler: RequestHandler[Req, StandardRoute],
    websocketHandler: RequestHandler[Req, Source[String, NotUsed]],
    sseHandler: RequestHandler[Req, StandardRoute]
) extends HttpCodecs {

  val route: Route = cors() {
    get {
      path("websocket") {
        handleWebSocketMessages {
          new WsServerFlow(websocketHandler).flow
        }
      } ~
      path("sse") {
        extractPayloadFromHeader { streamReq =>
          sseHandler.handle(streamReq)
        }
      }
    } ~
    post {
      path("post") {
        entity(as[Req])(postHandler.handle)
      }
    }
  }

  private def extractPayloadFromHeader: Directive1[Req] = headerValuePF {
    case QueryHeader(query) => Json.decode(query.getBytes()).to[Req].value
  }
}
