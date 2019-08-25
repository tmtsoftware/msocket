package msocket.simple.server

import akka.http.scaladsl.server.StandardRoute
import csw.simple.api.{PostRequest, SimpleApi}
import csw.simple.api.PostRequest.Hello
import msocket.api.PostHandler
import akka.http.scaladsl.server.Directives._
import mscoket.impl.HttpCodecs

class SimplePostHandler(simpleApi: SimpleApi) extends PostHandler[PostRequest, StandardRoute] with HttpCodecs {
  override def handle(request: PostRequest): StandardRoute = request match {
    case Hello(name) => complete(simpleApi.hello(name))
  }
}
