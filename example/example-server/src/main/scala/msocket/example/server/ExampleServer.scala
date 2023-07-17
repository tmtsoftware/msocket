package msocket.example.server

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.Route
import ch.megard.pekko.http.cors.scaladsl.CorsDirectives.cors

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
