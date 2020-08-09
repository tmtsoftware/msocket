package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

import scala.concurrent.{ExecutionContextExecutor, Future}

class ExampleServer(routes: Route)(implicit system: ActorSystem[_]) {
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  def start(host: String, port: Int): Future[Http.ServerBinding] = {
    Http().newServerAt(host, port).bind(routesWithCors)
  }

  def routesWithCors: Route =
    cors() {
      routes
    }
}
