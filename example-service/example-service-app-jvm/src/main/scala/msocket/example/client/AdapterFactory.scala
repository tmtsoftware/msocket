package msocket.example.client

import java.nio.file.Paths

import akka.actor.typed.ActorSystem
import csw.aas.installed.InstalledAppAuthAdapterFactory
import csw.aas.installed.api.InstalledAppAuthAdapter
import csw.aas.installed.scaladsl.FileAuthStore
import csw.location.api.scaladsl.LocationService
import csw.location.client.scaladsl.HttpLocationServiceFactory

import scala.concurrent.ExecutionContextExecutor

object AdapterFactory {
  def makeAdapter(implicit actorSystem: ActorSystem[_]): InstalledAppAuthAdapter = {
    implicit val ec: ExecutionContextExecutor = actorSystem.executionContext
    val locationService: LocationService      = HttpLocationServiceFactory.makeLocalClient(actorSystem, implicitly)
    val authStore                             = new FileAuthStore(Paths.get("/tmp/demo-cli/auth"))
    InstalledAppAuthAdapterFactory.make(locationService, authStore)
  }
}
