package msocket.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.{Done, actor}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

import scala.concurrent.{ExecutionContextExecutor, Future}

class ExampleServer(routes: Route)(system: ActorSystem[_]) {
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  def start(host: String, port: Int): Future[Http.ServerBinding] = {
    implicit val classic: actor.ActorSystem = system.toClassic
    Http().bindAndHandle(routes, host, port)
  }

  def stop(): Future[Done] = {
    system.terminate()
    system.whenTerminated
  }

  def routesWithCors: Route = cors() {
    routes
  }
}
