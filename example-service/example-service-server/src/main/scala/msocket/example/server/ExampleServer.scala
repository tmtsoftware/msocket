package msocket.example.server

import akka.actor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

import scala.concurrent.{ExecutionContextExecutor, Future}

class ExampleServer(routes: Route)(system: ActorSystem[_]) {
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  var eventualServerBinding: Future[Http.ServerBinding]   = _

  def start(host: String, port: Int): Unit = {
    implicit val classic: actor.ActorSystem = system.toClassic
    eventualServerBinding = Http().bindAndHandle(routes, host, port)
  }

  def stop(): Unit = {
    eventualServerBinding
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def routesWithCors: Route = cors() {
    routes
  }
}
