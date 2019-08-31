package msocket.simple.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import csw.simple.api.PostRequest.{Hello, HelloStream}
import csw.simple.api.{Codecs, PostRequest, SimpleApi}
import mscoket.impl.HttpCodecs
import mscoket.impl.post.PostStreamExtensions
import msocket.api.RequestHandler

class SimplePostHandler(simpleApi: SimpleApi)
    extends RequestHandler[PostRequest, StandardRoute]
    with HttpCodecs
    with Codecs
    with PostStreamExtensions {

  override def handle(request: PostRequest): StandardRoute = request match {
    case Hello(name)       => complete(simpleApi.hello(name))
    case HelloStream(name) => complete(stream(simpleApi.helloStream(name)))
  }
}
