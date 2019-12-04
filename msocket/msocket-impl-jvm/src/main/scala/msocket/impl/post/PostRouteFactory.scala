package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.MessageHandler
import msocket.impl.{MSocketDirectives, RouteFactory}

import scala.reflect.ClassTag

class PostRouteFactory[Req: Decoder, Err <: Throwable: Encoder: ClassTag](endpoint: String, postHandler: MessageHandler[Req, Route])
    extends RouteFactory
    with ServerHttpCodecs {

  def make(): Route = {
    post {
      path(endpoint) {
        MSocketDirectives.withAcceptHeader {
          MSocketDirectives.withExceptionHandler[Err].apply {
            entity(as[Req])(postHandler.handle)
          }
        }
      }
    }
  }

}
